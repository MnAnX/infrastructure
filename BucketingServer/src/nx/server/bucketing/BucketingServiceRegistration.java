package nx.server.bucketing;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import nx.server.zmq.components.ZmqServerUtils;
import nx.server.zmq.components.ZmqServiceRegistration;

import org.apache.log4j.Logger;

public class BucketingServiceRegistration extends ZmqServiceRegistration
{
	final static Logger logger = Logger.getLogger(BucketingServiceRegistration.class);
	
	HashMap<String, Integer> serviceBucketMap;	
	ZmqServerUtils util;

	public BucketingServiceRegistration()
	{
		super();
		serviceBucketMap = new HashMap<String, Integer>();
		util = new ZmqServerUtils();
	}
	
	public void addToBucketMap(String service, int scale)
	{
		serviceBucketMap.put(service, scale);
	}

	protected byte[] getBucketWorker(String service, String bucketKey)
	{
		synchronized (getLock())
		{
			int serviceScale = serviceBucketMap.get(service);
			int bucketIndex = Math.abs(bucketKey.hashCode()) % serviceScale;			
			return util.generateWorkerIdByte(service, bucketIndex);
		}
	}

	@Override
	public void onWorkerResponse(String service, byte[] workerId)
	{
		synchronized (getLock())
		{
			// process worker start up registration
			if (!getServiceReg().containsKey(service))
			{
				ConcurrentHashMap<byte[], Boolean> workerReg = new ConcurrentHashMap<byte[], Boolean>();
				workerReg.put(workerId, true);
				getServiceReg().put(service, workerReg);
				logger.info("Worker [" + new String(workerId) + "] register to service [" + service + "]");
			}
			if (!getServiceReg().get(service).containsKey(workerId))
			{
				getServiceReg().get(service).put(workerId, true);
				logger.info("Worker [" + new String(workerId) + "] register to service [" + service + "]");
			}
		}
	}
}
