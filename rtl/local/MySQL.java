package rtl.local;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class MySQL{
	private Connection connection;
	
	public MySQL(Connection connection){
		this.connection = connection;
	}
	
	public Object query(String query){
		try{
			Statement statment = this.connection.createStatement();
			if(!statment.execute(query))
				return false;
			
			if(query.indexOf("SELECT ") == 0){
				return new MySQLResult(statment);
			}
		
			return true;
		}catch(SQLException e){
			return false;
		}
	}
}
