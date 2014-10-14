/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration;

import bsh.EvalError;
import bsh.Interpreter;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;

import org.clever.administration.annotations.GetShellModule;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.CleverCommandClientProvider;
import org.clever.administration.api.Configuration;
import org.clever.administration.api.Session;
import org.clever.administration.api.SessionFactory;
import org.clever.administration.api.modules.AdministrationModule;
import org.clever.administration.clitools.MethodCompleter;
import org.clever.administration.clitools.MethodInfo;
import org.clever.administration.clitools.ModulesSet;
import org.clever.administration.clitools.ModuleInfo;
import org.clever.administration.exceptions.CleverClientException;








class ModuleDelimiter extends ArgumentCompleter.AbstractArgumentDelimiter
{

  

    @Override
    public boolean isDelimiterChar(CharSequence cs, int i) {
        System.out.println("chimato delimit: " + cs +" "+cs.charAt(i));
        return cs.charAt(i) == '.';
    }

}






/**
 *
 * @author maurizio
 */
public class Shell {
  private String prompt = " > ";
  private ConsoleReader cleverConsole;
  private FileHistory cleverConsoleHistory;
  private Interpreter interpreter;
  private StringsCompleter simpleCompletor;
  private ArgumentCompleter argumentCompletor;
  private ModulesSet modules;
  
  
  private Session s ;
  private SessionFactory sf;
  private Map<String, SessionFactory> sfs;
  public Shell(String conf)
  {
    init(conf);
  }
  
