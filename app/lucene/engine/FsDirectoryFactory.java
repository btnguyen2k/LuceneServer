package lucene.engine;

import java.io.File;
import java.io.IOException;

import lucene.IDirectoryFactory;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import play.Play;
import util.IndexUtils;

/**
 * Factory that creates {@link FSDirectory} objects.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class FsDirectoryFactory implements IDirectoryFactory {

    private String rootStoragePath;

    public String getRootStoragePath() {
        return rootStoragePath;
    }

    public FsDirectoryFactory setRootStoragePath(String rootStoragePath) {
        this.rootStoragePath = rootStoragePath;
        return this;
    }

    public FsDirectoryFactory init() throws IOException {
        // EMPTY
        return this;
    }

    public void destroy() {
        // EMPTY
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory createDirectory(String indexName) throws IOException {
        File storageDir;
        if (rootStoragePath.startsWith("/")) {
            storageDir = new File(rootStoragePath);
        } else {
            File dir = Play.application().path();
            storageDir = new File(dir, rootStoragePath);
        }

        File indexStorage = new File(storageDir, IndexUtils.normalizeName(indexName));
        indexStorage.mkdirs();

        FSDirectory DIR = FSDirectory.open(indexStorage.toPath());
        return DIR;
    }

}
