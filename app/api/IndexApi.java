package api;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import lucene.IIndex;
import lucene.IndexSpec;
import lucene.engine.AbstractIndex;
import lucene.engine.StandaloneIndex;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import play.Play;

import com.github.ddth.com.cassdir.CassandraDirectory;
import com.github.ddth.com.redir.RedisDirectory;
import com.github.ddth.plommon.utils.PlayAppUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Engine to generate IDs.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class IndexApi {

    // private Cache<String, IndexSpec> cacheSpec =
    // CacheBuilder.newBuilder().build();
    private Cache<String, AbstractIndex> cacheIndex = CacheBuilder.newBuilder()
            .expireAfterAccess(24 * 3600, TimeUnit.SECONDS) // 1 day
            .removalListener(new RemovalListener<String, AbstractIndex>() {
                @Override
                public void onRemoval(RemovalNotification<String, AbstractIndex> item) {
                    item.getValue().destroy();
                }
            }).build();

    /**
     * Init method.
     * 
     * @return
     */
    public IndexApi init() {
        // EMPTY
        return this;
    }

    /**
     * Destroy method.
     */
    public void destroy() {
        // try {
        // cacheSpec.invalidateAll();
        // } catch (Exception e) {
        // }

        try {
            cacheIndex.invalidateAll();
        } catch (Exception e) {
        }
    }

    /*----------------------------------------------------------------------*/

    private static String normalizeIndexName(final String indexName) {
        return indexName.trim().toLowerCase();
    }

    private Directory createFsDirectory(String normalizedIndexName) throws IOException {
        String storagePath = PlayAppUtils.appConfigString("directory.fs.path");
        File storageDir;
        if (storagePath.startsWith("/")) {
            storageDir = new File(storagePath);
        } else {
            File dir = Play.application().path();
            storageDir = new File(dir, storagePath);
        }

        File indexStorage = new File(storageDir, normalizedIndexName);
        indexStorage.mkdirs();

        FSDirectory DIR = FSDirectory.open(indexStorage.toPath());
        return DIR;
    }

    private Directory createRedisDirectory(String normalizedIndexName) {
        String redisHost = PlayAppUtils.appConfigString("directory.redis.host");
        Integer redisPort = PlayAppUtils.appConfigInteger("directory.redis.host");
        String redisPassword = PlayAppUtils.appConfigString("directory.redis.password");

        RedisDirectory DIR = new RedisDirectory(redisHost, redisPort, redisPassword);
        DIR.setHashDirectoryMetadata(RedisDirectory.DEFAULT_HASH_DIRECTORY_METADATA + "_"
                + normalizedIndexName);
        DIR.setHashFileData(RedisDirectory.DEFAULT_HASH_FILE_DATA + "_" + normalizedIndexName);
        DIR.init();
        return DIR;
    }

    private Directory createCassandraDirectory(String normalizedIndexName) {
        String cassHostsAndPorts = PlayAppUtils
                .appConfigString("directory.cassandra.hostsAndPorts");
        String cassKeyspace = PlayAppUtils.appConfigString("directory.cassandra.keyspace");
        String cassUser = PlayAppUtils.appConfigString("directory.cassandra.user");
        String cassPassword = PlayAppUtils.appConfigString("directory.cassandra.password");

        CassandraDirectory DIR = new CassandraDirectory(cassHostsAndPorts, cassUser, cassPassword,
                cassKeyspace);
        DIR.setTableFiledata(CassandraDirectory.DEFAULT_TBL_FILEDATA + "_" + normalizedIndexName);
        DIR.setTableMetadata(CassandraDirectory.DEFAULT_TBL_METADATA + "_" + normalizedIndexName);
        DIR.init();
        return DIR;
    }

    private Directory createDirectory(String indexName) throws IOException {
        indexName = normalizeIndexName(indexName);
        String directoryType = PlayAppUtils.appConfigString("directory.type");
        if (StringUtils.equalsIgnoreCase("fs", directoryType)) {
            return createFsDirectory(indexName);
        }
        if (StringUtils.equalsIgnoreCase("redis", directoryType)) {
            return createRedisDirectory(indexName);
        }
        if (StringUtils.equalsIgnoreCase("cassandra", directoryType)) {
            return createCassandraDirectory(indexName);
        }
        throw new IllegalStateException("Unsupported directory type [" + directoryType + "]");
    }

    /*----------------------------------------------------------------------*/
    private AbstractIndex createIndex(String normalizedIndexName, IndexSpec spec)
            throws IOException {
        String serverMode = PlayAppUtils.appConfigString("server.mode");
        if (StringUtils.equalsIgnoreCase("standalone", serverMode)) {
            Directory dir = createDirectory(normalizedIndexName);
            return StandaloneIndex.create(normalizedIndexName, dir, spec);
        }
        return null;
    }

    private AbstractIndex getIndex(final IndexSpec spec) {
        final String indexName = normalizeIndexName(spec.name());
        AbstractIndex index;
        try {
            index = cacheIndex.get(indexName, new Callable<AbstractIndex>() {
                @Override
                public AbstractIndex call() throws Exception {
                    return createIndex(indexName, spec);
                }
            });
        } catch (Exception e) {
            index = null;
        }
        return index;
    }

    /**
     * API: Creates a new index.
     * 
     * @param spec
     * @return
     * @throws IOException
     */
    public IIndex createIndex(IndexSpec spec, boolean override) throws IOException {
        // TODO verify secret

        AbstractIndex index = getIndex(spec);
        index.updateSpec(spec, override);
        return index;
    }

    /**
     * Checks if a index's name is valid.
     * 
     * @param indexName
     * @return
     */
    public boolean isValidIndexName(String indexName) {
        return indexName != null && indexName.matches("^[a-zA-Z0-9]+$");
    }
}
