package lucene.engine;

import java.io.IOException;

import lucene.IndexSpec;

import org.apache.lucene.store.Directory;

/**
 * Base class for standalone (aka single-server) indices.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class StandaloneIndex extends AbstractIndex {

    public static StandaloneIndex create(String normalizedIndexName, Directory directory)
            throws IOException {
        StandaloneIndex index = new StandaloneIndex(normalizedIndexName, directory);
        index.init();
        return index;
    }

    public static StandaloneIndex create(String normalizedIndexName, Directory directory,
            IndexSpec spec) throws IOException {
        StandaloneIndex index = new StandaloneIndex(normalizedIndexName, directory, spec);
        index.init();
        return index;
    }

    public StandaloneIndex(String name) {
        super(name);
    }

    public StandaloneIndex(String name, Directory directory) {
        super(name, directory);
    }

    public StandaloneIndex(String name, Directory directory, IndexSpec spec) {
        super(name, directory, spec);
    }

}
