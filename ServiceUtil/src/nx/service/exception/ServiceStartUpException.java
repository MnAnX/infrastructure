package nx.service.exception;

public class ServiceStartUpException extends ServiceException
{
	public ServiceStartUpException(String errMsg) {
		super(errMsg);
	}

	public ServiceStartUpException(String errMsg, Exception e) {
		super(errMsg, e);
	}

}
