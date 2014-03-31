/*
 * The MIT License
 *
 * Copyright Elena Sentimentale.
 * Copyright 2012 Giuseppe Tricomi
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
package org.clever.HostManager.HyperVisorPlugins.VirtualBox;

/**
 * @author elena
 * @author Giuseppe Tricomi 
 */


import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.ArrayList;
import org.clever.Common.Communicator.Agent;
import org.jdom.Element;

import org.virtualbox_4_1.*;

import org.apache.log4j.*;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.HyperVisorException;
import org.clever.Common.Exceptions.ResumeException;
import org.clever.Common.Exceptions.ResumeStateException;
import org.clever.Common.Exceptions.SaveStateException;
import org.clever.Common.Exceptions.StartException;
import org.clever.Common.Exceptions.StopException;
import org.clever.Common.Exceptions.SuspendException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.VEInfo.CpuSettings;
import org.clever.Common.VEInfo.CpuSettings.Architecture;
import org.clever.Common.VEInfo.MemorySettings;
import org.clever.Common.VEInfo.NetworkSettings;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VMWrapper;
import org.clever.HostManager.HyperVisor.HyperVisorPlugin;



public class HvVirtualBox implements HyperVisorPlugin {
    private Agent owner;
    private Map<String, VMWrapper> m = new HashMap<String, VMWrapper>();
    private VirtualBoxManager mgr ;
    private IVirtualBox vbox;
    private Agent ownerAgent;
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger = null;
    private String pathLogConf="/sources/org/clever/HostManager/HyperVisorPlugins/VirtualBox/log_conf/";
    private String pathDirOut="/LOGS/HostManager/HyperVisor/VirtualBox";
    //########
    

