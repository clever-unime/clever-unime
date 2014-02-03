/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sensoracquisitiongenerator;


import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.*;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * @author Giuseppe Tricomi <giu.tricomi@gmail.com>
 */
public class SensoracquisitionGenerator {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Connection conn=null;
        Takeconfig tc=new Takeconfig();
        tc.init();
        connDB cdb=new connDB(tc);
        conn=cdb.makeCon();
        if(conn!=null){
            cdb.takeLastPhenId();
            if(tc.sensorstatgeneration.equalsIgnoreCase("true"))
            {   
                System.out.println("inserimento elementi sensori");
                cdb.ins( cdb.makeInSensQuery());
                cdb.ins( cdb.makeInMisureQuery());
            }
            
            int e=0;
            try{
                    Thread.sleep(5000);
                    
               }
               catch(Exception ex){}
            while(e<100){
               try{
                    Thread.sleep(tc.genInterval);
                    
               }
               catch(Exception ex){}
               cdb.ins( cdb.makeInObsQuery(tc.sensid,tc.phenid));//cdb.takeLastsensId(), cdb.takeLastPhenId( )));
               System.out.println(e);
               e++;
            }
        }
    }    
}
    class connDB{

        Connection conn=null; 
        Takeconfig tc=null;
    public connDB(Takeconfig tc) {
        this.tc=tc;
    }
    
       
    public void ins(/*Connection conn,*/String q){
        if(conn==null)
            this.makeCon();
        try
            {  
                Statement st=(Statement) conn.createStatement(); 
               st.executeUpdate(q); 
            }
            catch(SQLException e)
            {
                System.err.println("is not possible insert element in database."+e.getSQLState()+e.getMessage());
                
            }
            catch(Exception eg)
            {
                System.err.println("is not possible insert element in database."+eg.getMessage());
            }
        this.closeConnection();
        this.conn=null;
    }
    public void closeConnection(){
        try{
            if(!this.conn.isClosed())
                this.conn.close();
        }
        catch(java.sql.SQLException sqle){
            System.err.println("Error occurred when the function close for the connection to db: "
                    +". \nThis have generated an error code:'"+sqle.getErrorCode()+"' and an SQL state:"+sqle.getSQLState()
                    +". \nThe localized message is:"+sqle.getLocalizedMessage());
        }
        catch(Exception e){
            System.err.println("Error occurred when the function close for the connection to db:"
                    +". \nThe localized message is:"+e.getLocalizedMessage());
        }
    }
    public int takeLastsensId(/*Connection conn,*/)
    {
        int value;
        if(conn==null)
            this.makeCon();
        try{
            
        
        Statement st = (Statement) conn.createStatement(); 

        ResultSet rs=st.executeQuery("select idsensore_anagrafica from sensore_anagrafica where descrizione LIKE '%"+tc.sensName+"' order by idsensore_anagrafica desc");
        
        rs.first();
        value=rs.getInt("idsensore_anagrafica");
        }
        catch(SQLException e){
            System.err.println("select idsensore_anagrafica from sensore_anagrafica where descrizione LIKE '%"+tc.sensName+"' order by idsensore_anagrafica desc");
            System.err.println("isn't possible retrieve correct id sensor generated, for the generation of virtual observation will be used default value"+e.getSQLState()+e.getMessage());
            value=1;
        }
        //this.closeConnection();
        return value;
        
    }
    
    public int takeLastPhenId(/*Connection conn,*/)
    {
        int value;
        if(conn==null)
            this.makeCon();
        try{
            
        
        Statement st = (Statement) conn.createStatement(); 
        ResultSet rs=st.executeQuery("select idmisura_anagrafica from misura_anagrafica where tipo_misura LIKE '%"+tc.phenName+"' order by idmisura_anagrafica desc");
        rs.first();
        value=rs.getInt("idmisura_anagrafica");
        System.out.println(value+ " "+rs.getRow());
        }
        catch(SQLException e){
            System.err.println("select idmisura_anagrafica from misura_anagrafica where tipo_misura LIKE '%"+tc.phenName+"' order by idmisura_anagrafica desc");
            System.err.println("isn't possible retrieve correct id sensor generated, for the generation of virtual observation will be used default value"+e.getSQLState()+e.getMessage());
            value=1;
        }
        //this.closeConnection();
        return value;
    }
        
    public Connection makeCon(){
        try{
            conn= (Connection) DriverManager.getConnection("jdbc:mysql://"+tc.serverIP+"/sensordb?user="+tc.user+"&password="+tc.pass);
            
            return conn;
        }
        catch(SQLException e){
            System.err.println("connection with db is not possible! error:"+e.getSQLState()+"  "+e.getMessage());
            return null;
        }
    }
    public String makeInSensQuery(){
        String iq="INSERT INTO `sensore_anagrafica`( `latitudine`, `longitudine`, `altitudine`, `latitudine_uom`, `longitudine_uom`, `altitudine_uom`, `descrizione`, `id_tipo`, `costruttore`, `modello`, `intervallo_misura`, `intervallo_misura_uom`, `packet`, `tipo_frame`, `tipo_nodo`, `id_device`, `versione_firmware`, `operatorArea`, `active`, `mobile`) VALUES  (66.00, 12.00, 34.0,'deg','deg','meter'";
        iq=iq+",'Virtualsensor"+tc.sensName+"',"+"'VIRT','unime','virtmodel',60,'sec','Voltage"+tc.phenName+"','n','n',NULL,'firmvers','Almere','s','n')";
        //System.out.println(iq);
        return iq; 
    }
    
    public String makeInMisureQuery(){
        String iq="INSERT INTO `misura_anagrafica`(`id_uom_tipomisura`, `status`, `mobile`, `frequenza`, `frequenza_uom`, `tipo_misura`, `tipo_misura_uom`, `sensore_anagrafica_idsensore_anagrafica`) VALUES ('V',1,0,0.033,'Hz','Voltage"+tc.phenName+"','Volt',"+this.takeLastsensId()+")";
        //System.out.println(iq);
        return iq; 
    }
    //NB sono stati invertiti sens e phen fare attenzione
    public String makeInObsQuery(int sens,int phen){
        String iq="INSERT INTO `osservazioni`(`timestamp_osservazione`, `valore_osservato`, `misura_anagrafica_idmisura_anagrafica`, `sensore_anagrafica_idsensore_anagrafica`) VALUES (";
        Date d = new Date();
        String pDate="";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            pDate=dateFormat.format(d);
        }
        catch(Exception e)
        {
            System.err.println("error in parsing date");
        }
        int val=(int)(Math.random()*10000)%220;
        //System.out.println(val);
        iq=iq+"'"+pDate.toString()+"','"+val+"',"+sens+","+phen+")";
        System.out.println(iq);
        return iq; 
        
    }
}
