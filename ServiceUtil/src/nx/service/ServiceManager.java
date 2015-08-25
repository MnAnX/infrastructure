package nx.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nx.service.exception.ServiceException;
import nx.service.exception.ServiceStartUpException;

import org.apache.log4j.Logger;

public class ServiceManager
{
	private final static Logger logger = Logger.getLogger(ServiceManager.class);

	private static ServiceManager session;

	/**
	 * @return service manager session
	 * @throws ServiceException
	 */
	public static ServiceManager session() throws ServiceException
	{
		if (session == null)
		{
			session = new ServiceManager();
		}
		return session;
	}

	private ExecutorService threadPool;
	private List<Object> runningProcesses;

	/**
	 * @throws ServiceStartUpException
	 */
	public ServiceManager() throws ServiceStartUpException
	{
		threadPool = Executors.newCachedThreadPool();
		runningProcesses = new ArrayList<Object>();
	}

	/**
	 * @param runnable
	 * @param threadName
	 */
	public void startThread(Runnable runnable, String threadName)
	{
		Thread thread = new Thread(runnable);
		thread.setName(threadName);
		threadPool.execute(thread);
		runningProcesses.add(runnable);
	}

	/**
	 * @param process
	 */
	public void regRunningProcess(Object process)
	{
		runningProcesses.add(process);
	}

	public void stopService()
	{
		for(Object proc : runningProcesses)
		{
			try
			{
				Method method = proc.getClass().getDeclaredMethod("stop");
				method.invoke(proc);
			}
			catch (Exception e)
			{
				logger.error("Failed to stop process [" + proc.getClass().getName()+ "]. Reason: " + e.getMessage(), e);
			}
		}
	}

	public void forceShutdown()
	{
		threadPool.shutdownNow();
		System.exit(0);
	}
}
