/*
 * The MIT License
 *
 * Copyright 2012 giancarloalteri.
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
package org.clever.ClusterManager.StorageManagerPlugins.StorageManagerClever;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.StorageManager.StorageManagerPlugin;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.LogicalCatalogException;
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.Storage.VirtualFileSystem;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMLTools.ParserXML;
import org.jdom.Element;
import org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile;
/**
 *
 * @author giancarloalteri
 */

public class StorageManager implements StorageManagerPlugin {
  private Logger logger;
  private Class cl;
  private ModuleCommunicator mc;
  private String hostName;
  private ParserXML pXML;
  private Agent owner;

  /**
   * Instantiates a new StorageManager object
   */
  public StorageManager() {
    this.logger = Logger.getLogger("StorageManager");
    try
    {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      this.logger.error("Error getting local host name :" + e.getMessage());
    }
  }

  @Override
  public void setModuleCommunicator(ModuleCommunicator m) {
    this.mc = m;
  }

  @Override
  public ModuleCommunicator getModuleCommunicator() {
    return this.mc;
  }

  /**
   * This method checks if a logical node exists in the catalog 
   * @param logicname
   * @return
   * @throws Exception 
   */
  private boolean existNode(String logicname) throws Exception{
        try{
            List params = new ArrayList();
            params.add("StorageManagerAgent");
            params.add(getLocation(logicname));
            MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","existNode", true, params);
            boolean res=(Boolean)this.mc.invoke(mi);
       //05/24/2012     boolean res = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);            
            return res;
            } catch (CleverException e) {
                logger.error("Error: " + e.getMessage());
                return false;
            }
  }
  /**
   * This method creates the location for DB Sedna from CLEVER path
   * @param path
   * @return 
   */
  private String getLocation(String path){
      String location="";
      StringTokenizer st = new StringTokenizer(path,"/");
      while (st.hasMoreTokens()){
        location=location+"/node[@name='" + st.nextToken()+"']";
     }  
      return location;
  }
  /**
   * This method returns the attribute of a clever node
   * @param namefolder
   * @param attribute
   * @return
   * @throws CleverException 
   */
  private String getAttributeNode(String namefolder,String attribute) throws CleverException{
      String att;
      List params = new ArrayList();
      params.add("StorageManagerAgent");
      params.add(getLocation(namefolder));
      params.add(attribute);
      MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","getAttributeNode", true, params);
      att=(String)this.mc.invoke(mi);
   //05/24/2012  att = (String) this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params);
      
      return att;
  }
  /**
   * This method returns the children of a node from a given attribute
   * @param namefolder
   * @param attribute
   * @return
   * @throws CleverException 
   */
  private String getChild(String namefolder,String attribute) throws CleverException{
      List params = new ArrayList();
      params.add("StorageManagerAgent");
      params.add(getLocation(namefolder));
      params.add("name");
      MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","getChild", true, params);
      attribute=(String)this.mc.invoke(mi);
  //05/24/2012    attribute = (String) this.owner.invoke("DatabaseManagerAgent", "getChild", true, params);
      return attribute;
    }
  /**
   * This method returns the contents of a clever node
   * @param namefolder
   * @return
   * @throws CleverException 
   */
  private List getContentNode(String namefolder) throws CleverException{
      List res = new ArrayList();
      List params = new ArrayList();
      params.add("StorageManagerAgent");
      params.add(getLocation(namefolder));
      MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","getContentNode", true, params);
      res=(List)this.mc.invoke(mi);
    //05/24/2012  res = (List) this.owner.invoke("DatabaseManagerAgent", "getContentNode", true, params);
      return res;
  }

