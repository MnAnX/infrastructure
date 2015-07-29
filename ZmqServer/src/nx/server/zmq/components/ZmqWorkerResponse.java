package nx.server.zmq.components;

public class ZmqWorkerResponse
{
	String service;
	byte[] clientId;
	String data;
	String error;

	public String getService()
	{
		return service;
	}

	public void setService(String service)
	{
		this.service = service;
	}

	public byte[] getClientId()
	{
		return clientId;
	}

	public void setClientId(byte[] clientId)
	{
		this.clientId = clientId;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}
}
