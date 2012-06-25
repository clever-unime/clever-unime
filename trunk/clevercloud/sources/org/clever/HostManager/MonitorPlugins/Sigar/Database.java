/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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

package org.clever.HostManager.MonitorPlugins.Sigar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
import java.sql.Statement;
//import java.util.List;
import java.util.Vector;


public class Database
{

  private String dbName;

  private String error;

  private Connection db;
  private boolean connected;
  private static Database database = null;
  private static PreparedStatement prep;
  private static String val;

public Database(String dbName)
  {
    this.dbName = dbName;
    connected = false;
    error = "";
  }
  /**
   * Return the instance of this class
   *
   */

   static public Database istance()throws Exception
  {
    if ( database == null )
    {

         database = new Database("MonitorPlugin.db");
    }
    if ( ! database.isConnected() )
    {
      database.connect();
    }

    return database;
  }

  /**
   * Connect to database server
   * @return
   */
  public boolean connect() throws Exception
  {
    connected = false;


      Class.forName("org.sqlite.JDBC");

      String connectionString = "";

         db = DriverManager.getConnection("jdbc:sqlite:MonitorPlugin.db");
         connected = true;
        System.out.println( "Database connected" );

    return connected;
  }


  public Connection conn() throws Exception
  {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:MonitorPlugin.db");

        return conn;
  }


  /**
   * Exectute one query
   * @param query
   */
  public Vector executeQuery( String query )
  {
    Vector v = null;
    String[] record;
    int columns = 0;
    try
    {
      Statement stmt = db.createStatement();
      ResultSet rs = stmt.executeQuery( query );
      v = new Vector();

      ResultSetMetaData rsmd = rs.getMetaData();
      columns = rsmd.getColumnCount();

      while ( rs.next() )
      {
        record = new String[columns];
        for ( int i = 0; i < columns; i++ )
        {
          record[i] = rs.getString( i + 1 );
        }
        v.add( ( String[] ) record.clone() );
      }
      rs.close();
      stmt.close();
    } catch ( Exception e )
    {
      e.printStackTrace();
      error = e.getMessage();
    }

    return v;
  }

  /**
   * Execute command
   * @param query
   */
  public boolean executeCommand( String query )
  {
    boolean result = false;
    try
    {
      Statement stmt = db.createStatement();
      result = stmt.execute( query );

    } catch ( Exception e )
    {
      e.printStackTrace();
      error = e.getMessage();
    }
    return result;
  }

  /**
   * Return whenever the client is connected to database server
   */
  public boolean isConnected()
  {
    return connected;
  }

  /**
   * Return the string for error
   * @return
   */
  public String getError()
  {
    return error;
  }

  /**
   * Close connection to server database
   */
  public void close() throws Exception
  {

      connected = false;
      db.close();

  }
}