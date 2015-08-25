package nx.service.wrapper;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nx.service.ServiceConfig;
import nx.service.ServiceManager;
import nx.service.exception.ServiceException;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class ServiceController
{
	private final static Logger logger = Logger.getLogger(ServiceController.class);

	private Gson gson;

	private ServiceControlListener listener;
	IControlHandler handler;
	private Map<String, Method> handlerMethodMap;

	public ServiceController() throws ServiceException
	{
		this(new ServiceControlHandler()); // basic control handler
	}

	public ServiceController(IControlHandler handler) throws ServiceException
	{
		this.handler = handler;
		gson = new GsonBuilder().create();
		listener = new ServiceControlListener(this);

		initHandlerMethods(handler);
	}

	protected void initHandlerMethods(IControlHandler handler)
	{
		handlerMethodMap = new HashMap<String, Method>();
		Set<Method> comp = new HashSet<Method>();
		Collections.addAll(comp, new Object().getClass().getMethods());
		for (Method m : handler.getClass().getMethods())
		{
			if (!comp.contains(m))
			{
				handlerMethodMap.put(m.getName(), m);
			}
		}
	}

	public void start() throws Exception
	{
		ServiceManager.session().startThread(listener, "ServiceControlListenerThread");
		handler.registerService(null);
	}

	public synchronized String process(String request)
	{
		ServiceControlResponse response = new ServiceControlResponse();
		if (request == null)
		{
			logger.debug("Invalid request. Request is empty,");
			response.setError("Invalid request. Request is empty,");
			return gson.toJson(response);
		}

		ServiceControlRequest req;
		try
		{
			req = gson.fromJson(request, ServiceControlRequest.class);
		}
		catch (JsonSyntaxException | Error e)
		{
			logger.debug("Invalid request. Reason: " + e.getMessage(), e);
			response.setError("Invalid request. Reason: " + e.getMessage());
			return gson.toJson(response);
		}

		try
		{
			String service = req.getService();
			if (service == null || !service.equalsIgnoreCase(ServiceConfig.session().getServiceName()))
			{
				response.setError("Error: Requested service [" + service + "] is not current service ["
						+ ServiceConfig.session().getServiceName() + "]");
				return gson.toJson(response);
			}
		}
		catch (Exception e)
		{
			logger.error("Unable to validate service. Reason: " + e.getMessage(), e);
		}

		String cmd = req.getCmd();
		if ("allCommands".equalsIgnoreCase(cmd) || "help".equalsIgnoreCase(cmd))
		{
			response.setResponse("Valid commands are: \n - "
					+ Joiner.on("\n - ").join(handlerMethodMap.keySet()));
			return gson.toJson(response);
		}
		if (handlerMethodMap.containsKey(cmd))
		{
			try
			{
				Method m = handlerMethodMap.get(cmd);
				String ret = (String) m.invoke(handler, req);
				response.setResponse(ret);
				return gson.toJson(response);
			}
			catch (Exception e)
			{
				logger.error("Command invoking error. Reason: " + e.getMessage(), e);
				response.setError("Command invoking error. Reason: " + e.getMessage());
				return gson.toJson(response);
			}
		}
		else
		{
			// invalid command
			response.setError("Invalid control command. Valid commands are: "
					+ Joiner.on(", ").join(handlerMethodMap.keySet()));
			return gson.toJson(response);
		}
	}
}
