package nx.server.monitor;

import nx.service.config.ConfigType;
import nx.service.config.ServiceConfig;
import nx.zmq.ZmqPublisher;

public class MonitorControlBroadcaster
{
	private static MonitorControlBroadcaster session;

	private ZmqPublisher publisher;

	protected MonitorControlBroadcaster() throws Exception
	{
		int pubPort = ServiceConfig.session().getInt(ConfigType.SERVICE, "port.notification");
		publisher = new ZmqPublisher(pubPort);
	}

	public static MonitorControlBroadcaster getSession() throws Exception
	{
		if(session == null)
		{
			session = new MonitorControlBroadcaster();
		}
		return session;
	}

	public synchronized void broadcast(String msg) throws Exception
	{
		publisher.publish(msg);
	}
}
