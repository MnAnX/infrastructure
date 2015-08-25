package nx.zmq;

import org.zeromq.ZMQ;

public class ZmqPusher
{
	private ZMQ.Context context;
	private ZMQ.Socket sender;

	/**
	 * @param port
	 */
	public ZmqPusher(int port)
	{
		String connect_str = String.format("tcp://*:%d", port);

		context = ZMQ.context(1);
		sender = context.socket(ZMQ.PUSH);
		sender.setLinger(0);
		sender.bind(connect_str);
	}

	/**
	 * @param msg
	 * @throws Exception
	 */
	public synchronized void send(String msg) throws Exception
	{
		sender.send(msg);
	}

	/**
	 * @param msgs
	 * @throws Exception
	 */
	public synchronized void send(String... msgs) throws Exception
	{
		if (msgs == null || msgs.length == 0)
			return;
		for (int i = 0; i < msgs.length - 1; i++)
		{
			sender.sendMore(msgs[i]);
		}
		sender.send(msgs[msgs.length - 1]);
	}

	public synchronized void close()
	{
		sender.close();
		context.term();
	}
}