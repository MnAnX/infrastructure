package nx.example;

import nx.engine.data.RedisEngine;
import nx.server.zmq.IHandler;
import nx.server.zmq.ZmqServer;
import nx.service.config.ConfigType;
import nx.service.config.ServiceConfig;
import nx.service.exception.ServiceProcessException;
import nx.service.exception.ServiceStartUpException;
import nx.service.thread.IProcess;
import nx.service.thread.ServiceManager;
import nx.service.wrapper.ServiceController;
import nx.service.wrapper.ServiceCounterStatus;
import nx.service.wrapper.ServiceStatus;

import org.apache.log4j.Logger;

public class ExampleService
{
	private final static Logger logger = Logger.getLogger(ExampleService.class);

	private String serviceName;
	private ZmqServer zmqServer;
	private DataGenerator dataGen;

	public ExampleService(String serviceName, String configFile) throws Exception
	{
		/*
		 * Your given service name must be defined in the given config file. The
		 * config file needs to have a format as:
		 *
		 * common = { // common parts across all nodes } cluster = [ { name =
		 * <service_name> // other configs } // more nodes if necessary ]
		 *
		 * ServiceConfig will find matching service name in the defined cluster.
		 */

		this.serviceName = serviceName;

		// Load service configuration.
		ServiceConfig.initialize(serviceName, configFile);

		if (!isServiceActive())
		{
			throw new ServiceStartUpException(
					"Service [" + serviceName + "] is not set to active as per configuration. Start up is aborted.");
		}

		logger.info("Initializing [" + serviceName + "]...");

		/*
		 * When getting configuration, if specify ConfigType to be SERVICE, it will
		 * look up for configurations matching underlying service name under the
		 * cluster section. If ConfigType is COMMON, it looks up under common
		 * section. If ALL, it will look up across the entire config file.
		 */
		int clientPort = ServiceConfig.session().getInt(ConfigType.SERVICE, "query.port.client");
		int workerPort = ServiceConfig.session().getInt(ConfigType.SERVICE, "query.port.worker");
		String redisHost = ServiceConfig.session().getString(ConfigType.SERVICE, "redis.host");
		int redisPort = ServiceConfig.session().getInt(ConfigType.SERVICE, "redis.port");
		int redisScale = ServiceConfig.session().getInt(ConfigType.SERVICE, "redis.scale");
		int handlerScale = ServiceConfig.session().getInt(ConfigType.SERVICE, "service.scale");

		// Init
		zmqServer = new ZmqServer(clientPort, workerPort);
		RedisEngine redisEngine = new RedisEngine(redisHost, redisPort, redisScale);
		dataGen = new DataGenerator(redisEngine);
		IHandler handler = new ExampleHandler(redisEngine);
		zmqServer.addHandler(handler, handlerScale);

		logger.info("[" + serviceName + "] has been initialized successfully");
	}

	public void start() throws Exception
	{
		logger.info("Starting service [" + serviceName + "]...");

		/*
		 * Start data generator as a new thread. This thread will be managed by
		 * Service Manager.
		 */
		ServiceManager.session().startThread(dataGen, "DataGeneratorThread");

		/*
		 * Start the zmq server and register it with Service Manager. This
		 * allows service manager to stop it. This will be used if the service
		 * is stopped remotely by Monitor.
		 */
		zmqServer.start();
		ServiceManager.session().regRunningProcess((IProcess) zmqServer);
	}

	public void stop()
	{
		logger.info("Stopping service [" + serviceName + "]");
		zmqServer.stop();
		dataGen.stop();
	}

	private boolean isServiceActive() throws Exception
	{
		return ServiceConfig.session().getBoolean(ConfigType.SERVICE, "active");
	}

	// ============ Main ============

	public static void main(String[] args) throws Exception
	{
		if (args == null || args.length != 2)
		{
			throw new ServiceStartUpException(
					"Parameters are required: 1) service name 2) config file");
		}
		/*
		 * An example config file is at conf/example-service.conf, which defined
		 * two valid service names. One of them is 'example-service-1'.
		 */
		String serviceName = args[0];
		String configFile = args[1];

		ExampleService service = new ExampleService(serviceName, configFile);
		service.start();

		/*
		 * Start the controller. This allows Monitor to control the service
		 * remotely.
		 */
		ServiceController controller = new ServiceController();
		controller.start();
	}
}

class ExampleHandler implements IHandler
{
	private RedisEngine redis;
	private ServiceCounterStatus reqCounter;

	public ExampleHandler(RedisEngine redis)
	{
		this.redis = redis;
		/*
		 * Declare a counter and register it with ServiceStatus. This counter
		 * could be accessed by Monitor, so client can query service status
		 * remotely.
		 */
		this.reqCounter = ServiceStatus.session().registerStatusRecorder(
				"ExampleHandler.Received_Requests",
				new ServiceCounterStatus());
	}

	@Override
	public String getServiceName()
	{
		return "example-service";
	}

	@Override
	public String process(String key) throws ServiceProcessException
	{
		reqCounter.incrCounter();
		if (redis.isKeyExist(key))
		{
			return redis.getStringData(key);
		}
		throw new ServiceProcessException("Key [" + key + "] does not exist.");
	}
}

class DataGenerator implements Runnable, IProcess
{
	private final static Logger logger = Logger.getLogger(DataGenerator.class);

	private boolean isStop;
	private RedisEngine redis;

	public DataGenerator(RedisEngine redis)
	{
		this.redis = redis;
	}

	@Override
	public void run()
	{
		/* Generate random data and put it in cache every second. */
		logger.info("Data Generator start running.");
		while (!isStop && !Thread.currentThread().isInterrupted())
		{
			try
			{
				redis.setData(String.valueOf(System.currentTimeMillis()), String.valueOf(Math.random()));
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
				logger.error("Error generating data.", e);
			}
		}
		redis.close();
	}

	@Override
	public void stop()
	{
		isStop = true;
	}
}
