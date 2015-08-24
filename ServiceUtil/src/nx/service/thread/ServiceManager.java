package nx.service.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nx.service.exception.ServiceException;
import nx.service.exception.ServiceStartUpException;

public class ServiceManager
{
	private static ServiceManager session;

	public static ServiceManager session() throws ServiceException
	{
		if(session == null)
		{
			session = new ServiceManager();
		}
		return session;
	}

	private ExecutorService threadPool;
	private List<IProcess> runningProcesses;

	public ServiceManager() throws ServiceStartUpException
	{
		threadPool = Executors.newCachedThreadPool();
		runningProcesses = new ArrayList<IProcess>();
	}

	public void startThread(Runnable runnable, String threadName)
	{
		Thread thread = new Thread(runnable);
		thread.setName(threadName);
		threadPool.execute(thread);
		if(runnable instanceof IProcess)
		{
			runningProcesses.add((IProcess) runnable);
		}
	}

	public void regRunningProcess(IProcess process)
	{
		runningProcesses.add(process);
	}

	public void stopService()
	{
		for(IProcess proc : runningProcesses)
		{
			proc.stop();
		}
	}

	public void forceShutdown()
	{
		threadPool.shutdownNow();
		System.exit(0);
	}
}
