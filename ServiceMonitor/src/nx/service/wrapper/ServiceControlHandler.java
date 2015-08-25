package nx.service.wrapper;

import java.net.InetAddress;
import java.net.UnknownHostException;

import nx.server.zmq.ClientRequest;
import nx.server.zmq.ClientResponse;
import nx.server.zmq.ZmqClient;
import nx.service.ConfigType;
import nx.service.ServiceConfig;
import nx.service.ServiceManager;
import nx.service.exception.ServiceException;
import nx.service.exception.ServiceProcessException;
import nx.service.exception.ServiceStartUpException;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;

public class ServiceControlHandler implements IControlHandler
{
	private final static Logger logger = Logger.getLogger(ServiceControlHandler.class);

	private Gson gson;

	/**
	 * @throws ServiceException
	 */
	public ServiceControlHandler() throws ServiceException
	{
		gson = new GsonBuilder().create();
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String registerService(ServiceControlRequest req) throws ServiceException
	{
		try
		{
			String cmd = "add";

			String serviceName = ServiceConfig.session().getServiceName();
			String monitorHost = ServiceConfig.session().getString(ConfigType.COMMON, "monitor.host");
			Integer monitorPort = ServiceConfig.session().getInt(ConfigType.COMMON, "monitor.port.request");

			ClientResponse resp = registerWithMonitor(cmd, serviceName, monitorHost, monitorPort);

			if (resp != null && resp.isSuccessful())
			{
				String info = "Service is registered with monitor [" + monitorHost + "]";
				logger.info(info);
				return info;
			}
			else
			{
				String err = "Failed to register service with monitor. Error is: " + resp.getError();
				logger.error(err);
				return err;
			}
		}
		catch (Exception e)
		{
			String err = "Unable to register service to monitor. Reason: " + e.getMessage();
			logger.error(err, e);
			return err;
		}
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String deregisterService(ServiceControlRequest req) throws ServiceException
	{
		try
		{
			String cmd = "remove";

			String serviceName = ServiceConfig.session().getServiceName();
			String monitorHost = ServiceConfig.session().getString(ConfigType.COMMON, "monitor.host");
			Integer monitorPort = ServiceConfig.session().getInt(ConfigType.COMMON, "monitor.port.request");

			ClientResponse resp = registerWithMonitor(cmd, serviceName, monitorHost, monitorPort);

			if (resp.isSuccessful())
			{
				String info = "Service is de-registered with monitor [" + monitorHost + "]";
				logger.info(info);
				return info;
			}
			else
			{
				String err = "Failed to de-register service with monitor. Error is: " + resp.getError();
				logger.error(err);
				return err;
			}
		}
		catch (Exception e)
		{
			String err = "Unable to de-register service to monitor. Reason: " + e.getMessage();
			logger.error(err, e);
			return err;
		}
	}

	protected ClientResponse registerWithMonitor(String cmd, String serviceName, String monitorHost,
			Integer monitorPort) throws UnknownHostException, ServiceException, ServiceStartUpException
	{
		ZmqClient client = new ZmqClient(serviceName, monitorHost, monitorPort, 1000);
		ServiceControlRegistrationRequest regReq = new ServiceControlRegistrationRequest();
		regReq.setService(serviceName);
		regReq.setIpAddr(InetAddress.getLocalHost().getHostName());
		regReq.setControlPort(ServiceConfig.session().getInt(ConfigType.SERVICE, "port.control"));
		regReq.setCmd(cmd);

		ClientRequest registerRequest = new ClientRequest();
		registerRequest.setService("register");
		registerRequest.setRequest(gson.toJson(regReq));

		client.send(registerRequest);
		String ret = client.receive();
		ClientResponse resp = gson.fromJson(ret, ClientResponse.class);
		client.close();
		return resp;
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String getSwitchByKey(ServiceControlRequest req) throws ServiceException
	{
		String statusKey = req.getKey();
		if (statusKey == null || statusKey.isEmpty())
		{
			return "Cannot find requested status name in field 'key'";
		}
		try
		{
			return String.valueOf(ServiceStatus.session().getSwitch(statusKey));
		}
		catch (ServiceProcessException e)
		{
			return "ERROR: " + e.getMessage();
		}
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String setSwitchByKeyAndValue(ServiceControlRequest req) throws ServiceException
	{
		String statusKey = req.getKey();
		String statusValue = req.getValue();
		if (statusKey == null || statusKey.isEmpty())
		{
			return "Cannot find requested status name in field 'key'";
		}
		if (statusValue == null || statusValue.isEmpty())
		{
			return "Cannot find new status value in field 'value'";
		}

		boolean toValue = "true".equalsIgnoreCase(statusValue);
		ServiceStatus.session().setSwitch(statusKey, toValue);
		return "Set status [" + statusKey + "] to value [" + toValue + "]";
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String getAllSwitch(ServiceControlRequest req) throws ServiceException
	{
		return ServiceStatus.session().getAllSwitchesInString();
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String getAllCounters(ServiceControlRequest req) throws ServiceException
	{
		return ServiceStatus.session().getAllCountersInString();
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String getConfigByKey(ServiceControlRequest req) throws ServiceException
	{
		String configKey = req.getKey();
		if (configKey == null || configKey.isEmpty())
		{
			return "Cannot find requested config name in field 'key'";
		}
		String resp;
		try
		{
			resp = ServiceConfig.session().getString(ConfigType.ALL, configKey);
		}
		catch (ServiceException e)
		{
			resp = "ERROR: " + e.getMessage();
		}
		return resp;
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String getAllConfig(ServiceControlRequest req) throws ServiceException
	{

		Config config = ServiceConfig.session().getConfig(ConfigType.ALL);
		String resp = config.root().render(ConfigRenderOptions.concise());
		return resp;
	}

	/**
	 * @param request
	 * @return response
	 * @throws ServiceException
	 */
	public String stopService(ServiceControlRequest req) throws ServiceException
	{
		ServiceManager.session().stopService();
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			//
		}
		ServiceManager.session().forceShutdown();
		return "Service [" + ServiceConfig.session().getServiceName() + "] will be stopped!";
	}
}
