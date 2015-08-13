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

	public void setService(String service)
	{
		this.service = service;
	}

	public String getCmd()
	{
		return cmd;
	}

	public void setCmd(String cmd)
	{
		this.cmd = cmd;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String toJson()
	{
		return new GsonBuilder().create().toJson(this);
	}
}
