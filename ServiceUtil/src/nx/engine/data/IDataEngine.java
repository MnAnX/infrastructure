package nx.engine.data;


public interface IDataEngine 
{	
	public IDataEngine newSession() throws Exception;
	public void close() throws Exception;
}
