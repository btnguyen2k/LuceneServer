package lucene.engine;

import java.io.IOException;

import lucene.IDirectoryFactory;

import org.apache.lucene.store.Directory;

import util.IndexUtils;

import com.github.ddth.com.cassdir.CassandraDirectory;

/**
 * Factory that creates {@link CassandraDirectory} objects.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class CassandraDirectoryFactory implements IDirectoryFactory {

    private String cassHostsAndPorts = "localhost:9042";
    private String cassKeyspace;
    private String cassUser, cassPassword;

    public String getCassHostsAndPorts() {
        return cassHostsAndPorts;
    }

    public CassandraDirectoryFactory setCassHostsAndPorts(String cassHostsAndPorts) {
        this.cassHostsAndPorts = cassHostsAndPorts;
        return this;
    }

    public String getCassKeyspace() {
        return cassKeyspace;
    }

    public CassandraDirectoryFactory setCassKeyspace(String cassKeyspace) {
        this.cassKeyspace = cassKeyspace;
        return this;
    }

    public String getCassUser() {
        return cassUser;
    }

    public CassandraDirectoryFactory setCassUser(String cassUser) {
        this.cassUser = cassUser;
        return this;
    }

    public String getCassPassword() {
        return cassPassword;
    }

    public CassandraDirectoryFactory setCassPassword(String cassPassword) {
        this.cassPassword = cassPassword;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directory createDirectory(String _indexName) throws IOException {
        final String indexName = IndexUtils.normalizeName(_indexName);
        CassandraDirectory DIR = new CassandraDirectory(cassHostsAndPorts, cassUser, cassPassword,
                cassKeyspace);
        DIR.setTableFiledata(CassandraDirectory.DEFAULT_TBL_FILEDATA + "_" + indexName);
        DIR.setTableMetadata(CassandraDirectory.DEFAULT_TBL_METADATA + "_" + indexName);
        DIR.init();
        return DIR;
    }

}
