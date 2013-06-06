/*
 *  The MIT License
 * 
 *  Copyright 2013 Tricomi Giuseppe.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.clever.Common.SqliteDB;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import java.sql.ResultSet;


 
/*SQLITE MANAGEMENT*/

public class SQLite{

    private Connection SQLiteConnection;
    private String SQLiteDB;
    private Logger logger;
    private Statement statement;
    /**The Default Costructor.
     * If is not present the directory and the db is not created, make it
     * 
     */
    public SQLite(){
        this.logger=Logger.getLogger("SQLiteDB");
        this.SQLiteDB=System.getProperty("user.dir")+"/WorkingData/StorageVM/";
        File workdir=new File(this.SQLiteDB);
        if(!workdir.exists())
        {
            workdir.mkdirs();
        }
        File db=new File(this.SQLiteDB+"VMStorageMapping.db");
        if(!db.exists())
        {
            if(this.openCon())
            {
                try{
                    //non appena verrà stabilito con precisione cosa fanno queste mappe si potrà creare un db decente 
                    statement = this.SQLiteConnection.createStatement();
                    statement.setQueryTimeout(30);
                    statement.executeUpdate("create table map (filename string,response string, size string,date string,lock string)");
                    this.SQLiteConnection.commit();
                    //statement.executeUpdate("create table paths (id String, path string)");
                    //statement.executeUpdate("create table sharedpaths (hostname string, path string)");
                    this.statement.close();
                    this.SQLiteConnection.close();
                }
                catch(SQLException e){
                    logger.error("Exception with init of sqlite db failed to create work table:"+e.getMessage());
                    try{
                         if(!this.SQLiteConnection.isClosed()){
                            this.statement.close();
                            this.SQLiteConnection.close();
                        }
                    }catch(SQLException sqle){
                        logger.error("The operation of closure of sqlitedb is failed:"+sqle.getSQLState()+" "+sqle.getMessage());
                    }
                }
                catch(Exception eg){
                    logger.error("Exception with init of sqlite db failed to create work table:"+eg.getMessage());
                }
            }
        }
    }
    
/**
 * This function open the connection whit sqlitedb
 * 
 */
    public boolean openCon(){
        boolean res=true;
        try
        {
            Class.forName("org.sqlite.JDBC");
             // create a database connection
            this.SQLiteConnection = DriverManager.getConnection("jdbc:sqlite:"+"WorkingData/StorageVM/MappingDB.db");
            this.SQLiteConnection.setAutoCommit(false);
            this.statement=this.SQLiteConnection.createStatement();//(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            logger.error("??XX1Exception caused by connection at the Mapping db:"+e.getMessage());
            this.SQLiteConnection =null;
            res=false;
        }
        catch(ClassNotFoundException ce)
        {
            logger.error("??XX2Exception caused by connection at the Mapping db:"+ce.getMessage());
            this.SQLiteConnection =null;
            res=false;
        }
        catch(Exception ex){
            logger.error("??XX3Exception caused by connection at the Mapping db:"+ex.getMessage());
            res=false;
        }
        return res;   
    }
    /**
     * This function close the connection with db 
     * @return 
     */
    public boolean closeCon(){
        boolean res=true;
        try
        {
           if(this.SQLiteConnection != null){
               this.statement.close();
               this.SQLiteConnection.close();
           }
        }
        catch(SQLException e)
        {
           // connection close failed.
           logger.error("Exception caused by connection's closing at the Mappingdb:"+e.getMessage());
           res=false;
        }
        catch(Exception ex)
        {
           // connection close failed.
           logger.error("Exception caused by connection's closing at the Mappingdb:"+ex.getMessage());
           res=false;
        }
        return res;
    }
    /**
     * Insert in table "map" the values passed at this function how parameter.
     * @param fname
     * @param response
     * @param size
     * @param date
     * @param lock
     * @return 
     */
    public boolean insertElementAtMap(String fname,String response,String size,String date, String lock){
        boolean res=true;
        try{
            if(this.SQLiteConnection.isClosed())
                this.openCon();
            Statement statement = this.SQLiteConnection.createStatement();
            statement.executeUpdate("insert into map values('"+fname+"','"+response+"','"+size+"','"+date+"','"+lock+"')");
            this.SQLiteConnection.commit();
            this.closeCon();
        }
        catch(Exception e){
            logger.error("The operation <insertElementAtMap> has generate an Exception:"+e.getMessage()+"\n and can't insert on the map db the entry:"+fname+"|"+response+"|"+size+"|"+date+"|"+lock);
            return false;
        }
        finally{
            this.closeCon();
            return res;
        }
    }
    public boolean deleteElementInMap(String condition){
        boolean res=true;
        try{
            if(this.SQLiteConnection.isClosed())
                this.openCon();
            Statement statement = this.SQLiteConnection.createStatement();
            statement.executeUpdate("delete from map where "+condition);
            this.SQLiteConnection.commit();
            this.closeCon();
        }
        catch(Exception e){
            logger.error("The operation <deleteElementInMap> has generate an Exception:"+e.getMessage()+"\n and can't delete on the map db the entry that meets the condition :"+condition);
            return false;
        }
        finally{
             this.closeCon();
             return res;
        }
    }
    /**
     * This function allow to retrieve the response values from map table where 
     * filename is indicate by filename parameter
     * @param filename
     * @return 
     */
    public ResultSet retrieveElementsInMap(String filename){
        try{
            if(this.SQLiteConnection.isClosed()){
                this.openCon();
            }
            ResultSet res = statement.executeQuery("select * from map where filename='"+filename+"'");
            /*while(!res.isAfterLast()){
                logger.debug("set entry"+res.getString("response"));
                res.next();
            }
            res.beforeFirst();*/
            return res;
        }
        catch(SQLException sqle){
            logger.error("Exception generate from RetrieveElementInMap function:"+sqle.getMessage()+" "+sqle.getSQLState());
        }
        return null;
    }
    /**
     * This function executes update on map table on the entry field pointed by set in the entry identify by condition
     * @param condition
     * @param set
     * @return 
     */
    public boolean updateElementInMap(String condition,String set){
        boolean res=true;
        try{
            if(this.SQLiteConnection.isClosed())
                this.openCon();
            Statement statement = this.SQLiteConnection.createStatement();
            statement.executeUpdate("update map set "+set+" where "+condition);
            
        }
        catch(Exception e){
            logger.error("The operation <updateElementInMap> has generate an Exception:"+e.getMessage()+"\n and can't delete on the map db the entry that meets the condition :"+condition);
            return false;
        }
        finally{
             this.closeCon();
             return res;
        }
    }
}