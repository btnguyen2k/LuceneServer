package qnd;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class QndFsSearchDemo {

    public static void main(String args[]) throws Exception {
        File path = new File("/Users/btnguyen/Workspace/Ubase/LuceneServer/data/demo");
        Directory DIR = FSDirectory.open(path.toPath());
        IndexReader ir = DirectoryReader.open(DIR);
        IndexSearcher is = new IndexSearcher(ir);
        Analyzer analyzer = new SimpleAnalyzer();

        long t1 = System.currentTimeMillis();
        QueryParser parser = new QueryParser("contents", analyzer);
        Query q = parser.parse("www");

        System.out.println("Num docs: " + is.count(q));
        TopDocs result = is.search(q, 1);
        System.out.println("Num hits: " + result.totalHits);
        // for (ScoreDoc sDoc : result.scoreDocs) {
        // int docId = sDoc.doc;
        // Document doc = is.doc(docId);
        // System.out.println(doc);
        // }
        ir.close();
        long t2 = System.currentTimeMillis();
        System.out.println("Finished in " + (t2 - t1) / 1000.0 + " sec");
    }

}
