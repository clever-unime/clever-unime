/*
 * Copyright 2014 Università di Messina
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
/*
 * The MIT License
 *
 * Copyright 2012 alessiodipietro.
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
package org.clever.HostManager.SOS.SOSModuleTransactional.Readers;

import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.TestReader;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Sensor_Struct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Random;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.apache.log4j.Logger;
import org.clever.HostManager.SOS.Database;
import org.clever.HostManager.SOS.ParameterContainer;
import org.clever.HostManager.SOS.SOSModuleTransactional.InsertObservationXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.SOSmodule;
import org.clever.HostManager.SOS.SOSModuleTransactional.StdRandom;

/**
 *
 * @author alessiodipietro
 */
public class TestReaderThread extends Thread {
    private Logger loggerf = null;
    private TestReader testReader;
    private double lambda;
    private Sensor_Struct sensorStruct;
    private SOSmodule sosModule;
    private Random generator = new Random();
    private Database testDatabase;
    private ParameterContainer parameterContainer=new ParameterContainer();
    private ResultSet rs;    
    private Object monitor;
    public TestReaderThread(Object monitor,Sensor_Struct sensorStruct, TestReader testReader, Double lambda, SOSmodule sosModule) {
        try {
            this.testReader = testReader;
            this.lambda = lambda;
            this.sensorStruct = sensorStruct;
            this.sosModule = sosModule;
            parameterContainer=ParameterContainer.getInstance();
            testDatabase=Database.getTestInstance(parameterContainer.getTestDbServer(),parameterContainer.getTestDbDriver(),
                                                  parameterContainer.getTestDbName(),
                                                  parameterContainer.getTestDbUsername(),parameterContainer.getTestDbPassword());
            //database.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
            //         this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());
            InetAddress localmachine = InetAddress.getLocalHost();
            String hostname = localmachine.getHostName();
            String query="SELECT `observation` "
                       + "FROM `test_reader_observation` "
                       + "WHERE `sensor_id`="+sensorStruct.getSensor_info().getid()+" AND `hostname`='"+hostname+"' ";
            this.monitor=monitor; 
            loggerf= Logger.getLogger(hostname);
            loggerf.debug("thread "+hostname);
            rs=testDatabase.exQuery(query);
            /*database.exQuery(database.getStatement(), "SELECT `observation` "
                                                    + "FROM `test_reader_observation` "
                                                    );*/
            
            
            //this.rs=database.getResultSet();
            
            
            //database.closeDB();
        } catch (UnknownHostException ex) {
            parameterContainer.getLogger().error("Error getting observations from pool");
        }
    }

    @Override
    public void run() {
        synchronized(monitor){
            try {
                monitor.wait();
            } catch (InterruptedException ex) {
                //Logger.getLogger(TestReaderThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            while (rs.next()) {
                try {
                    sleep((long)((StdRandom.exp(lambda)+1)*1000));
                    //for each component     
                    for (int i = 0; i < sensorStruct.getSensor_Component().size(); i++) {
                        sensorStruct.getSensor_Component().elementAt(i);
                        //generate the observation
                        //String observation = Float.toString(generator.nextFloat() * 100000);
                        String observation=Long.toString(rs.getLong(1));
                        //insert the observation          
                        InsertObservationXml ios = new InsertObservationXml(sensorStruct, sensorStruct.getSensor_info().getid(), sosModule);
                        ios.writexml(observation, sensorStruct.getSensor_info().getpacket().split("_")[i].trim());
                        loggerf.debug("inserita observation:"+observation);
                    }
                    
                    
                    //sleep((long)StdRandom.exp(lambda));
                    //System.out.println("RANDOM NUMBER:"+(long)((StdRandom.exp(lambda)+1)*1000)+" lambda:"+lambda);
                    //wait (exponential distribution of waiting time)
                    
                } catch (InterruptedException ex) {
                    //Logger.getLogger(TestReaderThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //database.closeDB();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        

    }
}
