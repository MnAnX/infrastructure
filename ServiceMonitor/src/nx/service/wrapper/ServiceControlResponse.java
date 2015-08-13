package nx.service.wrapper;


public class ServiceControlResponse
{
	String response;
	String error;

	public String getResponse()
	{
		return response;
	}

	public void setResponse(String response)
	{
		this.response = response;
	}

	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	public boolean isSuccessful()
	{
		return error == null || error.isEmpty();
	}
}
