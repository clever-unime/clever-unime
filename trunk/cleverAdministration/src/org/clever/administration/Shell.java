/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.ConsoleReader;
import jline.History;
import org.clever.administration.annotations.GetShellModule;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.api.Configuration;
import org.clever.administration.api.Session;
import org.clever.administration.api.SessionFactory;
import org.clever.administration.api.modules.AdministrationModule;
import org.clever.administration.exceptions.CleverClientException;

/**
 *
 * @author maurizio
 */
public class Shell {
  private String prompt = " > ";
  private ConsoleReader cleverConsole;
  private History cleverConsoleHistory;
  private Interpreter interpreter;
  private String confFile = null; 
  private Session s ;
    private SessionFactory sf;
  public Shell(String conf)
  {
    init(conf);
  }
  
  public void init(String conf)
  {
    interpreter = new Interpreter();
    initInterpreter(conf);
    
    
    
    try
    {
      cleverConsole = new ConsoleReader();
      cleverConsole.setBellEnabled( true );
      cleverConsoleHistory = new History();

      File cleverHistoryFile = new File( "cleverHistory.txt" );
      if( !cleverHistoryFile.exists() )
      {
        System.out.println( "History file created." );
        cleverHistoryFile.createNewFile();
      }
      cleverConsoleHistory.setHistoryFile( cleverHistoryFile );
      cleverConsole.setHistory( cleverConsoleHistory );
      sf.closeAllSessions();
      Runtime.getRuntime().addShutdownHook(new Thread(){
          @Override
          public void run()
          {
                sf.closeAllSessions();
                
          }
      });
      
      
      
      showShell();

    }
    catch( IOException e )
    {
      System.out.println( "Error history file creation. " + e );
    } 
  }

    private void showShell() {
      String command = "";
      do
      {
          try {
              command = cleverConsole.readLine( prompt );  
          } catch (IOException ex) {
             System.err.println( "Shell IO Error: " + ex );
             System.exit( 1 );
          }
        if( command.isEmpty() )
        {
          continue;
        }
        if(command.equals("exit"))
              System.exit(0);
        cleverConsoleHistory.addToHistory( command );
          try {
              interpreter.eval(command);
          } catch (EvalError ex) {
              System.err.println(ex);
          }
      } while(true);
    }

    private void initInterpreter(String file) {
      try {
          interpreter.eval("import org.clever.administration.api.*;");
          Configuration conf = new Configuration();
          File f ;
          if (file!=null && ((f = new File(file)).exists()))
          {
              
              conf.configure(f);
          }
          sf = conf.buildSessionFactory();
         
          s = sf.getSession();
          interpreter.set("sf", sf);
          interpreter.set("session", s);
          collectCommands();
      } catch (EvalError ex) {
          ex.printStackTrace();
      } catch (CleverClientException ex) {
          ex.printStackTrace();
      }
    }

    private void collectCommands() {
        Class sessionClass = Session.class;
        System.out.println("Prendo i metodi e compongo la shell");
        for (Method m : sessionClass.getMethods())
        {
            System.out.println("metodo: " + m.getName());
            if (m.getAnnotation(GetShellModule.class) != null)
            {
                String nomeModulo = ((GetShellModule)m.getAnnotation(GetShellModule.class)).name();
                String commento = ((GetShellModule)m.getAnnotation(GetShellModule.class)).comment();
                try {
                    //aggiungere l'autocompletamento 
                    //String nomeMetodo = m.getName();
                    //Decidere se usare eval
                    AdministrationModule modulo = (AdministrationModule) m.invoke(s, null);
                    System.out.println("modulo " + nomeModulo +  " valore: " +modulo);
                    interpreter.set(nomeModulo, modulo);
                    //prendere il tipo di ritorno fare reflection sulla classe , controllare i metodi annotati
                    //con ShellCommand e aggiungere a autocompletamento
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (EvalError ex) {
                    ex.printStackTrace();
                }
            }
            
        }
        
    }
  public static void main( String[] args ) throws IOException
  {
      
      
   
    
      
    if (args.length==0 )
    {
       
        new Shell(null);
    }
    else
    {
        new Shell(args[0]);
       
        
    }
  }
    
}
