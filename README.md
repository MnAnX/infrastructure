# Infra
Infrastructure components I use to quickly build up a stable and scalable service.

## ZMQ Server
It's a lightweight, easy to embed to anything, server that communicates through ZeroMQ. You can add handlers to serve different kinds of requests, and scale them by just giving one parameter. You can add workers easily either on the localhost or remote hosts.
### Single server mode
An example of how to create a simple server that runs on one host:

    public static void main(String[] args) throws Exception {
      	ZmqServer server = new ZmqServer(clientPort, workerPort);
    
      	IHandler handler1 = new ExampleMsgHandler1();
      	IHandler handler2 = new ExampleMsgHandler2();
    
      	server.addHandler(handler1, 2);	// handler1 serves as 'service1', scales by 2.
      	server.addHandler(handler2, 3);	// hanlder2 serves as 'service2', scales by 3.
    
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

### Distributed mode
You can Also create workers on remote server and distribute the system:

Firstly start the server (and a couple workers) on one host:

    public void startServer() throws Exception {
    	ZmqServer server = new ZmqServer(clientPort, workerPort);
    	server.start();
    }

Then create more workers on remote hosts:

    public void startWorker(int index) {
    	ZmqWorker worker = new ZmqWorker(serverHost, workerPort, new ExampleBasicHandler(), index);
    	new Thread(worker).start();
    }

    public void startMultipleWorkers(int numWorkers) {
    	for (int i = 0; i < numWorkers; i++) {
    		startWorker(i);
    	}
    }

## Bucketing Server
Bucketing server is built on top of the Zmq Server. It buckets requests that have same key (specified in the request) to be processed by same thread. This could be used to process update requests to avoid race condition.

The way to create server and workers is same as the Zmq Server. Only that you need to specify bucket key in the request.

## Service Monitor
Service monitor remotely monitors and controls services that register with it. It itself could be remotely controled by monitor client, to query on all the registered services, and send control commands to specified service. 

A service can start a controller if it wants to be monitored.
