package nx.server.monitor;

import java.util.Set;

import nx.server.zmq.IHandler;
import nx.service.wrapper.ServiceControlRequest;
import nx.service.wrapper.ServiceControlResponse;
import nx.zmq.ZmqDealer;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MonitorControlHandler implements IHandler
{
	private final String SERVICE_NAME = "control";
	private Gson gson;

	public MonitorControlHandler()
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
		ServiceControlRequest req = gson.fromJson(data, ServiceControlRequest.class);
		ServiceControlResponse response = new ServiceControlResponse();

		if (isMonitorCmd(req))
		{
			if ("help".equals(req.getCmd()) || "allServices".equals(req.getCmd()) || "all".equals(req.getCmd()))
			{
				// list all registered services
				Set<String> serviceList = MonitorServiceRegistry.getSession().getAllRegisteredServices();
				response.setResponse("All the registered services: \n - "
						+ Joiner.on("\n - ").join(serviceList));
				return gson.toJson(response);
			}
			else
			{
				response.setError("Invalid monitor control command.");
				return gson.toJson(response);
			}
		}
		else
		{
			// not a monitor command, pass it along to the service
			try
			{
				response = sendControlToService(req.getService(), req.getCmd());
				return gson.toJson(response);
			}
			catch (Exception e)
			{
				response.setError(e.getMessage());
				return gson.toJson(response);
			}
		}
	}

	private boolean isMonitorCmd(ServiceControlRequest req)
	{
		if (req.getService() == null)
		{
			return true;
		}
		return false;
	}

	private ServiceControlResponse sendControlToService(String serviceName, String cmd) throws Exception
	{
		String uri = MonitorServiceRegistry.getSession().getServiceUri(serviceName);
		if (uri == null)
		{
			throw new Exception("Requested service [" + serviceName + "] is not registered with Monitor.");
		}

		ZmqDealer client = new ZmqDealer("monitor".getBytes(), uri, 1000);

		ServiceControlRequest request = new ServiceControlRequest();
		request.setService(serviceName);
		request.setCmd(cmd);

		String req = request.toJson();
		client.send(req.getBytes());
		String response = client.receiveStr();
		client.close();

		ServiceControlResponse resp = new GsonBuilder().create().fromJson(response, ServiceControlResponse.class);
		return resp;
	}
}
