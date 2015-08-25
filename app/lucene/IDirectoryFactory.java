package lucene;

import java.io.IOException;

import org.apache.lucene.store.Directory;

/**
 * Factory to create {@link Directory} objects.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IDirectoryFactory {
    /**
     * Creates a new {@link Directory} object.
     * 
     * @param indexName
     * @return
     * @throws IOException
     */
    public Directory createDirectory(String indexName) throws IOException;
}
