package nx.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

public class ZmqPuller
{
	private ZMQ.Context context;
	private ZMQ.Socket receiver;
	private Poller poller;
	private Integer timeout;

	/**
	 * @param host
	 * @param port
	 * @param timeout
	 */
	public ZmqPuller(String host, int port, Integer timeout)
	{
		this.timeout = timeout == null ? -1 : timeout;
		String connect_str = String.format("tcp://%s:%d", host, port);
		context = ZMQ.context(1);
		receiver = context.socket(ZMQ.PULL);
		receiver.connect(connect_str);
		poller = new Poller(1);
		poller.register(receiver, Poller.POLLIN);
	}

	/**
	 * @return received data in bytes
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
			return receiver.recv(0);
		}
		return null;
	}

	public synchronized void close()
	{
		receiver.close();
		context.term();
	}
}