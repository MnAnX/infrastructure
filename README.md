# Infra
Infrastructure components I use to quickly build up a stable and scalable service.

## ZMQ Server
It's a lightweight, easy to embed to anything, server that communicates through ZeroMQ. You can add handlers to serve different kinds of requests, and scale them by just giving one parameter. You can add workers easily either on the localhost or remote hosts.
### Single server mode
An example of how to create a simple server that runs on one host:

    public static void main(String[] args) throws Exception {
  		int client_port = 15000;
  		int worker_port = 15001;
  		ZmqServer server = new ZmqServer(client_port, worker_port);
  
  		IHandler handler1 = new ExampleMsgHandler1();
  		IHandler handler2 = new ExampleMsgHandler2();
  
  		server.addHandler(handler1, 2);	// handler1 serves for service1. scale for 2 sessions
  		server.addHandler(handler2, 3);	// hanlder2 serves for service2. scale for 3 sessions
  
  		server.start();
  	}

And to implement a handler, you focus only on business logic:

    class ExampleMsgHandler1 implements IHandler {
    	@Override
    	public String getServiceName(){
    		return "service1";
    	}
    	@Override
    	public String process(String data) throws Exception{
    		return "handler1: " + data;
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
