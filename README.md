# Infra
Framework I use to quickly build up scalable services in Lego-style. 

It helps me to write a simple distributed service in 5 mins, so I can focus on business logic. Or to experiment in different distribution scenarios.

## ZMQ Server
It's a lightweight, easily embedded server that communicates through ZeroMQ. You can add handlers to serve different kinds of requests, and scale them. You can add workers either on the localhost or remote hosts. Server dispatches workload evenly amongst all available workers.
### Single server
An example of how to create a simple server that runs on one host:

    public static void main(String[] args) throws Exception {
      	ZmqServer server = new ZmqServer(clientPort, workerPort);
    
      	server.addHandler(new ExampleMsgHandler1(), 2);	// handler1 serves as 'service1', scales by 2.
      	server.addHandler(new ExampleMsgHandler2(), 3);	// hanlder2 serves as 'service2', scales by 3.
    
      	server.start();
      }

And to implement a handler, you focus only on business logic:

    class ExampleMsgHandler1 implements IHandler {
    	@Override
    	public String getServiceName(){
    		return "service1";
    	}
    	@Override
    	public String process(String request) throws Exception{
    		return "handler1: " + request;
    	}
    }

As you add the handler to the server, it gets wrapped to a proper worker and interacts with the server.

### Distributed
You can Also create workers on remote server and distribute the system:

Firstly start the server (and maybe a couple workers) on one host:

    public void startServer() throws Exception {
    	ZmqServer server = new ZmqServer(clientPort, workerPort);
    	server.start();
    }

Then create more workers on remote hosts:

    public void startWorker(int index) {
    	ZmqWorker worker = new ZmqWorker(serverHost, workerPort, new ExampleBasicHandler(), index);
    	new Thread(worker).start();
    }

## Bucketing Server
Bucketing server is built on top of the Zmq Server. It buckets requests that have same key (specified in the request) to be processed by the same worker. Each worker equips buffer, so will always be 'free' to the server. 

Bucketing server could be used to process write/update requests to avoid race conditions.

The way to create servers and workers is same as the Zmq Server. Only that you need to specify bucket key in the request.

## Service Monitor
Service monitor remotely monitors and controls services that register with it. It itself could be remotely controlled by a monitor client, to query on all the registered services, or send control commands to specified services. 

You can start the monitor server from MonitorServer. There's a consol client MonitorClient. Any service that wants to interact with monitor needs the nx.service.wrapper package, which is included in Service Monitor.

A service can start a controller if it wants to be monitored. An example of how to use it is in ExampleService.

## Example Service
This is an example of a simple service that generates random data and stores them periodically to Redis. Client can query data by key (timestamp when data was generated). The service register itself to Service Monitor, and could be controled remotely.
