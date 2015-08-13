package nx.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class ZmqRequester
{
	private ZMQ.Context context;
	private Socket requester;

	public ZmqRequester(String targetAddress, int targetPort, String clientId, Integer timeout)
	{
		context = ZMQ.context(1);
		// Socket to talk to server
		requester = context.socket(ZMQ.REQ);
		requester.setLinger(0);
		requester.setIdentity(clientId.getBytes());
		String url = String.format("tcp://%s:%d", targetAddress, targetPort);
		requester.connect(url);
		if (timeout != null)
		{
			requester.setReceiveTimeOut(timeout);
		}
	}

	public synchronized String sendRequest(String request) throws Exception
	{
		// send request
		requester.send(request, 0);

		// get response
		return requester.recvStr();
	}

	public synchronized void close()
	{
		requester.close();
		context.term();
	}
}