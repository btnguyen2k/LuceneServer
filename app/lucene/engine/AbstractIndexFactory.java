package lucene.engine;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import lucene.IActionQueue;
import lucene.IDirectoryFactory;
import lucene.IIndex;
import lucene.IIndexFactory;
import lucene.spec.IndexSpec;
import play.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Abstract implementation of {@link IIndexFactory}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractIndexFactory implements IIndexFactory {

    private Cache<String, AbstractIndex> cacheIndex = CacheBuilder.newBuilder()
            .expireAfterAccess(3600, TimeUnit.SECONDS) // 1 hour
            .removalListener(new RemovalListener<String, AbstractIndex>() {
                @Override
                public void onRemoval(RemovalNotification<String, AbstractIndex> item) {
                    item.getValue().destroy();
                }
            }).build();

    private IDirectoryFactory directoryFactory;

    public IDirectoryFactory getDirectoryFactory() {
        return directoryFactory;
    }

    public AbstractIndexFactory setDirectoryFactory(IDirectoryFactory directoryFactory) {
        this.directoryFactory = directoryFactory;
        return this;
    }

    /**
     * Init method.
     * 
     * @return
     */
    public AbstractIndexFactory init() {
        return this;
    }

    /**
     * Destroy method.
     */
    public void destroy() {
        try {
            cacheIndex.invalidateAll();
        } catch (Exception e) {
        }
    }

    /**
     * Creates a new index instance.
     * 
     * @param spec
     * @param actionQueue
     * @return
     * @throws IOException
     */
    protected abstract AbstractIndex createIndexInternal(IndexSpec spec, IActionQueue actionQueue)
            throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public IIndex createIndex(final IndexSpec spec, final IActionQueue actionQueue)
            throws IOException {
        final String indexName = spec.name();
        AbstractIndex index;
        try {
            index = cacheIndex.get(indexName, new Callable<AbstractIndex>() {
                @Override
                public AbstractIndex call() throws Exception {
                    return createIndexInternal(spec, actionQueue);
                }
            });
        } catch (Exception e) {
            index = null;
            Logger.warn(e.getMessage(), e);
        }
        return index;
    }

    /**
     * Opens an existing index, or {@code null} if index does not exist.
     * 
     * @param spec
     * @param actionQueue
     * @return
     * @throws IOException
     */
    protected abstract AbstractIndex openIndexInternal(IndexSpec spec, IActionQueue actionQueue)
            throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public IIndex openIndex(final IndexSpec spec, final IActionQueue actionQueue)
            throws IOException {
        final String indexName = spec.name();
        AbstractIndex index;
        try {
            index = cacheIndex.get(indexName, new Callable<AbstractIndex>() {
                @Override
                public AbstractIndex call() throws Exception {
                    return openIndexInternal(spec, actionQueue);
                }
            });
        } catch (ExecutionException e) {
            index = null;
        } catch (Exception e) {
            index = null;
            Logger.warn(e.getMessage(), e);
        }
        return index;
    }
}