  /**
   * This method creates a new node in the catalog
   * @param namefolder
   * @param tipo
   * @param contenuto
   * @param contenuto1
   * @return
   * @throws CleverException
   * @throws Exception 
   */
  @Override
  public boolean createNode(String namefolder,String tipo,VFSDescription vfsD,String contenuto1) throws CleverException,Exception{  
            String[] result;
            String contenuto="";
            String location="";
            String r="";
            String type="";
            int i = 0;  
            int indice=0;
            StringTokenizer st = new StringTokenizer(namefolder,"/");
            result = new String[st.countTokens()];
            while (st.hasMoreTokens()){
                result[i++] = st.nextToken(); 
                }
            
            //NEW
            
            String [] cmd;
            cmd=resolverPath(contenuto1);
            if(!"".equals(cmd[0])){
                if(existNode(cmd[0])==false){  
                throw new LogicalCatalogException("Logical path: '" + cmd[0]+ "' not exist");
                }
            }
            if(!"".equals(cmd[1])){
                registerVeNew(contenuto1);
            }
            
            //
            /*
            if(!"".equals(contenuto1)){
                if(existNode(contenuto1)==false){  
                throw new LogicalCatalogException("Destination path: '" + contenuto1+ "' not exist");
                }
            }
            */
            
            if(existNode(namefolder)==true){  
              throw new LogicalCatalogException("Unable to create the node: '" + namefolder+ "' already exist");

            }
            if(i>1){
                for(indice=0;indice<i-1;indice++){ 
                    location=location+"/node[@name='" + result[indice]+"']"; 
                    r=r+"/"+result[indice];
                    }
                if(existNode(r)==false)
                throw new LogicalCatalogException("Unable to create the node: '" + namefolder+ "' not exist");
            }
            type=getAttributeNode(r,"type");
            if("mount".equals(type) || "link".equals(type)){
              throw new LogicalCatalogException("mount and link nodes can not have child nodes! They are leaf nodes!");
            } 
            
            
            if("mount".equals(tipo))
                contenuto=MessageFormatter.messageFromObject(vfsD);
            else if ("link".equals(tipo))
                contenuto="<path>"+contenuto1+"</path>";
            else
                contenuto="Logical CLEVER Node";

            
            
            List params = new ArrayList();
            String prova="<node name='"+result[indice]+"' type='"+tipo+"'>"+contenuto+"</node>"; 
            params.add("StorageManagerAgent");
            params.add(prova);
            params.add("into");
            params.add(location);         
            MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","insertNode", true, params);
            this.mc.invoke(mi);
          // 05/24/2012  this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
            return true;    
  } 
  /**
   * This method returns the physical path and virtual path of a generic path
   * @param path
   * @return
   * @throws CleverException
   * @throws FileSystemException
   * @throws Exception 
   */
  private String [] resolverPath(String path) throws CleverException, FileSystemException, Exception{
  
      String type="";
      String[] result;
      int flag=0;
      String path_logico="";
      String path_fisico="";
            int i = 0;  
            int indice=0;
            StringTokenizer st = new StringTokenizer(path,"/");
            result = new String[st.countTokens()];
            while (st.hasMoreTokens()) {
                result[i++] = st.nextToken(); 
            }
            for(indice=0;indice<i;indice++){ 
                if(flag==0){
                  path_logico=path_logico+result[indice]+"/";
                    if(existNode(path_logico)==false){
                        throw new LogicalCatalogException("node "+path_logico+" not exist");
                    }
                type=getAttributeNode(path_logico,"type");
                }
                if(flag==1){
                path_fisico=path_fisico+result[indice]+"/";
                }
            if("mount".equals(type)|| "link".equals(type) ){
            flag=1;
        }
            }
      String res[]={path_logico,path_fisico};
      return res;
  }
  
  /**
   * 
   * @param namefolder
   * @param property
   * @return
   * @throws CleverException 
   */
  // per il tree command (DA RIVEDERE ??)
  public String getContentNodeXML(String namefolder,String property) throws CleverException{
      String res="";
      List params = new ArrayList();
      params.add("StorageManagerAgent");
      params.add(getLocation(namefolder));
      params.add(property);
      MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","getContentNodeXML", true, params);
      res=(String)this.mc.invoke(mi);
     //05/24/2012 res = (String) this.owner.invoke("DatabaseManagerAgent", "getContentNodeXML", true, params);
      //System.out.println(res);
      return res;
  }

  /**
   * This method retrieves the access parameters from DB Sedna for a particular File System
   * @param str
   * @param contenuto
   * @return
   * @throws CleverException
   * @throws IOException 
   */
  private VFSDescription getCredentials(String str,String contenuto) throws CleverException, IOException{        
    String pathxml="";
    List params = new ArrayList();
    params.add("StorageManagerAgent");
    params.add(getLocation(str));
    MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","getContentNodeObject", true, params);
    pathxml=(String)this.mc.invoke(mi);
  //05/24/2012  pathxml = (String) this.owner.invoke("DatabaseManagerAgent", "getContentNodeObject", true, params);
    VFSDescription vfsD =(VFSDescription) MessageFormatter.objectFromMessage(pathxml);
    return vfsD; 

  }

