package lucene.engine;

import java.io.IOException;

import lucene.IActionQueue;
import lucene.IDirectoryFactory;
import lucene.spec.IndexSpec;

import org.apache.lucene.store.Directory;

/**
 * Factory that creates {@link StandaloneIndex} objects.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class StandaloneIndexFactory extends AbstractIndexFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractIndex createIndexInternal(IndexSpec spec, IActionQueue actionQueue)
            throws IOException {
        IDirectoryFactory dirFactory = getDirectoryFactory();
        Directory dir = dirFactory.createDirectory(spec.name());
        return StandaloneIndex.create(dir, spec, actionQueue);
    }

}
