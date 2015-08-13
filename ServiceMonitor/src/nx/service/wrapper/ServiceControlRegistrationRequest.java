package nx.service.wrapper;

public class ServiceControlRegistrationRequest extends ServiceControlRequest
{
	String ipAddr;
	int controlPort;

	public String getIpAddr()
	{
		return ipAddr;
	}

	public void setIpAddr(String ipAddr)
	{
		this.ipAddr = ipAddr;
	}

	public int getControlPort()
	{
		return controlPort;
	}

	public void setControlPort(int controlPort)
	{
		this.controlPort = controlPort;
	}
}
