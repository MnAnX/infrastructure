package nx.server.bucketing;

import nx.server.zmq.components.ZmqProxy;

import org.apache.log4j.Logger;

import com.google.gson.JsonSyntaxException;

public class BucketingProxy extends ZmqProxy
{
	private final static Logger logger = Logger.getLogger(BucketingProxy.class);

	public BucketingProxy(int clientRequestPort, int workerResponsePort, BucketingServiceRegistration serviceReg)
	{
		super(clientRequestPort, workerResponsePort, serviceReg);
	}

	@Override
	protected void handleRequest(byte[] clientId, byte[] req)
	{
		if (req == null)
		{
			return;
		}

		BucketingClientRequest request;
		try
		{
			request = getGson().fromJson(new String(req), BucketingClientRequest.class);
		}
		catch (JsonSyntaxException e)
		{
			String resp = createErrorResponse("Invalid request format in Json.");
			sendToClient(clientId, resp);
			return;
		}

		String service = request.getService();
		if (!getServiceReg().isServiceRegistered(service))
		{
			String resp = createErrorResponse("Requested service [" + service + "] is not available.");
			sendToClient(clientId, resp);
			return;
		}
		
		if (request.getBucketKey() == null || request.getBucketKey().isEmpty())
		{
			String resp = createErrorResponse("Bucket key needs to be specified.");
			sendToClient(clientId, resp);
			return;
		}
		
		byte[] workerId = ((BucketingServiceRegistration)getServiceReg()).getBucketWorker(service, request.getBucketKey());

		sendWorkerRequest(clientId, workerId, request.getRequest());
	}
	
	protected BucketingServiceRegistration getServiceReg()
	{
		return (BucketingServiceRegistration)super.getServiceReg();
	}
}
