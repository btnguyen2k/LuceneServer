package thrift;

import org.apache.thrift.TException;

import com.github.btnguyen2k.bloomserver.thrift.TBloomResponse;
import com.github.btnguyen2k.bloomserver.thrift.TBloomService;

public class TBloomServiceImpl implements TBloomService.Iface {

    public final static TBloomServiceImpl instance = new TBloomServiceImpl();

    @Override
    public void ping() throws TException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean ping2() throws TException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TBloomResponse put(String _bloomName, String _item) throws TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TBloomResponse mightContain(String _bloomName, String _item) throws TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TBloomResponse initBloom(String _secret, String _bloomName, long _numItems,
            double _expectedFpp, boolean _force, boolean _counting, boolean _scaling)
            throws TException {
        // TODO Auto-generated method stub
        return null;
    }

}
