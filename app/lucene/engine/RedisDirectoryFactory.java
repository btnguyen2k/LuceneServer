package lucene.engine;

import java.io.IOException;

import lucene.IDirectoryFactory;

import org.apache.lucene.store.Directory;

import util.IndexUtils;

import com.github.ddth.com.redir.RedisDirectory;

/**
 * Factory that creates {@link RedisDirectory} objects.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class RedisDirectoryFactory implements IDirectoryFactory {

    private String redisHost = "localhost";
    private int redisPort = 6379;
    private String redisPassword;

    public String getRedisHost() {
        return redisHost;
    }

    public RedisDirectoryFactory setRedisHost(String redisHost) {
        this.redisHost = redisHost;
        return this;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public RedisDirectoryFactory setRedisPort(int redisPort) {
        this.redisPort = redisPort;
        return this;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public RedisDirectoryFactory setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory createDirectory(String _indexName) throws IOException {
        final String indexName = IndexUtils.normalizeName(_indexName);
        RedisDirectory DIR = new RedisDirectory(redisHost, redisPort, redisPassword);
        DIR.setHashDirectoryMetadata(RedisDirectory.DEFAULT_HASH_DIRECTORY_METADATA + "_"
                + indexName);
        DIR.setHashFileData(RedisDirectory.DEFAULT_HASH_FILE_DATA + "_" + indexName);
        DIR.init();
        return DIR;
    }

}
