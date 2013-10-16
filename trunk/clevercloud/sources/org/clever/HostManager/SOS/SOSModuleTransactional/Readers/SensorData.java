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


