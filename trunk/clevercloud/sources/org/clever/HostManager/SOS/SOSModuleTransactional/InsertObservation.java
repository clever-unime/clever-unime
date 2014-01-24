/*
 * The MIT License
 *
 * Copyright 2012 Università di Messina.
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
package org.clever.HostManager.SOS.SOSModuleTransactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.clever.HostManager.SOS.Database;
import org.clever.HostManager.SOS.ParameterContainer;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class InsertObservation {

    private Database db;
    private insertObsDomCleanParser iic;
    private ParameterContainer parameterContainer = null;
    private Logger logger;
    

    InsertObservation(String insertObservationRequest) {
        try {
            this.parameterContainer = ParameterContainer.getInstance();
            iic = new insertObsDomCleanParser();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            //File xmlFile = new File(filename);
            Document document = builder.parse(new ByteArrayInputStream(insertObservationRequest.getBytes()));
            iic.insertObsInfo(document);
            db = Database.getInstance();
            this.logger = parameterContainer.getLogger();
            

        } catch (SAXException ex) {
            logger.error("InsertObservation: SASException " + ex);
        } catch (IOException ex) {
            logger.error("InsertObservation: IOException " + ex);
        } catch (ParserConfigurationException ex) {
            logger.error("InsertObservation: ParserConfigurationException " + ex);

        }



    }

    public String insertObsdb() throws SQLException, ParseException, ParserConfigurationException, TransformerException {
        try{
        int sensidtemp = 0;
        int[] phenidtemp = new int[iic.getinfo().getObsPhenomena().size()];
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
        DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder2 = dbf2.newDocumentBuilder();
        Document doc = builder2.newDocument();
        //File file = new File(this.filename_output);
        Element root = doc.createElement("InsertObservationResponse");
        root.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");
        root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/om.xsd");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        String checksens = "SELECT `sensor_id` FROM `sensor` WHERE `unique_id` LIKE '" + iic.getinfo().getSensor_id() + "%'";
        ResultSet rs = db.exQuery(checksens);
        if (rs.next() == true) {
            sensidtemp = rs.getInt(1);
            int countphen = 0;
            //posso inserire un phenomeno composto solo se i phenomena da cui è formato sono presenti, in quanto vuol dire che vi è registrato un sensore per poterli misurare. se ciò non avviene allora c'è stato un errore. vedi manuale  di sos dove si esplicita che per inserire un osservazione ci deve essere il sensore i phenomeni già presenti nel db
            for (int i = 0; i < iic.getinfo().getObsPhenomena().size(); i++) {
                String querycheckphen = "SELECT `phenomenon_id` FROM `phenomenon` WHERE `unique_id` LIKE '" + iic.getinfo().getObsPhenomena().elementAt(i).getPhenomena_id().split(";")[0] + "'";
                rs = db.exQuery(querycheckphen);
                if (rs.next() == true) {
                    countphen++;
                    phenidtemp[i] = rs.getInt(1);
                }
            }
            for (int i = 0; i < iic.getinfo().getObsPhenomena().size(); i++) {
                String possibility = "SELECT * FROM `sens_phen` WHERE `phenomenon_id`LIKE '" + phenidtemp[i] + "' AND `sensor_id`LIKE '" + sensidtemp + "'";
                rs = db.exQuery(possibility);
                if (rs.next() == false) {
                    this.logger.debug("Phenomenon not associated to sensor");
                    return "";
                }

            }
            if (countphen == iic.getinfo().getObsPhenomena().size()) {
                if (iic.getinfo().getPhenomenon_composite().equals("") == false) {
                    String checkphencomp = "SELECT `composite_phenomenon_id` FROM `composite_phenomenon` WHERE `composite_unique_id` LIKE '" + iic.getinfo().getPhenomenon_composite().split(";")[0] + "';";
                    rs = db.exQuery(checkphencomp);
                    if (rs.next() == false) {
                        this.logger.debug("insertobds2");
                        String queryinsertcompphen = "INSERT INTO `sensorml`.`composite_phenomenon` (`composite_unique_id`, `composite_phenomenon_name`) VALUES ('" + iic.getinfo().getPhenomenon_composite().split(";")[0] + "', ' ' );";
                        db.exUpdate(queryinsertcompphen);
                        
                    }
                } else {
                    logger.debug("phenomeno non composito");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                Date insertDate = new Date();


                for (int i = 0; i < iic.getinfo().getObsPhenomena().size(); i++) {
                    //iic.getinfo().getTime_stamp()
                    String queryinsobs = "INSERT INTO `sensorml`.`observation`(`sensor_id`, `phenomenon_id`, `time_stamp`, `coordinate`, `uom_code`, `value`, `time_definition`, `lat_definition`, `long_definition`, `long_def_uom`, `lat_def_uom`) VALUES('" + sensidtemp + "','" + phenidtemp[i] + "',TIMESTAMP('" + iic.getinfo().getTime_stamp() + "'),GeomFromText('Point(" + iic.getinfo().getLongitude() + " " + iic.getinfo().getLatitude() + ")'),'" + iic.getinfo().getObsPhenomena().elementAt(i).getUom().split("=")[1].split(";")[0] + "','" + iic.getinfo().getObsPhenomena().elementAt(i).getValue() + "', '" + iic.getinfo().gettime_definition().split(";")[0] + "', '" + iic.getinfo().getlat_definition().split(";")[0] + "', '" + iic.getinfo().getlong_definition().split(";")[0] + "','" + iic.getinfo().getlong_uom().split("=")[1].split(";")[0] + "','" + iic.getinfo().getlat_uom().split("=")[1].split(";")[0] + "');";
                    db.exUpdate(queryinsobs);
                    String assigned_id = "SELECT `observation_id` FROM `observation` WHERE `sensor_id`LIKE'" + sensidtemp + "' AND `phenomenon_id` LIKE'" + phenidtemp[i] + "' AND `time_stamp` LIKE TIMESTAMP('" + sdf.format(insertDate).replace("T", " ") + "')";
                    db.exQuery(assigned_id);
                    if (rs.next()) {
                        Element assigned = doc.createElement("AssignedObservationId");
                        Text asstext = doc.createTextNode(rs.getString(1));
                        assigned.appendChild(asstext);
                        root.appendChild(assigned);
                    }
                    doc.appendChild(root);

                }

            } else {
                logger.debug("phenomena non tutti presenti");
            }
        } else {
            logger.debug("InsertObservation: sensore atto alle misure non presente");
            logger.debug("InsertObservation: registrare il sensore mediante operazione di RegiterSensor prima di inserire l'osservazione");
            return "";
        }
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);
        //db.closeDB();
        return stringWriter.getBuffer().toString();
        }catch(Exception e){
            logger.error("errore nell'inserimento dell'osservazione:"+e.getMessage());
            return "";
        }
    }

    void printInfo() {
        /*System.out.println("sensor id"+iic.getinfo().getSensor_id());
        System.out.println("istante "+iic.getinfo().getTime_stamp());
        System.out.println("latitude "+iic.getinfo().getLatitude()+" in "+iic.getinfo().getlat_uom());
        System.out.println("longitude "+iic.getinfo().getLongitude() +" in "+iic.getinfo().getlong_uom());
        System.out.println("latitude def "+iic.getinfo().getlat_definition());
        System.out.println("longitude def "+iic.getinfo().getlong_definition());
        System.out.println("time def "+iic.getinfo().gettime_definition());
        
        // System.out.println("phenomeno composito "+iic.getinfo().phenomenon_composite);
        for (int i=0;i<iic.getinfo().getObsPhenomena().size();i++)
        System.out.println("phen id:"+iic.getinfo().getObsPhenomena().elementAt(i).getPhenomena_id()+" uom:"+iic.getinfo().getObsPhenomena().elementAt(i).getUom()+" value:"+iic.getinfo().getObsPhenomena().elementAt(i).getValue());
        
         */
    }
}
