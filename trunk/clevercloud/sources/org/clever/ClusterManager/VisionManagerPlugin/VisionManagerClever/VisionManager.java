/*
 * The MIT License
 *
 * Copyright 2012 s89.
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
package org.clever.ClusterManager.VisionManagerPlugin.VisionManagerClever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.VisionManager.VisionManagerPlugin;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.VisionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;





/**
 *
 * @author s89
 */
class ClusterBuildThread extends VisionManager implements Runnable 
{   private String  name_cl;
    private int n_vms;
    private int n_netinterface;
    private String gold_name;
    private String name_bridge;
    private String name_vm;
    private List VisionVmInfo;
    private int n_cl;
    private String ip;
    private String staticIp;
    private String mac;
    private Element net_info;
    private boolean ipstatic; 
    private String name_cluster;
    
    public ClusterBuildThread(String name_cluster,int n_cl,String cl,int n_vms,int n_netinterface,String gold_name,String name_bridge,Logger l,String ip,Agent owner,Element net_info,Boolean ipstatic)
    {   this.gold_name=gold_name;
        this.n_vms=n_vms;
        this.n_netinterface=n_netinterface;
        this.name_cl=cl;
        this.name_bridge=name_bridge;
        logger=l;
        this.n_cl=n_cl;
        this.ip=ip;
        this.owner=owner;
        this.net_info=net_info;
        this.ipstatic=ipstatic;
        this.name_cluster=name_cluster;
    }
    @Override
    public void run(){
      
     
     
    int i,j;
    List vms=new ArrayList();
    List params=new ArrayList();
    String nameserver="";
    for(i=0;i<n_vms;i++){
        name_vm=name_cl+n_cl+"."+(i+1);
        VisionVmInfo vmi=new VisionVmInfo(name_vm);
        params.add(gold_name);
        params.add(name_vm);
        params.add("");
        params.add(name_cl);
        try {
             this.owner.invoke( "VirtualizationManagerAgent","takeEasySnapshot", true, params);
             params.clear();
             logger.info("vm "+name_vm+" created");
             for(j=0;j<n_netinterface;j++){
                 params.add(name_vm);
                 params.add(name_bridge);
                 mac=macCalculator(n_cl,i+1,j);
                 params.add(mac);
                 params.add("bridge");
                 this.owner.invoke("VirtualizationManagerAgent","attachInterface", true, params);
                 params.clear();
                 String cfgIp=macFileConfigurator(net_info,ip,mac,j,ipstatic);
                  if(ipstatic)
                 nameserver=net_info.getChildText("dns");
                 params.add(name_vm);
                 params.add(cfgIp);
                 params.add(nameserver);
                 params.add(j);
                 params.add(ipstatic);
                 logger.debug("salvo89 prima di staticIpConfigurator");
                 this.owner.invoke("VirtualizationManagerAgent","staticIpConfigurator", true, params);
                 params.clear();
                 logger.info("interface number "+(j+1)+"attached");
                 vmi.addNetInterface(ip,mac);
                 if(ipstatic)
                     ip=IpManager.getNextIp(ip);
                 
                }
                //String cfgIp=staticIpToString(net_info,vmi.getIp(),n_netinterface);
                //String nameserver=net_info.getChildText("dns");
                //params.add(name_vm);
                //params.add(cfgIp);
                //params.add(nameserver);
                //this.owner.invoke("VirtualizationManagerAgent","staticIpConfigurator", true, params);
                //params.clear();
                vms.add(vmi); 
                addVmDb(name_cluster,vmi);
                logger.info("vm "+name_vm+" succesfully setted");
                 
                
             
        }catch (CleverException ex) {
                logger.error("Error", ex);
             }       
        }
        
                
         }
       
    }




public class VisionManager implements VisionManagerPlugin {
    Agent owner;
    Logger logger;
    private Document doc;
    private String cfgpath;
    private String name_cluster;
   
     
    
    public VisionManager(){
       logger = Logger.getLogger("VisionManagerPlugin");
       cfgpath=System.getProperty("user.dir")+"vision/vision.cfg";
       logger.debug("Vision plugin created!");  
    }
    
