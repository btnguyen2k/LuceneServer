package lucene;

import java.io.IOException;

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
}
