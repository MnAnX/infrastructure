package nx.server.monitor;

import nx.server.zmq.IHandler;
import nx.service.wrapper.ServiceControlRegistrationRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MonitorServiceRegistrationHandler implements IHandler
{
	final static Logger logger = Logger.getLogger(MonitorServiceRegistrationHandler.class);

	private final String SERVICE_NAME = "register";
	private Gson gson;

	public MonitorServiceRegistrationHandler()
	{
		gson = new GsonBuilder().create();
	}

	@Override
	public String getServiceName()
	{
		return SERVICE_NAME;
	}

	@Override
	public String process(String data) throws Exception
	{
		ServiceControlRegistrationRequest req = gson.fromJson(data, ServiceControlRegistrationRequest.class);

		String serviceName = req.getService();
		if ("add".equals(req.getCmd()))
		{
			if (req.getIpAddr() == null || req.getIpAddr().isEmpty() || req.getControlPort() == 0)
			{
				throw new Exception("Both IP Address and Control Port are required for registration.");
			}
			MonitorServiceRegistry.getSession().registerService(serviceName, req.getIpAddr(), req.getControlPort());
		}
		else if ("remove".equals(req.getCmd()))
		{
			MonitorServiceRegistry.getSession().deregisterService(serviceName);
		}
		else
		{
			logger.info("Invalid cmd [" + req.getCmd() + "].");
		}

		return "done";
	}
}
