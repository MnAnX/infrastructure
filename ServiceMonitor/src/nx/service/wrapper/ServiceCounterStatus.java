package nx.service.wrapper;

public class ServiceCounterStatus implements IServiceStatus<Long>
{
	long counter;

	public ServiceCounterStatus()
	{
		counter = 0;
	}

	@Override
	public Long getStatus()
	{
		return counter;
	}
	
	public synchronized void incrCounter()
	{
		counter++;
	}
	
	public synchronized void decrCounter()
	{
		counter--;
	}
}