  /**
   * This method runs through the hierarchical tree until you reach a mount mode
   * @param path_dest
   * @param cont_dest
   * @return
   * @throws Exception 
   */
   @Override
   public VFSDescription discoveryNode(String path_dest,String cont_dest) throws Exception{      
   String [] dest;
   dest=resolverPath(path_dest);   
   String type_dest="";
   type_dest=getAttributeNode(dest[0],"type");
   if("dir".equals(type_dest)){
        throw new LogicalCatalogException("Operation not permitted: It's a clever node");
      }
      // DESTINATION
   if("link".equals(type_dest)){ 
    List res_dest = new ArrayList();
       res_dest=getContentNode(dest[0]);
       return discoveryNode((String) res_dest.get(0)+"/"+dest[1],cont_dest);
      }
   if("".equals(cont_dest))
      cont_dest=dest[1];
   
   VFSDescription vfsD=getCredentials(dest[0],cont_dest);
   vfsD.setPath1(cont_dest);
   return vfsD;    
  }

   /**
     * This method makes a copy of data between two mount nodes 
     * @param path_src
     * @param cont_src
     * @param path_dest
     * @param cont_dest
     * @return
     * @throws CleverException
     * @throws FileSystemException
     * @throws Exception 
     */ 
   @Override
   public boolean cp(String path_src,String cont_src,String path_dest,String cont_dest) throws CleverException, FileSystemException, Exception{
   String [] src;
   String [] dest;
   src=resolverPath(path_src);
   dest=resolverPath(path_dest);   
   String type_src="";
   String type_dest="";
   type_src=getAttributeNode(src[0],"type"); 
   type_dest=getAttributeNode(dest[0],"type"); 
   if("dir".equals(type_src) || "dir".equals(type_dest)){
        throw new LogicalCatalogException("Operation not permitted: the source and destination can not be a clever node");
      } 
   List res_src = new ArrayList();
   List res_dest = new ArrayList();
   // DESTINATION
   if("link".equals(type_dest)){  
       res_dest=getContentNode(dest[0]);
       return cp(path_src,cont_src,(String) res_dest.get(0)+"/"+dest[1],cont_dest);
      }
   if("".equals(cont_dest))
      cont_dest=dest[1];
   // SOURCE
   if("link".equals(type_src)){     
       res_src=getContentNode(src[0]);
       return cp((String) res_src.get(0)+"/"+src[1],cont_src,path_dest,cont_dest);
      }
   if("".equals(cont_src))
      cont_src=src[1];

   // DESTINATION
   VFSDescription vfsD_d=getCredentials(dest[0],cont_dest);
   
   VirtualFileSystem a_dest=new VirtualFileSystem();
   a_dest.setURI(vfsD_d);
   FileObject file_d=a_dest.resolver(vfsD_d,a_dest.getURI(),cont_dest);
 
   // SOURCE
   VFSDescription vfsD_s=getCredentials(src[0],cont_src);
   VirtualFileSystem a_src=new VirtualFileSystem();
   a_src.setURI(vfsD_s);
   FileObject file_s=a_src.resolver(vfsD_s,a_src.getURI(),cont_src);

   VirtualFileSystem a=new VirtualFileSystem();
   a.cp(file_s,file_d); 
   return true; 
      
  }
   /**
    * This method displays the contents of a clever node
    * @param path
    * @return
    * @throws CleverException
    * @throws FileSystemException
    * @throws Exception 
    */
   @Override
   public String ls(String path) throws CleverException, FileSystemException, Exception{
      String [] pathname;
      String node="";
      pathname=resolverPath(path); 
      node=resolverNode(pathname[0],pathname[1]); 
      return node; 
   }   

