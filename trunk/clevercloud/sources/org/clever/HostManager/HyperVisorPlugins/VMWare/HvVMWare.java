/*
 * The MIT License
 *
 * Copyright 2011 giovalenti.
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
package org.clever.HostManager.HyperVisorPlugins.VMWare;


import com.vmware.vim25.ConcurrentAccess;
import com.vmware.vim25.ConfigTarget;
import com.vmware.vim25.DatastoreInfo;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.Description;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.HttpNfcLeaseDeviceUrl;
import com.vmware.vim25.HttpNfcLeaseInfo;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidDatastore;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.OvfCreateDescriptorParams;
import com.vmware.vim25.OvfCreateDescriptorResult;
import com.vmware.vim25.OvfCreateImportSpecParams;
import com.vmware.vim25.OvfCreateImportSpecResult;
import com.vmware.vim25.OvfFile;
import com.vmware.vim25.OvfFileItem;
import com.vmware.vim25.OvfNetworkMapping;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDeviceConnectInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskSparseVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigOption;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineConfigSummary;
import com.vmware.vim25.VirtualMachineDatastoreInfo;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.EnvironmentBrowser;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.HttpNfcLease;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.apache.log4j.*;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.*;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.VEInfo.CpuSettings;
import org.clever.Common.VEInfo.CpuSettings.Architecture;
import org.clever.Common.VEInfo.DesktopVirtualization;
import org.clever.Common.VEInfo.MemorySettings;
import org.clever.Common.VEInfo.NetworkSettings;
import org.clever.Common.VEInfo.NetworkSettings.NetworkType;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.StorageSettings.DiskMode;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VMWrapper;
import org.clever.HostManager.HyperVisor.HyperVisorPlugin;
import org.clever.HostManager.NetworkManager.AdapterInfo;
import org.clever.HostManager.NetworkManager.IPAddress;
import org.jdom.Element;
import org.libvirt.LibvirtException;


/**
 *
 * @author giovalenti
 */
public class HvVMWare implements HyperVisorPlugin{
    
    private Agent owner;

    private String version = "0.0.1";
    private String description = "Plugin per Hypervisor VMWare";
    private String name = "VMWare Plugin";

    private String hypervisorType;
    private boolean HypervisorHosted=false;
    private boolean HypervisorNative=false;
    private String ipHypervisor;
    
    private Map<String, VMWrapper> m = new HashMap<String, VMWrapper>();
    
    private String url;
    private String user;
    private String password;    
    private String datacenter;
    private String datastore;
    private String netName;
    private String nicName;
    private ServiceInstance si = null;
    private Folder rootFolder = null; 
    private List params = null;
    private int inf_port;
    private int sup_port;
    //private String passVNC;

    private Notification notification;
    private String notificationIdRegisterVirtualDeskHTML5 = "Virtualization/RegisterVirtualDesktopHTML5";
    private String notificationIdUnRegisterVirtualDeskHTML5 = "Virtualization/UnRegisterVirtualDesktopHTML5";
    private String notificationStartedVm = "Virtualization/VmStarted";
    private String notificationCreatedVm = "Virtualization/VmCreated";
    private String notificationImportedVm = "Virtualization/VmImported";
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger = null;
    private String pathLogConf="/sources/org/clever/HostManager/HyperVisorPlugins/VMWare/log_conf/";
    private String pathDirOut="/LOGS/HostManager/HyperVisor/VMWare";
    //########
    
    
    
    public HvVMWare() throws Exception{
        
      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("VMWarePlugin");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
        
      this.logger.info("VMWare plugin created: ");

    }    
        
    
    /**
     * init()
     * Set url, user and pass for open session with VMWare Server
     * @param params : This object contains the node <pluginParams> of configuration_hypervisor.xml
     */
    @Override
    public void init(Element params, Agent owner) throws CleverException{
        if(params!=null){
            this.hypervisorType = params.getChildText("HypervisorType");
            if(this.hypervisorType.equals("hosted"))
                this.HypervisorHosted=true;
            else if(this.hypervisorType.equals("native")){
                this.HypervisorNative=true;
            }
            this.ipHypervisor = params.getChildText("IP");
            this.url = params.getChildText("URL");
            this.user = params.getChildText("user");
            this.password = params.getChildText("password");
            this.datacenter = params.getChildText("datacenter");
            this.datastore = params.getChildText("datastore");
            this.netName = params.getChildText("NetworkNetName");
            this.nicName = params.getChildText("NetworkNicName");
            this.inf_port = Integer.parseInt(params.getChildText("inf_port_vnc"));
            this.sup_port = Integer.parseInt(params.getChildText("sup_port_vnc"));
        }
        this.updateMap();
        logger.info( "VMWare plugin initialized");
        this.notification = new Notification();
        this.owner = owner;
    }
    
    public String getHYPVRName(){
        return "VMWare";
    }
   
    /**
     * Instance connect service with Hypervisor VMWare
     * @param url Url host VMWare
     * @param user Username VMWare
     * @param password Password like O.S. root's password 
     * @return Return an Object ServiceIstance for access to infrastructure VMWare
     * @throws MalformedURLException
     * @throws RemoteException 
     */
    private ServiceInstance connectVMWare(String url, String user, String password) throws MalformedURLException, RemoteException{
        si = new ServiceInstance(new URL(url), user, password, true);            
        this.rootFolder = si.getRootFolder();
        this.logger.info("Session client to VMWare Server established ");
        return si;
    } 
    
    /**
     * Disconnect from VMWare
     * @param si ServiceInstance
     */
    private void disconnectVMWare(ServiceInstance si){
        si.getServerConnection().logout();
    }

    @Override
    /**
     * Destroy a Virtual Machine
     * @param id Name of Virtual Machine we want delete
     * @return The state of operation
     * @throws Exception
     */
    public boolean destroyVm(String id) throws Exception {       
        String vmname = id;
        
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new DestroyException("VM "  + vmname + " not found.");
            }
            this.logger.info("vm: " + vm.getName() + " destroying......");
            Task task = vm.destroy_Task();
            String result = task.waitForTask();
            if(result.equals(Task.SUCCESS)){
                this.logger.info("vm: "+vmname+" destroyed.");                 
                this.m.remove(vmname); //Delete VM from HashMap di Clever
            }
            else {      
                this.disconnectVMWare(this.si);
                throw new DestroyException("Task destroy VM "  + vmname + " failed: " + result);
            }                                   
              
            this.disconnectVMWare(this.si);
            
