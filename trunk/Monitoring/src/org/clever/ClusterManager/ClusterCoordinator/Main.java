/*
 *  The MIT License
 * 
 *  Copyright 2011 sabayonuser.
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

package org.clever.ClusterManager.ClusterCoordinator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory; 

import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.clever.Common.Exceptions.CleverException;


/**Main class of Cluster Coordinator.
 * This class contains the main function of the CC, 
 * with which it can be run in a separate process
 *
 * @author sabayonuser
 */
public class Main 
{
    private Logger logger = null;
    ClusterCoordinator cc;   
    
    
    public Main()
    {
        try
        {
       
          Properties prop = new Properties();
          InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
          prop.load(in);
          PropertyConfigurator.configure(prop);
          logger = Logger.getLogger( "Main_ClusterCoordinator" );
          }
        
        catch (IOException e) 
      {
          logger.error("Missing logger.properties");
      }
     
        try{
        
            logger.info("\n Prima del lancio del costruttore cc!\n");
            cc = new ClusterCoordinator(); //Istantiate de Cluster Coordinator
            logger.info("\nDopo il lancio del costruttore CC, si sta x avviare la start()");
            cc.start(); //Start it!        
            logger.info("\ndopo la start()\n");
            
        }
        
        
        catch ( CleverException ex )
        {   
           logger.error( "Error launching ClusterCoordinator: " + ex.getMessage() );
            
            if ( ex.getInternalException() != null )
            {
                // Print the error and the stack
                logger.error( ex.getInternalException().getMessage() );
                 ex.getInternalException().printStackTrace();
                System.exit( 1 );
            }
        }   
    }



  public static void main( String[] args )
  {
      
      
      
      
    Logger logger = Logger.getLogger( "ClusterCoordinator" );
    new Main();
     // TODO put away the sleep and use the join thread mechanism  
    /*try
        {
       
          Properties prop = new Properties();
          InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
          prop.load(in);
          PropertyConfigurator.configure(prop);
          logger = Logger.getLogger( "Main_ClusterCoordinator" );
        }
      catch (IOException e) 
      {
          logger.error("Missing logger.properties");
      }*/
    //This infinite loop is necessary to prevent the DC terms
    while ( true )
    {       
     try
      {
        Thread.sleep( 1000000 ); //Inside, the main thread goes to sleep periodically          
      }
      catch ( InterruptedException ex )
      {
        logger.error( "Error during thread master sleep" );
        System.exit( 1 );
      }
    }    
  }
}