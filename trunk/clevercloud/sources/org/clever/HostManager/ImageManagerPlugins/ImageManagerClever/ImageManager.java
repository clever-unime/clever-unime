/*
 * The MIT License
 *
 * Copyright Valerio Barbera & Luca Ciarniello.
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
package org.clever.HostManager.ImageManagerPlugins.ImageManagerClever;

import org.clever.Common.SqliteDB.SQLite;
import java.sql.ResultSet;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.vfs2.*;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.Shared.Host;
import org.clever.Common.Shared.ImageFileInfo;
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.Storage.VirtualFileSystem;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.HostManager.ImageManager.ImageManagerPlugin;
import org.clever.HostManager.Monitor.ResourceState;
import org.clever.HostManager.NetworkManager.AdapterInfo;
import org.clever.HostManager.NetworkManager.IPAddress;
import org.jdom.Element;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
//import org.safehaus.uuid.UUIDGenerator;

/*
 * @author Valerio Barbera & Luca Ciarniello 2011
 * @author Giuseppe Tricomi 2012
 */

/**
 * This class implements the Image Manager, with the methods necessary to save
 * and transfer VM image files on the cluster Hosts.
 * @author Luca Ciarniello
 */
public class ImageManager implements ImageManagerPlugin {
 // private UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
  private String localRepository;  
  private MultiMap map;
  private MultiMap map1;
  private List a;
  private ArrayList b;
  private ArrayList b1;
  private ConcurrentHashMap<String, String> paths;
  private HashMap localVE;
  private HashMap<String, String> sharedPath;
  private String mountPoint;
  private String savePoint;
  private String hostName;
  //private StoragePluginFactory storagePluginFactory;
  //private DistributedStoragePlugin distributedStorage;
  private ConnectionXMPP conn;
  private ModuleCommunicator mc;
  private FileTransferManager ftm;
  private Agent owner;
  private SQLite sqlite;
  
  /**
   * A constant representing the TCP-Socket based file transfer
   */
  public static final int SOCK = 100;
  /**
   * A constant representing the XMPP-based file transfer
   */
  public static final int XMPP = 101;
  
  //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/ImageManager/log_conf/";
    private String pathDirOut="/LOGS/HostManager/ImageManager";
    //########

  /**
   * Instantiates a new ImageManager
   */
  public ImageManager(Element pp) {
    
     //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("ImageManagerAgent");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################  
      
   
   // this.uuidGenerator=null;
    //this.des=pp.getChildText("dest");
    this.map = new MultiHashMap( );
    this.map1 = new MultiHashMap( );
    this.a=null;
    this.b=new ArrayList();
    this.b1=new ArrayList();
    
    
    //this.params=new ArrayList();
    //this.vfs=new VirtualFileSystem();
    this.sqlite=new SQLite();    
    
    this.localRepository=System.getProperty("user.dir")+"/repository/";
    this.paths = new ConcurrentHashMap<String, String>();
    this.sharedPath = new HashMap<String, String>();
    // We could maybe create a specific user for Clever operations
    // and use a relative path
    this.savePoint = System.getProperty("user.dir");
    this.mountPoint = System.getProperty("user.dir");    
    try
    {
      this.hostName = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException uhe) {
      logger.error("Error getting Ip address : " + uhe.getMessage());
      return;
    }
    Thread hook = new ThreadCloser(this);
    Runtime.getRuntime().addShutdownHook(hook);
  }
  
  // stabilire quali uteriori servizi dovrebbe offrire l'IM
  public String getFile(VFSDescription vfsD){
      return vfsD.getType().name();
      
  }

