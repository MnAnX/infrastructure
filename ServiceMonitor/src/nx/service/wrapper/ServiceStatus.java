package nx.service.wrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nx.service.exception.ServiceProcessException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class ServiceStatus
{
	private static ServiceStatus session;

	private Map<String, ServiceSwitchStatus> switchMap;
	private ListMultimap<String, ServiceCounterStatus> counterMap;

	public static ServiceStatus session()
	{
		if(session == null)
		{
			session = new ServiceStatus();
		}
		return session;
	}

	public ServiceStatus()
	{
		switchMap = new ConcurrentHashMap<String, ServiceSwitchStatus>();
		counterMap = ArrayListMultimap.create();
	}

	public <T> T registerStatusRecorder(String name, IServiceStatus statusRecorder)
	{
		if (statusRecorder instanceof ServiceSwitchStatus)
		{
			switchMap.put(name, (ServiceSwitchStatus) statusRecorder);
		}
		if (statusRecorder instanceof ServiceCounterStatus)
		{
			counterMap.put(name, (ServiceCounterStatus) statusRecorder);
		}
		return (T) statusRecorder;
	}

	public boolean getSwitch(String name) throws ServiceProcessException
	{
		if (switchMap.containsKey(name))
		{
			return switchMap.get(name).getStatus();
		}
		else
		{
			throw new ServiceProcessException("Switch [" + name + "] is not found.");
		}
	}

	public synchronized void setSwitch(String name, boolean val)
	{
		if (switchMap.containsKey(name))
		{
			switchMap.get(name).setStatus(val);
		}
	}

	public String getAllSwitchesInString()
	{
		StringBuilder sb = new StringBuilder();
		for (String key : switchMap.keySet())
		{
			sb.append(key).append(" = ").append(switchMap.get(key).getStatus()).append("\n");
		}
		return sb.toString();
	}

	public String getAllCountersInString()
	{
		StringBuilder sb = new StringBuilder();
		for (String key : counterMap.keySet())
		{
			long sum = sumCounters(counterMap.get(key));
			sb.append(key).append(" = ").append(sum).append("\n");
		}
		return sb.toString();
	}

	private long sumCounters(List<ServiceCounterStatus> list)
	{
		long sum = 0;
		for (ServiceCounterStatus r : list)
		{
			sum = sum + r.getStatus();
		}
		return sum;
	}
}
