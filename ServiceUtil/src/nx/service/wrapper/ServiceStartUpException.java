package nx.service.wrapper;

public class ServiceStartUpException extends ServiceException
{
	public ServiceStartUpException(String errMsg) {
		super(errMsg);
	}

	public ServiceStartUpException(String errMsg, Exception e) {
		super(errMsg, e);
	}

}
