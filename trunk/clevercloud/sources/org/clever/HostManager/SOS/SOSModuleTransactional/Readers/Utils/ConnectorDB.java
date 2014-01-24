/*
 *  Copyright (c) 2013 Universita' degli studi di Messina
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Utils;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;


/**
 *
 * @author Giuseppe Tricomi
 */

public class ConnectorDB {

    HashMap<String,Integer> resultsetMap;
    ArrayList<ResultSet> arlistResultMap;
    Logger logger=Logger.getLogger("connectorDB");
    private String hostname;
    private String dbname;
    private String user;
    private String pass;
    private Connection conn=null;
    private String timestampCol="";
    private String sBoardCol="";
    
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    private String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
    
/////////////////////CONSTRUCTOR//////////////////////////////////////
    /**
     * Standard constructor. When this implementation is used, is necessary 
     * set the connection parameter after invocation of constructor.
     */
    public ConnectorDB(){
        resultsetMap= new HashMap<String,Integer>();
        arlistResultMap= new ArrayList();
    }
    /**
     * Constructor with parameter connection. This implementation take 
     * connection parameter when is istantiated.
     * @param host String
     * @param db String
     * @param user String
     * @param pass String
     */
    public ConnectorDB(String host, String db, String user, String pass,String timestampCol,String sBoardCol){
        resultsetMap= new HashMap<String,Integer>();
        arlistResultMap= new ArrayList();
        this.hostname=host;
        this.dbname=db;
        this.user=user;
        this.pass=pass;
        this.timestampCol=timestampCol;
        this.sBoardCol=sBoardCol;
     }
/////////////////////FUNCTION///////////////////////////////////////////////////
    /**
     * This function is used to connect with DB.
     * @return boolean. False if an exception is generated.
     */
    private boolean makeConnection(){
        
        try{
            if(this.conn==null)
            {
                //logger.debug("jdbc:mysql://"+this.getHostname()+"/"+this.getDbname()+"?user="+this.getUser()+"&password="+this.getPass());
                this.conn = (Connection) DriverManager.getConnection("jdbc:mysql://"+this.getHostname()
                        +"/"+this.getDbname()+"?user="+this.getUser()+"&password="+this.getPass());//simone-S1mone
                return true;
            }
            else if(this.conn.isClosed())
            {
                //logger.debug("jdbc:mysql://"+this.getHostname()+"/"+this.getDbname()+"?user="+this.getUser()+"&password="+this.getPass());
                this.conn = (Connection) DriverManager.getConnection("jdbc:mysql://"+this.getHostname()
                        +"/"+this.getDbname()+"?user="+this.getUser()+"&password="+this.getPass());//simone-S1mone
                return true;
            }
            else
                return true;
        } 
        catch(java.sql.SQLException sqle){
            logger.error("Error occurred in connection to db:"+this.hostname+this.dbname
                    +". \nThis have generated an error code:'"+sqle.getErrorCode()+"' and an SQL state:"+sqle.getSQLState()
                    +". \nThe localized message is:"+sqle.getLocalizedMessage());
        }
        catch(Exception e){
            logger.error("Error occurred in connection to db:"+this.hostname+this.dbname
                    +". \nThe localized message is:"+e.getLocalizedMessage());
        }
        return false;
    }
    /**
     * This function is used to close connection with DB.
     */
    private void closeConnection(){
        try{
            if(!this.conn.isClosed())
                this.conn.close();
        }
        catch(java.sql.SQLException sqle){
            logger.error("Error occurred when the function close for the connection to db:"+this.hostname+this.dbname+" was invoked"
                    +". \nThis have generated an error code:'"+sqle.getErrorCode()+"' and an SQL state:"+sqle.getSQLState()
                    +". \nThe localized message is:"+sqle.getLocalizedMessage());
        }
        catch(Exception e){
            logger.error("Error occurred when the function close for the connection to db:"+this.hostname+this.dbname+" was invoked"
                    +". \nThe localized message is:"+e.getLocalizedMessage());
        }
    }
    
