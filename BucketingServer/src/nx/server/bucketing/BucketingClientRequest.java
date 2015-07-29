package nx.server.bucketing;

import nx.server.zmq.ClientRequest;

public class BucketingClientRequest extends ClientRequest
{
	String bucketKey; // bucketing will be based on Hash of this key

	public String getBucketKey()
	{
		return bucketKey;
	}

	public void setBucketKey(String bucketKey)
	{
		this.bucketKey = bucketKey;
	}

}
