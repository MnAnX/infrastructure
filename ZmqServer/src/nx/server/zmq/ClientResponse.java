package nx.server.zmq;

public class ClientResponse
{
	String data;
	String error;
	
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
