package lucene.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lucene.IActionQueue;
import lucene.IIndex;
import lucene.action.BaseAction;
import lucene.action.DeleteAction;
import lucene.action.IndexAction;
import lucene.action.TruncateAction;
import lucene.spec.FieldSpec;
import lucene.spec.IndexSpec;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

import play.Logger;
import util.IndexException;

/**
 * Abstract implementation of {@link IIndex}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractIndex implements IIndex {

    private Directory directory;
    private IndexSpec spec;
    private IActionQueue actionQueue;

    public AbstractIndex(Directory directory, IndexSpec spec, IActionQueue actionQueue) {
        this.directory = directory;
        this.spec = spec;
        this.actionQueue = actionQueue;
    }

    protected String getName() {
        return spec.name();
    }

    protected Directory getDirectory() {
        return directory;
    }

    public AbstractIndex setDirectory(Directory directory) {
        this.directory = directory;
        return this;
    }

    protected IndexSpec getSpec() {
        return spec;
    }

    public AbstractIndex setSpec(IndexSpec spec) {
        this.spec = spec;
        return this;
    }

    protected IActionQueue getActionQueue() {
        return actionQueue;
    }

    public AbstractIndex setActionQueue(IActionQueue actionQueue) {
        this.actionQueue = actionQueue;
        return this;
    }

    /*----------------------------------------------------------------------*/

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    protected Lock getReadLock() {
        return lock.readLock();
    }

    protected Lock getWriteLock() {
        return lock.writeLock();
    }

    private IndexReader indexReader;

    /**
     * Creates {@link IndexReader} instance for this index.
     * 
     * <p>
     * Sub-class may override this method to implement its own business rule.
     * </p>
     * 
     * @return
     * @throws IOException
     */
    protected IndexReader openIndexReader() throws IOException {
        return DirectoryReader.open(getDirectory());
    }

    /**
     * Gets {@link IndexReader} for this index.
     * 
     * @return
     * @throws IOException
     */
    synchronized protected IndexReader getIndexReader() throws IOException {
        if (indexReader == null) {
            indexReader = openIndexReader();
        }
        return indexReader;
    }

    private IndexSearcher indexSearcher;

    /**
     * Creates {@link IndexSearcher} instance for this index.
     * 
     * <p>
     * Sub-class may override this method to implement its own business rule.
     * </p>
     * 
     * @return
     * @throws IOException
     */
    protected IndexSearcher openIndexSearcher() throws IOException {
        return new IndexSearcher(getIndexReader());
    }

    /**
     * Gets {@link IndexSearcher} for this index.
     * 
     * @return
     * @throws IOException
     */
    synchronized protected IndexSearcher getIndexSearcher() throws IOException {
        if (indexSearcher == null) {
            indexSearcher = openIndexSearcher();
        }
        return indexSearcher;
    }

    private IndexWriter indexWriter;

    /**
     * Creates {@link IndexWriter} instance for this index.
     * 
     * <p>
     * Sub-class may override this method to implement its own business rule.
     * </p>
     * 
     * @return
     * @throws IOException
     */
    protected IndexWriter openIndexWriter() throws IOException {
        IndexWriterConfig iwc = getIndexWriterConfig();
        IndexWriter iw = new IndexWriter(getDirectory(), iwc);
        return iw;
    }

    /**
     * Gets {@link IndexWriter} for this index.
     * 
     * @return
     * @throws IOException
     */
    synchronized protected IndexWriter getIndexWriter() throws IOException {
        if (indexWriter == null) {
            indexWriter = openIndexWriter();
        }
        return indexWriter;
    }

    protected Analyzer getAnalyser() {
        return new StandardAnalyzer();
    }

    protected IndexWriterConfig getIndexWriterConfig() {
        Analyzer analyzer = getAnalyser();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setUseCompoundFile(true);
        iwc.setCommitOnClose(true);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        return iwc;
    }

    /**
     * Creates a new field according to value.
     * 
     * @param fieldName
     * @param fieldValue
     * @return
     * @throws IOException
     */
    protected FieldSpec createField(String fieldName, Object fieldValue) throws IOException {
        FieldSpec field;
        if (fieldValue instanceof Number) {
            if (fieldValue instanceof Double || fieldValue instanceof Float) {
                field = FieldSpec.newInstance(fieldName, FieldSpec.Type.DOUBLE);
            } else {
                field = FieldSpec.newInstance(fieldName, FieldSpec.Type.LONG);
            }
        } else {
            field = FieldSpec.newInstance(fieldName, FieldSpec.Type.STRING);
        }
        IndexSpec spec = getSpec();
        spec.field(fieldName, field);
        updateSpec(spec, false);
        return field;
    }

    /**
     * Builds a new Lucene field.
     * 
     * @param fieldName
     * @param fieldValue
     * @return
     * @throws IOException
     */
    protected Field buildField(String fieldName, Object fieldValue) throws IOException {
        if (fieldName == null || fieldValue == null) {
            return null;
        }
        FieldSpec field = getSpec().field(fieldName);
        if (field == null) {
            field = createField(fieldName, fieldValue);
        }
        switch (field.type()) {
        case ID:
            return new StringField(field.name(), fieldValue.toString(),
                    field.isStored() ? Field.Store.YES : Field.Store.NO);
        case STRING:
            return new TextField(field.name(), fieldValue.toString(),
                    field.isStored() ? Field.Store.YES : Field.Store.NO);
        case LONG:
            if (fieldValue instanceof Number) {
                Number numValue = (Number) fieldValue;
                return new LongField(field.name(), numValue.longValue(),
                        field.isStored() ? Field.Store.YES : Field.Store.NO);
            }
        case DOUBLE:
            if (fieldValue instanceof Number) {
                Number numValue = (Number) fieldValue;
                return new DoubleField(field.name(), numValue.doubleValue(),
                        field.isStored() ? Field.Store.YES : Field.Store.NO);
            }
        }
        return null;
    }

    /**
     * Builds a document for indexing.
     * 
     * @param docData
     * @return
     * @throws IOException
     */
    protected Document buildDocument(Map<String, Object> docData) throws IOException {
        Document doc = new Document();
        boolean isEmpty = true;
        for (Entry<String, Object> entry : docData.entrySet()) {
            Field field = buildField(entry.getKey(), entry.getValue());
            if (field != null) {
                doc.add(field);
                isEmpty = false;
            }
        }
        return isEmpty ? null : doc;
    }

    // private final static Term[] EMPTY_TERM_ARRAY = new Term[0];
    //
    // /**
    // * Builds list of terms to delete a document (for updating).
    // *
    // * @param docData
    // * @return
    // * @throws IOException
    // */
    // protected Term[] buildTermsForDeletion(Map<String, Object> docData)
    // throws IOException {
    // List<Term> result = new ArrayList<Term>();
    // for (Entry<String, Object> entry : docData.entrySet()) {
    // String fieldName = entry.getKey().trim().toLowerCase();
    // FieldSpec field = spec.field(fieldName);
    // if (field != null && field.type() == FieldSpec.Type.ID) {
    // Term term = new Term(fieldName, entry.getValue().toString());
    // result.add(term);
    // }
    // }
    // return result.toArray(EMPTY_TERM_ARRAY);
    // }

    /**
     * Builds a query to delete document(s).
     * 
     * @param docData
     * @return
     * @throws IOException
     */
    protected Query buildQueryForDeletion(Map<String, Object> docData) throws IOException {
        BooleanQuery result = new BooleanQuery();
        boolean isEmpty = true;
        for (Entry<String, Object> entry : docData.entrySet()) {
            String fieldName = entry.getKey().trim().toLowerCase();
            FieldSpec field = spec.field(fieldName);
            if (field != null && field.type() == FieldSpec.Type.ID) {
                Term term = new Term(fieldName, entry.getValue().toString());
                TermQuery termQuery = new TermQuery(term);
                result.add(termQuery, Occur.MUST);
                isEmpty = false;
            }
        }
        return isEmpty ? null : result;
    }

    /**
     * Initialization method.
     * 
     * @return
     * @throws IOException
     */
    public AbstractIndex init() throws IOException {
        IndexSpec existingSpec = IndexSpec.loadSpec(directory);
        if (existingSpec == null) {
            existingSpec = IndexSpec.newInstance(spec.name());
        }
        spec = existingSpec.merge(spec);
        saveSpec();

        return this;
    }

    /**
     * Destroy method.
     */
    public void destroy() {
        try {
            closeIndexReader();
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }

        try {
            closeIndexWriter();
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }

        try {
            closeDirectory();
        } catch (Exception e) {
            Logger.warn(e.getMessage(), e);
        }
    }

    protected void closeIndexReader() {
        if (indexReader != null) {
            try {
                indexReader.close();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            }
        }
    }

    protected void closeIndexWriter() {
        if (indexWriter != null) {
            try {
                indexWriter.close();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            }
        }
    }

    protected void closeDirectory() {
        if (directory != null) {
            try {
                directory.close();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            }
        }
    }

    /*----------------------------------------------------------------------*/

    /**
     * Loads this index's spec.
     * 
     * @return
     * @throws IOException
     */
    protected IndexSpec loadSpec() throws IOException {
        return IndexSpec.loadSpec(directory);
    }

    /**
     * Saves this index's spec.
     * 
     * @param indexSpec
     * @throws IOException
     */
    synchronized protected void saveSpec() throws IOException {
        IndexSpec.saveSpec(directory, spec);
    }

    /*----------------------------------------------------------------------*/
    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     */
    @Override
    public boolean isNew() throws IOException {
        return DirectoryReader.indexExists(directory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractIndex updateSpec(IndexSpec spec, boolean override) throws IOException {
        this.spec.merge(spec, override);
        saveSpec();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateDocument(Map<String, Object> doc) throws IndexException {
        if (spec == null) {
            throw new IndexException(500, "Null/Invalid index's metadata");
        }
        if (doc == null || doc.size() == 0) {
            throw new IndexException(400, "Empty document");
        }
        for (Entry<String, Object> fieldData : doc.entrySet()) {
            String fieldName = fieldData.getKey().trim().toLowerCase();
            Object fieldValue = fieldData.getValue();
            FieldSpec field = spec.field(fieldName);
            if (field == null) {
                continue;
            } else if (field.validateValue(fieldValue)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean indexDocument(Map<String, Object> document) throws IndexException, IOException {
        IndexAction action = new IndexAction(getName());
        action.doc(document);
        IActionQueue actionQueue = getActionQueue();
        return actionQueue != null ? actionQueue.queue(action) : performAction(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexDocuments(Collection<Map<String, Object>> docs) throws IndexException,
            IOException {
        int count = 0;
        for (Map<String, Object> doc : docs) {
            if (!indexDocument(doc)) {
                return count;
            } else {
                count++;
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexDocuments(Map<String, Object>[] docs) throws IndexException, IOException {
        int count = 0;
        for (Map<String, Object> doc : docs) {
            if (!indexDocument(doc)) {
                return count;
            } else {
                count++;
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean truncate() throws IndexException, IOException {
        TruncateAction action = new TruncateAction(getName());
        IActionQueue actionQueue = getActionQueue();
        return actionQueue != null ? actionQueue.queue(action) : performAction(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performAction(BaseAction action) throws IndexException, IOException {
        if (action instanceof DeleteAction) {
            return performDeleteAction((DeleteAction) action);
        }
        if (action instanceof TruncateAction) {
            return performTruncateAction((TruncateAction) action);
        }
        if (action instanceof IndexAction) {
            return performIndexAction((IndexAction) action);
        }
        return false;
    }

    /**
     * Performs an index truncation action.
     * 
     * @param action
     * @return
     * @throws IndexException
     * @throws IOException
     */
    protected abstract boolean performTruncateAction(TruncateAction action) throws IndexException,
            IOException;

    /**
     * Performs a document deletion action.
     * 
     * @param action
     * @return
     * @throws IndexException
     * @throws IOException
     */
    protected abstract boolean performDeleteAction(DeleteAction action) throws IndexException,
            IOException;

    /**
     * Performs a document indexing action.
     * 
     * @param action
     * @return
     * @throws IndexException
     * @throws IOException
     */
    protected abstract boolean performIndexAction(IndexAction action) throws IndexException,
            IOException;
}
