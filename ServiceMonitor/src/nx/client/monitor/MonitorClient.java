package nx.client.monitor;

import java.util.Scanner;
import java.util.UUID;

import nx.server.zmq.ClientRequest;
import nx.server.zmq.ClientResponse;
import nx.service.wrapper.ServiceControlRequest;
import nx.service.wrapper.ServiceControlResponse;
import nx.zmq.ZmqDealer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MonitorClient
{
	private static final int MONITOR_PORT = 10001;
	private Gson gson;
	private String clientName;

	public void init()
	{
		gson = new GsonBuilder().create();
		clientName = "controller_" + UUID.randomUUID().toString();
	}

	// Monitor mode

	public void controlThroughMonitor()
	{
		Scanner in = new Scanner(System.in);

		System.out.println("> What is the monitor host?");
		String host = in.nextLine();

		ZmqDealer client = new ZmqDealer(clientName.getBytes(), host, MONITOR_PORT, 1000);
		System.out.println("> Controller starts up as: " + clientName);

		while (!Thread.currentThread().isInterrupted())
		{
			ServiceControlRequest request = new ServiceControlRequest();
			System.out.println("> Please specify service (type 'quit' to exit, or 'all' for registered services):");
			String service = in.nextLine();
			if ("quit".equalsIgnoreCase(service))
			{
				break;
			}
			else if ("all".equalsIgnoreCase(service))
			{
				request.setCmd("all");
				ServiceControlResponse resp = sendRequestToMonitor(client, request);
				printResponse(resp);
			}
			else
			{
				request.setService(service);
				System.out.println("> Please specify command (or 'help' to get available commands on the service):");
				String cmd = in.nextLine();
				request.setCmd(cmd);
				ServiceControlResponse resp = sendRequestToMonitor(client, request);
				printResponse(resp);
			}
		}
		client.close();
		in.close();
	}

	// Service mode

	public void controlThroughService()
	{
		Scanner in = new Scanner(System.in);

		System.out.println("> What is the service URI (e.g. tcp://localhost:9000)?");
		String uri = in.nextLine();

		ZmqDealer client = new ZmqDealer(clientName.getBytes(), uri, 1000);
		System.out.println("Controller starts up as: " + clientName);
		System.out.println("> What is the service name?");
		String service = in.nextLine();

		while (!Thread.currentThread().isInterrupted())
		{
			ServiceControlRequest request = new ServiceControlRequest();
			request.setService(service);
			System.out
					.println("> Please specify command (type 'quit' to exit, or 'help' to get available commands on the service):");
			String cmd = in.nextLine();
			if ("quit".equalsIgnoreCase(cmd))
			{
				break;
			}
			else
			{
				request.setCmd(cmd);

				System.out.println("> Put Key if there is one:");
				String key = in.nextLine();
				if (key != null && !key.isEmpty())
				{
					request.setKey(key);
				}
				System.out.println("> Put Value if there is one:");
				String value = in.nextLine();
				if (value != null && !value.isEmpty())
				{
					request.setValue(value);
				}

				client.send(gson.toJson(request).getBytes());
				String response = client.receiveStr();
				ServiceControlResponse resp = new GsonBuilder().create().fromJson(response,
						ServiceControlResponse.class);
				printResponse(resp);
			}
		}
		client.close();
		in.close();
	}

	// Helper

	protected void printResponse(ServiceControlResponse resp)
	{
		if(resp != null)
		{
			if (!resp.isSuccessful())
			{
				System.out.println("Error: " + resp.getError());
			}
			else
			{
				System.out.println(resp.getResponse());
			}
		}
	}

	protected ServiceControlResponse sendRequestToMonitor(ZmqDealer client, ServiceControlRequest request)
	{
		ClientRequest serviceReq = new ClientRequest();
		serviceReq.setService("control");
		serviceReq.setRequest(request.toJson());

		client.send(gson.toJson(serviceReq).getBytes());
		String response = client.receiveStr();
		ClientResponse ret = gson.fromJson(response, ClientResponse.class);
		if(ret == null)
		{
			System.out.println("Error: timeout!");
			return null;
		}
		if(!ret.isSuccessful())
		{
			System.out.println("Error: " + ret.getError());
			return null;
		}
		ServiceControlResponse resp = gson.fromJson(ret.getResponse(), ServiceControlResponse.class);
		return resp;
	}

	// ============ Main ============

	public static void main(String[] args) throws Exception
	{
		if (args == null || args.length != 1)
		{
			throw new Exception("Please specify mode as the first parameter: 'monitor' or 'service'");
		}

		MonitorClient controller = new MonitorClient();
		controller.init();

		String mode = args[0];
		if ("monitor".equalsIgnoreCase(mode))
		{
			controller.controlThroughMonitor();
		}
		else if ("service".equalsIgnoreCase(mode))
		{
			controller.controlThroughService();
		}
		else
		{
			throw new Exception("Invalid mode [" + mode + "]. Valid ones are: 'monitor' or 'service'");
		}
	}
}
