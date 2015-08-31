package qnd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

import org.apache.commons.io.IOUtils;

import com.github.ddth.commons.utils.SerializationUtils;

public class QndDemoIndexMultiThread {

    static final long MAX_ITEMS = 10000;
    static final AtomicLong COUNTER = new AtomicLong(0);
    static final ExecutorService ES = Executors.newFixedThreadPool(32);
    static final AtomicLong JOBS_DONE = new AtomicLong(0);

    public static void main(String args[]) throws Exception {
        Map<String, Object> requestData = new HashMap<String, Object>();
        requestData.put("override", Boolean.TRUE);
        requestData.put("secret", "secret");
        requestData.put("default_search_field", "contents");
        Map<String, Object> fields = new HashMap<String, Object>();
        requestData.put("fields", fields);
        {
            Map<String, Object> field = new HashMap<String, Object>();
            field.put("type", "id");
            fields.put("path", field);
        }
        {
            Map<String, Object> field = new HashMap<String, Object>();
            field.put("type", "long");
            fields.put("modified", field);
        }
        {
            Map<String, Object> field = new HashMap<String, Object>();
            field.put("type", "string");
            fields.put("content", field);
        }
        {
            HttpResponse response = HttpRequest.put("http://localhost:9000/demo")
                    .body(SerializationUtils.toJsonString(requestData)).send();
            System.out.println("CreateIndex: " + response.bodyText());
        }

        {
            HttpResponse response = HttpRequest.post("http://localhost:9000/demo/truncate")
                    .body(SerializationUtils.toJsonString(requestData)).send();
            System.out.println("TruncateIndex: " + response.bodyText());
        }

        long t1 = System.currentTimeMillis();
        {
            Path docDir = Paths
                    .get("/Users/btnguyen/Workspace/Apps/Apache-Cassandra-2.1.8/javadoc/");
            indexDocs(docDir);
        }
        {
            Path docDir = Paths.get("/Users/btnguyen/Workspace/Apps/ZooKeeper-3.3.6/docs/");
            indexDocs(docDir);
        }
        {
            Path docDir = Paths.get("/Users/btnguyen/Workspace/Apps/phpmyadmin/");
            indexDocs(docDir);
        }
        ES.shutdown();
        ES.awaitTermination(3600, TimeUnit.SECONDS);
        long t2 = System.currentTimeMillis();
        System.out.println("Finished in " + (t2 - t1) / 1000.0 + " sec");
    }

    static void indexDocs(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    indexDoc(file, attrs.lastModifiedTime().toMillis());
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    static void indexDoc(final Path file, final long lastModified) {
        long counter = COUNTER.incrementAndGet();
        if (counter > MAX_ITEMS) {
            return;
        }
        System.out.println("Counter: " + counter);

        Runnable command = new Runnable() {
            @Override
            public void run() {
                try (InputStream stream = Files.newInputStream(file)) {
                    Map<String, Object> docData = new HashMap<String, Object>();
                    docData.put("path", file.toString());
                    docData.put("modified", lastModified);
                    docData.put("contents", IOUtils.toString(stream));
                    List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>();
                    docs.add(docData);
                    Map<String, Object> requestData = new HashMap<String, Object>();
                    requestData.put("docs", docs);

                    String requestDataJson = SerializationUtils.toJsonString(requestData);
                    System.out.println("Indexing " + file + " [" + requestDataJson.length() + "]");

                    HttpResponse response = HttpRequest.post("http://localhost:9000/demo/index")
                            .body(requestDataJson).send();
                    System.out.println(response.bodyText());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    long jobsDone = JOBS_DONE.incrementAndGet();
                    System.out.println("Jobs done: " + jobsDone);
                }
            }
        };
        ES.execute(command);
    }

}
