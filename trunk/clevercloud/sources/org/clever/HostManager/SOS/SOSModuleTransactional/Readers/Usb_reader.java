/*
 * Copyright [2014] [Università di Messina]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional.Readers;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author user
 */
import org.clever.HostManager.SOS.SOSModuleTransactional.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.clever.HostManager.SOS.SOSModuleTransactional.InsertObservationXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.RegisterSensorXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.SOSmodule;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Usb_reader implements ReaderInterface {

    private int debug = 1;//0 no debug, 1 debug  
    public static final String SERIALDUMP_WINDOWS = "serialdump-windows.exe";
    public static final String SERIALDUMP_LINUX = "./serialdump-linux";
    //public Database db;
    public SensorData sensor;
    String sys = System.getProperty("os.name");
    String Xevent;
    private Process serialDumpProcess;
    private String comPort;
    private long resettime;
    private SOSmodule sosModule;
    Vector<Sensor_Struct> sens_nodeid;
    private int number_repeat;
    private int init = 1;

    Usb_reader() {
    }
//  public void init(String comPort,final long resettime, int number_repeat) {

    public void init(Node currentNode, SOSmodule sosModule) {
        this.sosModule = sosModule;
        parseElement(currentNode);

        //   number_repeat=Integer.getInteger(temp_repeat);
        //   resettime=Integer.getInteger(temp_resettime);
        System.out.println("comPort:" + this.comPort + "repeat:" + this.number_repeat + " reset  time:" + this.resettime);
        this.sens_nodeid = new Vector<Sensor_Struct>(1);

        //  db = new Database();
        sensor = new SensorData();
        if (debug == 1) {
            System.out.println("Listening on COM port: " + comPort);
        }

        /* Connect to COM using external serialdump application */
        String fullCommand;

        if (sys.contains("Windows")) {
            fullCommand = SERIALDUMP_WINDOWS + " " + "-b115200" + " " + comPort;

        } else {
            fullCommand = SERIALDUMP_LINUX + " " + "-b115200" + " " + comPort;
        }

        try {
            serialDumpProcess = Runtime.getRuntime().exec("make login", null, new File(System.getProperty("user.home") + "/contiki-2.4/examples/sky-shell/"));
            // String[] cmd = fullCommand.split(" ");

            //   serialDumpProcess = Runtime.getRuntime().exec(cmd);
            final BufferedReader input = new BufferedReader(new InputStreamReader(serialDumpProcess.getInputStream()));
            final BufferedReader err = new BufferedReader(new InputStreamReader(serialDumpProcess.getErrorStream()));
            final PrintWriter output = new PrintWriter(new OutputStreamWriter(serialDumpProcess.getOutputStream()));
            /*thread to collect sensor info*/
            Thread readInput = new Thread(new Runnable() {

                public void run() {

                    String line;
                    try {
                        //fase di conoscenza della rete - acquisizione dei nodi



                        Thread.sleep((int) 60000);
                        while (true) {
                            Discovery_nodes();
                            if (init == 1) {
                                RegisterInfo();
                                init = 0;
                            }
                            if (debug == 1) {
                                System.out.println("netcmd sendersend");
                            }
                            output.println("netcmd sendersend");
                            output.flush();
                            //System.out.println(input.readLine());
                            if (debug == 1) {
                                System.out.println("sinkrecv");
                            }

                            output.println("sinkrecv");
                            output.flush();

                            long currenttime = new GregorianCalendar().getTimeInMillis();
                            //   System.out.println("time:"+currenttime);
                            while ((new GregorianCalendar().getTimeInMillis()) < (currenttime + resettime)) {
                                //     System.out.println("time in while:"+new GregorianCalendar().getTimeInMillis());
                                line = input.readLine();
                                if (debug == 1) {
                                    System.out.println("info " + line + " t: " + new GregorianCalendar().getTimeInMillis());
                                }
                                // parseIncomingLine(line);

                                if (line != null) {
                                    if (line.indexOf("@") != -1) {
                                        //     System.out.println("info non null "+line);
                                        parseIncomingLine(line, "netcmd sendersend");

                                    }
                                }
                            }
                        }
                        /* System.out.println("Serialdump process shut down, exiting");
                        input.close();*/

                    } catch (InterruptedException ex) {
                        Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException e) {
                        System.err.println("Exception when reading from serialdump");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }, "read input stream thread");


            readInput.start();

        } catch (Exception e) {
            System.err.println("Exception when executing '" + fullCommand + "'");
            System.err.println("Exiting application");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void sink_recv_init(String cmd) {
        try {
            String line;
            final BufferedReader input = new BufferedReader(new InputStreamReader(serialDumpProcess.getInputStream()));
            final PrintWriter output = new PrintWriter(new OutputStreamWriter(serialDumpProcess.getOutputStream()));
            output.println(cmd);
            output.flush();
            if (debug == 1) {
                System.out.println("sinkrecvinit");
            }
            String[] data;
            output.println("sinkrecvinit");
            output.flush();
            int count = 0;

            //contatore count_gen per scorrere il tempo, dopo il quale esco dal ciclo anche se nn ho tutte le informazioni da tutti i sensori, così eventualmente riinvio il comando
            while (true) {



                line = input.readLine();

                if (line != null) {
                    if (debug == 1) {
                        System.out.println("linea " + line);
                    }
                    if (line.indexOf("@") != -1) {
                        if (debug == 1) {
                            System.out.println(" " + line + " t: " + new GregorianCalendar().getTimeInMillis());
                        }
                        parseIncomingLine(line, cmd);

                    } else if (line.indexOf("Sky Contiki shell") != -1) {

                        break;

                    } else if (line.indexOf("end") != -1) {
                        break;
                    }
                }
            }
            check(cmd);


        } catch (IOException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        }





    }

    public void parseIncomingLine(String line, String cmd) {
        System.out.println(line);
        if (line == null) {
            System.err.println("Parsing null line");
            return;
        }

        /* Spli    if(line.indexOf("sensor")>-1){t line into components */
        if (line.indexOf("@sensor") > -1) {
            String temp_id = line.split("@")[0].trim();
            for (int i = 0; i < sens_nodeid.size(); i++) {
                if (sens_nodeid.elementAt(i).getSensor_info().getid().equals(temp_id)) {
                    if (debug == 1) {
                        System.out.println("info nodo" + temp_id);
                    }
                    if (line.split("@")[2].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().settype_id(line.split("@")[2].trim());
                    }
                    if (line.split("@")[3].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setproduct_description(line.split("@")[3].trim());
                    }
                    if (line.split("@")[4].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setmanufacturer(line.split("@")[4].trim());
                    }
                    if (line.split("@")[5].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setmodel(line.split("@")[5].trim());
                    }
                    if (line.split("@")[6].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setoperator_area(line.split("@")[6].trim());
                    }


                }

            }

            /*
            for(int i=0;i<sens_nodeid.size();i++){
            if(sens_nodeid.elementAt(i).getid().equals("")==true)
            {
            System.out.println("richiamo "+cmd);
            sink_recv_init(cmd);   
            }
            }*/

        } else if (line.indexOf("@capabilities") > -1) {
            String temp_id = line.split("@")[0].trim();
            for (int i = 0; i < sens_nodeid.size(); i++) {
                if (sens_nodeid.elementAt(i).getSensor_info().getid().equals(temp_id)) {
                    if (debug == 1) {
                        System.out.println("info nodo" + temp_id);
                    }
                    if (line.split("@")[2].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setmeasures_interval(line.split("@")[2].trim());
                    }
                    if (line.split("@")[3].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setmeasures_interval_uom(line.split("@")[3].trim());
                    }
                    if (line.split("@")[4].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setactive(line.split("@")[4].trim());
                    }
                    if (line.split("@")[5].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setmobile(line.split("@")[5].trim());
                    }


                }

            }

        } else if (line.indexOf("@classifier") > -1) {
            String temp_id = line.split("@")[0].trim();
            for (int i = 0; i < sens_nodeid.size(); i++) {
                if (sens_nodeid.elementAt(i).getSensor_info().getid().equals(temp_id)) {
                    if (debug == 1) {
                        System.out.println("info nodo" + temp_id);
                    }
                    if (line.split("@")[2].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setclass_application(line.split("@")[2].trim());
                    }
                    if (line.split("@")[3].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setpacket(line.split("@")[3].trim());
                    }

                }

            }

        } else if (line.indexOf("@position") > -1) {
            String temp_id = line.split("@")[0].trim();
            for (int i = 0; i < sens_nodeid.size(); i++) {
                if (sens_nodeid.elementAt(i).getSensor_info().getid().equals(temp_id)) {
                    if (debug == 1) {
                        System.out.println("info nodo" + temp_id);
                    }
                    if (line.split("@")[2].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setref(line.split("@")[2].trim());
                    }
                    if (line.split("@")[3].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setalt_uom(line.split("@")[3].trim());
                    }
                    if (line.split("@")[4].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setlat_uom(line.split("@")[4].trim());
                    }
                    if (line.split("@")[5].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setlong_uom(line.split("@")[5].trim());
                    }
                    if (line.split("@")[6].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setalt_val(line.split("@")[6].trim());
                    }
                    if (line.split("@")[7].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setlat_val(line.split("@")[7].trim());
                    }
                    if (line.split("@")[8].trim() != null) {
                        sens_nodeid.elementAt(i).getSensor_info().setlong_val(line.split("@")[8].trim());
                    }


                }

            }

        } else if (line.indexOf("@phen") > -1) {
            String temp_id = line.split("@")[0].trim();
            for (int i = 0; i < sens_nodeid.size(); i++) {
                if (sens_nodeid.elementAt(i).getSensor_info().getid().equals(temp_id)) {
                    Sensor_Phenomena tp = new Sensor_Phenomena();
                    if (line.split("@")[2].trim() != null) {
                        tp.setphen_id(line.split("@")[2].trim());
                    }
                    if (line.split("@")[3].trim() != null) {
                        tp.setphen_descr(line.split("@")[3].trim());
                    }
                    if (line.split("@")[4].trim() != null) {
                        tp.setphen_uom(line.split("@")[4].trim());
                    }
                    if (line.split("@")[5].trim() != null) {
                        tp.setphen_uom_id(line.split("@")[5].trim());
                    }
                    if (line.split("@")[6].trim() != null) {
                        tp.setoffering_id(line.split("@")[6].trim());
                    }
                    if (sens_nodeid.elementAt(i).getSensor_Phenomena().contains(tp) == false) {
                        sens_nodeid.elementAt(i).getSensor_Phenomena().add(tp);
                    }

                }
            }

        } else if (line.indexOf("@comp") > -1) {
            String temp_id = line.split("@")[0].trim();
            for (int i = 0; i < sens_nodeid.size(); i++) {
                if (sens_nodeid.elementAt(i).getSensor_info().getid().equals(temp_id)) {
                    Sensor_Component tc = new Sensor_Component();
                    if (line.split("@")[2].trim() != null) {
                        tc.setcomp_id(line.split("@")[2].trim());
                    }
                    if (line.split("@")[3].trim() != null) {
                        tc.setcomp_descr(line.split("@")[3].trim());
                    }
                    if (line.split("@")[4].trim() != null) {
                        tc.setcomp_phenomena(line.split("@")[4].trim());
                    }
                    if (line.split("@")[5].trim() != null) {
                        tc.setcomp_status(line.split("@")[5].trim());
                    }


                    if (sens_nodeid.elementAt(i).getSensor_Component().contains(tc) == false) {
                        sens_nodeid.elementAt(i).getSensor_Component().add(tc);
                    }

                }
            }

        } else if (cmd.indexOf("sendersend") != -1) {


            String[] components = line.split("@");
            if (debug == 1) {
                System.out.println("comp length:" + components.length);
            }


            for (int i = 0; i < sens_nodeid.size(); i++) {
                if (sens_nodeid.elementAt(i).getSensor_info().getid().trim().equals(components[0].trim())) {
                    for (int j = 1; j < components.length; j++) {
                        InsertObservationXml ios = new InsertObservationXml(sens_nodeid.elementAt(i), components[0].trim(), sosModule);
                        ios.writexml(components[j].trim(), sens_nodeid.elementAt(i).getSensor_info().getpacket().split("_")[j - 1].trim());

                    }
                }
            }
        }

    }

    public void Discovery_nodes() {

        String line = new String();
        String cmd;

        final PrintWriter output = new PrintWriter(new OutputStreamWriter(serialDumpProcess.getOutputStream()));
        //fase di conoscenza della rete - acquisizione dei nodi
        for (int i = 0; i < number_repeat; i++) {
            try {
                String temp = "";
                if (debug == 1) {
                    System.out.println("netcmd sendernodeid" + i + " t: " + new GregorianCalendar().getTimeInMillis());
                }
                output.println("netcmd sendernodeid");
                output.flush();
                //System.out.println(input.readLine());
                // System.out.println("sinkrecv");
                output.println("sinkrecvinit");
                output.flush();
                int count_gen = 0;

                while (true) {
                    final BufferedReader input = new BufferedReader(new InputStreamReader(serialDumpProcess.getInputStream()));
                    count_gen++;

                    line = input.readLine();

                    if (line != null) {
                        if (debug == 1) {
                            System.out.println("linea " + line);
                        }
                        if (line.indexOf("@") != -1) {
                            if (debug == 1) {
                                System.out.println(" " + line);
                            }
                            temp = line.split("@")[0].trim();
                            if (debug == 1) {
                                System.out.println("node " + temp);
                            }
                            int flag_node = 0;
                            for (int j = 0; j < sens_nodeid.size(); j++) {
                                if (sens_nodeid.elementAt(j).getSensor_info().getid().indexOf(temp) != -1) {
                                    flag_node = 1;
                                    if (debug == 1) {
                                        System.out.println("node control " + temp);
                                    }
                                }
                            }
                            if (flag_node == 0) {
                                Sensor_Struct tempsens = new Sensor_Struct();
                                tempsens.getSensor_info().setid(temp);
                                if (debug == 1) {
                                    System.out.println("node write" + temp);
                                }
                                sens_nodeid.add(tempsens);
                            }
                        } else if (line.indexOf("Sky Contiki shell") != -1) {
                            break;

                        } else if (line.indexOf("end") != -1) {
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //verifica nodi acquisiti
        for (int i = 0; i < sens_nodeid.size(); i++) {
            if (debug == 1) {
                System.out.println("node " + sens_nodeid.elementAt(i).getSensor_info().getid());
            }
        }
        if (init == 0) {
            check("netcmd senderinfo " + new GregorianCalendar().getTimeInMillis());
            check("netcmd senderposition " + new GregorianCalendar().getTimeInMillis());
            check("netcmd sendercapab " + new GregorianCalendar().getTimeInMillis());
            check("netcmd sendercomp " + new GregorianCalendar().getTimeInMillis());
            check("netcmd senderphen " + new GregorianCalendar().getTimeInMillis());
            check("netcmd senderclass " + new GregorianCalendar().getTimeInMillis());
            for (int i = 0; i < sens_nodeid.size(); i++) {
                try {
                    RegisterSensorXml rsx = new RegisterSensorXml(sens_nodeid.elementAt(i), i, sosModule);
                    rsx.write_descrsens_xml();
                } catch (SQLException ex) {
                    Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void RegisterInfo() {
        try {
            String line = new String();
            String cmd;

            final PrintWriter output = new PrintWriter(new OutputStreamWriter(serialDumpProcess.getOutputStream()));

            /*
            senderclass: node sender sends its classifier
            sendercomp: node sender sends its component sensors
            senderinfo: node sender sends its info
            sendernodeid: node sender sends its node id
            senderphen: node sender sends its observable property
            senderposition: node sender sends its position
            sendersend: node sender sends its info and observations
             */
            //ottenimento info sul sensore
            cmd = "netcmd senderinfo";
            if (debug == 1) {
                System.out.println(cmd + " " + new GregorianCalendar().getTimeInMillis());
            }
            sink_recv_init(cmd);
            //position
            cmd = "netcmd senderposition";
            if (debug == 1) {
                System.out.println(cmd + " " + new GregorianCalendar().getTimeInMillis());
            }
            sink_recv_init(cmd);
            //capabilities
            cmd = "netcmd sendercapab";
            if (debug == 1) {
                System.out.println(cmd + " " + new GregorianCalendar().getTimeInMillis());
            }
            sink_recv_init(cmd);
            //component
            cmd = "netcmd sendercomp";
            if (debug == 1) {
                System.out.println(cmd + " " + new GregorianCalendar().getTimeInMillis());
            }
            sink_recv_init(cmd);
            //phenomenon
            cmd = "netcmd senderphen";
            if (debug == 1) {
                System.out.println(cmd + " " + new GregorianCalendar().getTimeInMillis());
            }
            sink_recv_init(cmd);
            //classifier
            cmd = "netcmd senderclass";
            if (debug == 1) {
                System.out.println(cmd + " " + new GregorianCalendar().getTimeInMillis());
            }
            sink_recv_init(cmd);
            for (int i = 0; i < sens_nodeid.size(); i++) {
                for (int j = 0; j < sens_nodeid.elementAt(i).getSensor_Phenomena().size(); j++) {
                    if (debug == 1) {
                        System.out.println("phen check for:" + sens_nodeid.elementAt(i).getSensor_info().getid() + " " + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getoffering_id());
                    }
                }
            }
            for (int i = 0; i < sens_nodeid.size(); i++) {
                RegisterSensorXml rsx = new RegisterSensorXml(sens_nodeid.elementAt(i), i, sosModule);
                rsx.write_descrsens_xml();
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Usb_reader.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private void check(String cmd) {

        if (cmd.indexOf("sendinit") != -1) {
            if (cmd.indexOf("sendinitinfo") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    if (sens_nodeid.elementAt(i).getSensor_info().gettype_id().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getproduct_description().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmanufacturer().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmodel().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getoperator_area().equals("") == true) {
                        sink_recv_init(cmd);
                    } else {
                        System.out.println("check " + cmd + " " + cmd.split(" ")[1] + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().gettype_id() + "," + sens_nodeid.elementAt(i).getSensor_info().getproduct_description() + "," + sens_nodeid.elementAt(i).getSensor_info().getmanufacturer() + "," + sens_nodeid.elementAt(i).getSensor_info().getmodel() + "," + sens_nodeid.elementAt(i).getSensor_info().getoperator_area());
                    }
                }
            }

            if (cmd.indexOf("sendinitclass") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    if (sens_nodeid.elementAt(i).getSensor_info().getclass_application().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getpacket().equals("") == true) {
                        sink_recv_init(cmd);
                    } else {
                        System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().getclass_application());
                    }
                }
            }

            if (cmd.indexOf("sendinitposition") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {

                    if (sens_nodeid.elementAt(i).getSensor_info().getref().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getalt_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getalt_val().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlat_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlat_val().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlong_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlong_val().equals("") == true) {
                        sink_recv_init(cmd);
                    } else {
                        System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().getref() + "," + sens_nodeid.elementAt(i).getSensor_info().getalt_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getalt_val() + "," + sens_nodeid.elementAt(i).getSensor_info().getlat_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getlat_val() + "," + sens_nodeid.elementAt(i).getSensor_info().getlong_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getlong_val());
                    }
                }
            }

            if (cmd.indexOf("sendinitcapab") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    if (sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getactive().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmobile().equals("") == true) {
                        sink_recv_init(cmd);
                    } else {
                        System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval() + "," + sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getactive() + "," + sens_nodeid.elementAt(i).getSensor_info().getmobile());
                    }
                }
            }

            if (cmd.indexOf("sendinitphen") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    int flag_c = 0;
                    if (sens_nodeid.elementAt(i).getSensor_Phenomena().isEmpty() == true) {
                        flag_c = 1;
                    } else {
                        for (int j = 0; j < sens_nodeid.elementAt(i).getSensor_Phenomena().size(); j++) {
                            if (sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_id().equals("") == true || sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_descr().equals("") == true || sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom_id().equals("") == true) {
                                flag_c = 1;
                            } else {
                                System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_id() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_descr() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom_id());
                            }
                        }
                    }
                    if (flag_c == 1) {
                        System.out.println("netcmd sendinitphen " + sens_nodeid.elementAt(i).getSensor_info().getid());
                        sink_recv_init("netcmd sendinitphen " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    }
                }
            }
            if (cmd.indexOf("sendinitcomp") != -1) {
                System.out.println("check " + cmd);

                for (int i = 0; i < sens_nodeid.size(); i++) {
                    int flag_c = 0;
                    if (sens_nodeid.elementAt(i).getSensor_Component().isEmpty() == true) {
                        flag_c = 1;
                    } else {
                        for (int j = 0; j < sens_nodeid.elementAt(i).getSensor_Component().size(); j++) {
                            if (sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_id().equals("") == true || sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_descr().equals("") == true || sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_phenomena().equals("") == true || sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_status().equals("") == true) {
                                flag_c = 1;
                            } // sink_recv_init(cmd);
                            else {
                                System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_id() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_descr() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_phenomena() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_status());
                            }
                        }
                    }
                    if (flag_c == 1) {
                        System.out.println("netcmd sendinitcomp " + sens_nodeid.elementAt(i).getSensor_info().getid());
                        sink_recv_init("netcmd sendinitcomp " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    }
                }
            }
        } else {
            if (cmd.indexOf("senderinfo") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    if (sens_nodeid.elementAt(i).getSensor_info().gettype_id().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getproduct_description().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmanufacturer().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmodel().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getoperator_area().equals("") == true) {
                        sink_recv_init("netcmd sendinitinfo " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    } // sink_recv_init(cmd);
                    else {
                        System.out.println("check netcmd " + sens_nodeid.elementAt(i).getSensor_info().getid() + " " + cmd.split(" ")[1] + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().gettype_id() + "," + sens_nodeid.elementAt(i).getSensor_info().getproduct_description() + "," + sens_nodeid.elementAt(i).getSensor_info().getmanufacturer() + "," + sens_nodeid.elementAt(i).getSensor_info().getmodel() + "," + sens_nodeid.elementAt(i).getSensor_info().getoperator_area());
                    }
                }
            }

            if (cmd.indexOf("senderclass") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    if (sens_nodeid.elementAt(i).getSensor_info().getclass_application().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getpacket().equals("") == true) {
                        sink_recv_init("netcmd sendinitclass " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    } // sink_recv_init(cmd);
                    else {
                        System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().getclass_application());
                    }
                }
            }

            if (cmd.indexOf("senderposition") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {

                    if (sens_nodeid.elementAt(i).getSensor_info().getref().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getalt_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getalt_val().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlat_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlat_val().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlong_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getlong_val().equals("") == true) {
                        sink_recv_init("netcmd sendinitposition " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    } // sink_recv_init(cmd);
                    else {
                        System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().getref() + "," + sens_nodeid.elementAt(i).getSensor_info().getalt_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getalt_val() + "," + sens_nodeid.elementAt(i).getSensor_info().getlat_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getlat_val() + "," + sens_nodeid.elementAt(i).getSensor_info().getlong_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getlong_val());
                    }
                }
            }

            if (cmd.indexOf("sendercapab") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    if (sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getactive().equals("") == true || sens_nodeid.elementAt(i).getSensor_info().getmobile().equals("") == true) {
                        sink_recv_init("netcmd sendinitcapab " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    } // sink_recv_init(cmd);
                    else {
                        System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval() + "," + sens_nodeid.elementAt(i).getSensor_info().getmeasures_interval_uom() + "," + sens_nodeid.elementAt(i).getSensor_info().getactive() + "," + sens_nodeid.elementAt(i).getSensor_info().getmobile());
                    }
                }
            }

            if (cmd.indexOf("senderphen") != -1) {
                System.out.println("check " + cmd);
                for (int i = 0; i < sens_nodeid.size(); i++) {
                    int flag_c = 0;
                    if (sens_nodeid.elementAt(i).getSensor_Phenomena().isEmpty() == true) {
                        flag_c = 1;
                    } else {
                        for (int j = 0; j < sens_nodeid.elementAt(i).getSensor_Phenomena().size(); j++) {
                            if (sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_id().equals("") == true || sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_descr().equals("") == true || sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom().equals("") == true || sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom_id().equals("") == true) {
                                flag_c = 1;
                            } else {
                                System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_id() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_descr() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom() + "," + sens_nodeid.elementAt(i).getSensor_Phenomena().elementAt(j).getphen_uom_id());
                            }
                        }
                    }
                    if (flag_c == 1) {
                        System.out.println("netcmd sendinitphen " + sens_nodeid.elementAt(i).getSensor_info().getid());
                        sink_recv_init("netcmd sendinitphen " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    }
                }
            }
            if (cmd.indexOf("sendercomp") != -1) {
                System.out.println("check " + cmd);

                for (int i = 0; i < sens_nodeid.size(); i++) {
                    int flag_c = 0;
                    if (sens_nodeid.elementAt(i).getSensor_Component().isEmpty() == true) {
                        flag_c = 1;
                    } else {
                        for (int j = 0; j < sens_nodeid.elementAt(i).getSensor_Component().size(); j++) {
                            if (sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_id().equals("") == true || sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_descr().equals("") == true || sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_phenomena().equals("") == true || sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_status().equals("") == true) {
                                flag_c = 1;
                            } // sink_recv_init(cmd);
                            else {
                                System.out.println("check " + cmd + " collect info:" + sens_nodeid.elementAt(i).getSensor_info().getid() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_id() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_descr() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_phenomena() + "," + sens_nodeid.elementAt(i).getSensor_Component().elementAt(j).getcomp_status());
                            }
                        }
                    }
                    if (flag_c == 1) {
                        System.out.println("netcmd sendinitcomp " + sens_nodeid.elementAt(i).getSensor_info().getid());
                        sink_recv_init("netcmd sendinitcomp " + sens_nodeid.elementAt(i).getSensor_info().getid());
                    }
                }
            }
        }

    }

    private void parseElement(Node currentNode) {
        short sNodeType = currentNode.getNodeType();
        //Se è di tipo Element ricavo le informazioni e le stampo
        if (sNodeType == Node.ELEMENT_NODE) {
            String sNodeValue = utils.searchTextInElement(currentNode);
            //per ogni componente 
            if (currentNode.getNodeName().equals("comPort")) {
                this.comPort = utils.searchTextInElement(currentNode).trim();
                currentNode = currentNode.getNextSibling();
            }
            if (currentNode.getNodeName().equals("resettimeMillisec")) {
                System.out.println("sNodeValue: " + utils.searchTextInElement(currentNode));
                this.resettime = Integer.parseInt(utils.searchTextInElement(currentNode).trim());
                currentNode = currentNode.getNextSibling();
            }
            if (currentNode.getNodeName().equals("numberRepeat")) {
                System.out.println("sNodeValue: " + utils.searchTextInElement(currentNode));
                this.number_repeat = Integer.parseInt(utils.searchTextInElement(currentNode).trim());
                currentNode = currentNode.getNextSibling();
            }
        }
        int iChildNumber = currentNode.getChildNodes().getLength();
        //Se non si tratta di una foglia continua l'esplorazione
        if (currentNode.hasChildNodes()) {
            NodeList nlChilds = currentNode.getChildNodes();
            for (int iChild = 0; iChild < iChildNumber; iChild++) {
                parseElement(nlChilds.item(iChild));
            }
        }
    }
     @Override
    public void parseIncomingLine(String a,String line,String t, Object cmd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