   /**
    * This method makes the resolver of a node depending on the type of node
    * @param namefolder
    * @param contenuto
    * @return
    * @throws CleverException
    * @throws FileSystemException
    * @throws Exception 
    */
   private String resolverNode(String namefolder,String contenuto) throws CleverException, FileSystemException, Exception{
      String name="";
      String type="";
      List res = new ArrayList();
      name=getChild(namefolder,"name");

      if("".equals(name)){
          // means that you're a leaf 
          // check type of node (mount,link,dir) 
        type=getAttributeNode(namefolder,"type");
        name=getAttributeNode(namefolder,"name");
        if("dir".equals(type)){
            //res=getContentNode(namefolder);      
            //return (String) res.get(0);

            return "Logical CLEVER Node";
        }
        if("link".equals(type)){ 

            res=getContentNode(namefolder);
            
            // NEW : in questo modo è possibile fare un link ad una directory all'interno di un VFS
            String [] cmd;
            cmd=resolverPath((String) res.get(0));
            if( !"".equals(cmd[1])){
                return ls(cmd[0]+cmd[1]+"/"+contenuto); 
            }
            // 

            if( "dir".equals(getAttributeNode((String) res.get(0),"type"))){
            return ls((String) res.get(0)+"/"+contenuto);       
            }
            else
            return resolverNode((String) res.get(0),contenuto);
        }     
        if("mount".equals(type)){    

        VFSDescription vfsD=getCredentials(namefolder,contenuto);
        String node="";
        VirtualFileSystem a=new VirtualFileSystem();
        a.setURI(vfsD);
        FileObject file=a.resolver(vfsD,a.getURI(),contenuto); 
        node=a.ls(file);
        //node=a.lsNew(file);
        return node;
        }
      }
      return name;       
 } 
   
   /**
    * This method checks if a directory has child nodes (VirtualizationManager)
    * Is necessary to differentiate the two cases: creatVM and registerVM (HYP)
    * @param path
    * @return
    * @throws Exception 
    */
    @Override
   public boolean check(String path) throws Exception{
       
         VFSDescription vfsD=discoveryNode(path,"");
        VirtualFileSystem a=new VirtualFileSystem();
        a.setURI(vfsD);
        FileObject file=a.resolver(vfsD,a.getURI(),vfsD.getPath1());
        
      //  FileObject[] children = file.getChildren(); 
      //MODIFIED 05/22/2012 ,in this way there isn't error if file is not a folder
            if(file.getType().equals(FileType.FOLDER) && (file.getChildren()).length>0){
                return false;
            }

            return true;

   }
   /**
    * This method handles the locks on the replicas of data
    * @param path
    * @param targetHM
    * @param lock
    * @return
    * @throws CleverException
    * @throws Exception 
    */
    @Override
   public String lockManager(String path,String targetHM,LockFile.lockMode lock) throws CleverException,Exception{

       VFSDescription vfsD=discoveryNode(path,"");
       List result = new ArrayList();
       List params = new ArrayList();
       params.add(vfsD);
       params.add(lock);
      // result=(List)((CmAgent)this.mc.getMethodInvokerHandler()).remoteInvocation(targetHM,"ImageManagerAgent", "storageIM", true, params);
       result=(List) ((CmAgent) this.owner).remoteInvocation(targetHM,"ImageManagerAgent", "storageIM", true, params);
       
       /*
        * 0:type operation
        * 1:localpath
        * 2:date
        * 3:size
        * 4:lock    
        */

       // Strat Controls
       List params1 = new ArrayList();
       if(result.get(0).toString().isEmpty()){
            throw new LogicalCatalogException("object VFS not exist");
       }
       else if("notUpdate".equals(result.get(0).toString())){
            return result.get(1).toString();
       }
       else if("update".equals(result.get(0).toString())){
           //List params4 = new ArrayList();
           LockFile.lockMode l=(LockFile.lockMode)result.get(4);
           params1.add("StorageManagerAgent");
           params1.add("<lock>"+new LockFile().getLockType(l) +"</lock>");
           params1.add("with");
           params1.add("/file/hm/replica[@localpath='"+result.get(1).toString()+"']/lock");
           //params4.add("/file/hm/replica/lock");
         //  MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","updateNode", true, params1);
        //   this.mc.invoke(mi);
           this.owner.invoke("DatabaseManagerAgent", "updateNode", true, params1);
           params1.clear();
           //return result.get(1).toString();
           
       }
       else if("insert".equals(result.get(0).toString())){
            //List params3 = new ArrayList();
           LockFile.lockMode l=(LockFile.lockMode)result.get(4);
            params1.add("StorageManagerAgent");
            params1.add("<replica localpath='"+result.get(1) +"'>"
                                + "<date>"+result.get(2) +"</date>"
                                + "<size>"+result.get(3) +"</size>"
                                + "<lock>"+new LockFile().getLockType(l) +"</lock>"
                                + "</replica>");
            params1.add("into");
            params1.add("/file[@cleverpath='"+path+"']/hm[@name='"+targetHM+"']"); 
            //MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent","insertNode", true, params1);
           // this.mc.invoke(mi);
            this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params1);
            params1.clear();
 
       }
       

