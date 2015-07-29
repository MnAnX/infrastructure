package nx.example;

import nx.server.bucketing.BucketingServer;
import nx.server.zmq.IHandler;

public class BucketingServerExample
{
	public static void main(String[] args) throws Exception
	{
		int client_port = 15000;
		int worker_port = 15001;
		BucketingServer server = new BucketingServer(client_port, worker_port);

		IHandler handler1 = new ExampleBucketHandler1();
		IHandler handler2 = new ExampleBucketHandler2();

		server.addHandler(handler1, 2);	// handler1 serves for service1. scale for 2 buckets
		server.addHandler(handler2, 3);	// hanlder2 serves for service2. scale for 3 buckets

		server.start();
	}
}

class ExampleBucketHandler1 implements IHandler
{
	@Override
	public String getServiceName()
	{
		return "service1";
	}

	@Override
	public String process(String data) throws Exception
	{
		return "handler1: " + data;
	}
}

class ExampleBucketHandler2 implements IHandler
{
	@Override
	public String getServiceName()
	{
		return "service2";
	}

	@Override
	public String process(String data) throws Exception
	{
		return "handler2: " + data;
	}
}