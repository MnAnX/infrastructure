package nx.server.bucketing;

import nx.server.zmq.IHandler;
import nx.server.zmq.ZmqServer;
import nx.server.zmq.components.IZmqWorker;

import org.apache.log4j.Logger;

public class BucketingServer extends ZmqServer
{
	private final static Logger logger = Logger.getLogger(BucketingServer.class);

	/**
	 * @param clientRequestPort
	 * @param workerResponsePort
	 */
	public BucketingServer(int clientRequestPort, int workerResponsePort)
	{
		super(clientRequestPort, workerResponsePort,
				new BucketingProxy(clientRequestPort, workerResponsePort, new BucketingServiceRegistration()));
	}

	@Override
	public void addHandler(IHandler handler, int scale) throws Exception
	{
		super.addHandler(handler, scale);;
		((BucketingProxy)getProxy()).getServiceReg().addToBucketMap(handler.getServiceName(), scale);
	}

	@Override
	protected IZmqWorker createWorker(int workerResponsePort, IHandler handler, int i)
	{
		return new BucketingWorker(workerResponsePort, handler, i);
	}
}
