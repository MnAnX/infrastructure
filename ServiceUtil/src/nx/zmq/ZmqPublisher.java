package nx.zmq;

import org.zeromq.ZMQ;

public class ZmqPublisher
{
	private ZMQ.Context context;
	private ZMQ.Socket pub;

	/**
	 * @param port
	 */
	public ZmqPublisher(int port)
	{
		String connect_str = String.format("tcp://*:%d", port);

		context = ZMQ.context(1);
		pub = context.socket(ZMQ.PUB);
		pub.bind(connect_str);
	}

	/**
	 * @param message
	 * @throws Exception
	 */
	public synchronized void publish(byte[] message) throws Exception
	{
		pub.send(message, 0);
	}

	/**
	 * @param message
	 * @throws Exception
	 */
	public synchronized void publish(String message) throws Exception
	{
		pub.send(message, 0);
	}

	public synchronized void close()
	{
		pub.close();
		context.term();
	}
}