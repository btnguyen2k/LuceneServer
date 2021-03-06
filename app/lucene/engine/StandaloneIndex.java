package lucene.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import lucene.IActionQueue;
import lucene.action.DeleteAction;
import lucene.action.IndexAction;
import lucene.action.TruncateAction;
import lucene.spec.IndexSpec;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import play.Logger;

/**
 * Base class for stand-alone (aka single-server) indices.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class StandaloneIndex extends AbstractIndex {

    public static StandaloneIndex create(Directory directory, IndexSpec spec,
            IActionQueue actionQueue) throws IOException {
        if (directory != null && spec != null) {
            StandaloneIndex index = new StandaloneIndex(directory, spec, actionQueue);
            index.init();
            return index;
        }
        return null;
    }

    public StandaloneIndex(Directory directory, IndexSpec spec, IActionQueue actionQueue) {
        super(directory, spec, actionQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StandaloneIndex init() throws IOException {
        super.init();

        commitThread = new CommitThread(getName());
        commitThread.start();

        return this;
    }

    public void destroy() {
        if (commitThread != null) {
            try {
                commitThread.stopExecution();
            } catch (Exception e) {
                // EMPTY
            } finally {
                commitThread = null;
            }
        }

        super.destroy();
    }

    private AtomicLong uncommitActions = new AtomicLong(0);

    private final class CommitThread extends Thread {
        private boolean running = true;

        public CommitThread(String indexName) {
            super("CommitThread - " + indexName);
            setDaemon(true);
        }

        public void stopExecution() {
            this.running = false;
        }

        public void run() {
            while (running && !isInterrupted()) {
                try {
                    doCommit();
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }
    }

    /*----------------------------------------------------------------------*/
    private CommitThread commitThread;

    /**
     * {@inheritDoc}
     */
    @Override
    protected IndexWriter openIndexWriter() throws IOException {
        Directory directory = getDirectory();
        try {
            if (directory.fileLength(IndexWriter.WRITE_LOCK_NAME) >= 0) {
                directory.deleteFile(IndexWriter.WRITE_LOCK_NAME);
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
        }
        return super.openIndexWriter();
    }

    /**
     * Perform index commit.
     * 
     * @throws IOException
     */
    protected void doCommit() throws IOException {
        if (uncommitActions.get() > 0) {
            Lock lock = getWriteLock();
            lock.lock();
            try {
                long numDocs = uncommitActions.get();
                IndexWriter iw = getIndexWriter();
                if (Logger.isDebugEnabled()) {
                    Map<String, Object> stats = new HashMap<String, Object>();
                    stats.put("docs", iw.numDocs());
                    stats.put("del", iw.hasDeletions());
                    stats.put("pending", iw.hasPendingMerges());
                    stats.put("change", iw.hasUncommittedChanges());
                    Logger.debug("[" + getName() + "] committing " + numDocs
                            + (numDocs > 1 ? " changes: " : " change: ") + stats);
                }
                iw.commit();
                if (Logger.isDebugEnabled()) {
                    Map<String, Object> stats = new HashMap<String, Object>();
                    stats.put("docs", iw.numDocs());
                    stats.put("del", iw.hasDeletions());
                    stats.put("pending", iw.hasPendingMerges());
                    stats.put("change", iw.hasUncommittedChanges());
                    Logger.debug("\tafter commit: " + stats);
                }
                uncommitActions.set(0);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unused")
    @Override
    protected boolean performIndexAction(IndexAction action) throws IOException {
        Map<String, Object> docData = action.doc();
        Document doc = buildDocument(docData);
        if (doc != null) {
            Lock lock = getReadLock();
            lock.lock();
            try {
                IndexWriter iw = getIndexWriter();
                Query queryForDeletion = buildQueryForDeletion(docData);
                if (queryForDeletion != null) {
                    iw.deleteDocuments(queryForDeletion);
                }
                iw.addDocument(doc);
                long value = uncommitActions.incrementAndGet();
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unused")
    @Override
    protected boolean performDeleteAction(DeleteAction action) throws IOException {
        Lock lock = getReadLock();
        lock.lock();
        try {
            IndexWriter iw = getIndexWriter();
            switch (action.deleteMethod()) {
            case DeleteAction.DELETE_METHOD_TERM: {
                Query queryForDeletion = buildQueryForDeletion(action.term());
                if (queryForDeletion != null) {
                    iw.deleteDocuments(queryForDeletion);
                    long value = uncommitActions.incrementAndGet();
                    return true;
                }
            }
            default: {
                Query queryForDeletion = parseQuery(action.query());
                if (queryForDeletion != null) {
                    iw.deleteDocuments(queryForDeletion);
                    long value = uncommitActions.incrementAndGet();
                    return true;
                }
            }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unused")
    @Override
    protected boolean performTruncateAction(TruncateAction action) throws IOException {
        Lock lock = getReadLock();
        lock.lock();
        try {
            IndexWriter iw = getIndexWriter();
            iw.deleteAll();
            long value = uncommitActions.incrementAndGet();
            return true;
        } finally {
            lock.unlock();
        }
    }
}
