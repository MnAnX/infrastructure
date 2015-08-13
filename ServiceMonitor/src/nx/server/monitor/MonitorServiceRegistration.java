package nx.server.monitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class MonitorServiceRegistration
{
	final static Logger logger = Logger.getLogger(MonitorServiceRegistration.class);	
	private final String URI_FORMAT = "tcp://%s:%d";
	
	private static MonitorServiceRegistration session;

	private final Object lock;
	private Map<String, String> serviceReg;

	public static MonitorServiceRegistration getSession()
	{
		if(session == null)
		{
			session = new MonitorServiceRegistration();
		}
		return session;
	}
	
	public MonitorServiceRegistration()
	{
		lock = new Object();
		serviceReg = new HashMap<String, String>();
	}

	public void registerService(String serviceName, String ipAddr, int controlPort)
	{
		synchronized (lock)
		{
			String uri = String.format(URI_FORMAT, ipAddr, controlPort);
			serviceReg.put(serviceName, uri);
		}
	}

	public void deregisterService(String serviceName)
	{
		synchronized (lock)
		{
			serviceReg.remove(serviceName);
		}
	}

	public Set<String> getAllRegisteredServices()
	{
		synchronized (lock)
		{
			Set<String> clonedSet = new HashSet<String>();
			clonedSet.addAll(serviceReg.keySet());
			return clonedSet;
		}
	}

	public Map<String, String> getAllRegisteredServicesWithUri()
	{
		synchronized (lock)
		{
			Map<String, String> clonedMap = new HashMap<String, String>();
			clonedMap.putAll(serviceReg);
			return clonedMap;
		}
	}
	
	public String getServiceUri(String service)
	{
		synchronized (lock)
		{
			return serviceReg.get(service);
		}
	}
}
