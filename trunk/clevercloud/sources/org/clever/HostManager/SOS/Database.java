/*
 * Copyright [2014] [Università di Messina]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
/*
 * The MIT License
 *
 * Copyright 2012 alessiodipietro.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.clever.HostManager.SOS;

/**
 *
 * @author alessiodipietro
 */
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
public final class Database {

    private Statement ST;
    private Connection con;
    public static Database testDatabase;
    public static ParameterContainer parameterContainer;
    private Logger logger=Logger.getLogger("SOSDatabase");
    private Database(String ip, String driver, String db, String username, String password) {
        
        try {
            //Class.forName(driver);
            String connectionString="jdbc:mysql://"+ip+"/" + db+"?user="+username+"&password="+password;
            this.con = (Connection)DriverManager.getConnection(connectionString);
            //logger.debug("DB& "+connectionString);
        //} catch (ClassNotFoundException cnfe) {
            //System.out.println("openDB: Attenzione classe non trovata, " + cnfe.getMessage());

        } catch (SQLException sqle) {
            logger.error("openDB: Errore sql, " + sqle.getMessage(),sqle);

        }

    }
    
  public static Database getNewInstance(){
      ParameterContainer parameterContainer=ParameterContainer.getInstance();
      Logger logger= Logger.getLogger("verifica");
      //logger.debug("db da istanziare: "+parameterContainer.getDbServer()+parameterContainer.getDbDriver()+parameterContainer.getDbName()+parameterContainer.getDbUsername()+parameterContainer.getDbPassword());
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
    private final static ParameterContainer parameterContainer=ParameterContainer.getInstance();
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
           logger.error("exQuery: "+query+"\n Errore query" + e.getMessage(),e);
        }
        return rs;
    }

    public void exUpdate(String query) {
        try {
            this.ST =getCon().createStatement();
            this.ST.executeUpdate(query);
        } catch (SQLException e) {
           logger.error("exUpdate: "+query+" Errore query" + e.getMessage(),e);
        }
    }

  
}