   /**
     * This function is used to get all the entries from the database with timestamp
     * more recent than the one that is passed in the variable timestamp.
     * After acquisition the connection will be close.
     * 
     * @param timestamp String:timestamp relative at last sensor acquisition
     * @param tabella String:table name where is stored the information that must be retrieved.
     * @param idsensorBoard String: id of sensor board or sensor network  
     * @return 
     */
    private ResultSet getRecordset (String timestamp, String tabella, String idsensorBoard)
    {
        ResultSet rs=null;
        try
        {
            if(this.makeConnection())
            {
                if((timestamp==null) && (idsensorBoard==null) ){
                    Statement stmt =  (Statement) this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    String query="select * from "+this.dbname+"." + tabella;
                    //logger.debug(query);
                    stmt.executeQuery(query);
                    rs=stmt.getResultSet ();
                    //this.closeConnection();
                    //logger.debug(query+"  "+rs==null?"null":"notnull");
                    return rs;
                }
                else if((timestamp==null) && !(idsensorBoard==null) ){
                    Statement stmt =  (Statement) this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    String query="select * from " +this.dbname+"."+ tabella+ " where `"+this.sBoardCol+"` = "
                        + idsensorBoard;
                    //logger.debug("select * from " +this.dbname+"."+ tabella+ " where `"+this.sBoardCol+"` = "+ idsensorBoard);
                    stmt.executeQuery(query);
                    rs=stmt.getResultSet ();
                    //this.closeConnection();
                    return rs;                    
                }
                else if(!(timestamp==null) && (idsensorBoard==null) ){
                    Statement stmt =  (Statement) this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    String query="select * from " +this.dbname+"."+ tabella+ " where `"+this.timestampCol+"` > '"
                        + timestamp+"'";
                    //logger.debug("select * from " +this.dbname+"."+ tabella+ " where `"+this.timestampCol+"` > '"+ timestamp+"'");
                    stmt.executeQuery(query);
                    rs=stmt.getResultSet ();
                    //this.closeConnection();
                    return rs;                    
                }
                else
                {
                    Statement stmt =  (Statement) this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    String query="select * from " +this.dbname+"."+ tabella + " where `"+this.sBoardCol+"` = "
                        + idsensorBoard+ " and `"+this.timestampCol+"` > '" + timestamp +"' order by `"+this.timestampCol+"` asc ";
                    //logger.debug("select * from " +this.dbname+"."+ tabella + " where `"+this.sBoardCol+"` = "
                    //    + idsensorBoard+ " and `"+this.timestampCol+"` > '" + timestamp +"' order by `"+this.timestampCol+"` asc ");
                    stmt.executeQuery(query);
                    rs=stmt.getResultSet ();
                    //this.closeConnection();
                    //logger.debug("select * from " +this.dbname+"."+ tabella + " where `"+this.sBoardCol+"` = "
                    //    + idsensorBoard+ " and `"+this.timestampCol+"` > '" + timestamp +"' order by `"+this.timestampCol+"` asc ");
                    
                    return rs;
                }
            } 
            else
            {
                logger.error("problem is occurred when try to establish a connection to db");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            this.closeConnection();
        }
        return null;
    }
    
    
    /**
     * This function is used to put in an HashMap the resultset taken from db.
     * 
     * @param timestamp String
     * @param tabella String : table name where the information are stored and is used as key in hashmap
     * @param idsensorBoard String 
     */
    public void createRSMap(String timestamp, String tabella, String idsensorBoard){
        Integer index=null;
        //logger.debug("getRecordset("+timestamp+","+ tabella+","+ idsensorBoard);
        this.arlistResultMap.add(this.getRecordset(timestamp, tabella, idsensorBoard));
        if(!this.resultsetMap.containsKey(tabella))
        {
            index=this.arlistResultMap.size();
            this.resultsetMap.put(tabella, index);
            //logger.debug("put @"+tabella+" in "+index);
        }
        
    }
    
    /**
     * This function is used to remove a resultset from Hash Map.
     * @param tabella String :name of the table that will be deleted 
     */
    public void remove_EL_RSMap(String tabella){
        //this.printrsMap();
        if(this.resultsetMap.containsKey(tabella))
        {
            int i=this.resultsetMap.get(tabella);
            this.arlistResultMap.remove(i-1);
        }
    }
    
    /**
     * This function is used to retrieve the text rapresentation of the misure.
     * @param misura String : Coloumn label of table whose value we want.
     * @param idmisure String : Index value of row.
     * @param tab String : table name.
     * @return 
     */
    public String getMisure (String misura, String idmisure,String tab,String idcolname)
    {
        try
        {
            
        ResultSet rs=this.arlistResultMap.get(this.resultsetMap.get(tab)-1);
        
            rs.first();
            String v = "";
            do
            {
                
                if(rs.getString(idcolname).equals(idmisure)){
                    v=rs.getString(misura);
                    break;
                }            
            }
            while (rs.next());
            
            return v;
        }
        
        catch (Exception ex)
        {
            logger.error(ex.getMessage());
            
            ex.printStackTrace();
        }
        return null;
    }
   
    /**
     * This function is used to take all sensor board or all sensor network present on Database.
     * @param col String : name of the column whose values set we want.
     * @param tab String : table name
     * @return 
     */
    public Vector<String> getSensorBoard(String col,String tab)
    {
         
         try
        {
            if(this.makeConnection())
            {
                Statement stmt =  (Statement) conn.createStatement();
                String query="select `"+col+"` as id from "+tab;
                stmt.executeQuery(query);
                Vector<String> v = new Vector<String>();
                String e;
                ResultSet rs =  stmt.getResultSet ();
                while (rs.next())
                {
                  e=rs.getString("id");
                  v.add(e);

                 } 
                this.closeConnection();
                return v;
            }
            else
            {
                logger.error("problem is occurred when try to establish a connection to db");
            }
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage());
            ex.printStackTrace();
            this.closeConnection();
        }
         
         return null;
    }
    /**
     * This function returns a Vector<String>, the element represents the text representation of
     * "misura" field of the entries. 
     * @param misura String : Coloumn label of table.
     * @param tab String : There is the table where you want make the research.
     * @return 
     */
    public Vector<String> getMisure (String misura, String tab)
    {
        try
        {
            ResultSet rs=this.arlistResultMap.get(this.resultsetMap.get(tab)-1);
            //logger.debug("valore della mappa"+(this.resultsetMap.get(tab)-1));
            //this.printrsMap();
            if(rs==null)
                logger.error("null");
            else
            {
                rs.first();
                Vector<String> v = new Vector<String>();
                String e;
                
                if(rs.wasNull()){
                    logger.debug("resultset is empty");
                }
                else
                {    
                    do
                    {
                      e=rs.getString(misura);
                      v.add(e);
                    }
                    while (rs.next());
                    //logger.debug("lunghezza array: "+v.size());
                    return v;
                }
            }
        }
        catch (Exception ex)
        {        
            logger.error(ex.getMessage(),ex);
            logger.error(ex.getStackTrace()[0]);
        }
        
        return null;
    }
    
    public void printrsMap(){
        try{
            Set s=this.resultsetMap.entrySet();
            Iterator i=s.iterator();
            while(i.hasNext())
            {
                Entry e =(Entry)i.next();
                logger.debug("Entry element "+e.getKey()+"| |"+e.getValue());
            }
        }catch(Exception e){
            logger.error("errore nella stampa della mappa");
        }
    }
}