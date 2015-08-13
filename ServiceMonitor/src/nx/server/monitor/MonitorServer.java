package nx.server.monitor;

import nx.server.zmq.ZmqServer;
import nx.service.wrapper.ConfigType;
import nx.service.wrapper.ServiceConfig;
import nx.service.wrapper.ServiceControlRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MonitorServer
{
	private ZmqServer requestHandlingServer;
	private Gson gson;

	public MonitorServer() throws Exception
	{
		gson = new GsonBuilder().create();

		ServiceConfig.initialize("monitor", "monitor.conf");
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
		MonitorServer server = new MonitorServer();
		server.start();
	}
}
