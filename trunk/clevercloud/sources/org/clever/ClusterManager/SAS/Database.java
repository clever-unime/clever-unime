
package org.clever.ClusterManager.SAS;


import org.clever.ClusterManager.SAS.ParameterDbContainer;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public final class Database {
    public static Logger logger = null;
    private Statement ST;
    private Connection con;
    public static Database testDatabase;
    public static ParameterDbContainer parameterContainer;

    private Database(String ip, String driver, String db, String username, String password) {
        try {
            logger=logger.getLogger("Dbmysql");
            logger.debug("driver: "+driver);
            Class.forName(driver);
            logger.debug("jdbc:mysql://localhost/" + db+"?user=root&password=mandrake");
        
            this.con = (Connection)DriverManager.getConnection("jdbc:mysql://localhost/" + db+"?user=root&password=mandrake");
        } catch (ClassNotFoundException cnfe) {
            logger.error("openDB: Attenzione classe non trovata, " + cnfe.getMessage());
            System.out.println("openDB: Attenzione classe non trovata, " + cnfe.getMessage());

        } catch (SQLException sqle) {
            logger.error("openDB: Errore sql, " + sqle.getMessage());
                
            System.out.println("openDB: Errore sql, " + sqle.getMessage());

        }

    }
    
  public static Database getNewInstance(){
      ParameterDbContainer parameterContainer=ParameterDbContainer.getInstance();
      return new Database(parameterContainer.getDbServer(),parameterContainer.getDbDriver(),
                                                          parameterContainer.getDbName(),
                                                          parameterContainer.getDbUsername(),parameterContainer.getDbPassword());
  }
  
  
    public static Database getNewInstance(ParameterDbContainer parameterContainer){
     
      return new Database(parameterContainer.getDbServer(),parameterContainer.getDbDriver(),
                                                          parameterContainer.getDbName(),
                                                          parameterContainer.getDbUsername(),parameterContainer.getDbPassword());
  }
  

    /**
     * @return the con
     */
    public Connection getCon() {
        return con;
    }
    
  private static class Wrapper { 
    private final static ParameterDbContainer parameterContainer=ParameterDbContainer.getInstance();
    private final static Database database = new Database(parameterContainer.getDbServer(),parameterContainer.getDbDriver(),
                                                          parameterContainer.getDbName(),
                                                          parameterContainer.getDbUsername(),parameterContainer.getDbPassword());
                                  
  }
  
    public static Database getTestInstance(String ip, String driver, String db, String username, String password){
        if(testDatabase==null){
            //parameterContainer=ParameterContainer.getInstance();
            testDatabase=new Database(ip,driver,db,
                                  username,password);
        }
        return testDatabase;     
    }
  
    public static Database getInstance(){
        
        return Wrapper.database;
            
    }
    
    
    public ResultSet exQuery(String query) {
        ResultSet rs=null;
        try {
            this.ST =getCon().createStatement();
            rs= this.ST.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("exQuery: Errore query" + e.getMessage());
        }
        return rs;
    }

    public void exUpdate(String query) {
        try {
            this.ST =getCon().createStatement();
            this.ST.executeUpdate(query);

        } catch (SQLException e) {
            System.out.println("exUpdate: Errore query" + e.getMessage());
        }
    }

    public int exInsert(String query) {
        ResultSet rs=null;
        int autoIncKeyFromApi = -1;
        try {
            this.ST =getCon().createStatement();
            this.ST.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
            
            rs = this.ST.getGeneratedKeys();
            if (rs.next()) 
            autoIncKeyFromApi = rs.getInt(1);

            return autoIncKeyFromApi;
            
        } catch (SQLException e) {
           logger.error("exUpdate: Errore query" + e.getMessage());
            System.out.println("exUpdate: Errore query" + e.getMessage());
            return autoIncKeyFromApi;
        }
        
    }
    
    
  
}