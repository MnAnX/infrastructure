package nx.service.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private ExecutorService threadManager;
	private List<IProcess> runningProcesses;

	public ServiceManager() throws ServiceStartUpException
	{
		threadManager = Executors.newCachedThreadPool();
		runningProcesses = new ArrayList<IProcess>();
	}

	public void startThread(Runnable runnable, String threadName)
	{
		Thread thread = new Thread(runnable);
		thread.setName(threadName);
		threadManager.execute(thread);
		if(runnable instanceof IProcess)
		{
			runningProcesses.add((IProcess) runnable);
		}
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
		threadManager.shutdownNow();
		System.exit(0);
	}
}
