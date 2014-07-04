/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.Common.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Antonio Galletta 2014
 */
public class CalculateOnFieldParameterContainer {
    
   private String dbName,collectionName,fieldName;
   private OperationName opName;
   private long step,range;
   private TimeUnit unitStep,unitRange;
   private Calendar startDate,endDate;
 //  private final Logger logger;
   
   public CalculateOnFieldParameterContainer(String dbName,String collectionName,String fieldName,OperationName opName){
       this.dbName=dbName;
       this.collectionName=collectionName;
       this.fieldName=fieldName;
       this.opName=opName;
      // this.logger=Logger.getLogger("CalculateOnFieldParameterContainer");
   }
   
   public CalculateOnFieldParameterContainer(String dbName,String collectionName,String fieldName,OperationName opName,long step,TimeUnit unitStep,String date,String time,TimeZone timeZone,long range,TimeUnit unitRange, boolean start){
       this.dbName=dbName;
       this.collectionName=collectionName;
       this.fieldName=fieldName;
       this.opName=opName;
       this.step=step;
       this.unitStep=unitStep;
       if(start ==true){
            this.setStartDate(date, time, timeZone);
       }
       else{
           this.setEndDate(date, time, timeZone);
       }
       this.range=range;
       this.unitRange=unitRange;
       //this.logger=Logger.getLogger("CalculateOnFieldParameterContainer");
   }
   
   public CalculateOnFieldParameterContainer(String dbName,String collectionName,String fieldName,OperationName opName,long step,TimeUnit unitStep,String initialDate,String initialTime,String finalDate,String finalTime,TimeZone timeZone){
       this.dbName=dbName;
       this.collectionName=collectionName;
       this.fieldName=fieldName;
       this.opName=opName;
       this.step=step;
       this.unitStep=unitStep;
       this.setStartDate(initialDate, initialTime, timeZone);
       this.setEndDate(finalDate, finalTime, timeZone);
       //this.logger=Logger.getLogger("CalculateOnFieldParameterContainer");
   }
   
   public CalculateOnFieldParameterContainer(String dbName,String collectionName,String fieldName,OperationName opName,long step,TimeUnit unitStep,String initialDate,String initialTime,String finalDate,String finalTime,TimeZone timeZone,long range,TimeUnit unitRange){
       
       this.dbName=dbName;
       this.collectionName=collectionName;
       this.fieldName=fieldName;
       this.opName=opName;
       this.step=step;
       this.unitStep=unitStep;
       this.setStartDate(initialDate, initialTime, timeZone);
       this.setEndDate(finalDate, finalTime, timeZone);
       this.range=range;
       this.unitRange=unitRange;
       
       
   }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public OperationName getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName=OperationName.valueOf(opName);
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public long getRange() {
        return range;
    }

    public void setRange(long range) {
        this.range = range;
    }

    public TimeUnit getUnitStep() {
        return unitStep;
    }

    public void setUnitStep(TimeUnit unitStep) {
        this.unitStep = unitStep;
    }

    public TimeUnit getUnitRange() {
        return unitRange;
    }

    public void setUnitRange(TimeUnit unitRange) {
        this.unitRange = unitRange;
    }

    public long getStartDate() {
        if(startDate!=null){
            return startDate.getTimeInMillis();
            }
        else{
            return -1;
            }
        }

    public final void setStartDate(String initialDate,String initialTime){
        
        this.setStartDate(initialDate, initialTime,null);
    
    }
    
    public final void setStartDate(String initialDate,String initialTime,TimeZone timeZone) {
        SimpleDateFormat sdf=new SimpleDateFormat();
        
        startDate=Calendar.getInstance();
        
        if(timeZone==null){
            timeZone=TimeZone.getDefault();
            }
        
        sdf.setTimeZone(timeZone);
        sdf.setLenient(false);
        
            if(initialDate!=null&&initialTime!=null){
                sdf.applyPattern("dd/MM/yyyy - HH:mm:ss");
                try{
                    startDate.setTime(sdf.parse(initialDate+" - "+initialTime));
//                    logger.debug(startDate.getTime().toString());
                }
                catch(ParseException e){
                     startDate=null;
  //                logger.error("insert correct date",e);
                }
            }
            else
                if(initialDate!=null&&initialTime==null){
                sdf.applyPattern("dd/MM/yyyy");
                 try{
                    startDate.setTime(sdf.parse(initialDate));
    //                logger.debug(startDate.getTime().toString());
                }
                catch(ParseException e){
                     startDate=null;
      //              logger.error("insert correct date",e);
                }
            }
            else
                if(initialDate==null&&initialTime!=null){
                sdf.applyPattern("dd/MM/yyyy - HH:mm:ss");
                Calendar oggi=Calendar.getInstance();
                String today=oggi.get(Calendar.DAY_OF_MONTH)+"/"+(oggi.get(Calendar.MONTH)+1)+"/"+oggi.get(Calendar.YEAR)+" - "+initialTime;
                    
                try{
                    startDate.setTime(sdf.parse(today));
//                     logger.debug(startDate.getTime().toString());
                }
                catch(ParseException e){
                    //logger.error("insert correct date",e);
                    startDate=null;
                }
            }
            else{
                 startDate=null;  
                        }
             
    }

    public long getEndDate() {
        if(endDate!=null){
            return endDate.getTimeInMillis();
            }
        else{
            return -1;
            }
        }

    public final void setEndDate(String finalDate,String finalTime){
        
        this.setEndDate(finalDate, finalTime,null);
    
    }
    
    public final void setEndDate(String finalDate,String finalTime,TimeZone timeZone) {
        SimpleDateFormat sdf=new SimpleDateFormat();
      
        endDate=Calendar.getInstance();
        
        if(timeZone==null){
            timeZone=TimeZone.getDefault();
            }
        sdf.setTimeZone(timeZone);
        sdf.setLenient(false);
        
            if(finalDate!=null&&finalTime!=null){
                sdf.applyPattern("dd/MM/yyyy - HH:mm:ss");
                try{
                    endDate.setTime(sdf.parse(finalDate+" - "+finalTime));
//                    logger.debug(endDate.getTime().toString());
                }
                catch(ParseException e){
                    endDate=null;
  //                 logger.error("insert correct date",e);
                }
            }
            else
                if(finalDate!=null&&finalTime==null){
                sdf.applyPattern("dd/MM/yyyy");
                 try{
                    endDate.setTime(sdf.parse(finalDate));
    //                logger.debug(endDate.getTime().toString());
                }
                catch(ParseException e){
                    endDate=null;
      //              logger.error("insert correct date",e);
                }
            }
            else
                if(finalDate==null&&finalTime!=null){
                sdf.applyPattern("dd/MM/yyyy - HH:mm:ss");
                Calendar oggi=Calendar.getInstance();
                String today=oggi.get(Calendar.DAY_OF_MONTH)+"/"+(oggi.get(Calendar.MONTH)+1)+"/"+oggi.get(Calendar.YEAR)+" - "+finalTime;
        //            logger.debug("today "+today);
                try{
                    endDate.setTime(sdf.parse(today));
          //           logger.debug(endDate.getTime().toString());
                }
                catch(ParseException e){
            //       logger.error("insert correct date",e);
                    endDate=null;
                }
            }
             else{
                    endDate=null;
                
                }
    }

}
