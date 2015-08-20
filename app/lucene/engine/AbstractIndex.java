package lucene.engine;

import java.io.IOException;

import lucene.IIndex;
import lucene.IndexSpec;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import play.Logger;

/**
 * Abstract implementation of {@link IIndex}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractIndex implements IIndex {

    private String name;
    private Directory directory;
    private IndexSpec spec;

    public AbstractIndex(String name) {
        this.name = name;
    }

    public AbstractIndex(String name, Directory directory) {
        this.name = name;
        this.directory = directory;
    }

    public AbstractIndex(String name, Directory directory, IndexSpec spec) {
        this.name = name;
        this.directory = directory;
        this.spec = spec;
    }

    protected String getName() {
        return name;
    }

    public AbstractIndex setName(String name) {
        this.name = name;
        return this;
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

    /*----------------------------------------------------------------------*/

    private IndexWriter indexWriter;

    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    /**
     * Opens a the index-writer for this index.
     * 
     * @param spec
     * @param directory
     * @return
     * @throws IOException
     */
    protected IndexWriter openIndexWriter(IndexSpec spec, Directory directory) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(directory, iwc);
    }

    /**
     * Opens the index-reader for this index.
     * 
     * @param spec
     * @param directory
     * @return
     * @throws IOException
     */
    protected IndexReader openIndexReader(IndexSpec spec, Directory directory) throws IOException {
        return DirectoryReader.open(directory);
    }

    /**
     * Opens the index-searcher for this index.
     * 
     * @param spec
     * @param indexReader
     * @return
     */
    protected IndexSearcher openIndexSearcher(IndexSpec spec, IndexReader indexReader) {
        return new IndexSearcher(indexReader);
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
            existingSpec = IndexSpec.newInstance(name);
        }
        spec = existingSpec.merge(spec);
        saveSpec();

        return this;
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
}
