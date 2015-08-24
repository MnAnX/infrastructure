package nx.service.exception;

public class ServiceProcessException extends ServiceException
{
	public ServiceProcessException(String errMsg) 
	{
		super(errMsg);
	}
	
	public ServiceProcessException(String errMsg, Exception e) 
	{
		super(errMsg, e);
	}

}