    public static String macCalculator(int cl, int host,int inter){
    
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s%s",52,":"));
    sb.append(String.format("%s%s",52,":"));
    sb.append(String.format("%02x%s",inter,":"));
    sb.append(String.format("%01x",cl));
    sb.append(String.format("%01x%s",0,":"));
    sb.append(String.format("%02x%s",0,":"));
    sb.append(String.format("%02x",host));
    return sb.toString();
    }
    public boolean clusterBuild(Document c_info,Document net_info,Boolean ipstatic) throws VisionException, CleverException{
        List thread=new ArrayList();
        Element n_info=null;
        String ip="";
        if(ipstatic){
        n_info=net_info.getRootElement();
        IpManager.setMaxIp(n_info.getChildText("max_address"));
        IpManager.setMinIp(n_info.getChildText("min_address"));
        IpManager.setCurrentIp(n_info.getChildText("min_address"));
        }
        
        Element root=c_info.getRootElement();
        List  clusters=root.getChildren("cluster");
        Iterator it=clusters.iterator();
        while (it.hasNext()){
         Element cluster=(Element)it.next();
         name_cluster=cluster.getChildText("name_cluster");
         String gold_image= cluster.getChild("golden_image").getText();
         addClusterDb(name_cluster,gold_image);
         List hosts=cluster.getChildren("host");
         Iterator it1=hosts.iterator();
          while (it1.hasNext()){
            Element host=(Element)it1.next();    
            int n_vms=Integer.parseInt(host.getChild("n_vms").getText());
            int n_cluster=Integer.parseInt(host.getChild("n_host").getText());
            String name=host.getChild("name").getText();
            int n_interfaces=Integer.parseInt(host.getChild("n_interfaces").getText());
            String name_bridge=host.getChild("name_bridge").getText();
            if(ipstatic)
               ip=IpManager.getBlockIp(n_vms*n_interfaces);
            ClusterBuildThread r= new ClusterBuildThread(name_cluster,n_cluster,name,n_vms,n_interfaces,gold_image,name_bridge,logger,ip,this.owner,n_info,ipstatic);
            Thread t=new Thread(r);
            t.start();
            thread.add(t);
            }
        }
        Iterator iterator = thread.iterator();
        while(iterator.hasNext())
          try {
            ((Thread)iterator.next()).join();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(VisionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    
    public void setOwner(Agent owner) {
        this.owner=owner;
    }
    
    
   

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(Element params, Agent owner) throws CleverException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /*public boolean addClusterDb(String nameC,String image,List vms) throws CleverException{
     
      String stringafinale;
      VisionVmInfo vm;
      List params=new ArrayList();
      String iniziale="";
      params.add("VisionManagerAgent");
      params.add("/Clusters/cluster"); 
      boolean r= (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
      
      if(!r){
        
        String node="<Clusters/>";
        params = new ArrayList();
        params.add("VisionManagerAgent");
        params.add(node);
        params.add("into");
        params.add(""); 
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
     }
      params.clear();
      Iterator it=vms.iterator();
      
      while(it.hasNext()){
          
          vm=(VisionVmInfo)it.next();
          iniziale=iniziale+vm.toXml();
                  
      }
      stringafinale="<cluster>"
                        +"<name>"+nameC+"</name>"
                        +"<vms image=\""+image+"\">"
                            +iniziale
                        +"</vms>"
                    +"</cluster>";  
      params.add("VisionManagerAgent");
      params.add(stringafinale);
      params.add("into");
      params.add("/Clusters");
      this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
      logger.info("cluster added to database");
      return true;
    }*/
    public boolean addVmDb(String nameC,VisionVmInfo vmi) throws CleverException{
      List params= new ArrayList();
      params.add("VisionManagerAgent");
      params.add(vmi.toXml());
      params.add("into");
      params.add("/Clusters/cluster[./name/text()='"+nameC+"']/vms");
      this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
      logger.info("cluster added to database");
      return true;
    }
    
    public boolean addClusterDb(String nameC,String image) throws CleverException{
     
      String stringafinale;
      VisionVmInfo vm;
      List params=new ArrayList();
      String iniziale="";
      params.add("VisionManagerAgent");
      params.add("/Clusters/cluster[./name/text()='"+nameC+"']/vms"); 
      boolean r= (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
      
      if(!r){
        
        String node="<Clusters>"+
                         "<cluster>"+
                              "<name>"+nameC+"</name>"+
                              "<vms image=\""+image+"\">"+"</vms>"+
                         "</cluster>"+
                    "</Clusters>";
        params = new ArrayList();
        params.add("VisionManagerAgent");
        params.add(node);
        params.add("into");
        params.add(""); 
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        
     }
      return true;
    } 
    
    public boolean delVmRunning(String vmname) throws CleverException{
     List params=new ArrayList();
     params.add("VisionManagerAgent");
     params.add("/VMs_Running/VM[@name='"+vmname+"']");
     if((Boolean)this.owner.invoke("DatabaseManagerAgent", "existNode", true, params))
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
     
     return true;
 }
    
    public boolean delAllDb() throws CleverException{
     List params=new ArrayList();
     params.add("VisionManagerAgent");
     params.add("");
     this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
     logger.info("VisionManagerAgent deleted to database");
     return true;
 }
    
   /* public String staticIpToString(Element n_info,List ip,int n_interface){
        String result="";
        for (int i=0;i<n_interface;i++)
               result=result +"auto eth"+i
               +"\niface eth"+i+ " inet static"
               +"\naddress "+ip.get(i)
               +"\nnetmask "+n_info.getChildText("netmask")
               +"\nnetwork "+n_info.getChildText("network")
               +"\nbroadcast "+n_info.getChildText("broadcast")
               +"\ngateway "+n_info.getChildText("gateway")+"\n";
        return result;
       
    }*/
    
    
    @Override
    public boolean startVisionVm(String id) throws CleverException{
        List params=new ArrayList();
        params.add(id);
        boolean result=(Boolean)this.owner.invoke("VirtualizationManagerAgent","startVm", true, params);
        addVmRunning(id);
        logger.info(id+" started");
        return result;
       }
    
    public boolean startVisionCluster(String id) throws CleverException{
        List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster[./name/text()='"+id+"']/vms/vm/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          startVisionVm(st.nextToken());  
        }
        logger.info("cluster "+id+" started");
        return true;
    }
    
    public boolean startVisionClusters() throws CleverException{
        List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        logger.debug("salvo89 str "+result);
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          startVisionCluster(st.nextToken());
          logger.debug("salvo89 nel while");
        }
        logger.info("All clusters started");
        return true; 
    }
    public boolean addVmRunning(String vmname) throws CleverException{
     List params=new ArrayList();
     params.add("VisionManagerAgent");
     params.add("/VMs_Running"); 
     boolean b= (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
     if(!b){
        String node="<VMs_Running/>";
        params = new ArrayList();
        params.add("VisionManagerAgent");
        params.add(node);
        params.add("into");
        params.add(""); 
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
     }
     params.clear();
     String node="<VM name=\""+vmname+"\"/>";
     params.add("VisionManagerAgent");
     params.add(node);
     params.add("into");
     params.add("/VMs_Running");
     this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
            
     
     return true;
    }
    
    
    public boolean delVmDb(String id) throws CleverException{
        List params=new ArrayList();
     params.add("VisionManagerAgent");
     params.add("/Clusters/cluster/vms/vm[./name/text()='"+id+"']");
     this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
     return true;   
    }
    
    
    public boolean stopVisionVm(String id,Boolean poweroff) throws CleverException{
       List params=new ArrayList();
        params.add(id);
        params.add(poweroff);
        boolean result=(Boolean)this.owner.invoke("VirtualizationManagerAgent","stopVm", true, params);
        delVmRunning(id);
        logger.info(id+" stopped");
        return result;
    
}
    
    public boolean stopVisionCluster(String id,Boolean poweroff) throws CleverException{
     List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster[./name/text()='"+id+"']/vms/vm/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          stopVisionVm(st.nextToken(),poweroff);  
        }
        logger.info("cluster "+id+" stopped");
        return true;   
    }
    
    public boolean stopVisionClusters(Boolean poweroff) throws CleverException{
        List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          stopVisionCluster(st.nextToken(),poweroff);  
        }
        logger.info("All clusters stopped");
        return true; 
}
    
    
    public boolean stopVisionVm(String id) throws CleverException{
       List params=new ArrayList();
        params.add(id);
        boolean result=(Boolean)this.owner.invoke("VirtualizationManagerAgent","stopVm", true, params);
        delVmRunning(id);
        logger.info(id+" stopped");
        return result;
    
}
    
    public boolean stopVisionCluster(String id) throws CleverException{
     List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster[./name/text()='"+id+"']/vms/vm/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          stopVisionVm(st.nextToken());  
        }
        logger.info("cluster "+id+" stopped");
        return true;   
    }
    
