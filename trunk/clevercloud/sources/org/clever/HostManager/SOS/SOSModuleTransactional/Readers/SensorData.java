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


public class SensorData {

  public String getData(String data, String phen){
    if(phen.indexOf("temp")!=-1){
        return getTemperature(data);
    }
    if(phen.indexOf("humidity")!=-1){
        return getHumidity(data);
    }   
    if(phen.indexOf("radiation")!=-1){
        return getLight1(data);
    }
    else
        return data;
  
  }

  public String getTemperature(String data) {
      //float valore=(float) (-39.6 + 0.01 * Float.valueOf(data).floatValue());
      //String temp=""+valore;
      String temp=data;
     return temp;
  }

  

  public String getHumidity(String data) {
      float valore=Float.valueOf(data);
      double v = (-2.0468+0.0367*valore)+0.00000159*(valore*valore);
      String temp="";
      if(v>100)
          temp=""+100;
      else
          temp=""+v;

     return temp;
  }

  public String getLight1(String data)
  {
        float valore=Float.valueOf(data);
        double v=10.0 * valore / 7.0;
        String temp=""+v;
     return temp;
  }

  



}


