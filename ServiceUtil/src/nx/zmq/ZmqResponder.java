package nx.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class ZmqResponder
{
	private ZMQ.Context context;
	private Socket responder;
	private Poller poller;
	private Integer timeout;

	public ZmqResponder(int port, Integer timeout)
	{
		this.timeout = timeout == null ? -1 : timeout;
		context = ZMQ.context(1);
		// Socket to talk to server
		responder = context.socket(ZMQ.REP);
		responder.setLinger(0);
		String url = String.format("tcp://localhost:%d", port);
		responder.connect(url);
		poller = new Poller(1);
		poller.register(responder, Poller.POLLIN);
		if (timeout != null)
		{
			responder.setReceiveTimeOut(timeout);
			responder.setSendTimeOut(timeout);
		}
	}

	public synchronized String receiveRequest()
	{
		if (poller.poll(timeout) == -1)
		{
			return null;
		}
		if (poller.pollin(0))
		{
			return responder.recvStr();
		}
		return null;
	}

	public synchronized void sendResponse(String response)
	{
		// send request
		responder.send(response, 0);
	}

	public synchronized void close()
	{
		responder.close();
		context.close();
	}
}