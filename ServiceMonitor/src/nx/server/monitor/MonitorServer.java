package nx.server.monitor;

import nx.server.zmq.ZmqServer;
import nx.service.ConfigType;
import nx.service.ServiceConfig;
import nx.service.exception.ServiceStartUpException;
import nx.service.wrapper.ServiceControlRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MonitorServer
{
	private ZmqServer requestHandlingServer;
	private Gson gson;

	public MonitorServer(String configFile) throws Exception
	{
		gson = new GsonBuilder().create();

		ServiceConfig.initialize("monitor", configFile);
		int clientPort = ServiceConfig.session().getInt(ConfigType.SERVICE, "port.request");
		int workerPort = ServiceConfig.session().getInt(ConfigType.SERVICE, "port.worker");
		requestHandlingServer = new ZmqServer(clientPort, workerPort);
		initRequestHandlers();
	}

	public void initRequestHandlers() throws Exception
	{
		// service registration handler
		requestHandlingServer.addHandler(new MonitorServiceRegistrationHandler(), 2);
		requestHandlingServer.addHandler(new MonitorControlHandler(), 2);
	}

	public void start() throws Exception
	{
		requestHandlingServer.start();
		notifyAllServicesToRegister();
	}

	public void notifyAllServicesToRegister() throws Exception
	{
		// Ask all the services to register with this monitor
		ServiceControlRequest req = new ServiceControlRequest();
		req.setCmd("registerService");
		MonitorControlBroadcaster.getSession().broadcast(gson.toJson(req));
	}

	// ============ Main ============

	public static void main(String[] args) throws Exception
	{
		if (args == null || args.length != 1)
		{
			throw new ServiceStartUpException(
					"Parameters is required: 1) config file");
		}
		/*
		 * An example config file is at conf/monitor.conf. You can modify ports
		 * to fit your needs.
		 */
		String configFile = args[0];

		MonitorServer server = new MonitorServer(configFile);
		server.start();
	}
}