            return true;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (DestroyException ex) {
            disconnectVMWare(this.si);
            this.logger.error("Error destroyVm:" +ex);
            throw ex;
        } 
    }
    
    @Override
    public boolean shutDownVm(String id) throws Exception {
        return this.shutDownVm(id, Boolean.FALSE); //true: guest shutdown; false: vm shutdown
    }
    
    /**
     * Shutdown a Virtual Machine
     * @param id Name of Virtual Machine we want power off
     * @param guest Shutdown guest flag. TRUE leads OS guest shutdown, false leads virtual machine shutdown
     * @return The state of operation
     * @throws Exception
     */
    private boolean shutDownVm(String id, Boolean OSguest) throws Exception {
        String vmname = id;
        boolean status = false;

        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new StopException("VM "  + vmname + " not found.");
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff)
            {
                throw new StopException("Virtual Machine "  + vmname + " is already stopped.");
            }
            else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOn){
                if(OSguest)
                    vm.shutdownGuest();
                else{    
                    Task task = vm.powerOffVM_Task();
                    String result = task.waitForTask();
                    if(result.equals(Task.SUCCESS)){
                        this.releasePortRemoteAccessVm(vmname);
                        this.NotifyToVirtualizationManager(vmname, this.notificationIdUnRegisterVirtualDeskHTML5);
                        this.logger.info("vm: " + vm.getName() + " stopped and released the port for remote access.");
                        status = true;
                    }
                    else {
                        this.disconnectVMWare(this.si);
                        throw new StopException("Task powerOff VM "  + vmname + " failed: " + result);
                    }
                }
            }

            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (StopException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error shutDownVm:" +ex);
            throw ex;
        }
    }

    @Override
     /**
     * Resume a Virtual Machine
     * @param id Name of Virtual Machine we want power off
     * @return The state of operation
     * @throws Exception
     */
    public boolean resume(String id) throws Exception {
        //In VMWare the resume operation is performed by start operation
        String vmname = id;
        boolean status = false;
        
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new ResumeException("VM "  + vmname + " not found.");
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn)
            { 
                this.disconnectVMWare(this.si);
                throw new ResumeException("Virtual Machine "  + vmname + " is in state ON.");                
            } else if (vmri.getPowerState() == VirtualMachinePowerState.suspended){   
                Task task = vm.powerOnVM_Task(null);
                String result = task.waitForTask();
                if(result.equals(Task.SUCCESS)){
                    this.logger.info("vm: " + vm.getName() + " resumed."); 
                    status = true;
                }
                else {
                    this.disconnectVMWare(this.si);
                    throw new ResumeException("Task resume VM "  + vmname + " failed: " + result);
                }                                
            }
            
            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (ResumeException ex) {            
            this.disconnectVMWare(this.si);
            this.logger.error("Error resumeVm:" +ex);
            throw ex;
        } 
    }

    private String VNCPasswordGenerator(){
        String lettere = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";  // Eventualmente anche le minuscole
	int indice;
	String pass="";
	for (int i=0; i<9; i++) {
	   indice = ((int) (Math.random() * 10000)) % 36;
	   pass=pass+(lettere.charAt(indice));
	}
        return pass;
    }
    
    @Override
     /**
     * Start a Virtual Machine
     * @param id Name of Virtual Machine we want power off
     * @return The state of operation
     * @throws Exception
     */
    public boolean startVm(String id) throws Exception {
        String vmname = id;
        AdapterInfo adapter = null;
        boolean status_port=false;
        
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new StartException("VM "  + vmname + " not found.");
            }
            this.m.get(vmname).getDescription().getDesktopVirtualization().setVmVNCPassword(this.VNCPasswordGenerator()); //Conservo nel virtual enviroment classe Desktop Virtualization la password VNC generata casualmente
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn){ 
                throw new StartException("Virtual Machine "  + vmname + " already started.");
            } else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOff){
                if((status_port=attachPortRemoteAccessVm(vmname))){
                    this.logger.info("Attached port "+ this.m.get(vmname).getDescription().getDesktopVirtualization().getPort() +" to VM "+vmname+" (Remote Access VM)");
//                    if(this.HypervisorHosted){
//                        adapter = this.InvokeMethodGetAdapterInfoOfNetworkManagerAgent();
//                        IPAddress ip = adapter.getIPv4Address();
//                        this.m.get(vmname).getDescription().getDesktopVirtualization().setIpDesktop(ip.getAddress());
//                    }else if(this.HypervisorNative)
//                        this.m.get(vmname).getDescription().getDesktopVirtualization().setIpDesktop(this.ipHypervisor);
                    this.m.get(vmname).getDescription().getDesktopVirtualization().setIpVNC(this.ipHypervisor);                    
                    this.NotifyToVirtualizationManager(this.m.get(vmname).getDescription().getDesktopVirtualization(),this.notificationIdRegisterVirtualDeskHTML5);
                }
                else
                    this.logger.error("Attach port to VM "+vmname+" failed (Remote Access VM)");

                Task task = vm.powerOnVM_Task(null);
                String result = task.waitForTask();
                if(result.equals(Task.SUCCESS)){
                    this.logger.info("vm: " + vm.getName() + " started.");                     
                }
                else {      
                    this.disconnectVMWare(this.si);
                    throw new StartException("Task powerOn VM "  + vmname + " failed: " + result);
                }                                
            }

            //Notify started VM timestamp to Virtualization Manager (format: vmname)
            this.NotifyToVirtualizationManager(vmname, this.notificationStartedVm);

            this.disconnectVMWare(this.si);

            if(!status_port)
                throw new CleverException("No port vnc attached to VM. Check permissions of file .vmx");
            
        } catch (InvalidProperty ex) {
            this.logger.error("Error startVm:" +ex);
        } catch (RuntimeFault ex) {
            this.logger.error("Error startVm:" +ex);
        } catch (RemoteException ex) {
            this.logger.error("Error startVm:" +ex);
        } catch (StartException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error startVm:" +ex);
            throw ex;
        }
        return true;
    }

    /**
     * Get Info about adapter from NetworkManager Agent
     * @return Return an Object AdapterInfo
     */
    private AdapterInfo InvokeMethodGetAdapterInfoOfNetworkManagerAgent(){
        AdapterInfo adapter = null;
        params = new ArrayList();
        params.add("wlan0");
        //params.add("eth0");
        try{
            adapter =(AdapterInfo)this.owner.invoke("NetworkManagerAgent", "getAdapterInfo", true, params);
        }catch(CleverException ex){
            this.logger.error("Method Invoker o Module Communicator returned result CleverException:" +ex);
        } 
        return adapter;
    }

    /**
     * Notify info about Desktop Virtualization access to Virtualization Agent
     * @param item Object that encapsulates the info to send to VirtualizationManager Agent 
     * @param IdNotification
     */
    private void NotifyToVirtualizationManager(Object item, String IdNotification){
        this.notification.setId(IdNotification);
        this.notification.setBody(item);
        this.owner.sendNotification(this.notification);
    }

    @Override
     /**
     * Create a new Virtual Machine
     * @param id Name of Virtual Machine we want power off
     * @param veD Description of Virtual Machine by its features
     * @param notExclusive Give information on lock tipe used for VM's Hdd
     * @return The state of operation
     * @throws Exception
     */
    public boolean createVm(String id, VEDescription veD,Boolean notExclusive) throws Exception {
        boolean status = false;
        String vmName = id;
        int porta=-1;

        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            Datacenter dc = (Datacenter) new InventoryNavigator(this.rootFolder).searchManagedEntity("Datacenter", this.datacenter);
            //The Datacenter managed object type can be used to organize host resources and virtual machines into a high‐level organizational construct
            //Essa eredita la classe ManegedEntity
            ResourcePool rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntities("ResourcePool")[0]; //The ResourcePool managed object
            //is used to partition CPU and memory resources for use by virtual machines. ResourcePool eredita la classe ManagedEntity

            Folder vmFolder = dc.getVmFolder();

            // create vm config spec
            VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec(); //This object contains all the configuration

            this.setMachine(vmSpec,veD,vmName);

            this.attachDevice(vmSpec,veD,vmName,((StorageSettings)veD.getStorage().get(0)).getDiskPath());

            // call the createVM_Task method on the vm folder. The last param is l'host ESXi on which deploy the new virtual machine
            Task task = vmFolder.createVM_Task(vmSpec, rp, null); //Each virtual machine must be associated with a specific instance of a ResourcePool in order to run
            String result = task.waitForTask();
            if(result.equals(Task.SUCCESS)){
                this.logger.info("VM "+vmName+" Created Sucessfully");
                VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmName);
                VMWrapper vmW = new VMWrapper(vm, veD); //Of default set port=-1
                this.m.put(vmName, vmW);
                status = true;
                try{
                    this.addVirtualCD(vmName, this.datastore, "ubuntu-11.04-desktop-i386");
                }catch(Exception ex){
                    this.logger.warn("Virtual Machine " + vmName + "created but Virtual CD/DVD could not be appended." + ex);
                }
            }
            else {
                this.logger.error("VM could not be created: " + result);
                status = false;
            }

            //Notify created VM timestamp to Virtualization Manager (format: vmname)
            this.NotifyToVirtualizationManager(vmName, this.notificationCreatedVm);

            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (Exception ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error createVm:" +ex);
            throw ex;
        }
    }
    
    /**
     * Setup features of new Virtual Machine in state of creation 
     * @param vmSpec This object contains all the configuration 
     * @param vmD This object contains all the features descriptions 
     * @param vmName Name of virtual Machine  
     * @return the state of operation
     */
    private void setMachine(VirtualMachineConfigSpec vmSpec, VEDescription vmD, String vmName){ 
            String annotation = "VirtualMachine Annotation";

            //Get 0 poiché al momento suppongo che la virtual machine ha un solo storage
            ((StorageSettings)vmD.getStorage().get(0)).setDatacenterName(this.datacenter);
            ((StorageSettings)vmD.getStorage().get(0)).setDatastoreName(this.datastore);
            ((StorageSettings)vmD.getStorage().get(0)).setDiskMode(DiskMode.persistent);//Per realizzare futuri snapshot sulla VM, il disk device deve essere in modalità persistent

            //Get 0 poiché al momento suppongo che la virtual machine ha una sola scheda di rete
            ((NetworkSettings)vmD.getNetwork().get(0)).setNetName(this.netName);
            ((NetworkSettings)vmD.getNetwork().get(0)).setNicName(this.nicName);
            ((NetworkSettings)vmD.getNetwork().get(0)).setAddressType(NetworkSettings.MacAddressType.generated);

            vmSpec.setMemoryMB(vmD.getMemorySettings().getSize());
            vmSpec.setNumCPUs(vmD.getCpu().getNumCpu()); 
            vmSpec.setName(vmName);
            vmSpec.setGuestId(vmD.getOSGuestID());//Is required to know the list name of supported guestOsId for VMWare
            vmSpec.setAnnotation(annotation);
    }
    
    /**
     * Save features of the new Virtual Machine in state of creation 
     * @param vmSpec his object contains all the configuration 
     * @param vmD This object contains all the features descriptions 
     * @param vmName vmName Name of virtual Machine 
     * @throws Exception 
     */
    private void attachDevice (VirtualMachineConfigSpec vmSpec, VEDescription vmD, String vmName, String physicalPath) throws Exception{

        //Di tutto il path prendo solo l'ultima dir (uuid della cartella all'interno della quale creo la virtual machine
        StringTokenizer st = new StringTokenizer(physicalPath,"/");
        while(st.hasMoreTokens())
            physicalPath=st.nextToken();
        
        try {
            StorageSettings stor = (StorageSettings)vmD.getStorage().get(0);
            NetworkSettings net = (NetworkSettings)vmD.getNetwork().get(0);
            
            // create virtual devices
            int cKey = 1000;
            VirtualDeviceConfigSpec scsiSpec = this.createScsiSpec(cKey);
            VirtualDeviceConfigSpec diskSpec = this.createDiskSpec(stor.getDatastoreName(), cKey, stor.getCapacity(), stor.getDiskMode().toString());
            
            VirtualDeviceConfigSpec nicSpec = this.createNicSpec(net.getNetName(), net.getNicName(), net.getAddressType().toString());
                
            vmSpec.setDeviceChange(new VirtualDeviceConfigSpec[]{scsiSpec, diskSpec, nicSpec});
                
            // create vm file info for the vmx file
            VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();


            //vmfi.setVmPathName("["+ stor.getDatastoreName() +"] /"+stor.getDiskPath()+"/" + vmName + "/"+vmName+".vmx");
            vmfi.setVmPathName("["+ stor.getDatastoreName() +"] /"+physicalPath+"/"+vmName+".vmx");

            
            vmSpec.setFiles(vmfi);
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }
        
    /**
     * Create and setup a Scsi Device for new Virtul Machine in state of creation 
     * @param cKey
     * @return A new Scsi Device
     */
    private VirtualDeviceConfigSpec createScsiSpec(int cKey){
      VirtualDeviceConfigSpec scsiSpec = new VirtualDeviceConfigSpec();
      scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
      VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
      scsiCtrl.setKey(cKey);
      scsiCtrl.setBusNumber(0);
      scsiCtrl.setSharedBus(VirtualSCSISharing.noSharing);
      scsiSpec.setDevice(scsiCtrl);
      return scsiSpec;
    }
    
    /**
     * Create and setup a Disk Device for the new Virtual Machine in state of creation 
     * @param dsName Datastore name
     * @param cKey
     * @param diskSizeKB Size of disk in KB
     * @param diskMode Disk Mode: persistent, independent_persistent and independent_nonpersistent
     * @return A new Disk Device
     */
    private VirtualDeviceConfigSpec createDiskSpec(String dsName, int cKey, long diskSizeKB, String diskMode){
      VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
      diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
      diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);
      
      VirtualDisk vd = new VirtualDisk();
      vd.setCapacityInKB(diskSizeKB);
      diskSpec.setDevice(vd);
      vd.setKey(0);
      vd.setUnitNumber(0);
      vd.setControllerKey(cKey);

      VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
      String fileName = "["+ dsName +"]";
      diskfileBacking.setFileName(fileName);
      diskfileBacking.setDiskMode(diskMode);
      diskfileBacking.setThinProvisioned(true);
      
      vd.setBacking(diskfileBacking);
      
      return diskSpec;
    }
    
    /**
     * Create and setup a Network Device for the new Virtual Machine in state of creation 
     * @param netName Net name 
     * @param nicName Nic name
     * @param addressType Address Type: generated, manual, assigned
     * @return A new Network Device
     * @throws Exception 
     */
    private VirtualDeviceConfigSpec createNicSpec(String netName, String nicName, String addressType) throws Exception {
      VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
      nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

      VirtualEthernetCard nic =  new VirtualPCNet32();
      VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
      nicBacking.setDeviceName(netName);

      Description info = new Description();
      info.setLabel(nicName);
      info.setSummary(netName); //Nel netName è indicata la tipologia (BRIDGED, LOCAL, NAT). La lista dei netName ammissibili deve essere
                                //gia formata sull'hypervisor VMWare e il netName inserito deve farne parte
      nic.setDeviceInfo(info);
      
      // type: "generated", "manual", "assigned" 
      //nic.setAddressType("generated");
      nic.setAddressType(addressType);
      nic.setBacking(nicBacking);
      nic.setKey(0);
     
      nicSpec.setDevice(nic);
      
      return nicSpec;
    }

    /**
     * Add the CD/DVD device to a Virtual Machine already present in VMWare
     * @param rootFolder Root Node of entity (virtual machines and hosts) tree of the inventory
     * @param vmName Name of Virtual Machine
     * @param datastoreName Datastore name when is located the Virtual Machine
     * @param isoName Iso name of image disk
     * @throws InvalidProperty
     * @throws RuntimeFault
     * @throws RemoteException
     * @throws Exception 
     */
    public void addVirtualCD(String vmName, String datastoreName, String isoName) throws InvalidProperty, RuntimeFault, RemoteException, Exception{
        
        VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmName);
        VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
        VirtualDeviceConfigSpec cdSpec = this.createAddCdConfigSpec(vm, datastoreName, isoName);
        vmConfigSpec.setDeviceChange(new VirtualDeviceConfigSpec[]{cdSpec});
        
        Task task = vm.reconfigVM_Task(vmConfigSpec);
        task.waitForTask();
        
    }
    
    /**
     * Create and setup a CD/DVD Device 
     * @param vm Name of Virtual Machine
     * @param dsName Datastore name when is located the Virtual Machine
     * @param isoName Iso name of image disk
     * @return A new CD/DVD Device
     * @throws Exception 
     */
    private VirtualDeviceConfigSpec createAddCdConfigSpec(VirtualMachine vm, String dsName, String isoName) throws Exception {
        VirtualDeviceConfigSpec cdSpec = new VirtualDeviceConfigSpec();
        cdSpec.setOperation(VirtualDeviceConfigSpecOperation.add);   
        VirtualCdrom cdrom =  new VirtualCdrom();
        VirtualCdromIsoBackingInfo cdDeviceBacking = new  VirtualCdromIsoBackingInfo();
        DatastoreSummary ds = this.findDatastoreSummary(vm, dsName);
        cdDeviceBacking.setDatastore(ds.getDatastore());
        cdDeviceBacking.setFileName("[" + dsName +"] "+ isoName+ ".iso");
        VirtualDevice vd = this.getIDEController(vm);          
        cdrom.setBacking(cdDeviceBacking);                    
        cdrom.setControllerKey(vd.getKey());
        cdrom.setUnitNumber(vd.getUnitNumber());
        cdrom.setKey(-1);  
        cdSpec.setDevice(cdrom); 
        return cdSpec;          
    }

    /**
     * Find the Datastore Summary
     * @param vm Name of Virtual Machine
     * @param dsName Name of Datastore
     * @return a DatastoreSummary Object of the Virtual Machine
     * @throws Exception 
     */
    private DatastoreSummary findDatastoreSummary(VirtualMachine vm, String dsName) throws Exception {
        DatastoreSummary dsSum = null;
        VirtualMachineRuntimeInfo vmRuntimeInfo = vm.getRuntime();
        EnvironmentBrowser envBrowser = vm.getEnvironmentBrowser(); 
        ManagedObjectReference hmor = vmRuntimeInfo.getHost();
        if(hmor == null) {
            this.logger.error("Errore AddVirtualCD/DVD to VM " + vm.getName() +": No Datastore found");
            return null;
        }
        ConfigTarget configTarget = envBrowser.queryConfigTarget(new HostSystem(vm.getServerConnection(), hmor));
        VirtualMachineDatastoreInfo[] dis = configTarget.getDatastore();
        for (int i=0; dis!=null && i<dis.length; i++){
            dsSum = dis[i].getDatastore();
            if (dsSum.isAccessible() && dsName.equals(dsSum.getName())){
                break;
            }
        }
        return dsSum;
    }

    /**
     * Get IDE Controller of a Virtual Machine
     * @param vm Name of Virtual Machine
     * @return A VirtualIDEController object of the Virtual Machine
     * @throws Exception 
     */
    private VirtualDevice getIDEController(VirtualMachine vm) throws Exception {
        VirtualDevice ideController = null;
        VirtualDevice[] defaultDevices = this.getDefaultDevices(vm);
        for (VirtualDevice i : defaultDevices){
            if (i instanceof VirtualIDEController){
                ideController = i;             
                break;
            }
        }
        //System.out.println(ideController.toString());
        return ideController;
    }

    /**
     * Get Default Devices of a Virtual Machine
     * @param vm Name of Virtual Machine
     * @return DefaultDevice object of the Virtual Machine
     * @throws Exception 
     */
    private VirtualDevice[] getDefaultDevices(VirtualMachine vm) throws Exception{
        VirtualMachineRuntimeInfo vmRuntimeInfo = vm.getRuntime();
        EnvironmentBrowser envBrowser = vm.getEnvironmentBrowser(); 
        ManagedObjectReference hmor = vmRuntimeInfo.getHost();
        VirtualMachineConfigOption cfgOpt = envBrowser.queryConfigOption(null, new HostSystem(vm.getServerConnection(), hmor));
        VirtualDevice[] defaultDevs = null;
        if (cfgOpt != null){
            defaultDevs = cfgOpt.getDefaultDevice();
                if (defaultDevs == null) 
                {
                    throw new Exception("No Datastore found in ComputeResource");
                }
        }
        else{
            throw new Exception("No VirtualHardwareInfo found in ComputeResource");
        }
        return defaultDevs;
    }
    
    
    @Override
    public boolean resumeState(String id, String path) throws Exception{
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    /**
     * Create and Start a Virtual Machine
     * @param id Name of the Virtual Machine
     * @param parameters VEDescription of the Virtual Machine
     * @return State of the operation
     * @throws Exception 
     */
    public boolean createAndStart(String id, VEDescription veD,Boolean notExclusive) throws Exception {
        this.createVm(id, veD,notExclusive);
        return ( this.startVm(id) );
    }
       
    /**
     * Create a new VMwrapper that carry a guest object (Virtual Machine) in Clever
     * @param id Name of Virtual Machine
     * @param porta Number of port for remote access to VM
     * @return A single wrapper that carry a name and VeDescription of a Virtual Machine
     * @throws Exception 
     */
    private VMWrapper createVMwrapper(String id) throws Exception{
    //TODO: retrieve VM info from Hypervisor        
        
        VMWrapper wrap = null;
        VEDescription ved = null;
              
        ArrayList storage = new ArrayList();
        ArrayList network = new ArrayList();
        
        StorageSettings storageSettings = null;
        NetworkSettings networkSetting = null;
        
        try{
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", id);
            VirtualMachineConfigInfo vmci = (VirtualMachineConfigInfo)vm.getConfig();
            VirtualHardware vh = vmci.getHardware();
            VirtualDevice[] vd = vh.getDevice();
            
            for (VirtualDevice i : vd){
                if(i instanceof VirtualDisk){ 
                    storageSettings = this.createStorageSettingsVM((VirtualDisk)i, vm);
                    storage.add(storageSettings);              
                }
            
                if(i instanceof VirtualPCNet32){ //Ripetere il quanto fatto con VirtualDisk
                    networkSetting = this.createNetworksSettingsVM((VirtualPCNet32)i);
                    network.add(networkSetting);
                }
            }
        
            CpuSettings cpu = this.createCpuSettingsVM(vh);
                
            MemorySettings memory = this.createMemorySettingsVM(vh);
        
            ved = new VEDescription(storage, network, id, cpu, memory, new DesktopVirtualization(id,"pass"));
            ved.getDesktopVirtualization().setPort(Integer.toString(-1));//Of default set port=-1

            wrap = new VMWrapper(vm, ved);    
            
            this.m.put(id, wrap);
            
            this.disconnectVMWare(this.si);
            return wrap;
        }
        catch(Exception e){
            this.disconnectVMWare(this.si);
            logger.error("Error: "+e);
            throw e;
        }
    }
        
    /**
     * Get CPUSettings in Clever Format
     * @param vh Virtual Hardware in VMWare Format 
     * @return CPUSettings object
     */
    private CpuSettings createCpuSettingsVM(VirtualHardware vh){
        CpuSettings cpu = new CpuSettings(vh.getNumCPU(), 0,0,Architecture.X86_64);
        return cpu;
    }
    
    /**
     * Get MemorySettings in Clever Format
     * @param vh Virtual Hardware in VMWare Format 
     * @return MemorySettings object
     */
    private MemorySettings createMemorySettingsVM(VirtualHardware vh){
        MemorySettings memory = new MemorySettings(vh.getMemoryMB());
        return memory;
    }
    
    /**
     * Get StorageSettings in Clever Format
     * @param virtualdisk Storage in VMWare Format 
     * @return StorageSettings object
     */
    private StorageSettings createStorageSettingsVM(VirtualDisk virtualdisk, VirtualMachine vm) throws Exception{
        DiskMode diskMode = null;
        VirtualDiskFlatVer2BackingInfo vdbiFlat = null;
        VirtualDiskSparseVer2BackingInfo vdbiSparse = null;
        StorageSettings storageClever = null;
        
        Description desc = virtualdisk.getDeviceInfo(); //VirtualDiskFlatVer2BackingInfo
        //Devo prevedere se il virtual disk sarà un Flat o Sparse Version. Al momento nel createVm si opta per il Flat Version
        if(virtualdisk.getBacking() instanceof VirtualDiskSparseVer2BackingInfo){
            vdbiSparse = (VirtualDiskSparseVer2BackingInfo)virtualdisk.getBacking();
            if (vdbiSparse.getDiskMode().equals("persistent"))
                diskMode = StorageSettings.DiskMode.persistent;
            if (vdbiSparse.getDiskMode().equals("independent_persistent"))
                diskMode = StorageSettings.DiskMode.independent_persistent;
            if (vdbiSparse.getDiskMode().equals("independent_nonpersistent"))
                diskMode = StorageSettings.DiskMode.independent_nonpersistent; 
            String datastoreName = this.getDatastoreName(vm);
            storageClever = new StorageSettings(virtualdisk.capacityInKB,"hd", diskMode, desc.getLabel(), this.datacenter, this.datastore, vdbiSparse.getFileName()); //getFileName() fornisce il path al file disco virtuale compreso il datastore (tra []);
        }
        if(virtualdisk.getBacking() instanceof VirtualDiskFlatVer2BackingInfo){            
            vdbiFlat = (VirtualDiskFlatVer2BackingInfo)virtualdisk.getBacking(); 
            if (vdbiFlat.getDiskMode().equals("persistent"))
                diskMode = StorageSettings.DiskMode.persistent;
            if (vdbiFlat.getDiskMode().equals("independent_persistent"))
                diskMode = StorageSettings.DiskMode.independent_persistent;
            if (vdbiFlat.getDiskMode().equals("independent_nonpersistent"))
                diskMode = StorageSettings.DiskMode.independent_nonpersistent;
            String datastoreName = this.getDatastoreName(vm);            
            storageClever = new StorageSettings(virtualdisk.capacityInKB, "hd", diskMode, desc.getLabel(), this.datacenter, this.datastore, vdbiFlat.getFileName()); //getFileName() fornisce il path al file disco virtuale compreso il datastore (tra []);
        }
        //System.out.println("Space Virtual Disk used in KB: "+String.valueOf(vdbi.getSpaceUsedInKB())); //Dimensione sull'host del file immagine del disco virtuale assegnato a una MV. La dimensione si aggiorna alla chiusura della VM
        //System.out.println("uuid Virtual Disk: "+vdbi.getUuid())
     
        return storageClever;
    }
    
    /**
     * Get NetworkSettings in Clever Format
     * @param virtualpcnet Network in VMWare Format
     * @return NetworkSettings object
     */
    private NetworkSettings createNetworksSettingsVM(VirtualPCNet32 virtualpcnet){
        
        NetworkSettings networkClever = null;
        Description desc = virtualpcnet.getDeviceInfo();
        
        //networkClever.setNicName(desc.getLabel());
        //networkClever.setNetName(desc.getSummary()); //In Net Name (getSummary()) è incluso se la scheda di rete è di tipo bridged, HostOnly(LOCAL) o NAT
       
        //MAC Address Type. generated: MAC address generated by the host; manual: if you want to specify MAC address; assigned: MAC address assigned by VirtualCenter
        if(virtualpcnet.addressType.equals("generated"))
            networkClever = new NetworkSettings(desc.getSummary(),desc.getLabel(),NetworkSettings.MacAddressType.generated);
            //networkClever.setAddressType(NetworkSettings.AddressType.generated);
        if(virtualpcnet.addressType.equals("assigned"))
            networkClever = new NetworkSettings(desc.getSummary(),desc.getLabel(),NetworkSettings.MacAddressType.assigned);
            //networkClever.setAddressType(NetworkSettings.AddressType.assigned);
        if(virtualpcnet.addressType.equals("manual"))
            networkClever = new NetworkSettings(desc.getSummary(),desc.getLabel(),NetworkSettings.MacAddressType.manual);
            //networkClever.setAddressType(NetworkSettings.AddressType.manual);
        
        networkClever.setType(this.findNetworkType(desc.getSummary()));// find is bridged, HostOnly(LOCAL) or NAT 
        
        VirtualDeviceConnectInfo vdci = virtualpcnet.getConnectable();
        networkClever.setConnected(vdci.connected);
        networkClever.setStartConnected(vdci.startConnected);
        networkClever.setMac(virtualpcnet.getMacAddress());  
  
        return networkClever;
    }

    /**
     * Find the type of Network; Bridged, Local (HostOnly) or NAT. It is 
     * into NetName of NetworkDevice in VMWare Format
     * @param value NetName of NetworkDevice in VMWare Format
     * @return The type of Network
     */
    private NetworkType findNetworkType(String value){
        NetworkType type = null;
        String value_lower = value.toLowerCase();
	if(value_lower.indexOf("bridged")!=-1)
            type = NetworkSettings.NetworkType.BRIDGED;
	if(value_lower.indexOf("hostonly")!=-1)
            type = NetworkSettings.NetworkType.LOCAL;	
        if(value_lower.indexOf("nat")!=-1)
            type = NetworkSettings.NetworkType.NAT;
        return type;
    }
    
    /**
     * Get the name of the datastore of VMWare. It is incapsulated into
     * File Name of virtual disk of VMWare
     * @param value Name of virtual disk
     * @return Datastore name
     */
    private String getDatastoreName(VirtualMachine vm) throws InvalidProperty, RuntimeFault, RemoteException {        
        VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
	Datastore[] datastore = vm.getDatastores();
	DatastoreInfo di = datastore[0].getInfo(); //La VM ha un solo datastore
	return(di.getName());
    }
    
    @Override
    public boolean addAdapter(String id, NetworkSettings settings) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    /** Query VMWare for know the running state of a Virtual Machine
     * @param id Name of Virtual Machine
     * @throws Exception
     */
    public boolean isRunning(String id) throws Exception{
        boolean status = false;
        String vmname = id;
        
        try {            
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new Exception("VM "  + vmname + " not found.");
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn)
                status = true;
            else 
                status = false;
            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            this.disconnectVMWare(this.si);
            throw ex;
        } catch (RuntimeFault ex) {
            this.disconnectVMWare(this.si);
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        }
    }   

    /**updateMap() preleva la lista delle macchine registrate in VMWare (listHVms()) e procede con la verifica della presenza 
    *di ognuna di esse nella lista delle macchine definite in Clever. 
    */ 
    private void updateMap(){
        try{
            ArrayList listCl = new ArrayList(this.m.keySet());
            ArrayList listLib = new ArrayList(this.listHVms());
            
            if (listCl.isEmpty()){
               for(Object id1 : listLib){
                    String id = id1.toString();
                    logger.info("VM adding: "+id);  
                    VMWrapper wrap = this.createVMwrapper(id);
                    this.m.put(id, wrap);
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
			VMWrapper wrap = this.createVMwrapper(id);
			this.m.put(id, wrap);
                     }
		  }
              }
        }
        catch(Exception ex){
            logger.error("Error on updateMap: "+ex);  
        }
    }
    
    @Override
    /**
     * List listVMs() si avvale dell'HashMap di Clever
     * @return List of Virtual Machines and its port into this HostManager of Clever
     * @throws Exception
     */
    public List listVms() throws Exception {
        VMWrapper wrap = null;
        String s = null;
        ArrayList ret = new ArrayList();
        try {             
            ArrayList l = new ArrayList(this.m.keySet());    
            for(int i=0; i<l.size(); i++){
                wrap = this.m.get((String)l.get(i));
                s=(String)l.get(i)+" port:"+wrap.getDescription().getDesktopVirtualization().getPort();
                ret.add(s);
            }
            logger.info( "List VMS returned numero di macchine: " +l.size() );            
            return(ret); 
        }
        catch ( Exception ex ) {
            logger.error( "Error on listVms : " + ex );
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * List listHVms() si avvale direttamente di VMWare. La H evidenzia proprio 
     * il supporto dell'Hypervisor
     * @return List of Virtual Machines into VMWare
     * @throws Exception
     */
    private List listHVms() throws Exception {
        this.si = this.connectVMWare(this.url, this.user, this.password);

        ArrayList l = new ArrayList();
        ManagedEntity[] mes = this.rootFolder.getChildEntity(); //La classe Folder eredita la classe astratta ManagedEntity così come le
                                                           //le classi Datacenter e VirtualMachine
        for(Object i : mes){
            if(i instanceof Datacenter){
                Datacenter dc = (Datacenter)i;
                Folder vmFolder = dc.getVmFolder();
                ManagedEntity[] vms = vmFolder.getChildEntity();       
                for(ManagedEntity j : vms){
                    if(j instanceof VirtualMachine){
                        VirtualMachine vm = (VirtualMachine)j;
                        l.add(vm.getName());
                    }
                }
            }
        }
        
        this.disconnectVMWare(this.si);
        this.logger.info("List HVMs returned: " + l.size() + " Virtual Machines");
        return l;
    }
    
    @Override
    /**
     * List listRunningVms si avvale dell'HashMap di Clever (VM registrate in Clever)
     * dal quale preleva il nome della VM e interroga VMWare per lo stato
     * @return List of running Virtual Machines and its port into this Host Manager of Clever
     * @throws Exception
     */
    public List listRunningVms() throws Exception {
        
        VMWrapper wrap = null;
        String s = null;
        ArrayList ret = new ArrayList();
        try{
            this.si = this.connectVMWare(this.url, this.user, this.password);
        
            String name = "";
        
            ArrayList l = new ArrayList (this.m.keySet());
            VirtualMachine vm;

            for( Object obj : l ){
                name = (String) obj;
                vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", name); //find VM by name
                VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
                if (vmri.getPowerState() == VirtualMachinePowerState.poweredOn){
                    wrap = this.m.get(name);
                    s=name+" port:"+wrap.getDescription().getDesktopVirtualization().getPort();
                    ret.add(s);
                }
            }
             logger.info( "List running Vms returned number of machine: "+ ret.size() );
             this.disconnectVMWare(this.si);
             return ( ret );
         }
          catch( Exception ex ){
             this.disconnectVMWare(this.si);
             logger.error( "Error on listRunningVms: "+ex );
             throw new Exception(ex.getMessage());
         }
    }
   
    /**
     * List listRunningHVms() si avvale direttamente di VMWare. La H evidenzia proprio 
     * il supporto dell'Hypervisor
     * @return List of running Virtual Machines into VMWare
     * @throws Exception
     */
    private List listRunningHVms() throws Exception {
        
        this.si = this.connectVMWare(this.url, this.user, this.password);
        
        ArrayList l = new ArrayList();
        ManagedEntity[] mes = this.rootFolder.getChildEntity(); //La classe Folder eredita la classe astratta ManagedEntity così come le
                                                           //le classi Datacenter e VirtualMachine
        for(Object i : mes){
            if(i instanceof Datacenter){
                Datacenter dc = (Datacenter)i;
                Folder vmFolder = dc.getVmFolder();
                ManagedEntity[] vms = vmFolder.getChildEntity();       
                for(ManagedEntity j : vms){
                    if(j instanceof VirtualMachine){
                        VirtualMachine vm = (VirtualMachine)j;
                        VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
                        if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn)
                        {
                              l.add(vm.getName());              
                        }
                    }
                }
            }
        }
        
        this.disconnectVMWare(this.si);
        this.logger.info("List RunningVms returned: " + l.size() + "Virtual Machines poweredOn");
        return l;        
    }

    @Override
    /** Suspend of a Virtual Machine
     * @param id Name of the Virtual Machine
     * @throws Exception
     */
    public boolean suspend(String id) throws Exception {
        String vmname = id;
        
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new SuspendException("VM "  + vmname + " not found.");
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff){ 
                this.logger.info("Virtual Machine " + vmname + "is poweredOff.");
                throw new SuspendException("Virtual Machine "  + vmname + " is poweredOff.");
            } else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOn){   
                Task task = vm.suspendVM_Task();
                String result = task.waitForTask();
                if(result.equals(Task.SUCCESS)){
                    this.logger.info("vm: " + vm.getName() + " suspended.");                     
                }
                else {    
                    this.disconnectVMWare(this.si);
                    throw new SuspendException("Task suspend VM "  + vmname + " failed: " + result);
                }                                
            }
            this.disconnectVMWare(this.si);
            return true;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SuspendException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error suspendVm:" +ex);
            throw ex;
        } 
    }
  
    @Override
    /**
     * Take a snapshot of a Virtual Machine
     * @param id Name of the Virtual Machine
     * @param nameS Name of the snapshot
     * @return State of operation
     * @throws Exception
     */
    public boolean takeSnapshot(String id, String nameS, String description) throws Exception {
        //Devo ignorare l'argomento path perché VMWare usa un datastore nel quale posiziona in automatico
        //il snapshot della macchina virtuale
        //In VMWare Server per realizzare uno snapshot (createSnapshot_Task) la macchina virtuale deve essere nello stato di ON,
        //non deve contenere spazi vuoti nel nome e il virtual disk device deve essere in modalità persistent 
        boolean status = false;
        String vmname = id;
        String snapshotname = null;        
        if(this.HypervisorHosted){
            snapshotname="VMware Server Undopoint"; //Nome di default poiché è ammessa la creazione di un solo snapshot.
            //Percui il snapshots tree è composto da un solo nodo. Limitazione data dall'utilizzo di VMWare Server.  
        }else if(this.HypervisorNative){//the snapshot tree is enabled (VMWare ESXi)
            snapshotname = nameS; 
        }
        String desc = description;        
        
        try{
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);        
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            } else {                
                VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
                if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff){ 
                    this.logger.info("Virtual Machine " + vmname + "is poweredOff. Must be in powerOn state");
                    throw new SnapshotException("Virtual Machine "  + vmname + " is poweredOff.");
                } else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOn){
                
                    Task task = vm.createSnapshot_Task(snapshotname, desc, false, false);
                    String result = task.waitForTask();
                    if(result.equals(Task.SUCCESS))
                    {
                        this.logger.info("Take Snapshot Virtual Machine "  + vmname + " done.");
                        status = true;
                    }else{
                        this.disconnectVMWare(this.si);
                        throw new SnapshotException("Task take snapshot VM "  + vmname + " failed: " + result );
                    }
                    
                }
            }
            this.disconnectVMWare(this.si);
            return status;        
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch(SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error takeSnapshot: Control if the disk is persistent_mode and the name of VM have no white space. " +ex);
            throw ex;
        }
    }

    @Override
    /**
     * Restore a Snapshot of a Virtual Machine by your name
     * @param id Name of Virtual Machine
     * @param nameS Name of the snapshot
     * @return State of operation
     * @throws Exception
     */
    public boolean restoreSnapshot(String id, String nameS) throws Exception {
        //In VMWare is revert snapshot operation
        boolean status = false;
        String vmname = id;
        String snapshotname = null;        
        if(this.HypervisorHosted){
            snapshotname="VMware Server Undopoint"; //Nome di default poiché è ammessa la creazione di un solo snapshot.
            //Percui il snapshots tree è composto da un solo nodo. Limitazione data dall'utilizzo di VMWare Server.  
        }else if(this.HypervisorNative){//the snapshot tree is enabled (VMWare ESXi)
            snapshotname = nameS;
        }
        
        try {           
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null) {
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn){ 
                this.logger.info("Virtual Machine " + vmname + "is poweredOn. Must be in powerOff state");
                throw new SnapshotException("Virtual Machine "  + vmname + " is poweredOn.");
            } else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOff){
                VirtualMachineSnapshot vmsnap = this.getSnapshotInTree(vm, snapshotname);
                if(vmsnap!=null){
                    Task task = vmsnap.revertToSnapshot_Task(null); //Argument optional. Choice of host for the virtual machine, in case this operation causes the virtual machine to power on.
                    String result = task.waitForTask();
                    if(result.equals(Task.SUCCESS)){
                        this.logger.info("Restored to snapshot:" + snapshotname +" for VM "+vmname+ " Done");
                        status = true;
                    }
                    else{ 
                        this.disconnectVMWare(this.si);
                    throw new SnapshotException("Task restore snapshot VM "  + vmname + " failed: " + result);
                    }
                }
            }
            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error restoreSnapshot: " +ex);
            throw ex;
        }
    }
    
    @Override
    public boolean renameSnapshot(String id, String snapName, String newSnapName, String description) throws Exception {
        boolean status = true;            
        String vmname = id;
            
        try { 
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null) {
                this.disconnectVMWare(this.si);
                status = false;
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            }
            VirtualMachineSnapshot vmsnap = this.getSnapshotInTree(vm, snapName);
            if(vmsnap!=null)
              vmsnap.renameSnapshot(newSnapName, description);               
            
            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error deleteAllSnapshot VM: " +ex);
            throw ex;
        }
    }

    @Override
    /**
     * Delete a snapshot of a Virtual Machine
     * @param id Name of Virtual Machine
     * @param nameS Name of the snapshot
     * @return State of operation
     * @throws Exception
     */
    public boolean deleteSnapshot(String id, String nameS) throws Exception {
       boolean removechild=true;
       return(this.deleteSnapshot(id, nameS, removechild));
    }
    
   
    /**
     * Delete a snapshot of a Virtual Machine
     * @param id Name of Virtual Machine
     * @param nameS Name of the snapshot
     * @param removechild Flag for remove all children of snapshot (snapshot tree about a Virtual Machine)
     * @return State of operation
     * @throws Exception
     */
    public boolean deleteSnapshot(String id, String nameS, boolean removechild) throws Exception {
        //In VMWare is remove snapshot operation
        String vmname = id;
        boolean status = false;
        String snapshotname = null; 
        if(this.HypervisorHosted){
            snapshotname="VMware Server Undopoint"; //Nome di default poiché è ammessa la creazione di un solo snapshot.
            //Percui il snapshots tree è composto da un solo nodo. Limitazione data dall'utilizzo di VMWare Server.  
        }else if(this.HypervisorNative){//the snapshot tree is enabled (VMWare ESXi)
            snapshotname = nameS;
        }
        try {               
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null) {
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            }
            VirtualMachineSnapshot vmsnap = this.getSnapshotInTree(vm, snapshotname);
            if(vmsnap!=null){
              Task task = vmsnap.removeSnapshot_Task(removechild);//boolean che specifica se eliminare il snapshot figlio e quello successivi (figli di figli)
              String result = task.waitForTask();
              if(result.equals(Task.SUCCESS)){
                this.logger.info("Deleted snapshot: " + snapshotname +" for VM "+vmname+"Done");
                status = true;
              }
              else{ 
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Task delete snapshot VM "  + vmname + " failed: " + result );
              }
            }
            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error deleteSnapshot: " +ex);
            throw ex;
        }
    }
    
    /**
     * Find the snapshot into snapshot tree of a Virtual Machine
     * @param vm Name of the Virtual Machine
     * @param snapName Name of the snapshot
     * @return The Snapshot node
     * @throws Exception 
     */
    private VirtualMachineSnapshot getSnapshotInTree(VirtualMachine vm, String snapName) throws Exception{
        if (vm == null || snapName == null){
            return null;
        }

        VirtualMachineSnapshotTree[] snapTree = vm.getSnapshot().getRootSnapshotList();
        //getRootSnapshotList consists of an array of VirtualMachineSnapshotTree data objects. Each element in the array contains a snapshot property 
        //that is the VirtualMachineSnapshot managed object reference for that snapshot.

        if(snapTree!=null){
            ManagedObjectReference mor = findSnapshotInTree(snapTree, snapName);
            if(mor!=null){
                return new VirtualMachineSnapshot(vm.getServerConnection(), mor);
            }
        }
        return null;
    }

    /** 
    *Ricerca ricorsiva della VirtualMachine managed object reference relativa al snapName fornito nel snapshots 
     * tree relativo al vm anch'essa fornita
     * @param snapTree Snapshot tree of the Virtual Machine 
     * @param snapName Name of the snapshot to find
     * @return The object reference of the snapshot node
     * @throws Exception 
     */
    private ManagedObjectReference findSnapshotInTree(VirtualMachineSnapshotTree[] snapTree, String snapName) throws Exception{
        for(VirtualMachineSnapshotTree i : snapTree) {
            VirtualMachineSnapshotTree node = i;
            if(snapName.equals(node.getName())){
                return node.getSnapshot();
            } 
            else {
                VirtualMachineSnapshotTree[] childTree = node.getChildSnapshotList();
                if(childTree!=null){
                    ManagedObjectReference mor = findSnapshotInTree(childTree, snapName);
                    if(mor!=null){
                        return mor;
                    }
                }
            }
        }
        return null;
    }
    
    
    @Override
    /**
     * Delete all snapshot of a Virtual Machine
     * @param id Name of the Virtual Machibe
     * @return The state of the operation
     * @throws Exception 
     */
    public boolean deleteAllSnapshot(String id) throws Exception{
        //In VMWare is remove all snapshot operation
        
        //Per VMWare Server è possibile realizzare un solo snapshot per una VM percui tale metodo non sarà utilizzabile
        //a meno che si aggiorni VMWare da Server a Workstation o ESXi
        
        boolean status = false;            
        String vmname = id;
            
        try { 
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null) {
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            }
            
            Task task = vm.removeAllSnapshots_Task();
            String result = task.waitForTask();
            if(result.equals(Task.SUCCESS)){
              this.logger.info("Deleted all snapshot of VM: " + vmname);
              status = true;
            }
            else{ 
              this.disconnectVMWare(this.si);
              throw new SnapshotException("Task delete All snapshot VM "  + vmname + " failed: " + result );
            }            
            this.disconnectVMWare(this.si);
            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error deleteAllSnapshot VM: " +ex);
            throw ex;
        }
    }
    
    @Override
    /**
     * List all snapshot of a Virtual Machine
     * @param id Name of Virtual Machine
     * @return The list of snapshots
     * @throws Exception 
     */
    public List listSnapshot(String id) throws Exception {   
        ArrayList l = new ArrayList();
        String vmname = id;
            
        try { 
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null) {
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            }
            
            VirtualMachineSnapshotInfo snapInfo = vm.getSnapshot();
            VirtualMachineSnapshotTree[] snapTree = snapInfo.getRootSnapshotList();
            this.printSnapshots(snapTree,l);           
            
            this.disconnectVMWare(this.si);
            return l;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error ListSnapshot of VM: " +ex);
            throw ex;
        }
    }
    
    /**
     * Create a snapshot name list of a Virtual Machine
     * @param snapTree Snapshot Tree of Virtual Machine
     * @param l Return List
     */
    private void printSnapshots(VirtualMachineSnapshotTree[] snapTree, ArrayList l)
    {
      for (int i = 0; snapTree!=null && i < snapTree.length; i++) 
      {
        VirtualMachineSnapshotTree node = snapTree[i];
        l.add(node.getName());           
        VirtualMachineSnapshotTree[] childTree = node.getChildSnapshotList();
        if(childTree!=null)
        {
          this.printSnapshots(childTree,l);
        }
      }
    }
    
    @Override
    /**
     * Return current snapshot of a Virtual Machine
     * @param id Name of the Virtual Machine
     * @return String 
     * @throws Exception  
     */
    public String currentSnapshot(String id) throws Exception {
        String vmname = id;
        String currentSnap = null;    
        
        try { 
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null) {
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            }
            
            VirtualMachineSnapshotInfo snapInfo = vm.getSnapshot();
            currentSnap = snapInfo.getCurrentSnapshot().val;         
            
            this.disconnectVMWare(this.si);
            return currentSnap;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error currentSnapshot of VM: " +ex);
            throw ex;
        }
    }

    @Override
    /**
     * Return the number snapshots of the Virtual Machine
     * @param id Name of the Virtual Machine
     * @return long 
     * @throws Exception  
     */
    public long snapshotCount(String id) throws Exception {
        int count=0;
        String vmname = id;
            
        try { 
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null) {
                this.disconnectVMWare(this.si);
                throw new SnapshotException("Virtual Machine "  + vmname + " not found" );
            }
            
            VirtualMachineSnapshotInfo snapInfo = vm.getSnapshot();
            VirtualMachineSnapshotTree[] snapTree = snapInfo.getRootSnapshotList();
            this.SnapshotsCounting(snapTree,count);           
            
            this.disconnectVMWare(this.si);
            return count;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (SnapshotException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error snapshotCount of VM: " +ex);
            throw ex;
        }
    }

    /**
     * Performed the counting of the snapshots
     * @param snapTree
     * @param count Variable of Counting
     */
    private void SnapshotsCounting(VirtualMachineSnapshotTree[] snapTree, int count)
    {
      for (int i = 0; snapTree!=null && i < snapTree.length; i++) 
      {
        VirtualMachineSnapshotTree node = snapTree[i];
        count++;          
        VirtualMachineSnapshotTree[] childTree = node.getChildSnapshotList();
        if(childTree!=null)
        {
          this.SnapshotsCounting(childTree,count);
        }
      }
    }    
    
    @Override
    /**
     * Rename Virtual Machine
     * @param id Old name 
     * @param new_id New name
     * @return The state of the operation
     * @throws Exception 
     */
    public boolean renameVM(String id, String new_id) throws Exception {
        String vmname = id;
        String new_vmname = new_id;
        boolean status = false;
            
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new RenameException("VM "  + vmname + " not found.");                
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn)
            { 
                this.disconnectVMWare(this.si);
                throw new RenameException("Virtual Machine "  + vmname + " is stopped. Rename operation denied");
            } 
            else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOff){   
                Task task = vm.rename_Task(new_vmname);
                String result = task.waitForTask();
                if(result.equals(Task.SUCCESS)){
                    this.m.put(new_vmname, this.m.remove(vmname));//Rename Key from vmname to new_vmname into hashmap                    
                    this.logger.info("vm: " + vmname + " renamed in "+new_vmname); 
                    status = true;
                }
                else {
                    this.disconnectVMWare(this.si); 
                    throw new RenameException("Task rename VM "  + vmname + " failed: " + result);
                }                                
            }  
            
            this.disconnectVMWare(this.si);
            return status; 
        } catch (InvalidProperty ex) {            
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (RenameException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error renameVm:" +ex);
            throw ex;
        }         
    }

    @Override
    /**
     * Reset Virtual Machine in running
     * @param id Name of the Virtual Machine 
     * @return The state of the operation
     * @throws Exception 
     */
    public boolean resetVM(String id) throws Exception {
        String vmname = id;
        boolean status = false;
        
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new ResetException("VM "  + vmname + " not found.");                
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff)
            { 
                this.disconnectVMWare(this.si);
                throw new ResetException("Virtual Machine "  + vmname + " is stopped. Reset operation denied");
            } 
            else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOn){   
                Task task = vm.resetVM_Task();
                String result = task.waitForTask();
                if(result.equals(Task.SUCCESS)){
                    this.logger.info("vm: " + vm.getName() + " reseted."); 
                    status = true;
                }
                else {
                    this.disconnectVMWare(this.si); 
                    throw new ResetException("Task reset VM "  + vmname + " failed: " + result);
                }                                
            }  
            
            this.disconnectVMWare(this.si);
            return status; 
        } catch (InvalidProperty ex) {            
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (ResetException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error renameVm:" +ex);
            throw ex;
        }   
    }
    
    @Override
    /**
     * Return list of OS Guest
     */
    public List getOSTypes() {
        ArrayList l = new ArrayList();
        
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            ManagedEntity[] mes = this.rootFolder.getChildEntity(); //La classe Folder eredita la classe astratta ManagedEntity così come le
                                                           //le classi Datacenter e VirtualMachine
            for(Object i : mes){
                if(i instanceof Datacenter){
                    Datacenter dc = (Datacenter)i;
                    Folder vmFolder = dc.getVmFolder();
                    ManagedEntity[] vms = vmFolder.getChildEntity();       
                    for(ManagedEntity j : vms){
                        if(j instanceof VirtualMachine){
                            VirtualMachine vm = (VirtualMachine)j;
                            VirtualMachineConfigInfo vmci = (VirtualMachineConfigInfo)vm.getConfig(); 
                            l.add(vmci.getGuestFullName());
                        }
                    }
                }
            }
        
            this.disconnectVMWare(this.si);
            this.logger.info("getOSTypes returned: " + l.size() + "OS Guest");
            
        }catch (Exception ex) {            
            this.disconnectVMWare(this.si);
            this.logger.error("Error getOSTypes:" +ex);
        } 
        return l;
    }

    @Override
    /**
     * Allocate and attach port to virtual Machine
     * @param id Name of Virtual Machine
     */
    
    public boolean attachPortRemoteAccessVm(String id) throws Exception {
        String vmname = id;
        int porta;
        boolean status = false;
        
        try{
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            if(vm==null){
                this.disconnectVMWare(this.si);
                throw new Exception("VM "  + vmname + " not found.");                
            }
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime(); 
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn){ 
                this.logger.info("Virtual Machine " + vmname + "is poweredOn. Must be in powerOff state");
                this.disconnectVMWare(this.si);
                throw new Exception("Virtual Machine "  + vmname + " is poweredOn. Must be in powerOff state.");
            }            
                       
            //porta=this.attachPortRemoteAccessVm(vm);
            porta=this.attachPortRemoteAccessVm(vm);
            if(porta!=-1){
                this.m.get(vmname).getDescription().getDesktopVirtualization().setPort(Integer.toString(porta));
                status = true;
            }
            else{
                this.logger.error("Attach port to VM "+vmname+" failed (Remote Access VM)");   
                status = false;
            } 
            
            this.disconnectVMWare(this.si);            
        } catch (NullPointerException ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("VM "+vmname+ "doesn't exist: " +ex);            
        }catch(Exception ex){
            this.disconnectVMWare(this.si);
            this.logger.error("Error attachPortRemoteAccessVm: " +ex);
            throw ex;
    	}
        return status;
    }

    /**Aggiornamento file .vmx per l'abilitazione del vnc server aggendo mediante il task di reconfiguration vm fornito dalle api vijava
     *
     * @param vm
     * @return
     */
    private int attachPortRemoteAccessVm(VirtualMachine vm){
        int porta = -1;
        try{
            porta = this.InvokeMethodGetPortOfNetworkManagerAgent();
            if (porta == -1) {
                logger.error("No more port free");
            } else { 
                this.vncActivateVm(vm, porta, this.m.get(vm.getName()).getDescription().getDesktopVirtualization().getVmVNCPassword());
            }
        }catch(Exception ex){
            porta=-1;
        }
        return porta;
    }

    /** Aggiornamento file .vmx per l'abilitazione del vnc server aggendo direttamente sul file
     * Allocate and Attach port to virtual Machine. Write the configuration file .vmx for to performance the work.
     * @param vm Reference Object VMWare to VirtualMachine
     * @return Number port allocate by PortVMScheduler. Otherwise -1 if work failed or no more port free
     */
