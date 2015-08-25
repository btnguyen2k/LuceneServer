package lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import lucene.action.BaseAction;
import lucene.action.IndexAction;
import lucene.action.TruncateAction;
import lucene.spec.IndexSpec;
import util.IndexException;

/**
 * Lucene index APIs.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IIndex {
    /**
     * Is this index newly created.
     * 
     * @return
     */
    public boolean isNew() throws IOException;

    /**
     * Updates index's spec.
     * 
     * @param spec
     * @param override
     *            existing fields will not be changed unless override is
     *            {@code true}
     * @return
     * @throws IOException
     */
    public IIndex updateSpec(IndexSpec spec, boolean override) throws IOException;

    /**
     * Validates a document against this index's spec.
     * 
     * @param doc
     * @return
     * @throws IndexException
     */
    public boolean validateDocument(Map<String, Object> doc) throws IndexException;

    /**
     * Indexes a document (existing document will be overridden).
     * 
     * <p>
     * Implementation should build an {@link IndexAction} object and put to
     * queue for async-execution.
     * </p>
     * 
     * @param doc
     * @return {@code true} if the document has been scheduled for indexing,
     *         {@code false} if error
     * @see #performAction(BaseAction)
     * @throws IndexException
     * @throws IOException
     */
    public boolean indexDocument(Map<String, Object> doc) throws IndexException, IOException;

    /**
     * Indexes documents (existing documents will be overridden).
     * 
     * <p>
     * Implementation should build {@link IndexAction} objects and put to queue
     * for async-execution.
     * </p>
     * 
     * @param docs
     * @return number of documents have been scheduled for indexing
     * @see #performAction(BaseAction)
     * @throws IndexException
     * @throws IOException
     */
    public int indexDocuments(Collection<Map<String, Object>> docs) throws IndexException,
            IOException;

    /**
     * Indexes documents (existing documents will be overridden).
     * 
     * <p>
     * Implementation should build {@link IndexAction} objects and put to queue
     * for async-execution.
     * </p>
     * 
     * @param docs
     * @return number of documents have been scheduled for indexing
     * @see #performAction(BaseAction)
     * @throws IndexException
     * @throws IOException
     */
    public int indexDocuments(Map<String, Object>[] docs) throws IndexException, IOException;

    /**
     * Truncates this index.
     * <p>
     * Implementation should build {@link TruncateAction} objects and put to
     * queue for async-execution.
     * </p>
     * 
     * @return
     * @throws IndexException
     * @throws IOException
     */
    public boolean truncate() throws IndexException, IOException;

    /**
     * Performs an index action.
     * 
     * <p>
     * This method should actually perform the action (i.e. non-async).
     * </p>
     * 
     * @param action
     * @return
     * @throws IndexException
     * @throws IOException
     */
    public boolean performAction(BaseAction action) throws IndexException, IOException;
}
