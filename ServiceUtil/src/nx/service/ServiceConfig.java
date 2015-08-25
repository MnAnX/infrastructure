package nx.service;

import java.io.File;
import java.util.List;

import nx.service.exception.ServiceException;
import nx.service.exception.ServiceStartUpException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

public class ServiceConfig
{
	private static ServiceConfig session;

	private String serviceName;
	private Config allConfig;
	private Config serviceConfig;

	public static void initialize(String serviceName, String configFile) throws ServiceStartUpException
	{
		session = new ServiceConfig(serviceName, configFile);
	}

	public static ServiceConfig session() throws ServiceStartUpException
	{
		if(session == null)
		{
			throw new ServiceStartUpException("Session hasn't been initialized. Please call initialize() first.");
		}
		return session;
	}

	public ServiceConfig(String serviceName, String configFilePath) throws ServiceStartUpException
	{
		if (serviceName == null || serviceName.isEmpty())
		{
			throw new ServiceStartUpException("Invalid service name. Service name cannot be empty.");
		}
		this.serviceName = serviceName;

		try
		{
			allConfig = ConfigFactory.parseFile(new File(configFilePath));
		}
		catch (Exception e)
		{
			throw new ServiceStartUpException("Unable to load configuration file [" + configFilePath + "]. Reason: "
					+ e.getMessage(), e);
		}

		if (allConfig == null || allConfig.isEmpty())
		{
			throw new ServiceStartUpException("Failed to load config file. Config file is empty.");
		}

		// get service configuration by service name
		serviceConfig = findServiceConfig(serviceName);
	}

	public Config findServiceConfig(String serviceName) throws ServiceStartUpException
	{
		List<? extends Config> cluster = allConfig.getConfigList("cluster");
		for (Config service : cluster)
		{
			if (serviceName.equalsIgnoreCase(service.getString("name")))
			{
				return service;
			}
		}
		throw new ServiceStartUpException("Unable to load config of service [" + serviceName
				+ "]. Please make sure its config exists in the cluster.");
	}

	public List<? extends Config> getServiceCluster()
	{
		return allConfig.getConfigList("cluster");
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public Config getConfig(ConfigType type)
	{
		switch (type)
		{
		case ALL:
			return allConfig;
		case COMMON:
			return allConfig.getConfig("common");
		case SERVICE:
			return serviceConfig;
		default:
			return allConfig;
		}
	}

	public String getString(ConfigType type, String key) throws ServiceException
	{
		try
		{
			return getConfig(type).getString(key);
		}
		catch (ConfigException e)
		{
			throw new ServiceException("Config [" + key + "] is missing or malformat.");
		}
	}

	public int getInt(ConfigType type, String key) throws ServiceException
	{
		try
		{
			return getConfig(type).getInt(key);
		}
		catch (ConfigException e)
		{
			throw new ServiceException("Config [" + key + "] is missing or malformat.");
		}
	}

	public long getLong(ConfigType type, String key) throws ServiceException
	{
		try
		{
			return getConfig(type).getLong(key);
		}
		catch (ConfigException e)
		{
			throw new ServiceException("Config [" + key + "] is missing or malformat.");
		}
	}

	public double getDouble(ConfigType type, String key) throws ServiceException
	{
		try
		{
			return getConfig(type).getDouble(key);
		}
		catch (ConfigException e)
		{
			throw new ServiceException("Config [" + key + "] is missing or malformat.");
		}
	}

	public boolean getBoolean(ConfigType type, String key) throws ServiceException
	{
		try
		{
			return getConfig(type).getBoolean(key);
		}
		catch (ConfigException e)
		{
			throw new ServiceException("Config [" + key + "] is missing or malformat.");
		}
	}
}
