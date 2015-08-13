package nx.service.wrapper;

import org.apache.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;

public class ServiceControlListener implements Runnable, IProcess
{
	private final static Logger logger = Logger.getLogger(ServiceControlListener.class);

	private boolean isStopped = false;

	private ServiceController controller;

	private ZMQ.Context zmqContext;
	private ZMQ.Socket router;
	private ZMQ.Socket subscriber;
	private Poller poller;

	public ServiceControlListener(ServiceController controller) throws ServiceException
	{
		this.controller = controller;
		// init zmq
		zmqContext = ZMQ.context(1);
		router = initRouter();
		subscriber = initSubscriber();
		poller = new Poller(2);
		poller.register(router, Poller.POLLIN);
		poller.register(subscriber, Poller.POLLIN);
	}

	public ZMQ.Socket initRouter() throws ServiceException
	{
		Integer serviceControlPort = ServiceConfig.session().getInt(ConfigType.SERVICE, "service.control_port");
		String uri = String.format("tcp://*:%d", serviceControlPort);
		ZMQ.Socket router = zmqContext.socket(ZMQ.ROUTER);
		router.setLinger(0);
		router.setReceiveTimeOut(100);
		router.bind(uri);
		logger.info("Service control router starts on port [" + serviceControlPort + "]");
		return router;
	}

	public ZMQ.Socket initSubscriber() throws ServiceException
	{
		String monitorHost = ServiceConfig.session().getString(ConfigType.COMMON, "monitor.host");
		Integer monitorNotificationPort = ServiceConfig.session()
				.getInt(ConfigType.COMMON, "monitor.port.notification");
		String uri = String.format("tcp://%s:%d", monitorHost, monitorNotificationPort);
		ZMQ.Socket subscriber = zmqContext.socket(ZMQ.SUB);
		subscriber.setLinger(0);
		subscriber.setReceiveTimeOut(100);
		subscriber.connect(uri);
		logger.info("Service control subscriber starts on uri [" + uri + "]");
		return subscriber;
	}

	@Override
	public void run()
	{
		logger.info("Service controller (" + Thread.currentThread().getName() + ") start running.");

		try
		{
			while (!isStopped && !Thread.currentThread().isInterrupted())
			{
				try
				{
					if (poller.poll(100) == -1)
					{
						continue;
					}
					if (poller.pollin(0))
					{
						// router
						byte[] senderId = router.recv(0);
						byte[] bmsg = router.recv(0);

						String msg = new String(bmsg);
						logger.debug("Incoming control cmd from [" + new String(senderId) + "]: " + msg);
						String response = controller.process(msg);
						router.send(senderId, ZMQ.SNDMORE);
						router.send(response, 0);
						logger.info("Control cmd processed.");
					}
					if (poller.pollin(1))
					{
						// subscriber
						byte[] bmsg = subscriber.recv(0);

						String msg = new String(bmsg);
						logger.debug("Incoming monitor notification: " + msg);
						controller.process(msg);
						logger.info("Notification processed.");
					}
				}
				catch (Exception e)
				{
					logger.error("Error processing control request: " + e.getMessage());
				}
			}
		}
		finally
		{
			close();
		}
	}

	private void close()
	{
		router.close();
		subscriber.close();
		zmqContext.term();
		logger.info("Service control listener is stopped.");
	}

	public void stop()
	{
		isStopped = true;
	}
}