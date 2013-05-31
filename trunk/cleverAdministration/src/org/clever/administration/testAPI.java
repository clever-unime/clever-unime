/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration;

import org.clever.administration.common.Configuration;
import org.clever.administration.common.SessionFactory;
import org.clever.administration.exceptions.CleverClientException;
import org.clever.administration.test.TestApi;

/**
 *
 * @author maurizio
 */
public class testAPI {
    public static void main( String[] args ) 
  {
      
      
      
      
      
     
      int numt = Integer.parseInt(args[0]);
      
      
      Configuration conf = new Configuration();
       
        try {
            SessionFactory sf = conf.buildSessionFactory();
            for (int i=0;i<numt;i++)
            {
                new TestApi(sf,"marvell").start();
            }
            
            
        } catch (CleverClientException ex) {
            System.err.println("errore nella configurazione: controllare log");
            ex.printStackTrace();
        }
        
      
  }
}
