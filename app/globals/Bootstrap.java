package globals;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import thrift.TBloomServiceImpl;
import util.ThriftServerUtils;

import com.github.btnguyen2k.bloomserver.thrift.TBloomService;

public class Bootstrap extends GlobalSettings {

    public void onStart(Application app) {
        super.onStart(app);
        Registry.init();

        startThriftServer();
    }

    @Override
    public void onStop(Application app) {
        stopThriftServer();

        Registry.destroy();
        super.onStop(app);
    }

    private TServer thriftServer;

    private void stopThriftServer() {
        if (thriftServer != null) {
            try {
                thriftServer.stop();
            } catch (Exception e) {
                Logger.warn(e.getMessage(), e);
            } finally {
                thriftServer = null;
            }
        }
    }

    private void startThriftServer() {
        int thriftPort = 0;
        if (Play.isDev()) {
            thriftPort = 9090;
        } else {
            try {
                thriftPort = Integer.parseInt(System.getProperty("thrift.port"));
            } catch (Exception e) {
                thriftPort = 0;
            }
        }

        if (thriftPort > 0) {
            int clientTimeoutMillisecs = 0;
            try {
                clientTimeoutMillisecs = Integer.parseInt(System
                        .getProperty("thrift.clientTimeout"));
            } catch (Exception e) {
                clientTimeoutMillisecs = 0;
            }

            int maxFrameSize = 0;
            try {
                maxFrameSize = Integer.parseInt(System.getProperty("thrift.maxFrameSize"));
            } catch (Exception e) {
                maxFrameSize = 0;
            }

            long maxReadBufferSize = 0;
            try {
                maxReadBufferSize = Integer
                        .parseInt(System.getProperty("thrift.maxReadBufferSize"));
            } catch (Exception e) {
                maxReadBufferSize = 0;
            }

            int numSelectorThreads = 0;
            try {
                numSelectorThreads = Integer.parseInt(System.getProperty("thrift.selectorThreads"));
            } catch (Exception e) {
                numSelectorThreads = 0;
            }

            int numWorkerThreads = 0;
            try {
                numWorkerThreads = Integer.parseInt(System.getProperty("thrift.workerThreads"));
            } catch (Exception e) {
                numWorkerThreads = 0;
            }

            TServer thriftServer = null;
            try {
                TProcessorFactory processorFactory = new TProcessorFactory(
                        new TBloomService.Processor<TBloomService.Iface>(TBloomServiceImpl.instance));
                TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
                thriftServer = ThriftServerUtils.createThreadedSelectorServer(processorFactory,
                        protocolFactory, thriftPort, clientTimeoutMillisecs, maxFrameSize,
                        maxReadBufferSize, numSelectorThreads, numWorkerThreads);
            } catch (Exception e) {
                thriftServer = null;
            }
            Logger.info("Starting Thrift API server on port " + thriftPort + "...");
            this.thriftServer = thriftServer;
            Registry.startThriftServer(thriftServer);
        }
    }
}