//    private int attachPortRemoteAccessVm(VirtualMachine vm) throws InvalidProperty, RuntimeFault, RemoteException {
//        int porta = -1;
//        //Prelievo il path assoluto del datastore. Es. "/home/utente/vmware/VirtualMachins"
//        Datastore[] datastore = vm.getDatastores();
//        DatastoreInfo di = datastore[0].getInfo(); //La VM ha un solo datastore
//        String datastorePath = di.getUrl();
//        //String datastorePath = "/home/giovalenti/NetBeansProjects/VirtualMachine";
//        //Prelevo il path assoluto del file di configuraione della VM. Es. "[datastore] Ubuntu/ubuntu.vmx"
//        VirtualMachineSummary summary = (VirtualMachineSummary) (vm.getSummary());
//        VirtualMachineConfigSummary vmcs = summary.getConfig();
//        String PathConfigFileVM = vmcs.getVmPathName();
//        //Elimino la substring "[datastore] "
//        String[] v = PathConfigFileVM.split("]");
//        String path_vmx = v[1].substring(1);
//        String path_absolute_vmx = datastorePath + File.separatorChar + path_vmx;
//
//        try {
//            porta = this.InvokeMethodGetPortOfNetworkManagerAgent();
//            if (porta == -1) {
//                logger.error("No more port free");
//            } else {
//                if (this.HypervisorHosted) {
//                    this.updatePortVmConfigurationFile(path_absolute_vmx, porta, this.m.get(vm.getName()).getDescription().getDesktopVirtualization().getVmVNCPassword());
//                } else if (this.HypervisorNative) {
//                    String localpath = System.getProperty("user.dir"); //local path in cui è stato copiato di file
//                    //Devo utilizzare il VFS Common sito nello storage manager (Fatto da Giancarlo)
//                    //Cp del file .vmx dall'hypervisor ESXi remoto a locale sulla dir localpath (mediante l'image manager)
//                    this.InvokeMethodCpOfImageManager(localpath, "copyTo", path_absolute_vmx, "");
//                    //aggiorno il file appena copiato
//                    this.updatePortVmConfigurationFile(localpath + "/" + vm.getName() + ".vmx", porta, this.m.get(vm.getName()).getDescription().getDesktopVirtualization().getVmVNCPassword());
//                    //cp al contrario per aggiornare così il file remoto (sempre mediante l'image manager)
//                    this.InvokeMethodCpOfImageManager(localpath + "/" + vm.getName() + ".vmx", "copyFrom", "", "");
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error Open File " + path_absolute_vmx + ": " + e);
//            porta = -1;
//        }
//
//        return porta;
//    }
          
    /**
     * Alter the number of port VNC into configuration file .vmx
     * @param filename Configuration file .vmx
     * @param new_port New port to set into configuration file
     * @throws IOException 
     */
    private void updatePortVmConfigurationFile(String file_vmx_vm, int port, String password) throws IOException{
        
            String s;
            String file_name_temp="./temp_file_vmx";
            BufferedReader reader;
            FileOutputStream file_out;
            PrintStream Output;

            //Remove old VNC params
            reader = new BufferedReader(new FileReader(file_vmx_vm));
            file_out = new FileOutputStream(file_name_temp);
            Output = new PrintStream(file_out);

            while( (s = reader.readLine()) != null ){
                if(!s.startsWith("RemoteDisplay.vnc.port") && !s.startsWith("RemoteDisplay.vnc.enabled") && !s.startsWith("RemoteDisplay.vnc.password") )
                    Output.println(s);
            }

            reader.close();
            file_out.close();
            Output.close();

            //Sovrascivo per intero il file originale con quello temporale
            File file_temp = new File(file_name_temp);
            FileReader fr_temp = new FileReader(file_temp);
            reader = new BufferedReader(fr_temp);

            file_out = new FileOutputStream(file_vmx_vm);
            Output = new PrintStream(file_out);

            while( (s = reader.readLine()) != null )
                Output.println(s);

            if(!file_temp.delete())//destroy file temp
                this.logger.warn("Update port operation into configuration file "+file_vmx_vm+". Delete file "+file_name_temp+": deletion failed");

            reader.close();
            fr_temp.close();
            Output.close();
            file_out.close();

            //Add new VNC params in tail
            FileOutputStream file = new FileOutputStream(file_vmx_vm,true);
            PrintStream Output2 = new PrintStream(file);
            Output2.println("RemoteDisplay.vnc.enabled = \"TRUE\"");
            Output2.println("RemoteDisplay.vnc.port = \""+port+"\"");
            Output2.println("RemoteDisplay.vnc.password = \""+password+"\"");
            Output2.close();
            file.close();
    }
    
    @Override
    /**
     * Release the port for a Virtual Machine into structure Clever. It doesn't alter 
     * the configuration file of virtual machine (.vmx)
     * @param id Name of virtual machine
     */
    public void releasePortRemoteAccessVm(String id) {
        VMWrapper vmwrapper = this.m.get(id);
        vmwrapper.getDescription().getDesktopVirtualization().setPort(Integer.toString(-1));
    }

    /**
     * Get port from Agent PortScheduler by Module Communicator
     * @return Number of port
     */
    private int InvokeMethodGetPortOfNetworkManagerAgent(){
        int porta=-1; 
        params = new ArrayList();
        params.add(new Integer(this.inf_port));
        params.add(new Integer(this.sup_port));
        params.add(this.ipHypervisor);
        try{
            porta=(Integer)this.owner.invoke("NetworkManagerAgent", "getPort", true, params);
        }catch(CleverException ex){
            this.logger.error("Method Invoker o Module Communicator returned result CleverException:" +ex);
        }
        return porta;
    }
    
    /**
     * Test if a port is busy from Agent PortScheduler by Module Communicator
     * @param port_present Number port test
     * @return Return true if port is busy otherwise return false
     */
    private boolean InvokeMethodIsPortBusyOfNetworkManagerAgent(int port_present){
        boolean status=false;
        params = new ArrayList();
        params.add(new Integer(port_present));
        params.add(this.ipHypervisor);
        try{            
            status = (Boolean)this.owner.invoke("NetworkManagerAgent", "isPortBusy", true, params);         
        }catch(CleverException ex){
            this.logger.error("Method Invoker of Module Communicator returned result CleverException: "+ex);
        }
        return status;
    }

    
    @Override
    /**
     * Register new Virtual Machine into Hypervisor's Inventory
     * @parm id Name of virtual machine
     * @param physicalPath Local path of the virtual machine folder
     * @return Return true if the virtual machine was Sucessfully registered into datastore otherwise return false
     */
    public boolean registerVm(String id, String physicalPath) throws Exception {
        boolean status=false;
        String vmname = id;
        
        //Di tutto il path prendo solo l'ultima dir (uuid della cartella all'interno della quale creo la virtual machine
        StringTokenizer st = new StringTokenizer(physicalPath,"/");
        while(st.hasMoreTokens())
            physicalPath=st.nextToken();

        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            Datacenter dc = (Datacenter) new InventoryNavigator(this.rootFolder).searchManagedEntity("Datacenter", this.datacenter);
            ResourcePool rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntities("ResourcePool")[0];
            Folder vmFolder = dc.getVmFolder();

            Task task = vmFolder.registerVM_Task("["+ this.datastore +"] "+physicalPath+"/"+vmname+".vmx", vmname, false, rp, null);
            String result = task.waitForTask();
            if(result.equals(Task.SUCCESS)){
                this.logger.info("vm: "+vmname+" registered into Hypervisor's Inventory.");
                //Add new virtual machine into local cache
                VMWrapper wrap = this.createVMwrapper(id);
                this.m.put(id, wrap);                
            }
            else {
                this.disconnectVMWare(this.si);
                throw new DestroyException("Task register VM "  + vmname + " into Hypervisor's Inventory failed: " + result);
            }

            this.disconnectVMWare(this.si);
            this.resetLocationVm(vmname);
            status=true;

            //Notify registerd/imported VM timestamp to Virtualization Manager (format: vmname)
            this.NotifyToVirtualizationManager(vmname, this.notificationImportedVm);

            return status;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        } catch (DestroyException ex) {
            disconnectVMWare(this.si);
            this.logger.error("Error registerVm:" +ex);
            throw ex;
        }

    }

    /**
     * Reset the location params of file .vmx because the vm was imported
     * @param vmname name of virtual machine
     * @throws Exception
     */
    private void resetLocationVm(String vmname) throws Exception{
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            //Devo settare l'eventuale campo uuid.location del file .vmx a null (""). In tal modo sarà il futuro poweron a settarlo al corretto
            //valore. Questo perché la VM è stata importata dall'esterno e presentava l'uuid della sua vecchia locazione
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmname);
            VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
            vmConfigSpec.locationId="";
            Task task = vm.reconfigVM_Task(vmConfigSpec);
            task.waitForTask();
            this.disconnectVMWare(this.si);
        } catch (InterruptedException ex) {
            throw ex;
        } catch (MalformedURLException ex) {
            throw ex;
        } catch (InvalidProperty ex) {
            throw ex;
        } catch (RuntimeFault ex) {
            throw ex;
        } catch (RemoteException ex) {
            throw ex;
        }


    }

    @Override
    /**
     * unRegister a Virtual Machine from Hypervisor's Inventory
     * @parm id Name of virtual machine
     * @return Return true if the virtual machine was Sucessfully unregistered into datastore otherwise return false
     */
    public boolean unregisterVm(String id) throws Exception {
        boolean status=false;
        String vmname = id;
        try {
            this.si = this.connectVMWare(this.url, this.user, this.password);
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmname);
            this.m.remove(vm.getName());
            vm.unregisterVM();

            this.disconnectVMWare(this.si);
            status=true;
            return status;
        } catch (InvalidProperty ex) {
            this.logger.error("Error unregisterVm:" +ex);
            throw ex;
        } catch (RuntimeFault ex) {
            this.logger.error("Error unregisterVm:" +ex);
            throw ex;
        } catch (RemoteException ex) {
            this.logger.error("Error unregisterVm:" +ex);
            throw ex;
        }
    }

    private void vncActivateVm(VirtualMachine Vm, int vncPort, String vncPassword) {
        try {
            //Extraconfig setting :
            // http://blogs.vmware.com/vipowershell/2008/09/changing-vmx-fi.html
            // Extraconfig gebruik : http://sourceforge.net/projects/vijava/forums/forum/826592/topic/3756870?message=8491628
            //               RemoteDisplay.vnc.enabled = "TRUE"
            //               RemoteDisplay.vnc.password = "your_password"
            //               RemoteDisplay.vnc.port = "5900"
            OptionValue vnc1 = new OptionValue();
            vnc1.setKey("RemoteDisplay.vnc.enabled");
            vnc1.setValue("TRUE");
            OptionValue vnc2 = new OptionValue();
            vnc2.setKey("RemoteDisplay.vnc.password");
            vnc2.setValue(vncPassword);
            OptionValue vnc3 = new OptionValue();
            vnc3.setKey("RemoteDisplay.vnc.port");
            vnc3.setValue(String.valueOf(vncPort));
            OptionValue[] extraConfig = {vnc1, vnc2, vnc3};
            // Need to get the old extraconfig + add new stuff
            // Default VNC is not enabled in ESX
            // Option 1-> use the ssh tunnel
            // Option 2-> start the vnc stuff
            //http://www.novell.com/communities/node/6544/launch-remote-console-vms-esx-server-platespin-orchestrator-without-installing-vnc-server-
            //esxcfg-firewall -e vncServer
            //Currently VMware supports 5901 - 5964 ports. It means at a time we can launch remote consoles for 65 VMs.
            VirtualMachineConfigSpec vmConfigSpec2 = new VirtualMachineConfigSpec();
            vmConfigSpec2.setExtraConfig(extraConfig);
            Task vnctask = Vm.reconfigVM_Task(vmConfigSpec2);
            vnctask.waitForTask();
        } catch (InterruptedException ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (InvalidName ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (VmConfigFault ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (DuplicateName ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (TaskInProgress ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (FileFault ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (InvalidState ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (ConcurrentAccess ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (InvalidDatastore ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (InsufficientResourcesFault ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (RuntimeFault ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        } catch (RemoteException ex) {
            this.logger.error("Enable VNC operation for Virtual Machine "+Vm.getName()+ "faild: "+ex);
        }

    }


    @Override
    /** Generate OVF file and image disk .vmdk
     * @param id Univocal Virtual Machine name
     * @param TargetPhysicalPath  path when vm will be exported
     */
    public boolean ExportOvfToLocal(String id, String TargetPhysicalPath) throws Exception {
        //For exportOvf and ImportOvf methods
        LeaseProgressUpdater leaseProgUpdater;
        String vmname = id;
        boolean status=false;
        try{
            this.si = this.connectVMWare(this.url, this.user, this.password);

	    InventoryNavigator iv = new InventoryNavigator(this.si.getRootFolder());

	    HttpNfcLease hnLease = null;

	    ManagedEntity me = iv.searchManagedEntity("VirtualMachine", vmname);
	    hnLease = ((VirtualMachine)me).exportVm();

	    // Wait until the HttpNfcLeaseState is ready
	    HttpNfcLeaseState hls;
	    for(;;) {
	      hls = hnLease.getState();
	      if(hls == HttpNfcLeaseState.ready) {
	        break;
	      }
	      if(hls == HttpNfcLeaseState.error)
	        throw new Exception();
	    }

	   // System.out.println("HttpNfcLeaseState: ready ");
	    HttpNfcLeaseInfo httpNfcLeaseInfo = hnLease.getInfo();
	    httpNfcLeaseInfo.setLeaseTimeout(300*1000*1000);

	    //Note: the diskCapacityInByte could be many time bigger than
	    //the total size of VMDK files downloaded.
	    //As a result, the progress calculated could be much less than reality.
	    long diskCapacityInByte = (httpNfcLeaseInfo.getTotalDiskCapacityInKB()) * 1024;

	    leaseProgUpdater = new LeaseProgressUpdater(hnLease, 5000);
	    leaseProgUpdater.start();

	    long alredyWrittenBytes = 0;
	    HttpNfcLeaseDeviceUrl[] deviceUrls = httpNfcLeaseInfo.getDeviceUrl();
	    if (deviceUrls != null) {
	      OvfFile[] ovfFiles = new OvfFile[deviceUrls.length];
	     // System.out.println("Downloading Files:");
	      for (int i = 0; i < deviceUrls.length; i++) {
	        String deviceId = deviceUrls[i].getKey();
	        String deviceUrlStr = deviceUrls[i].getUrl();
	        String diskFileName = deviceUrlStr.substring(deviceUrlStr.lastIndexOf("/") + 1);
	        String diskUrlStr = deviceUrlStr.replace("*", this.ipHypervisor);
	        String diskLocalPath = TargetPhysicalPath + diskFileName;
	        String cookie = this.si.getServerConnection().getVimService().getWsc().getCookie();
	        long lengthOfDiskFile = this.writeVMDKFile(leaseProgUpdater, diskLocalPath, diskUrlStr, cookie, alredyWrittenBytes, diskCapacityInByte);
	        alredyWrittenBytes += lengthOfDiskFile;
	        OvfFile ovfFile = new OvfFile();
	        ovfFile.setPath(diskFileName);
	        ovfFile.setDeviceId(deviceId);
	        ovfFile.setSize(lengthOfDiskFile);
	        ovfFiles[i] = ovfFile;
	      }

	      OvfCreateDescriptorParams ovfDescParams = new OvfCreateDescriptorParams();
	      ovfDescParams.setOvfFiles(ovfFiles);
	      OvfCreateDescriptorResult ovfCreateDescriptorResult = this.si.getOvfManager().createDescriptor(me, ovfDescParams);

	      String ovfPath = TargetPhysicalPath + vmname + ".ovf";
	      FileWriter out = new FileWriter(ovfPath);
	      out.write(ovfCreateDescriptorResult.getOvfDescriptor());
	      out.close();
	      //System.out.println("OVF Desriptor Written to file: " + ovfPath);
	    }

	    //System.out.println("Completed Downloading the files");
	    leaseProgUpdater.interrupt();
	    hnLease.httpNfcLeaseProgress(100);
	    hnLease.httpNfcLeaseComplete();

	    this.disconnectVMWare(this.si);

            status=true;
        }catch (Exception ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error ExportOvfToLocal:" +ex);
        }
        return status;
    }

    private long writeVMDKFile(LeaseProgressUpdater leaseProgUpdater, String localFilePath, String diskUrl, String cookie, long bytesAlreadyWritten, long totalBytes) throws IOException {
	HttpsURLConnection conn = this.getHTTPConnection(diskUrl, cookie);
	InputStream in = conn.getInputStream();
	OutputStream out = new FileOutputStream(new File(localFilePath));
	byte[] buf = new byte[102400];
	int len = 0;
	long bytesWritten = 0;
	while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
		bytesWritten += len;
		int percent = (int)(((bytesAlreadyWritten + bytesWritten) * 100) / totalBytes);
		leaseProgUpdater.setPercent(percent);
		//System.out.println("written: " + bytesWritten);
	}
	in.close();
	out.close();
	return bytesWritten;
    }

    private HttpsURLConnection getHTTPConnection(String urlStr, String cookieStr) throws IOException {
	HostnameVerifier hv = new HostnameVerifier() {
		public boolean verify(String urlHostName, SSLSession session) {
			return true;
		}
	};
	HttpsURLConnection.setDefaultHostnameVerifier(hv);
	URL url = new URL(urlStr);
	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	conn.setDoInput(true);
	conn.setDoOutput(true);
	conn.setAllowUserInteraction(true);
	conn.setRequestProperty("Cookie",cookieStr);
	conn.connect();
	return conn;
    }

    @Override
    /**
     * Import OVF file and its image disk .vmdk for install vm into Hypervisor
     * @param id Name of virtual machine will be install
     * @param OVF_physicalPath physical path path of the OVF file
     */
    public boolean ImportLocalOvf(String id, String OVF_physicalPath) throws Exception {
        LeaseProgressUpdater leaseUpdater;
        String vmname = id;
        boolean status=false;
        try{
            this.si = this.connectVMWare(this.url, this.user, this.password);

            HostSystem host = null;
            Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter",this.datacenter);

            ManagedEntity[] hosts= new InventoryNavigator(dc.getHostFolder()).searchManagedEntities("HostSystem");

            host= (HostSystem) hosts[0]; //Hypervisor with only one HostSystem

	    Datastore ds = null;
	    Datastore[] dss= host.getDatastores();
            for(int k=0; k<dss.length; k++) {
                if(dss[k].getName().equals(this.datastore)) {
                    ds=dss[k];
		}
            }

            Folder vmFolder = (Folder) host.getVms()[0].getParent();

            OvfCreateImportSpecParams importSpecParams = new OvfCreateImportSpecParams();
            importSpecParams.setHostSystem(host.getMOR());
            importSpecParams.setLocale("US");
            importSpecParams.setEntityName(vmname);
            importSpecParams.setDeploymentOption("");
            OvfNetworkMapping networkMapping = new OvfNetworkMapping();
            networkMapping.setName("Network 1");
            networkMapping.setNetwork(host.getNetworks()[0].getMOR()); // network);
            importSpecParams.setNetworkMapping(new OvfNetworkMapping[] { networkMapping });
            importSpecParams.setPropertyMapping(null);

            String ovfDescriptor = this.readOvfContent(OVF_physicalPath);
            if (ovfDescriptor == null)
               throw new Exception();

            ovfDescriptor = escapeSpecialChars(ovfDescriptor);
            //System.out.println("ovfDesc:" + ovfDescriptor);

            ResourcePool rp = ((ComputeResource)host.getParent()).getResourcePool();

            OvfCreateImportSpecResult ovfImportResult = this.si.getOvfManager().createImportSpec(ovfDescriptor, rp, ds, importSpecParams);

            if(ovfImportResult==null)
		throw new Exception();

            long totalBytes = this.addTotalBytes(ovfImportResult);
            //System.out.println("Total bytes: " + totalBytes);

            HttpNfcLease httpNfcLease = null;

	    httpNfcLease = rp.importVApp(ovfImportResult.getImportSpec(), vmFolder, host);

            // Wait until the HttpNfcLeaseState is ready
            HttpNfcLeaseState hls;
            for(;;) {
                hls = httpNfcLease.getState();
		if(hls == HttpNfcLeaseState.ready || hls == HttpNfcLeaseState.error)
                    break;
            }

            if (hls.equals(HttpNfcLeaseState.ready)) {
                //System.out.println("HttpNfcLeaseState: ready ");
                HttpNfcLeaseInfo httpNfcLeaseInfo = (HttpNfcLeaseInfo) httpNfcLease.getInfo();
                //printHttpNfcLeaseInfo(httpNfcLeaseInfo);

		leaseUpdater = new LeaseProgressUpdater(httpNfcLease, 5000);
		leaseUpdater.start();

		HttpNfcLeaseDeviceUrl[] deviceUrls = httpNfcLeaseInfo.getDeviceUrl();

		long bytesAlreadyWritten = 0;
		for (HttpNfcLeaseDeviceUrl deviceUrl : deviceUrls) {
                    String deviceKey = deviceUrl.getImportKey();
		    for (OvfFileItem ovfFileItem : ovfImportResult.getFileItem()) {
                        if (deviceKey.equals(ovfFileItem.getDeviceId())) {
                            //System.out.println("Import key==OvfFileItem device id: " + deviceKey);
                            String absoluteFile = new File(OVF_physicalPath).getParent() + File.separator + ovfFileItem.getPath();
                            String urlToPost = deviceUrl.getUrl().replace("*", this.ipHypervisor);
                            this.uploadVmdkFile(leaseUpdater, ovfFileItem.isCreate(), absoluteFile, urlToPost, bytesAlreadyWritten, totalBytes);
                            bytesAlreadyWritten += ovfFileItem.getSize();
                            //System.out.println("Completed uploading the VMDK file:" + absoluteFile);
                        }
                    }
                }

                leaseUpdater.interrupt();
                httpNfcLease.httpNfcLeaseProgress(100);
                httpNfcLease.httpNfcLeaseComplete();
            }

            status=true;
        }catch (Exception ex) {
            this.disconnectVMWare(this.si);
            this.logger.error("Error ExportOvfToLocal:" +ex);
        }
        return status;
    }

    private long addTotalBytes(OvfCreateImportSpecResult ovfImportResult) {
        OvfFileItem[] fileItemArr = ovfImportResult.getFileItem();

	long totalBytes = 0;
	if (fileItemArr != null) {
            for (OvfFileItem fi : fileItemArr) {
                //printOvfFileItem(fi);
                totalBytes += fi.getSize();
            }
	}
	return totalBytes;
    }

    private String escapeSpecialChars(String str) {
        str = str.replaceAll("<", "&lt;");
	return str.replaceAll(">", "&gt;"); // do not escape "&" -> "&amp;", "\"" -> "&quot;"
    }

    private String readOvfContent(String ovfFilePath) throws IOException {
        StringBuffer strContent = new StringBuffer();
	BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(ovfFilePath)));
	String lineStr;
	while ((lineStr = in.readLine()) != null) {
            strContent.append(lineStr);
	}
	in.close();
	return strContent.toString();
    }

    private void uploadVmdkFile(LeaseProgressUpdater leaseUpdater, boolean put, String diskFilePath, String urlStr, long bytesAlreadyWritten, long totalBytes) throws IOException {
        final int CHUCK_LEN = 64 * 1024;
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
        {
            public boolean verify(String urlHostName, SSLSession session)
            {
                return true;
            }
        });

        HttpsURLConnection conn = (HttpsURLConnection) new URL(urlStr).openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setChunkedStreamingMode(CHUCK_LEN);
        conn.setRequestMethod(put? "PUT" : "POST"); // Use a post method to write the file.
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "application/x-vnd.vmware-streamVmdk");
        conn.setRequestProperty("Content-Length", Long.toString(new File(diskFilePath).length()));

        BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());

        BufferedInputStream diskis = new BufferedInputStream(new FileInputStream(diskFilePath));
        int bytesAvailable = diskis.available();
        int bufferSize = Math.min(bytesAvailable, CHUCK_LEN);
        byte[] buffer = new byte[bufferSize];

        long totalBytesWritten = 0;
        while (true)
        {
            int bytesRead = diskis.read(buffer, 0, bufferSize);
            if (bytesRead == -1)
            {
                //System.out.println("Total bytes written: " + totalBytesWritten);
                break;
            }

            totalBytesWritten += bytesRead;
            bos.write(buffer, 0, bufferSize);
            bos.flush();
            //System.out.println("Total bytes written: " + totalBytesWritten);
            int progressPercent = (int) (((bytesAlreadyWritten + totalBytesWritten) * 100) / totalBytes);
            leaseUpdater.setPercent(progressPercent);
        }

        diskis.close();
        bos.flush();
        bos.close();
        conn.disconnect();
    }

    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public boolean saveState(String id, String path) throws Exception{
        throw new UnsupportedOperationException("Not supported yet.");    
    }
        
    @Override
    public boolean cloneVM(String id, String clone, String description) throws Exception {
        //In VMWare Server la cloneVM operation non è supportata. Bisognerebbe passare a VMWare Workstation
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOwner(Agent owner) {
       this.owner=owner;
    }

    @Override
    public String getLocalPath(String id) throws HyperVisorException{
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean attackInterface(String id, String inf, String mac, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