  public void init(String conf)
  {
    modules = new ModulesSet();
    interpreter = new Interpreter();
    initInterpreter(conf);
    
    
    
    try
    {
      cleverConsole = new ConsoleReader();
      cleverConsole.setBellEnabled( true );
      

      File cleverHistoryFile = new File( "cleverHistory.txt" );
      if( !cleverHistoryFile.exists() )
      {
        System.out.println( "History file created." );
        cleverHistoryFile.createNewFile();
      }
      cleverConsoleHistory = new FileHistory(cleverHistoryFile);
      cleverConsole.setHistory( cleverConsoleHistory );
      sf.closeAllSessions();
      Runtime.getRuntime().addShutdownHook(new Thread(){
          @Override
          public void run()
          {
              for(Entry<String,SessionFactory> e : sfs.entrySet())
              {
                System.out.println("Closing session factory with name: " + e.getKey());
                e.getValue().closeAllSessions();
              }
              try {
                  cleverConsoleHistory.flush();
              } catch (IOException ex) {
                 System.err.println("Error flushing history file");
              }
                
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
      Function tolist = new Function<ModuleInfo,String>(){

          @Override
          public String apply(ModuleInfo f) {
              return f.getName();
          }
      };
      
//      simpleCompletor = new StringsCompleter(Iterables.toArray(Iterables.transform(modules, tolist),String.class));
//              
//      argumentCompletor = new ArgumentCompleter(
//               new ModuleDelimiter(),
//              new Completer[] {simpleCompletor, new MethodCompleter(modules)}
//             
//              
//              );
      cleverConsole.addCompleter(new MethodCompleter(modules));
      
      
      
      
      
      do
      {
           String p;
          try {
              p = interpreter.get("prompt").toString();
          } catch (EvalError ex) {
              //prompt not defined into bsh interpreter: using default
              p = prompt +"*";
          }
          try {
              command = cleverConsole.readLine( p );  
          } catch (IOException ex) {
             System.err.println( "Shell IO Error: " + ex );
             System.exit( 1 );
          } 
        if( command.isEmpty() )
        {
          continue;
        }
        
        
       
        List<String> params = new ArrayList();
        String comandoShell = parseCommand(command,params);
        
        
        if(comandoShell ==  null)
        {
          //invocazione diretta della bsh
          cleverConsoleHistory.add( command );
          try {
              interpreter.eval(command);
          } catch (EvalError ex) {
              System.err.println(ex);
          }
          continue;
        }
        
        
        if(comandoShell.equals("exit"))
              System.exit(0);
        else if (comandoShell.equals("help"))
        {
            switch(params.size())
            {
                case 0:
                    for(ModuleInfo mi : modules)
                    {
                        System.out.println(mi);
                    }
                    break;
                case 1:
                    ModuleInfo mi = modules.findModule(params.get(0));
                    if(mi == null)
                    {
                        System.err.println("Module " + params.get(0) + " not found");
                        break;
                    }
                    System.out.println(mi.getDetails());
                    break;
                case 2:
                    mi = modules.findModule(params.get(0));
                    if(mi == null)
                    {
                        System.err.println("Module " + params.get(0) + " not found");
                        break;
                    }
                    System.out.println(mi.getMethodsDetails(params.get(1)));
                    break;
                    
            }
            
        }
        
      } while(true);
    }

    private void initInterpreter(String file) {
      try {
          //interpreter.eval("import org.clever.administration.api.*;");
           System.out.println("&&&");
          System.out.println("&&&:"+file);
          interpreter.eval(new InputStreamReader(this.getClass().getResourceAsStream("api/modules/scripts/init.bsh")));
          System.out.println("AL1");
          Configuration conf = new Configuration();
          File f ;
          System.out.println("AL2");
          //Settings settings = null;
          if (file!=null && ((f = new File(file)).exists()))
          {
              
              conf.configure(f);
          }
          else
          {
              
              conf.configure(); //altrimenti getSettings return null
          }
          System.out.println("AL3");
          //a rigore l'impostazione del prompt non andrebbe
          CleverCommandClientProvider cccp = conf.getSettings().getCleverCommandClientProvider();
       
          
          
         // prompt = new StringBuilder(cccp.getNickname()).
//                                             append("@").
//                                             append(cccp.getServername()).
//                                             append(":").
//                                             append(cccp.getRoom()).
//                                             append(" > ").
//                                             toString();
          
          prompt=" > ";
          
          sfs = Maps.newHashMap();
          
          
          sf = conf.buildSessionFactory();
          s = sf.getSession();
          
          sfs.put("initial", sf); //TODO: dare un nome alla sf tramite Configuration e prendere da li il nome
          
          
          
          interpreter.set("sfs", sfs);
          interpreter.set("sf", sf);
          interpreter.set("session", s);
          interpreter.set("prompt", prompt);
          interpreter.eval("changeprompt()");
          collectCommands();
          
          
          
          
      } catch (EvalError ex) {
          ex.printStackTrace();
      } catch (CleverClientException ex) {
          ex.printStackTrace();
      }
    }

    private void collectCommands() {
        Class sessionClass = Session.class;
      
        for (Method m : sessionClass.getMethods())
        {
            
            //if (m.getAnnotation(GetShellModule.class) != null)
            if (m.isAnnotationPresent(GetShellModule.class))
            {
                String nomeModulo = ((GetShellModule)m.getAnnotation(GetShellModule.class)).name();
                String commento = ((GetShellModule)m.getAnnotation(GetShellModule.class)).comment();
                try {
                                    
                    
                    
                    StringBuilder closure = new StringBuilder("create_loader(){get_module(){").append("return sf.getSession().").append(m.getName()).append("();}return this;}");
                    //System.out.println(closure);
                    interpreter.eval(closure.toString());
                  
                    interpreter.eval("register_module(\"" + nomeModulo +"\", create_loader())");
                    
                    Class moduloClass = m.getReturnType();
                    
                    
                    System.out.println("Creating wrapper module for  " + moduloClass.getName() +  " with name: " + nomeModulo+ " : " +commento);
                    
                    
                    //prendere il tipo di ritorno fare reflection sulla classe , controllare i metodi annotati
                    //con ShellCommand e aggiungere a autocompletamento
                    
                    
                    
                    ModuleInfo module = new ModuleInfo(nomeModulo, 
                                          commento,
                                          getMethods(moduloClass)
                                          
                                );
       
       
                    modules.add(module);
                    
                    
                    
                    if(moduloClass.isAnnotationPresent(HasScripts.class))
                    {
                        HasScripts ann = (HasScripts) moduloClass.getAnnotation(HasScripts.class);
                        
                        createScriptModule(ann.value(), ann.comment(), ann.script(),moduloClass);
                    }
                    
                    
               
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (EvalError ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                     ex.printStackTrace();
                }
            }
            
        }
      try {
          interpreter.eval("set_modules()");
      } catch (EvalError ex) {
          ex.printStackTrace();
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

    private void createScriptModule(final String moduleName, final String comment, final String scriptName, final Class module) throws EvalError, IOException {
       //Reader preamble = new StringReader("create_module(){"), end = new StringReader("}"),  script = new InputStreamReader(module.getClass().getResourceAsStream(scriptName));
      
        
        InputSupplier<Reader> preamble = new  InputSupplier<Reader>(){

            @Override
            public Reader getInput() throws IOException {
                return new StringReader("create_module(){");
            }
        };
       
       InputSupplier<Reader> end = new  InputSupplier<Reader>(){

            @Override
            public Reader getInput() throws IOException {
                return new StringReader("return this;}");
            }
        };
       
       InputSupplier<Reader> script = new  InputSupplier<Reader>(){

            @Override
            public Reader getInput() throws IOException {
                return new InputStreamReader(module.getResourceAsStream(scriptName));
            }
        };
       
       
       /*
        
        * Si dovrebbe leggere dgli scripts esterni
        * InputSupplier<Reader> test = new  InputSupplier<Reader>(){

            @Override
            public Reader getInput() throws IOException {
                return new StringReader("test(){print(\"funziona\");}");
            }
        };
       */
       
       
       //interpreter.eval(script); //carica create_module() and
       
       interpreter.eval(CharStreams.join(preamble,script,end).getInput()); //carica create_module() and
       
      
       interpreter.set(moduleName, interpreter.eval("create_module()")); //lancia create_module e il risultato lo mette in una variabile chiamata <moduleName> (object script)
       //String comment = interpreter.eval(moduleName + ".getComment()").toString();
       ModuleInfo modulo = new ModuleInfo(moduleName, 
                                          comment,
                                          new MethodInfo[]{
                                                    new MethodInfo("metododiprova",
                                                                   "Integer",
                                                                   "Un metodo di prova",
                                                                    new SimpleEntry[]{
                                                                        new SimpleEntry<String, String>("args1", "argomento 1")
                                                                    }
                                                          )
                                          }
                                );
       
       
       modules.add(modulo);
       System.out.println("Script loading from " + scriptName + " with module name: " + moduleName + " : " + comment);
       
    }

    
    private void parseArguments(String args, List<String> target)
    {
        Iterables.addAll(target,
                         Splitter.on(" ").
                                trimResults().
                                split(args));
    }
    
    private String parseCommand(String command, List<String> target) {
         Pattern p = Pattern.compile(" *:([^ ]+)(?: +(.*))?$");
         Matcher m = p.matcher(command);
         if(m.find())
         {
             if(m.group(2)!=null)
                 parseArguments(m.group(2), target);
             return m.group(1);
         }
         return null;
    }

    private MethodInfo[] getMethods(Class<? extends AdministrationModule> moduloClass) {
        List<MethodInfo> methods = Lists.newArrayList();
        
        for (Method m : moduloClass.getDeclaredMethods())
        {
            
            if(m.isAnnotationPresent(ShellCommand.class))
            {
                ShellCommand methodAnnotation = m.getAnnotation(ShellCommand.class);
                List<Entry<String,String>> params = Lists.newArrayList();
                for (Annotation[] anns : m.getParameterAnnotations())
                {
                    for (Annotation ann : anns)
                    {
                        if (ann instanceof ShellParameter)
                        {
                            ShellParameter pAnn = (ShellParameter)ann;
                            params.add(new SimpleEntry<String, String>(pAnn.name(), pAnn.comment()));
                        }
                    }
                }
                methods.add(new MethodInfo(
                                        m.getName(),
                                        m.getReturnType().getSimpleName(),
                                        methodAnnotation.comment(),
                                        params.toArray(new Entry[0])
                        
                            )
                        
                        
                        
                        );
            }
        }
        
        
        return methods.toArray(new MethodInfo[0]);
      
    }

   
    
}
