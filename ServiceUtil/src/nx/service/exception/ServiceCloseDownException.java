package nx.service.exception;

public class ServiceCloseDownException extends ServiceException {

	private static final long serialVersionUID = 3319966210353739935L;


	public ServiceCloseDownException(String errMsg) {
		super(errMsg);

	}

	public ServiceCloseDownException(String errMsg, Exception e)
	{
		super(errMsg, e);
	}



}
