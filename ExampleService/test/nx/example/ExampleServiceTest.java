package nx.example;

import junit.framework.TestCase;
import nx.server.zmq.ClientRequest;
import nx.server.zmq.ClientResponse;
import nx.server.zmq.ZmqClient;
import nx.service.wrapper.ServiceControlRequest;
import nx.service.wrapper.ServiceControlResponse;
import nx.zmq.ZmqDealer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExampleServiceTest extends TestCase
{
	/*
	 * Before running the tests, start Service Monitor (MonitorServer), and then
	 * start Example Service. Example service will register with the monitor.
	 * You can also run MonitorClient under project ServiceMonitor to control
	 * from command line.
	 */

	public void test_QueryFromClient()
	{
		// Send request to the Example Service, whose default client port is
		// 9100
		ZmqClient client = new ZmqClient("example-service-client", "localhost", 9100, 1000);

		ClientRequest req = new ClientRequest();
		req.setService("example-service");
		req.setRequest(String.valueOf(System.currentTimeMillis() / 1000 - 1));

		client.send(req);
		String response = client.receive();
		System.out.println("Recieved: " + response);

		ClientResponse resp = new GsonBuilder().create().fromJson(response, ClientResponse.class);
		assertNotNull(resp);
		assertTrue(resp.isSuccessful());
		System.out.println("Response: " + resp.getResponse());

		client.close();
	}

	public void test_ControlThroughMonitor_listAllRegisteredServices()
	{
		// Send request to the Service Monitor, whose default client port is
		// 10001
		ZmqDealer client = new ZmqDealer("monitor-client".getBytes(), "localhost", 10001, 1000);

		ClientRequest req = new ClientRequest();
		req.setService("control");
		// The request is Json of ServiceControlRequest. Leave field 'service'
		// to be empty indicates it's a monitor command
		req.setRequest(new ServiceControlRequest().setCmd("all").toJson());

		client.send(req.toJson());
		String response = client.receiveStr();
		System.out.println("Recieved: " + response);

		Gson gson = new GsonBuilder().create();
		ClientResponse resp = gson.fromJson(response, ClientResponse.class);
		assertNotNull(resp);
		assertTrue(resp.isSuccessful());
		System.out
				.println("Response: " + gson.fromJson(resp.getResponse(), ServiceControlResponse.class).getResponse());

		client.close();
	}

	public void test_ControlThroughMonitor_getAllCommandsOfExampleService()
	{
		ZmqDealer client = new ZmqDealer("monitor-client".getBytes(), "localhost", 10001, 1000);

		ClientRequest req = new ClientRequest();
		req.setService("control");
		// Set field 'service' to route the control command
		req.setRequest(new ServiceControlRequest().setService("example-service-1").setCmd("allCommands").toJson());

		client.send(req.toJson());
		String response = client.receiveStr();
		System.out.println("Recieved: " + response);

		Gson gson = new GsonBuilder().create();
		ClientResponse resp = gson.fromJson(response, ClientResponse.class);
		assertNotNull(resp);
		assertTrue(resp.isSuccessful());
		System.out
				.println("Response: " + gson.fromJson(resp.getResponse(), ServiceControlResponse.class).getResponse());

		client.close();
	}

	public void test_ControlThroughMonitor_getStatusOfExampleService()
	{
		ZmqDealer client = new ZmqDealer("monitor-client".getBytes(), "localhost", 10001, 1000);

		ClientRequest req = new ClientRequest();
		req.setService("control");
		// As a counter is added in the handler to record number of requests
		// received, it could be queried through monitor. getAllCounters is a
		// method in ServiceControlHandler.
		req.setRequest(new ServiceControlRequest().setService("example-service-1").setCmd("getAllCounters").toJson());

		client.send(req.toJson());
		String response = client.receiveStr();
		System.out.println("Recieved: " + response);

		Gson gson = new GsonBuilder().create();
		ClientResponse resp = gson.fromJson(response, ClientResponse.class);
		assertNotNull(resp);
		assertTrue(resp.isSuccessful());
		System.out
				.println("Response: " + gson.fromJson(resp.getResponse(), ServiceControlResponse.class).getResponse());

		client.close();
	}

	public void test_ControlThroughMonitor_LookupDataFromCache()
	{
		ZmqDealer client = new ZmqDealer("monitor-client".getBytes(), "localhost", 10001, 1000);

		ClientRequest req = new ClientRequest();
		req.setService("control");
		// lookupDataFromCache is in ExampleServiceControlHandler. It finds data
		// in cache by time.
		req.setRequest(new ServiceControlRequest().setService("example-service-1").setCmd("lookupDataFromCache")
				.setKey(String.valueOf(System.currentTimeMillis() / 1000 - 1)).toJson());

		client.send(req.toJson());
		String response = client.receiveStr();
		System.out.println("Recieved: " + response);

		Gson gson = new GsonBuilder().create();
		ClientResponse resp = gson.fromJson(response, ClientResponse.class);
		assertNotNull(resp);
		assertTrue(resp.isSuccessful());
		System.out
				.println("Response: " + gson.fromJson(resp.getResponse(), ServiceControlResponse.class).getResponse());

		client.close();
	}
}
