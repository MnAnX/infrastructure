package nx.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

public class ZmqRouter
{
	private ZMQ.Context context;
	private ZMQ.Socket router;
	private Poller poller;
	private Integer timeout;
	byte[] callbackId = null;

	/**
	 * @param port
	 * @param timeout
	 */
	public ZmqRouter(int port, Integer timeout)
	{
		this.timeout = timeout == null ? -1 : timeout;
		String connect_str = String.format("tcp://*:%d", port);
		context = ZMQ.context(1);
		router = context.socket(ZMQ.ROUTER);
		router.setLinger(0);
		router.bind(connect_str);
		poller = new Poller(1);
		poller.register(router, Poller.POLLIN);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public synchronized byte[] receive() throws Exception
	{
		if (poller.poll(timeout) == -1)
		{
			return null;
		}
		if (poller.pollin(0))
		{
			callbackId = router.recv(0);
			byte[] ret = router.recv(0);
			return ret;
		}
		return null;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public synchronized String receiveStr() throws Exception
	{
		if (poller.poll(timeout) == -1)
		{
			return null;
		}
		if (poller.pollin(0))
		{
			callbackId = router.recv(0);
			router.recv(0);
			return router.recvStr();
		}
		return null;
	}

	/**
	 * @param msg
	 */
	public synchronized void send(byte[] msg)
	{
		router.sendMore(callbackId);
		router.sendMore("");
		router.send(msg, 0);
	}

	/**
	 * @param msg
	 */
	public synchronized void sendMore(byte[] msg)
	{
		router.send(msg, 1);
	}

	public synchronized void send(String msg)
	{
		router.sendMore(callbackId);
		router.sendMore("");
		router.send(msg);
	}

	/**
	 * @param msg
	 */
	public synchronized void sendMore(String msg)
	{
		router.sendMore(msg);
	}

	public synchronized byte[] getCallbackId()
	{
		return callbackId;
	}

	public synchronized void close()
	{
		router.close();
		context.term();
	}
}