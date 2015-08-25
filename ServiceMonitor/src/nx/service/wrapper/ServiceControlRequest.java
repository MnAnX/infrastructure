package nx.service.wrapper;

import com.google.gson.GsonBuilder;

public class ServiceControlRequest
{
	String service;
	String cmd;
	String key;
	String value;

	public String getService()
	{
		return service;
	}

	public ServiceControlRequest setService(String service)
	{
		this.service = service;
		return this;
	}

	public String getCmd()
	{
		return cmd;
	}

	public ServiceControlRequest setCmd(String cmd)
	{
		this.cmd = cmd;
		return this;
	}

	public String getKey()
	{
		return key;
	}

	public ServiceControlRequest setKey(String key)
	{
		this.key = key;
		return this;
	}

	public String getValue()
	{
		return value;
	}

	public ServiceControlRequest setValue(String value)
	{
		this.value = value;
		return this;
	}

	public String toJson()
	{
		return new GsonBuilder().create().toJson(this);
	}
}