  /**
   * This method manages replicas of files with locking mechanisms. 
   * Make a copy of file or folder on the HM
   * @author giancarloalteri
   * @param vfsD
   * @param lock
   * @return
   * @throws FileSystemException
   * @throws CleverException
   * @throws Exception 
   */
    public List storageIM(VFSDescription vfsD, LockFile.lockMode lock) throws FileSystemException, CleverException, Exception {
//TODO: low priority-> il primo if e il caso in cui non viene trovato nessun "SI" sono coincidenti può essere reso più pulito
//cambiando la gestione delle info         
        String response = "";
        String respLock;
        List params = new ArrayList();
        VirtualFileSystem vfs = new VirtualFileSystem();
        vfs.setURI(vfsD);
        FileObject file_s = vfs.resolver(vfsD, vfs.getURI(), vfsD.getPath1());
        UUID id = UUID.randomUUID();
        try {
            if (!file_s.exists()) {
                // response="";  
                params.add(response);
            } else {

                //src.ChangeLastModificationTime(file_s);
                FileContent content = file_s.getContent();
                String lastMod = "";
                if (file_s.getType().equals(FileType.FILE)) {
                    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
                    lastMod = dateFormat.format(new Date(content.getLastModifiedTime()));
                }
                //se la chiave non esiste
                logger.debug("element searched : " + file_s.getName().getURI());
                ResultSet rs = this.sqlite.retrieveElementsInMap(file_s.getName().getURI());
                //logger.debug("pos0A");
                if ((!rs.isAfterLast() && !rs.isBeforeFirst())) {
                    //logger.debug("posA");
                    if (file_s.getType().equals(FileType.FOLDER)) {
                        //logger.debug("pos1");
                        response = "" + this.localRepository + id;
                        params.add("new");
                        params.add(response);
                        params.add(lastMod);//date
                        params.add("");//size
                        params.add(lock);//lock

                    } else {
                        //logger.debug("pos2");
                        response = this.localRepository + id + "." + file_s.getName().getExtension();
                        params.add("new");
                        params.add(response);
                        params.add(lastMod);//date
                        params.add(content.getSize());//size
                        params.add(lock);//lock

                    }
                    //logger.debug("pos3");
                    FileSystemManager mgr = VFS.getManager();
                    //logger.debug("pos4");
                    FileObject file_d = mgr.resolveFile(response);
                    //logger.debug("pos5");
                    vfs.cp(file_s, file_d);

                    if (file_s.getType().equals(FileType.FOLDER)) {
                        //logger.debug("Image1");
                        this.sqlite.insertElementAtMap(file_s.getName().getURI(), response, "", lastMod, LockFile.getLockType(lock));
                    } else {
                        //logger.debug("Image2");
                        this.sqlite.insertElementAtMap(file_s.getName().getURI(), response, new Long(content.getSize()).toString(), lastMod, LockFile.getLockType(lock));
                    }

                } else {
                    //logger.debug("posB");
                    while (!rs.isAfterLast()) {
                        LockFile l = new LockFile();
                        respLock = l.checkLock(LockFile.getLockEnumType(rs.getString("lock")), lock);//((LockFile.lockMode) b1.get(2), lock);////
                        if ("SI".equals(respLock)) {
                            LockFile.lockMode b1l = LockFile.getLockEnumType(rs.getString("lock"));//(LockFile.lockMode) b1.get(2);//

                            if (b1l.ordinal() >= lock.ordinal()) {
                                params.add("notUpdate");
                                params.add(rs.getString("response"));

                            } else {
                                params.add("update");
                                params.add(rs.getString("response"));
                                params.add("");
                                params.add("");
                                params.add(lock);

                                //cancellare vecchio lock ed inserire il nuovo
                                this.sqlite.updateElementInMap("response='" + rs.getString("response") + "'", "lock='" + LockFile.getLockType(lock) + "'");
                            }

                            return params;
                        }

                        rs.next();
                    }
                      //  }

                    if (file_s.getType().equals(FileType.FOLDER)) {
                        response = this.localRepository + id;
                        this.sqlite.insertElementAtMap(file_s.getName().getURI(), response, "", lastMod, LockFile.getLockType(lock));
                        params.add("insert");
                        params.add(response);
                        params.add(lastMod);
                        params.add("");
                        params.add(lock);

                    } else {
                        response = this.localRepository + id + "." + file_s.getName().getExtension();
                        //logger.debug("Image4");
                        this.sqlite.insertElementAtMap(file_s.getName().getURI(), response, new Long(content.getSize()).toString(), lastMod, LockFile.getLockType(lock));
                        params.add("insert");
                        params.add(response);
                        params.add(lastMod);
                        params.add(content.getSize());
                        params.add(lock);
                    }

                    FileSystemManager mgr = VFS.getManager();
                    FileObject file_d = mgr.resolveFile(response);
                    vfs.cp(file_s, file_d);
                    return params;

                }

            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
        return params;
    }

  /**
   * Sets the XMPP connection for the Manager
   * @param conn - the ConnectionXMPP object to use
   */
  public void setXMPP(ConnectionXMPP conn) {
    this.conn = conn;
    ftm = new FileTransferManager(this.conn.getXMPP());
    this.initFTM();
  }

  /**
   * Sets the ModuleCommunicator for the Manager
   * @param mc - the ModuleCommunicator object to use
   */
  public void setMC(ModuleCommunicator mc) {
    this.mc = mc;
  }

  /**
   * This method, called when the ImageManager is initialized, will register the
   * host into the database. It will do so by creating a new Host object, filling
   * it with the Host name (from a local variable) and the free space (queried
   * from the MonitorAgent). Then it will send this object to the DatabaseManager
   * to be inserted. 
   * @return true if the Host entry has been created and saved into the database,
   * false otherwise
   */
  public boolean registerHost() {
    Host host = new Host();
    List result = null;

    try
    {
      MethodInvoker mi = new MethodInvoker("MonitorAgent",
              "getStorageCurrentFreeSpace", true, null);
      result = (List) this.mc.invoke(mi);

      if (result == null)
      {
        logger.info("Could not get the free storage space from the host");
        return false;
      }

    } catch (Exception e) {//modifico l errore altrimenti non restituisce nulla 05/26/2012
      logger.error("Error getting free storage space info from host : " + e.getMessage());
      return false;
    }

    Float f = ((ResourceState) result.get(0)).getCurrentUsage();
    host.FreeSpace = f.longValue();
    host.Name = this.hostName;
    try
    {
      List para = new ArrayList();
      para.add(host);
      MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent", "addHost", true, para);
      boolean res = (Boolean) this.mc.invoke(mi);
      if (res)
        logger.info("Host registered within the cluster");
      else
        logger.info("Can't register the host within the cluster");

      return res;

    } catch (Exception e) { //modifico l errore altrimenti non restituisce nulla 05/26/2012 
     logger.error("Error inserting host info into the database : " + e.getMessage());
      return false;
    }
  }

  /**
   * This method creates a FileTransferListener for the ImageManager, so it can
   * listen for incoming file transfer requests from another host.  In case it
   * receives a request, it will accept the file transfer, save the file on disk
   * and put the file's absolute path in a temporary HashMap.
   */
  private void initFTM() {
    System.out.println("Adding a FileTransferListener");
    logger.info("Adding a FileTransferListener");
    ftm.addFileTransferListener(new FileTransferListener() {
      @Override
      public void fileTransferRequest(FileTransferRequest request) {
        System.out.println("Request received");
        if (true) // Check to see if the request should be accepted
        {
          // Accept it
          System.out.println("Entering FTListener because of FTRequest");
          IncomingFileTransfer transfer = request.accept();
          String id = request.getDescription();
          String path = savePoint + System.getProperty("file.separator") + request.getFileName();
          /*System.out.println("desc: " + request.getDescription());
          System.out.println("fname: " + request.getFileName());
          System.out.println("requestor: " + request.getRequestor());
          System.out.println("mimetype: " + request.getMimeType());
          System.out.println("sid: " + request.getStreamID());
          */
          try
          {
            System.out.println("Receiving...");
            transfer.recieveFile(new File(path));
            //Information put in HashMap for later retrieval
            System.out.println("IM - putting in path (" + id + "," + path + ")");
            paths.put(id, path);
          } catch (XMPPException e) {
            logger.error("Error getting the VM file: " + e.getMessage());
          }
        }
        else
        {
          // Reject it
          request.reject();
          logger.info("VM file transfer rejected");
        }
      }
    });
  }

  @Override
  public void setMountPoint(String path) {
    mountPoint = path;
  }

  @Override
  public String getMountPoint() {
    return mountPoint;
  }

  @Override
  public void setSavePoint(String path) {
    savePoint = path;
  }

  @Override
  public String getSavePoint() {
    return savePoint;
  }

  //  -------GESTIONE HashMap HOST/SHARED_PATH-------
  @Override
  public void setSharedPath(HashMap sp) {
    this.sharedPath = sp;
  }

  @Override
  public HashMap getSharedPath() {
    return this.sharedPath;
  }

  @Override
  public void addSharedPath(String host, String path) {
    this.sharedPath.put(host, path);
  }

  //  ------GESTIONE HashMap VE------
  @Override
  public void setLocalVE(HashMap dict) {
    localVE = dict;
  }

  @Override
  public HashMap getLocalVE() {
    return localVE;
  }

  @Override
  public void deleteFile(String path,VFSDescription vfsD){
        
         
          if (this.isAccessible(path))
          {
            // cancellare dalla hash-map?
            /*FileObject file_s=null;
            VirtualFileSystem vfs=new VirtualFileSystem();
            String s=vfs.setURI(vfsD);
            try {
                file_s=vfs.resolver(vfsD, vfs.getURI(), vfsD.getPath1());
            } catch (FileSystemException ex) {
                java.util.logging.Logger.getLogger(ImageManager.class.getName()).log(Level.SEVERE, null, ex);
            }*/
             //map.remove(file_s.getName(),path);
              
             map.remove(path);
            //localVE.remove(path);
            this.sqlite.deleteElementInMap("response='"+path+"'");
            //Cancellare file fisicamente?
            File f = new File(path);
            if (f.isFile())
              f.delete();
          }
       
  }

  //  -----DISTRIBUTED STORAGE PLUGIN------
  @Override
  public boolean isLocalPath(String path) {
    //storagePlugin è l'istanza di StoragePluginFactory
    //che al suo interno contiene il riferimento alla classe che implementa
    //l'interfaccia "DistributedStoragePlugin" che contiene il metodo isLocal()

    //return distributedStorage.isLocal(path, this.savePoint); //base=savepoint
    return false;
  }

  @Override
  public boolean isRemotePath(String path) {
    //storagePlugin è l'istanza di StoragePluginFactory
    //che al suo interno contiene il riferimento alla classe che implementa
    //l'interfaccia "DistributedStoragePlugin" che contiene il metodo isRemote()
    //return distributedStorage.isRemote(path, mountPoint); //base=mounpoint
    return false;
  }

  @Override
  public boolean isSharedPath(String path) {
    //Farei la ricerca del percorso all'interno del dizionario
    for (Map.Entry entry : this.sharedPath.entrySet())
    {
      String hostname = (String) entry.getValue();
      if (this.sharedPath.get(hostname).equals(path))
        return true;
    }
    return false;
  }

  //verifica l'accessibilità di un file o di una directory
  @Override
  public boolean isAccessible(String path) {
    try
    {
      File p = new File(path);
      if (p.isDirectory() || p.isFile())
        return true;
      else
        return false;

    } catch (NullPointerException e) {
      logger.error(e.getMessage());
      return false;
    }
  }

  //  -----FILE TRANSFER PLUGIN------
  //Sposta il file tra due cartelle interne all'host
  @Override
  public boolean saveVeToPath(String veId, String srcPath, String dstPath) {
    //Forse andrebbe controllato che il file da spostare non si trovi
    //originariamente nella cartella in cui ci sono i mount delle altre macchine...
    //Perchè questo diventerebbe uno spostamento di file tra host...
    try
    {
      File f = new File(srcPath + veId);
      f.renameTo(new File(dstPath + veId));
      return true;

    } catch (Exception e) {
      logger.error(e.getMessage());
      return false;
    }
  }

  @Override
  public String getPath(String veid, String host) {
    //Se è locale lo trovo in savePoint
    if (this.isLocalPath(veid))
    {
      //Se è accessibile lo restituisco
      if (this.isAccessible(this.savePoint + "/" + veid))
      return this.savePoint + "/" + veid;
    }

    //Se è remoto, sarà nella cartella dei montaggi
    if (this.isRemotePath(host + "/" + veid))
    {
      //Se è accessibile lo restituisco
      if (this.isAccessible(this.mountPoint + "/" + host + "/" + veid))
      return this.mountPoint + "/" + host + "/" + veid;
    }

    return null;
  }

  @Override
  public String getVMPath(String name) {
    return this.paths.remove(name);
  }

  @Override
  public Object sendFile(String name, String filePath, String destHost, Integer ftTech) {
    switch (ftTech)
    {
      case ImageManager.SOCK:
        return sendFileSOC(filePath, destHost);

      case ImageManager.XMPP:
        return sendFileFTM(name, filePath, destHost);

      default:
        this.logger.info("Invalid file transfer technology case");
        return false;
    }
  }

  @Override
  public String receiveFile(String name, String fileName) {
    try
    {
      MethodInvoker mi = new MethodInvoker("NetworkManagerAgent", "getAdaptersInfo", true, null);
      ArrayList<AdapterInfo> netcards = (ArrayList) this.mc.invoke(mi);
      IPAddress ip = null;
      for (int i = 0; i < netcards.size() && ip == null; i++)
      {
        if (!netcards.get(i).getType().endsWith("Loopback"))
          ip = netcards.get(i).getIPv4Address();
      }

      if (ip == null)
        return null;

      ServerSocket ss = new ServerSocket(9999);
      logger.info("Waiting to receive VM file on destination host");
      ThreadSocket ts = new ThreadSocket(ss, name, fileName);
      ts.start();
      System.out.println("returning " + ip.getAddress());
      return ip.getAddress();

    } catch (Exception e) {
      this.logger.error("Error while getting VM file:" + e.getMessage());
      return null;
    }
  }

  @Override
  public ImageFileInfo getFileInfo(String filePath) {
    File f = new File(filePath);
    if (!f.isFile())
    {
      logger.info("Can't open the VM's image file");
      return null;
    }

    ImageFileInfo ifi = new ImageFileInfo();
    ifi.Name = f.getName();
    ifi.Path = f.getAbsolutePath();
    ifi.Size = f.length();
    return ifi;
  }

  /**
   * This method implements a TCP Socket version of sendFile. Called on the
   * destination Host, it will start a TCP ServerSocket and run it in a thread,
   * waiting for a connection with the source Host. It will then receive the
   * file data and save it on disk, before sending the file's path to the source host.
   * @param filePath - the image file's absolute path on the source Host
   * @param destHost - the name of the destination Host
   * @return the absolute path of the file on the destination Host
   */
  private String sendFileSOC(String filePath, String destHost) {
    try
    {
      File f = new File(filePath);
      if (!f.exists())
      {
        logger.error("Can't read VM file for sending");
        return null;
      }

      logger.info("Sending " + f.getName() + " to " + destHost);
      Socket soc = new Socket(destHost, 9999);
      logger.info("Connected to " + destHost);
      byte[] data = new byte[(int) f.length()];
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
      bis.read(data, 0, data.length);
      bis.close();
      DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
      dos.writeInt(data.length);
      dos.write(data, 0, data.length);
      dos.flush();
      this.logger.info("VM file sent succesfully");
      DataInputStream dis = new DataInputStream(soc.getInputStream());
      String path = dis.readUTF();
      dis.close();
      dos.close();
      soc.close();
      return path;

    } catch (UnknownHostException ex) {
      this.logger.error("Unknown Host Error sending VM file : " + ex.getMessage());
      return null;

    } catch (FileNotFoundException ex) {
      this.logger.error("File not found Error sending VM file : " + ex.getMessage());
      return null;

    } catch (IOException ex) {
      this.logger.error("IO Error sending VM file : " + ex.getMessage());
      return null;
    }
  }

  /**
   * This method implements an XMPP file transfer version of sendFile. It will use
   * the FileTransfeManager of the XMPP connection to create an outgoing file transfer,
   * which will send the file to the destination host.
   * @param name - the logical name of the VirtualMachine
   * @param filePath - the image file's absolute path on the source Host
   * @param destHost - the name of the destination Host
   * @return true if the file transfer was successful, false otherwise
   */
  private boolean sendFileFTM(String name, String filePath, String destHost) {
    File f = new File(filePath);
    System.out.println("filesize: " + f.length());
    if (!f.exists())
    {
      logger.error("Can't read VM file for sending");
      return false;
    }

    try
    {
      String nick = destHost.toLowerCase() + "@" + this.conn.getServer().toLowerCase() + "/Smack";
      System.out.println("OFT to " + nick + " from " + conn.getXMPP().getUser());
      OutgoingFileTransfer oft = ftm.createOutgoingFileTransfer(nick);
      oft.sendFile(f, name);
      while (!oft.isDone())
      {
        if (oft.getStatus().equals(Status.error))
        {
          System.out.println("ERROR!!! " + oft.getError());
          oft.cancel();
          return false;
        }

        System.out.println(oft.getStatus());
        System.out.println(oft.getProgress());
        System.out.println("........");
        Thread.sleep(1500);
      }

      if (oft.getStatus().equals(Status.complete))
      {
        System.out.println("Transfer done");
        return true;
      }

      if (oft.getStatus().equals(Status.error))
        System.out.println("Transfer failed: " + oft.getError());
      return false;

    } catch (XMPPException e) {
      System.out.println("Error sending VM image file with the FTM : " + e.getMessage());
      return false;

    } catch (InterruptedException e) {
      System.err.println("Error sleeping during OFT : " + e.getMessage());
      return false;
    }
  }

    @Override
    public void setOwner(Agent owner) {
        this.owner=owner;
    }

  /**
   * This thread will start when the Image Manager has to receive a VM image file.
   * It will open a Server socket and wait for a connection. Once the connection
   * is established and the data is received, the thread will create a new file
   * in the "save point" directory. It will also save the image file's path in
   * a temporary HashMap for later retrieval.
   */
  public class ThreadSocket extends Thread {
    ServerSocket ss;
    String name;
    String fileName;

    /**
     * Instantiates a new ThreadSocket object
     * @param ss - the ServerSocket to place in accept
     * @param name - the logical name of the VirtualMachine
     * @param fileName - the name of the image file
     */
    public ThreadSocket(ServerSocket ss, String name, String fileName) {
      this.ss = ss;
      this.name = name;
      this.fileName = fileName;
    }

    /**
     * This method will wait for a socket connection, receive the file data,
     * write a new file on the disk, and put this file's absolute path on
     * a temporary HashMap. It will then close the connection.
     */
    @Override
    public void run() {
      try
      {
        logger.info("Waiting for VM file on destination host");
        Socket soc = ss.accept();
        DataInputStream dis = new DataInputStream(soc.getInputStream());
        byte[] data = new byte[dis.readInt()];
        String path = savePoint + System.getProperty("file.separator") + this.fileName;
        File f = new File(path);
        FileOutputStream fos = new FileOutputStream(f);

        for (int bytes = 0, read = 0; bytes < data.length; bytes += read)
          read = dis.read(data, bytes, data.length - bytes);

        fos.write(data, 0, data.length);
        fos.close();
        DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
        dos.writeUTF(path);
        dos.close();
        dis.close();
        soc.close();

        logger.info("VM file written on destination host");
        return;

      } catch (Exception e) {
        logger.error("Error receiving VM file:" + e.getMessage());
        return;
      }
    }
  }

  /**
   * This class is a shutdown hook-like thread. It is registered when the
   * Image Manager initializes and is run when the project closes. Its goal
   * is to send a last command the the DatabaseManager, asking that the
   * host info should be removed from the database.
   */
  public class ThreadCloser extends Thread {
    private ImageManager im;

    /**
     * Initializes a new ThreadCloser object
     * @param im - The ImageManager registering the shutdown hook
     */
    public ThreadCloser(ImageManager im) {
      this.im = im;
    }

    /**
     * This method will call the DatabaseManager's removeHost method, specifying
     * the hostName so its data can be removed from the database.
     */
    @Override
    public void run() {
      try
      {
        List para = new ArrayList();
        para.add(this.im.hostName);
        MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent", "removeHost", true, para);
        boolean res = (Boolean) this.im.mc.invoke(mi);
        if (res)
          logger.info("Host removed from the cluster");
        else
          logger.info("Can't remove the host from the cluster");

      } catch (CleverException e) {
        logger.error("Error deleting host info from the database : " + e.getMessage());
      }
    }
  }
/*  public void prova(){
      Collection c=map.values();
      Object array[]=c.toArray();
      for(int i=0;i<array.length;i++){
          logger.debug("UUUUUUUUUUUUUUUUUUUUU"+((String)array[i]));
      }
      Collection b=map1.values(); 
      Object array1[]=b.toArray();
      for(int i=0;i<array1.length;i++){
          logger.debug("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"+((String)array1[i]));
      }
  }*/
public List SnapshotImageCreate(String localpath,VFSDescription vfsD,LockFile.lockMode lock) throws IOException, InterruptedException{
            List params=new ArrayList();
            UUID uuid=UUID.randomUUID();
            String localrepository=System.getProperty("user.dir")+"/repository/snapshot/";
            File fl=new File(localrepository);
            if(!fl.exists())
                fl.mkdir();
            FileObject file_s=null;
            VirtualFileSystem vfs=new VirtualFileSystem();
            vfs.setURI(vfsD);
            file_s=vfs.resolver(vfsD, vfs.getURI(), vfsD.getPath1());
            String extension=null;
            StringTokenizer st=new StringTokenizer(localpath,".");
            while(st.hasMoreTokens())
             extension=st.nextToken();
            try{
            Process p=Runtime.getRuntime().exec("qemu-img create -f "+extension+" -b "+localpath+" "+localrepository+uuid+"."+extension);
            p.waitFor();
            //logger.debug("X?X qui arrivo");
            }
            catch(Exception e){
                logger.error(e.getMessage());
            }
            String response=localrepository+uuid+"."+extension;
            this.sqlite.insertElementAtMap(file_s.getName().getURI(), response,"" ,"",LockFile.getLockType(lock));
            //map.put(file_s.getName(),response);
            //map1.put(response,"");
            //map1.put(response, "");
            //map1.put(response, lock);
            params.add("new");
            params.add(response);
            params.add("");
            params.add("");
            params.add(LockFile.lockMode.CR);
            return params;
             
        
}
}