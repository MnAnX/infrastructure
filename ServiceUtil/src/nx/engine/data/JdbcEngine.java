package nx.engine.data;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class JdbcEngine implements IDataEngine
{
	private static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
	protected DriverManagerDataSource dataSource;
	protected Connection conn;

	public JdbcEngine(String host, String port, String service, String user, String pw) throws Exception
	{
		this.dataSource = getDataSource(host, port, service, user, pw);
		try
		{
			this.conn = dataSource.getConnection();
		}
		catch (SQLException e)
		{
			throw new Exception("Unable to connect to database. Reason: " + e.getMessage());
		}
	}

	public JdbcEngine(DriverManagerDataSource dataSource) throws Exception
	{
		try
		{
			this.dataSource = dataSource;
			this.conn = dataSource.getConnection();
		}
		catch (SQLException e)
		{
			throw new Exception("Unable to connect to database. Reason: " + e.getMessage());
		}
	}

	public IDataEngine newSession() throws Exception
	{
		return new JdbcEngine(this.dataSource);
	}

	public DriverManagerDataSource getDataSource(String host, String port, String service, String user, String pw) throws Exception
	{
		// Override this method if your datasource is different
		try
		{
			DriverManagerDataSource dataSource = new DriverManagerDataSource();

			dataSource.setDriverClassName(JDBC_DRIVER);
			String url = String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, service);
			dataSource.setUrl(url);
			dataSource.setUsername(user);
			dataSource.setPassword(pw);

			return dataSource;
		}
		catch (Exception e)
		{
			throw new Exception("Unable to get data source. Reason: " + e.getMessage());
		}
	}

	public void close() throws Exception
	{
		if (conn != null)
		{
			conn.close();
		}
	}

	public Connection getConnection()
	{
		return conn;
	}
}
