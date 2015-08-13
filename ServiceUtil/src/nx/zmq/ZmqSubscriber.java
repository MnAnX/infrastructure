package nx.zmq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

public class ZmqSubscriber
{
	private ZMQ.Context context;
	private ZMQ.Socket sub;
	private Poller poller;
	private Integer timeout;

	public ZmqSubscriber(String host, int port, String topic, Integer timeout)
	{
		this.timeout = timeout == null ? -1 : timeout;
		String connect_str = String.format("tcp://%s:%d", host, port);
		context = ZMQ.context(1);
		sub = context.socket(ZMQ.SUB);
		sub.subscribe(topic.getBytes());
		sub.connect(connect_str);
		poller = new Poller(1);
		poller.register(sub, Poller.POLLIN);

	}

	public synchronized byte[] receive() throws Exception
	{
		if (poller.poll(timeout) == -1)
		{
			return null;
		}
		if (poller.pollin(0))
		{
			return sub.recv(0);
		}
		return null;
	}

	public synchronized String receiveStr() throws Exception
	{
		if (poller.poll(timeout) == -1)
		{
			return null;
		}
		if (poller.pollin(0))
		{
			return sub.recvStr();
		}
		return null;
	}

	public synchronized void close()
	{
		sub.close();
		context.term();
	}
}