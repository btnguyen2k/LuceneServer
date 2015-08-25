package globals;

import java.io.File;

import org.apache.thrift.server.TServer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import play.Logger;
import play.Play;
import api.IndexApi;

public class Registry {

    synchronized public static void init() {
        initApplicationContext();

        indexApi = getIndexApi();
    }

    synchronized public static void destroy() {
        destroyApplicationContext();
    }

    /*----------------------------------------------------------------------*/
    private static IndexApi indexApi;

    public static IndexApi getIndexApi() {
        if (indexApi == null) {
            indexApi = getBean(IndexApi.class);
        }
        return indexApi;
    }

    /*----------------------------------------------------------------------*/
    public static void startThriftServer(final TServer thriftServer) {
        Thread t = new Thread("Thrift Server") {
            public void run() {
                thriftServer.serve();
            }
        };
        t.start();
    }

    /*----------------------------------------------------------------------*/
    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            return null;
        }
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        try {
            return applicationContext.getBean(name, clazz);
        } catch (BeansException e) {
            return null;
        }
    }

    private static void initApplicationContext() {
        if (Registry.applicationContext == null) {
            String configFile = "conf/spring/beans.xml";
            File springConfigFile = new File(Play.application().path(), configFile);
            AbstractApplicationContext applicationContext = new FileSystemXmlApplicationContext(
                    "file:" + springConfigFile.getAbsolutePath());
            applicationContext.start();
            Registry.applicationContext = applicationContext;
        }
    }

    private static void destroyApplicationContext() {
        if (applicationContext != null) {
            try {
                ((AbstractApplicationContext) applicationContext).destroy();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            } finally {
                applicationContext = null;
            }
        }
    }
}
