/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import static org.clever.Common.Communicator.Agent.logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.HostManager.CloudMonitor.CloudMonitorAgent;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.clever.Common.Initiator.Initiator;

/**
 *
 * @author
 */
public class Monitoring {
    
    static Initiator in;


    public Monitoring()
    {
        try
        {
       
          Properties prop = new Properties();
          InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
          prop.load(in);
          PropertyConfigurator.configure(prop);
          logger = Logger.getLogger( "Probe Monitor" );
          System.out.println("88888888888");
          
        }
        catch (IOException e) 
        {
            logger.error("Missing logger.properties");
        }
        
        
    }
    
    
    
    
    public static void main(String[] args) throws CleverException{

        
        
        Monitoring monitor = new Monitoring();
        
     /*
        Logger logger = null;
        CloudMonitorAgent mp;
        
        
    
        mp = new CloudMonitorAgent();
        mp.setAgentName("CloudMonitorAgent");
        
        
        */
      
        
        
        
        
        in = Initiator.getInstance(); //creo un oggetto Initiator
        in.start(); //faccio partire l'initiator  
        
        while ( true )
        {
            try
            {
                Thread.sleep( 1000000 );
            }
             catch ( InterruptedException ex )
            {
                System.exit( 1 );
            }
         }
        
        
        
        
        
        
        
        
        

        /*
        try {

            
            mp.initialization();
            
            System.out.println("88888888888"+((CloudMonitorPlugin)mp.getPlugin()).getTotalUsedMemory());
            
        
        } catch (CleverException ex) {
            
            logger.error("Error: " + ex);
        }
                */
                       


    }
}
