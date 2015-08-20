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
}