       else if("new".equals(result.get(0).toString()))  {
           //List params1 = new ArrayList();
           LockFile.lockMode l=(LockFile.lockMode)result.get(4);
            String entry= "<file cleverpath='"+path+"'>"
                            + "<hm name='"+targetHM+"'>"
                                + "<replica localpath='"+result.get(1) +"'>"
                                + "<date>"+result.get(2) +"</date>"
                                + "<size>"+result.get(3) +"</size>"
                                + "<lock>"+new LockFile().getLockType(l) +"</lock>"
                                + "</replica>"
                            + "</hm>"
                        + "</file>";   
       
            params1.add("StorageManagerAgent");
            params1.add(entry);
            params1.add("into");
            params1.add("");        
            this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params1);
            params1.clear();
      }
       return result.get(1).toString();
   }
    /**
    * This method checks if the path set by the user during registration is valid
    * @param cleverPath
    * @throws CleverException
    * @throws FileSystemException
    * @throws Exception 
    */
    @Override
    public void registerVeNew(String cleverPath) throws CleverException, FileSystemException, Exception{
 
        VFSDescription vfsD=discoveryNode(cleverPath,"");
        VirtualFileSystem a=new VirtualFileSystem();
        a.setURI(vfsD);
        FileObject file=a.resolver(vfsD,a.getURI(),vfsD.getPath1()); 
        if (!file.exists()){
           throw new LogicalCatalogException("file not exist");
       }
       
   }
   
   
    @Override
    public void init(Element params, Agent owner) throws CleverException {
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
    public void setOwner(Agent owner){
        this.owner=owner;
    }

    @Override
    public boolean deleteFile(String path,String id,String HMTarget) {
        try {
            List params=new ArrayList();
            //arams.add(path);precedentemente qui c'era il campo id invece di path, verificare che
            //path e localpath siano diversi in tal caso eliminare l'invocazione remota
            //String localpath=(String) ((CmAgent)this.owner).remoteInvocation(HMTarget,"HyperVisorAgent","getLocalPath", true, params);
            //params.clear();
            String localpath=path;
            params.add(localpath); 
            VFSDescription vfsD=null;
            try {
                //vfsD = discoveryNode(path,"");
                vfsD=new VFSDescription(VFSDescription.TypeVfs.file,"path","");
            } catch (Exception ex) {
                this.logger.error("Error in function discoveryNode:"+ex.getMessage());
                java.util.logging.Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            params.add(vfsD);
            ((CmAgent)this.owner).remoteInvocation(HMTarget,"ImageManagerAgent","deleteFile",true,params);
            params.clear();
            params.add("StorageManagerAgent");
            params.add(("/file[./hm/replica/@localpath='"+localpath+"']"));
            this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
            return true;
            
        } catch (CleverException ex) {
            java.util.logging.Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
 
public String SnapshotImageCreate(String localpath,String logicalpath,String HMTarget,LockFile.lockMode lock) throws CleverException{
            List params=new ArrayList();
            VFSDescription vfsD=null;
            try {
                 vfsD = discoveryNode(logicalpath,"");
            } catch (Exception ex) {
            java.util.logging.Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
         
            params.add(localpath);
            params.add(vfsD);
            params.add(lock);
            List result=(List)((CmAgent)this.owner).remoteInvocation(HMTarget,"ImageManagerAgent","SnapshotImageCreate", true, params);
            LockFile.lockMode l=(LockFile.lockMode)result.get(4);
            String entry= "<file cleverpath='"+logicalpath+"'>"
                            + "<hm name='"+HMTarget+"'>"
                                + "<replica localpath='"+result.get(1) +"'>"
                                + "<date>"+result.get(2) +"</date>"
                                + "<size>"+result.get(3) +"</size>"
                                + "<lock>"+new LockFile().getLockType(l) +"</lock>"
                                + "</replica>"
                            + "</hm>"
                        + "</file>";   
            params.clear();
            params.add("StorageManagerAgent");
            params.add(entry);
            params.add("into");
            params.add("");        
            this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
            params.clear();
            return result.get(1).toString();
}
}
