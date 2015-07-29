package nx.server.bucketing;

import java.util.LinkedList;
import java.util.Queue;

import nx.server.zmq.IHandler;
import nx.server.zmq.components.ZmqWorker;
import nx.server.zmq.components.ZmqWorkerRequest;

import org.apache.log4j.Logger;

public class BucketingWorker extends ZmqWorker
{
	final static Logger logger = Logger.getLogger(BucketingWorker.class);
	
	Queue<String> taskQueue;
	
	public BucketingWorker(int workerResponsePort, IHandler handler, int index)
	{
		super(workerResponsePort, handler, index);
		taskQueue = new LinkedList<String>();
	}
	
	@Override
	protected void process() throws Exception
	{
		String recvMsg = getDealer().receiveStr();	
		if (recvMsg == null || recvMsg.isEmpty())
		{
			Thread.sleep(100);
			return;
		}
		
		String msg = getNextTask(recvMsg);		
		
		logger.debug("Worker [" + getWorkerId() + "] received request: " + msg);

		ZmqWorkerRequest req = convertRequest(msg);

		try
		{
			String ret = getHandler().process(req.getData());
			sendResponse(req.getClientId(), ret);
		}
		catch (Exception e)
		{
			sendError(req.getClientId(), e.getMessage());
		}
	}

	protected String getNextTask(String recvMsg)
	{
		String msg;
		if(taskQueue.isEmpty())
		{
			msg = recvMsg;
		}
		else
		{
			taskQueue.add(recvMsg);
			msg = taskQueue.poll();
		}
		return msg;
	}
}
