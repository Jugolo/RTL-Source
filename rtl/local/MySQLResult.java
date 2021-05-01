package rtl.local;

import rtl.VariabelDatabase;
import rtl.TypeConveter;
import rtl.Reference;
import rtl.StructValue;
import rtl.Program;
import rtl.exception.RTLRuntimeException;
import rtl.Array;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class MySQLResult{
	private Statement statment;
	
	public MySQLResult(Statement statment){
		this.statment = statment;
	}
	
	public Object fetchHashmap(VariabelDatabase db, Program program) throws RTLRuntimeException{
		try{
			ResultSet set = this.statment.getResultSet();
			if(set == null || !set.next())
				return null;
			StructValue hashmap = newHashmap(db, program);
			ResultSetMetaData data = set.getMetaData();
			for(int i=0;i<data.getColumnCount();i++){
				hashmap_put(
					db,
					program,
					hashmap,
					data.getColumnName(i+1),
					set.getString(i+1)
				);
			}
		
			return hashmap;
		}catch(SQLException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}
	
	public Array fetch() throws RTLRuntimeException{
		try{
			ResultSet set = this.statment.getResultSet();
			if(set == null || !set.next())
				return null;
			Array array = new Array();
			ResultSetMetaData data = set.getMetaData();
			for(int i=0;i<data.getColumnCount();i++){
				array.add(set.getString(i+1));
			}
			return array;
	    }catch(SQLException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}
	
	private void hashmap_put(VariabelDatabase db, Program program, StructValue hashmap, String key, String value) throws RTLRuntimeException{
		TypeConveter.toFunction(Reference.toValue(db.get("hashmap_put"))).call(program, new Object[]{
			hashmap,
			key,
			value
		});
	} 
	
	private StructValue newHashmap(VariabelDatabase db, Program program) throws RTLRuntimeException{
		return TypeConveter.toStructValue(TypeConveter.toFunction(Reference.toValue(db.get("hashmap"))).call(program, new Object[0]));
	}
}
