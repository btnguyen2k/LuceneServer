package lucene;

import java.io.IOException;

import lucene.spec.IndexSpec;

/**
 * Factory to create {@link IIndex} objects.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IIndexFactory {

    /**
     * Opens an existing index, or {@code null} if index does not exist.
     * 
     * @param spec
     * @param actionQueue
     * @return
     * @throws IOException
     */
    public IIndex openIndex(IndexSpec spec, IActionQueue actionQueue) throws IOException;

    /**
     * Creates an {@link IIndex}.
     * 
     * @param spec
     * @param actionQueue
     * @return
     * @throws IOException
     */
    public IIndex createIndex(IndexSpec spec, IActionQueue actionQueue) throws IOException;
}
