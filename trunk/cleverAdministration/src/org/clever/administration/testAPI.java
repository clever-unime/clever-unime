/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.administration.api.Configuration;
import org.clever.administration.api.SessionFactory;
import org.clever.administration.exceptions.CleverClientException;
import org.clever.administration.test.TestApi;
import org.clever.administration.test.Cli2;
/**
 *
 * @author maurizio
 */
public class testAPI {
    public static void main( String[] args ) 
  {
     if(!args[args.length-1].equals("commandline"))
     {    
            int numt = Integer.parseInt(args[0]);
            Thread[] threads = new Thread[numt];

            Configuration conf = new Configuration();

              try {
                  SessionFactory sf = conf.buildSessionFactory();
                  int i=0;
                  for (i=0;i<numt;i++)
                  {
                      threads[i] = new TestApi(sf,args[1]);
                      threads[i].start();
                  }
                  for(i = 0; i < threads.length; i++)
                      threads[i].join();


                  sf.closeAllSessions();



                  System.out.println("Chiusura: ");

              } catch (CleverClientException ex) {
                  System.err.println("errore nella configurazione: controllare log");
                  ex.printStackTrace();
              } catch (InterruptedException ex) {
                  Logger.getLogger(testAPI.class.getName()).log(Level.SEVERE, null, ex);
              }
     }
     else
     {
         Cli2 cli=new Cli2(null,args[1]);
         cli.showShell();
     }
      
      
      
     
      int numt = Integer.parseInt(args[0]);
      Thread[] threads = new Thread[numt];
      
      Configuration conf = new Configuration();
       
        try {
            SessionFactory sf = conf.buildSessionFactory();
            int i=0;
            for (i=0;i<numt;i++)
            {
                threads[i] = new TestApi(sf,args[1]);
                threads[i].start();
            }
            for(i = 0; i < threads.length; i++)
                threads[i].join();
            
            
            sf.closeAllSessions();
            
            
            
            System.out.println("Provo ad uscire: ");
            
        } catch (CleverClientException ex) {
            System.err.println("errore nella configurazione: controllare log");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.getLogger(testAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
      
  }
}
