package nx.engine.data;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisEngine implements IDataEngine
{
	protected String host;
	protected int port;
	protected JedisPool pool;
	protected int maxConn;

	public RedisEngine(String host, int port, int maxConn)
	{
		this.host = host;
		this.port = port;
		this.maxConn = maxConn;
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxConn);
		pool = new JedisPool(config, host, port);
		pool.getResource();
	}

	public IDataEngine newSession() throws Exception
	{
		return new RedisEngine(this.host, this.port, this.maxConn);
	}

	public void close()
	{
		if (pool != null)
		{
			pool.destroy();
		}
	}

	public boolean isKeyExist(String key)
	{
		try (Jedis jedis = pool.getResource())
		{
			return jedis.exists(key);
		}
	}

	public void setData(String key, String data)
	{
		try (Jedis jedis = pool.getResource())
		{
			jedis.set(key, data);
		}
	}

	public void setData(byte[] key, byte[] data)
	{
		try (Jedis jedis = pool.getResource())
		{
			jedis.set(key, data);
		}
	}

	public String getStringData(String key)
	{
		try (Jedis jedis = pool.getResource())
		{
			return jedis.get(key);
		}
	}

	public byte[] getByteData(byte[] key)
	{
		try (Jedis jedis = pool.getResource())
		{
			return jedis.get(key);
		}
	}

	public void setHSetData(String key, String field, String data)
	{
		try(Jedis jedis = pool.getResource())
		{
			jedis.hset(key, field, data);
		}
	}

	public String getHSetDataByField(String key, String field)
	{
		try(Jedis jedis = pool.getResource())
		{
			return jedis.hget(key, field);
		}
	}

	public Map<String, String> getHSetDataAll(String key)
	{
		try(Jedis jedis = pool.getResource())
		{
			return jedis.hgetAll(key);
		}
	}
}
