package nx.service.wrapper;

public class ServiceSwitchStatus implements IServiceStatus<Boolean>
{
	boolean sswitch;

	public ServiceSwitchStatus()
	{
		sswitch = false;
	}
	
	public ServiceSwitchStatus(boolean initValue)
	{
		sswitch = initValue;
	}

	@Override
	public Boolean getStatus()
	{
		return sswitch;
	}
	
	public synchronized void setStatus(boolean val)
	{
		sswitch = val;
	}
	
	public synchronized void turnOn()
	{
		sswitch = true;
	}
	
	public synchronized void turnOff()
	{
		sswitch = false;
	}

}