    public boolean stopVisionClusters() throws CleverException{
        List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          stopVisionCluster(st.nextToken());  
        }
        logger.info("All clusters stopped");
        return true; 
}
    
    public boolean deleteVisionVm(String id) throws CleverException{
        List params=new ArrayList();
        delVmDb(id);
        params.add(id);
        boolean result=(Boolean)this.owner.invoke("VirtualizationManagerAgent","deleteVm",true,params);
        
        return result;
     }
    
    public boolean deleteVisionCluster(String id) throws CleverException{
        
     List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster[./name/text()='"+id+"']/vms/vm/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          deleteVisionVm(st.nextToken());  
        }
        logger.info("cluster "+id+" deleted");
        return true;   
        
    }
    
     public boolean deleteVisionClusters() throws CleverException{
        
        List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          deleteVisionCluster(st.nextToken());  
        }
        delAllDb();
        logger.info("All clusters deleted");
        return true; 
}
     public String listVisionClusterVm(String id) throws CleverException{
         List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster[./name/text()='"+id+"']/vms/vm/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        return result;
     }
     
     public String listVisionClustersVm() throws CleverException{
       List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        String response="";
        String vmname;
        String cl;
        while(st.hasMoreTokens()){
            cl=st.nextToken();
            response=response+"\n"+"cluster name "+cl+ "\n";
          vmname=listVisionClusterVm(cl);
          response=response+vmname;
        } 
        return response;
     }
     
     public String listRunningVisionClustersVm() throws CleverException{
         List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/VMs_Running/VM/attribute(name)");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        return result;
     }
     
     public String macFileConfigurator(Element n_info,String ip,String mac,int n_interface,boolean ipstatic){
         String result="DEVICE=eth"+n_interface
                   +"\nHWADDR="+mac;
         if(ipstatic)
                result=result+"\nBOOTPROTO=static"
                   +"\nNM_CONTROLLED=no"
                   +"\nONBOOT=yes"
                   +"\nIPADDR="+ip
                   +"\nNETMASK="+n_info.getChildText("netmask")
                   +"\nGATEWAY="+n_info.getChildText("gateway");
         else
               result=result+"\nBOOTPROTO=dhcp"
                   +"\nNM_CONTROLLED=yes";
         return result;
         
     }
      public boolean resumeVisionVm(String id) throws CleverException{
        List params=new ArrayList();
        params.add(id);
        boolean result=(Boolean)this.owner.invoke("VirtualizationManagerAgent","resumeVm", true, params);
        addVmRunning(id);
        logger.info(id+" resumed");
        return result;
       }
    
    public boolean resumeVisionCluster(String id) throws CleverException{
        List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster[./name/text()='"+id+"']/vms/vm/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          resumeVisionVm(st.nextToken());  
        }
        logger.info("cluster "+id+" resumed");
        return true;
    }
    
    public boolean resumeVisionClusters() throws CleverException{
        List params=new ArrayList();
        params.add("VisionManagerAgent");
         params.add("/Clusters/cluster/name/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        params.clear();
        StringTokenizer st=new StringTokenizer(result,"\n");
        while(st.hasMoreTokens()){
          resumeVisionCluster(st.nextToken());
          
        }
        logger.info("All clusters resumed");
        return true; 
    }
      
}
    
