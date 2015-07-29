package nx.server.bucketing;

import nx.server.zmq.ZmqClient;

public class BucketingServerClientTest
{
	public static void main(String[] args)
	{
		String clientId = "test_bucketing_client1";		
		ZmqClient client = new ZmqClient(clientId, "localhost", 15000, 1000);
		
		String service = "service1";
		String bucketKey = "key1";	// hash code of the key determines which bucket this request belongs to
		String request = "request1";		
		
		BucketingClientRequest req = new BucketingClientRequest();
		req.setService(service);
		req.setBucketKey(bucketKey);
		req.setRequest(request);		

		System.out.println("'" + bucketKey + "' hash code = " + bucketKey.hashCode());
		
		client.send(req);	
		String ret = client.receive();
		
		System.out.println(ret);
		
		client.close();
	}
}