    public HvVirtualBox() throws IOException{

      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("VirtualBoxPlugin");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
      logger.info( "VirtualBox plugin created: " );

    }
    
    
 @Override
    public void init(Element params, Agent owner) throws CleverException {
        try{
            // TODO: check if librarypath is present
            System.setProperty( "java.library.path", params.getChildText( "librarypath" ) + ":" + System.getProperty( "java.library.path" ));

            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);



            logger.debug( System.getProperty( "java.library.path" ) );


                  

            mgr = VirtualBoxManager.createInstance(".");
           // mgr.connect( "http://localhost:18083/"," test", "test" );
            
            vbox = mgr.getVBox();
            logger.info( "Virtualbox plugin initialized: "+ "connected to virtualbox" +vbox);
            updateMap();
            this.ownerAgent = owner;

        }
        catch( VBoxException e ){
            e.getWrapped().printStackTrace();
             logger.error( "Error: "+e );
        }
         catch(Exception e){
            logger.error("Exception in HvVirtualBox. :"+e);
            e.printStackTrace();
        }
       
    }

    

     public boolean createAndStart ( String id, VEDescription vmD,Boolean notExclusive ) throws Exception
     {

             createVm( id, vmD,notExclusive );
             return ( startVm(id) );

     }
     
     public String getHYPVRName(){
             return "VirtualBox";
     }
     
     public List getOSTypes()
    {
          IVirtualBox vbox = (IVirtualBox) mgr.getVBox();
          return vbox.getGuestOSTypes();

     }




     public boolean testException() throws Exception{
         throw new Exception("Test Exception");
     }



      public boolean createVm(String id, VEDescription veD,Boolean notExclusive)throws Exception {
           try{
                IMachine vm;
                
    //createMachine (	                in wstring 		settingsFile 	Fully qualified path where the settings file should be created, or NULL for a default folder and file based on the name argument (see composeMachineFilename).
    //					in wstring		name 	        Machine name.
    //					in wstring		osTypeId 	Guest OS Type ID.
    //					in wstringUUID          id              Machine UUID (optional).
    //					in Boolean		forceOverwrite 	If true, an existing machine settings file will be overwritten.
    //
                vm = vbox.createMachine(null, id,"Linux26", null, false);
                setMachine(vm, veD);
                logger.info ("VM "+id+"created ");

                vbox.registerMachine(vm);
                attachDevice(vm, veD,notExclusive);
                VMWrapper vmW = new VMWrapper(vm, veD);
                m.put(id, vmW);
                
                

                logger.info("VM " + id + " created");
                return(true);
            }
           catch(Exception e){
                logger.error("Error: " + e);
                throw new HyperVisorException(e.getMessage());
           }
          
    }


     private void setMachine (IMachine vm, VEDescription vmD){
        try{
            int numCpu = vmD.getCpu().getNumCpu();
            String num = Integer.toString(numCpu);
            Long n = Long.parseLong(num);
            vm.setCPUCount(n);
            vm.setMemorySize(vmD.getMemorySettings().getSize());
            vm.saveSettings();
         }
        catch(VBoxException ex){
            logger.error("Error: " + ex);
        }
     }

     private void attachDevice (IMachine vm, VEDescription vmD,boolean notExclusive){
         try{
             ISession session = mgr.getSessionObject();
             vm.lockMachine(session, LockType.Write);
             IMachine mutable = session.getMachine();
             StorageSettings stor = (StorageSettings)vmD.getStorage().get(0);
 // In openMedium I used AccessMode.Readonly because other accessmode does not work. Verify.
 // for virtualbox 4.0 : vbox.openMedium(stor.getDiskPath(), DeviceType.HardDisk, AccessMode.ReadOnly);
 // Revision make 03/12/2012 openMedium work whit AccessMode.Readonly then we use this AccessMode and 
 // parameter boolean true to create a clone of the HDD image pointed by VEDescriptor.            
             IMedium med = vbox.openMedium(stor.getDiskPath(), DeviceType.HardDisk, AccessMode.ReadWrite,true);
             IMedium medium = vbox.findMedium(stor.getDiskPath(), DeviceType.HardDisk);
 //StorageController data should be already in VEDescription object.
             IStorageController controller= mutable.addStorageController("IDE", StorageBus.IDE);
                          //settiamo il tipo di hd come multiattach.Si potrebbe anche realizzare con createDiffStorage 
             //ma prima bisogna testare se l'immagine nella repository locale viene vista come in NotCreated state
             if(notExclusive)
                medium.setType(MediumType.MultiAttach);
             mutable.attachDevice(controller.getName(), 0, 0, DeviceType.HardDisk, medium);
             mutable.saveSettings();
             session.unlockMachine();
             
             
             
             
             
             
         }
         catch(VBoxException ex){
             logger.error("Exception: "+ex);
             
         }
     }

     public boolean destroyVm ( String id ) throws Exception {
         try{
            IMachine vm;
            vm = ( IMachine ) resolveUUID( id ).getReference();
            boolean b = shutDownVm( id );
            ISession s = mgr.openMachineSession( vm );
            s.unlockMachine();
            ArrayList l = new ArrayList( vm.unregister(CleanupMode.DetachAllReturnHardDisksOnly) );
            IProgress pro = vm.delete( l );
            pro.waitForCompletion( 7000 );
            m.remove(id);
            return ( true );
         }
          catch(VBoxException e){
             logger.error( "Vbox exception: "+e );
             throw new HyperVisorException(e.getMessage());
         }
         catch( Exception e ){
           logger.error( "Error on destroy: "+e );
           throw new HyperVisorException(e.getMessage());
         }
    }

     public VMWrapper resolveUUID(String id) throws Exception{
         IMachine vm;
          try{
             vm = vbox.findMachine(id);
            return new VMWrapper(vm,null);
        }
        catch(Exception ex){
                       logger.error("Error: " + ex);
            throw new Exception("id not found");

        }
     }

     public List listVms() throws Exception{
        try {
            ArrayList l = new ArrayList( m.keySet() );
            logger.info( "List VMS returned numero di macchine: " +l.size() );
            
            return( l );
        }
        catch ( Exception ex ) {
            logger.error( "Error on listVms : " + ex );
            throw new CleverException(ex.getMessage());
        }
    }

     public List listRunningVms() throws Exception{
         String name = "";
         try{
             ArrayList l = new ArrayList ( m.keySet() );
             ArrayList l2 = new ArrayList();
             IMachine vm;

             for( Object obj : l ){
                 name = (String) obj;
                 vm = vbox.findMachine(name);
                 if ( vm.getState().name().compareTo(MachineState.Running.name()) == 0 )
                     l2.add( vm.getName() );
             }
             logger.info( "List running Vms returned number of machine: "+ l2.size() );
             return ( l2 );
         }
          catch( VBoxException ex ){
             logger.error( "Error on listRunningVms: "+ex );
             throw new CleverException(ex.getMessage());
         }
     }


    


     public List listHVms() throws Exception{
         try{
            ArrayList l = new ArrayList(vbox.getMachines());
            logger.info("List VMs rturned numero di macchine: "+l.size());
            ArrayList l2 = new ArrayList();
            IMachine vm;
            for(Object obj: l){
                vm = (IMachine)obj;
                l2.add(vm.getName());
            }
            
            
            return (l2);
         }
         catch(Exception e){
             logger.error("Error on listVirtualBox: "+e);
             throw new HyperVisorException(e.getMessage());
         }
     }

      public List listRunningHVms()throws Exception{
         try{
             ArrayList l = new ArrayList( vbox.getMachines() );
             ArrayList l2 = new  ArrayList();
             IMachine vm;
             for ( Object obj : l ){
                 vm = ( IMachine ) obj;
                 if( vm.getState().name().compareTo(MachineState.Running.name()) == 0 )
                     l2.add( vm.getName() );
             }
             logger.info( "List running HVms returned number of machine: "+ l2.size() );
             return ( l2 );
         }
         catch( Exception ex ){
             logger.error( "Error on listRunningVms: "+ex );
             throw new HyperVisorException(ex.getMessage());
         }
     }

      private  void updateMap(){
          try{
            ArrayList listCl = new ArrayList(m.keySet());
            ArrayList listLib = new ArrayList (listHVms());
            if (listCl.isEmpty()){
               for(Object id1 : listLib){
                    String id = id1.toString();
                    logger.info("VM adding: "+id);
                    VMWrapper wrap = createVMwrapper(id);
                    m.put(id, wrap);
                    logger.info("VM added: "+id);
               }
               return;
            }
	    else{
		  for(Object id1 : listLib){

		      boolean a = false;
		      for(Object id2 : listCl){
			  if(id1.equals(id2)){
			      a = true;
			      break;
			  }
		      }
		      if(a == false){
			  String id = id1.toString();
			  VMWrapper wrap = createVMwrapper(id);
			  m.put(id, wrap);
		      }
		  }
              }
          }
          catch(Exception ex){
              logger.error("Error on updateMap");
              
          }
        }

      private VMWrapper createVMwrapper(String id) throws Exception{
//TODO: retrieve VM info from Hypervisor
          try{
              IMachine vm =  vbox.findMachine(id);
              VMWrapper wrap;
              VEDescription ved;
              List list = vm.getStorageControllers();
              IStorageController controller = (IStorageController) list.get(0);
              IMedium medium = vm.getMedium(controller.getName(), 0, 0);
              ArrayList storage = new ArrayList();
              storage.add(new StorageSettings(0, medium.getType().toString(), medium.getName(), medium.getLocation()));

              int numCpu = Integer.parseInt(vm.getCPUCount().toString());
              CpuSettings cpu = new CpuSettings(numCpu, 0,0,Architecture.X86_64);

              MemorySettings memory = new MemorySettings(vm.getMemorySize());

              ved = new VEDescription(storage, null, id, cpu, memory, null);
              wrap = new VMWrapper(vm, ved);
              m.put(id, wrap);
                return wrap;
            }
             catch(VBoxException e){
                 logger.error("Error :"+e);
                 throw e; //TODO add a efficent exception managing
            }
            catch(Exception e){
                 logger.error("Error: "+e);
                throw e;
            }
        }

     public boolean isRunning( String id ) throws Exception{
         try{
             IMachine vm = vbox.findMachine(id);
             if( vm.getState().name().compareTo(MachineState.Running.name()) == 0 )
                 return ( true );
             else
                 return ( false );
         }
         catch( Exception ex ){
             logger.error( "Error : "+ex );
             throw new CleverException(ex.getMessage());
         }
        
     }


    public boolean addAdapter(String id, NetworkSettings settings) {
        logger.error("addAdapter Not supported yet ");
        throw new UnsupportedOperationException("Not supported yet.");
    }

     public boolean resumeState(String id, String path) throws Exception{

        try {
            IMachine vm = (IMachine) resolveUUID(id).getReference();
             if(vm.getSessionState().compareTo(SessionState.Locked) == 0){
                if(vm.getState().compareTo(MachineState.Running) == 0){
                    logger.error("Virtual Machine: "+vm.getName()+" is already running");
                    return (true);
                }
                logger.error("Error on startVm: machine is already locked because another session has a write lock");
                throw new ResumeStateException("machine is already locked because another session has a write lock");
            }
            ISession session= mgr.getSessionObject();
            IProgress pro = vm.launchVMProcess(session, "sdl", "");
            pro.waitForCompletion(7000);
            mgr.closeMachineSession(session);
            logger.info("VM " + id + " resumed");
            return(true);
        }
        catch (Exception ex) {
            logger.error("Error on resumeState: " + ex);
            throw new HyperVisorException(ex.getMessage());
        }
    }

      
         public String getName() {
            return( "HvVirtualBox" );
    }

    public String getVersion() {
        try{
            return ( vbox.getVersion() );
        }
        catch( VBoxException ex){
            logger.error("Error: " + ex);
            return (null);
        }
    }

    public String getDescription(){
        return("This plugin provides a virtualizing mechanism through Virtualbox");
    }


    public String xmlToString(String path) throws IOException{
        try{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(path));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
        }
        catch(IOException e){
            logger.error("Error on xmlToString "+e);
            throw e;
        }
    }

    public boolean saveState(String id, String path) throws Exception{
        ISession session = null;
        IMachine vm = (IMachine) resolveUUID(id).getReference();
        try {
            
            if(! ((vm.getState().compareTo(MachineState.Running) == 0) || (vm.getState().compareTo(MachineState.Paused) == 0))){
                logger.error("Error on savestate: Virtual machine state neither Running nor Paused. ");
                throw new SaveStateException("Virtual machine state neither Running nor Paused");
            }
            session = mgr.openMachineSession(vm);
            IConsole console = session.getConsole();
            IProgress pro = console.saveState();
            pro.waitForCompletion(-1);
            session.unlockMachine();
            return (true);
        }
        catch (Exception ex) {
            mgr.closeMachineSession(session);
            logger.error("Error: " + ex);
            throw new HyperVisorException(ex.getMessage());
        }
    }


    public boolean startVm(String id) throws Exception{
         IMachine vm = (IMachine) resolveUUID(id).getReference();
         try {
            if(vm.getSessionState().compareTo(SessionState.Locked) == 0){
                if(vm.getState().compareTo(MachineState.Running) == 0){
                    logger.error("Virtual Machine: "+vm.getName()+" is already running");
                    return (true);
                }
                logger.error("Error on startVm: machine is already locked because another session has a write lock");
                throw new StartException("machine is already locked because another session has a write lock");
            }
            ISession s =  mgr.getSessionObject();
            IProgress pro = vm.launchVMProcess(s, "sdl", "");
            pro.waitForCompletion(7000);
            mgr.closeMachineSession(s);
            logger.info("VM " + id + " started");
            return true;
        }

        catch (VBoxException ex) {
            logger.error("Errorn on startVM: "+ex);
            throw new HyperVisorException(ex.getMessage());
        }
    }

     public boolean resume( String id ) throws Exception{
         IMachine vm;
            vm = ( IMachine ) resolveUUID( id ).getReference();
        ISession session = null;
        try {
           
             session = mgr.openMachineSession( vm );
            if(!(vm.getState().compareTo(MachineState.Paused) == 0)){
                logger.error("Error on resume: cannot resume vm as it is not paused");
                mgr.closeMachineSession(session);
                throw new ResumeException("cannot resume vm as it is not paused");
            }
          
            IConsole console = session.getConsole();
            console.resume();
            mgr.closeMachineSession( session );
            logger.info( "VM "+id+"  resumed" );
            return ( true );
        }
        catch ( Exception ex )  {
            mgr.closeMachineSession(session);
            logger.error( "Error on resume : "+ex);
            throw new HyperVisorException("Error on resume: "+ex.getMessage());
            
        }
    }

     public boolean suspend (String id) throws Exception{
         try{
             IMachine vm = (IMachine) resolveUUID(id).getReference();
             if(!(vm.getState().compareTo(MachineState.Running) == 0)){
                logger.error("Error on suspend: domain is not running");
                throw new SuspendException("Error on suspend: domain is not running");
             }
             ISession session = mgr.openMachineSession(vm);
             IConsole console = session.getConsole();
             console.pause();
             mgr.closeMachineSession(session);
             return (true);
         }
         catch(Exception ex){
             logger.error("Error on suspend: " + ex);
             throw new HyperVisorException(ex.getMessage());
         }
     }


     public boolean shutDownVm( String id) throws Exception {
         return this.shutDownVm(id, Boolean.FALSE);
     }

      public boolean shutDownVm( String id , Boolean poweroff) throws Exception{
          ISession ses = null;
          IMachine vm;
            vm = ( IMachine ) resolveUUID( id ).getReference();
        try {
            if(vm.getState().compareTo(MachineState.PoweredOff) == 0){
                  logger.error("Error on shutdown: Virtual machine is already powered off");
                  return (true);
            }
              
            if(!((vm.getState().compareTo(MachineState.Paused) == 0) || (vm.getState().compareTo(MachineState.Stuck) == 0) || (vm.getState().compareTo(MachineState.Running) == 0)))
            {
                logger.error("Error on shutdown: Virtual machine must be Running, Paused or Stuck to be powered down.");
                throw new StopException ("Error on shutdown: Virtual machine must be Running, Paused or Stuck to be powered down.");
            }
            ses = mgr.openMachineSession( vm );
            IConsole console = ses.getConsole();
            IProgress pro = null;
            if(poweroff)
                    pro = console.powerDown();
            else
                    console.powerButton();
            if(pro != null)
                    pro.waitForCompletion( 7000);
            mgr.closeMachineSession( ses );
            logger.info( "VM "+id+"  shutted " );
            return ( true );
            
            
        }
        catch ( Exception ex ) {
            mgr.closeMachineSession(ses);
            logger.error( "Error on shutdownVM : "+ex.getMessage() );
            throw new HyperVisorException(ex.getMessage());
        }
    }







        // ******              gestione snapshot          ************






        public boolean cloneVM (String id, String clone, String description) throws Exception{
         
           IMachine vmnew;
           ISession session = null;


    //createMachine (	                in wstring 		settingsFile 	Fully qualified path where the settings file should be created, or NULL for a default folder and file based on the name argument (see composeMachineFilename).
    //					in wstring		name 	        Machine name.
    //					in wstring		osTypeId 	Guest OS Type ID.
    //					in wstringUUID          id              Machine UUID (optional).
    //					in Boolean		forceOverwrite 	If true, an existing machine settings file will be overwritten.
    //

          try{



               IMachine vmsource = (IMachine) resolveUUID(id).getReference();
               session =  mgr.openMachineSession(vmsource);
                IConsole console = session.getConsole();
               vmnew = vbox.createMachine(null, clone,"Linux26", null, false);
               vmnew.setDescription(description);






              //TODO check if already exist a VM snapshot of source
              ISnapshot snap = takeSnap (id,"snap-"+clone,"snap-"+description,console,vmsource);

              ArrayList <CloneOptions> options =new ArrayList(1);
              options.add(CloneOptions.Link);
              IProgress pro  = snap.getMachine().cloneTo(vmnew, CloneMode.MachineState, options);
              pro.waitForCompletion(-1);
              mgr.closeMachineSession(session);
              vbox.registerMachine(vmnew);
         
              return (true);
          }
          catch(Exception ex){
                mgr.closeMachineSession(session);
              logger.error("Error on cloneVM: "+ex);
              throw new HyperVisorException(ex.getMessage());
          }
      }

      private ISnapshot takeSnap(String id, String nameS, String description,  IConsole console, IMachine vm) throws Exception{
        
       
          IProgress pro  = console.takeSnapshot(nameS, "");
          pro.waitForCompletion(-1);
          ISnapshot snap = vm.findSnapshot(nameS);
          snap.setDescription(description);
          
          return snap;
    }

      public boolean takeSnapshot (String id, String nameS, String description) throws Exception{
         ISession session = null;
          try{
                
                IMachine vm = (IMachine) resolveUUID(id).getReference();

              session =  mgr.openMachineSession(vm);
              IConsole console = session.getConsole();
             takeSnap(id, nameS, description,console,vm);
             mgr.closeMachineSession(session);
             return (true);
          }
          catch(Exception ex){
              mgr.closeMachineSession(session);
              logger.error("Error on takeSnapshot: "+ex);
              throw new HyperVisorException(ex.getMessage());
          }
      }


      public boolean restoreSnapshot (String id, String nameS)throws Exception{
          ISession session = null;
          IMachine vm = (IMachine) resolveUUID(id).getReference();
          try{
            if (vm.getState().compareTo(MachineState.Running) == 0){
                  logger.error("Error on restorSnapshot: Virtual machine is running");
                  throw new HyperVisorException ("The machine must not be running");
              }
              ISnapshot snap = vm.findSnapshot(nameS);
              session = mgr.openMachineSession(vm);
              IConsole console = session.getConsole();
              IProgress pro = console.restoreSnapshot(snap);
              pro.waitForCompletion(-1);
              mgr.closeMachineSession(session);
              return (true);
          }
          catch(Exception ex){
              mgr.closeMachineSession(session);
              logger.error("Error on restoreSnapshot: "+ex);
              throw new HyperVisorException(ex.getMessage());
          }
      }

      public boolean deleteSnapshot (String id, String nameS) throws Exception{
          ISession session = null;
          IMachine vm = (IMachine) resolveUUID(id).getReference();
          try{
              
              session = mgr.openMachineSession(vm);
              IConsole console = session.getConsole();
              ISnapshot snap = vm.findSnapshot(nameS);

              IProgress pro = console.deleteSnapshot(snap.getId());
              pro.waitForCompletion(-1);
              mgr.closeMachineSession(session);
              return (true);
          }
          catch(Exception ex){
              mgr.closeMachineSession(session);
              logger.error("Error on deleteSnapshot: "+ex);
              throw new HyperVisorException(ex.getMessage());
          }
      }


      public String currentSnapshot (String id)throws Exception{
          try{
              IMachine vm;
              vm = (IMachine) resolveUUID(id).getReference();
              ISnapshot snap = vm.getCurrentSnapshot();
              if (snap == null)
                  return null;
             return ( snap.getName());



          }
          catch(Exception ex){
              logger.error("Error on currentSnapshot"+ex);
              throw new HyperVisorException (ex.getMessage());
          }
      }

      public long snapshotCount(String id)throws Exception{
          try{
              IMachine vm;
              vm = (IMachine) resolveUUID(id).getReference();
              return ( vm.getSnapshotCount());
          }
          catch(Exception ex){
              logger.error(("Error on snapshotCount"));
              throw new HyperVisorException(ex.getMessage());
          }
      }

       public String getDefaultMachineFolder() throws Exception
    {
//         try
//         {
//         IVirtualBox vbox = mgr.getVBox();
//
//         return vbox.getSystemProperties().getDefaultMachineFolder();
//        }
//         catch (NullPointerException e)
//         {
//
//             throw new CleverException("Hypervisor connection failed");
//         }
            return new java.io.File(".").getAbsolutePath();
     }

        public List<String> getVRDEProperties(String vmId) throws Exception
    {
        try
        {
            return mgr.getVBox().findMachine(vmId).getVRDEServer().getVRDEProperties();
        }
        catch (VBoxException e)
        {
            logger.error("error : "+e);
            throw (e);
        }
    }


    public String getVRDEProperty(String vmId,String prop) throws Exception
    {
        try
        {
            return mgr.getVBox().findMachine(vmId).getVRDEServer().getVRDEProperty(prop);
        }
        catch (VBoxException e)
        {
            logger.error("error : "+e);
            throw (e);
        }
    }


    public String setVRDEProperty(String vmId,String prop,String value) throws Exception
    {
        try
        {
            IMachine vm = mgr.getVBox().findMachine(vmId);


             ISession session = mgr.getSessionObject();
             vm.lockMachine(session, LockType.Write);
             IMachine mutable = session.getMachine();
             String old = mutable.getVRDEServer().getVRDEProperty(prop);
             mutable.getVRDEServer().setVRDEProperty(prop, value);
             mutable.saveSettings();
             session.unlockMachine();

             return old;
        }
        catch (VBoxException e)
        {
            logger.error("error : "+e);
            throw new CleverException(e.getMessage());
        }
    }

   

    @Override
    public boolean deleteAllSnapshot(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean renameVM(String id, String new_id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean resetVM(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List listSnapshot(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean renameSnapshot(String id, String snapName, String newSnapName, String description) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean attachPortRemoteAccessVm(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void releasePortRemoteAccessVm(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean registerVm(String id, String path) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean unregisterVm(String id) throws Exception {
                ISession session = null;
          IMachine vm = (IMachine) resolveUUID(id).getReference();
          try{
              
              session = mgr.openMachineSession(vm);
              IConsole console = session.getConsole();
              
              mgr.closeMachineSession(session);
            IProgress pro = vm.delete(vm.unregister(CleanupMode.Full));
            
            pro.waitForCompletion(-1);
              return (true);
          }
          catch(Exception ex){
              mgr.closeMachineSession(session);
              logger.error("Error on deleteSnapshot: "+ex);
              throw new HyperVisorException(ex.getMessage());
          }
         
    }

    @Override
    public boolean ExportOvfToLocal(String id, String TargetPhysicalPath) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean ImportLocalOvf(String id, String OVF_physicalPath) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOwner(Agent owner) {
        this.owner=owner;
    }

    @Override
    public String getLocalPath(String id) throws HyperVisorException {
        IMedium med=vbox.findMedium(id, DeviceType.HardDisk);
        String location=med.getLocation();
        return location;
    }

    @Override
    public boolean attackInterface(String id, String inf, String mac, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }




}


