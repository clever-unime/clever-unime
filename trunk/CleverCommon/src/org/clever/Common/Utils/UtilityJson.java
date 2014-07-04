/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.Common.Utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


/**
 *
 * @author Antonino Galletta 2014
 */
public class UtilityJson {
    private Logger logger;
    
    public UtilityJson(){
    
        logger=Logger.getLogger("UtilityJson");
    }
   
    public ArrayList XMLMultiLevelToJSON2(String XMLString) throws FileNotFoundException, IOException{
      
      Document document;
      SAXBuilder builder = new SAXBuilder();
      Element rootElement;
      HashMap mapObject=new HashMap();
      HashMap mapField=new HashMap();
      ArrayList<BasicDBObject> lista=new ArrayList();
      HashMap mapAttr=new HashMap();
      
   try{
      document = builder.build( new StringReader( XMLString ) );
      rootElement = document.getRootElement();
     this.funzRicorsiva(rootElement, mapObject,mapField,mapAttr, "");
      }
        catch ( IOException ex ){
     logger.error( "Error while opening the file xml: " + ex );
    }
    catch(Exception ex){
     logger.error( "Error while opening the file xml: " + ex );
    }
    
    finally{
       lista.add(0, this.createDBFieldOBject(mapField,"campi"));
       lista.add(1, this.createDBFieldOBject(mapAttr,"attributi"));
       lista.add(2, this.createDBOBject(mapObject));
       return lista;
    }
    
  }
    
    private BasicDBObject createDBOBject(HashMap mapObject){
        Entry entryObject;
        Iterator<Map.Entry> itEntryObject;
        BasicDBObject obj=new BasicDBObject();
     
        itEntryObject=mapObject.entrySet().iterator();
     
   while(itEntryObject.hasNext()){
          entryObject=itEntryObject.next();
          obj.append((String)entryObject.getKey(), entryObject.getValue());
           }
            obj.append("insertTimestamp", Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
            obj.append("timeZone", "GMT");
            return obj;
    }
    
    private BasicDBObject createDBFieldOBject(HashMap map,String id){
        Entry entry;
        Iterator<Map.Entry> itEntry;
        BasicDBObject obj=new BasicDBObject();
        HashMap mapName=new HashMap();
        BasicDBList dbList;
        
         itEntry=map.entrySet().iterator();
     
   while(itEntry.hasNext()){
          entry=itEntry.next();
          
          if(mapName.get(entry.getValue())==null){
              dbList=new BasicDBList();
              dbList.add(entry.getKey());
              mapName.put(entry.getValue(), dbList);
            }
          else {
              dbList=(BasicDBList)mapName.get(entry.getValue());
              dbList.add(entry.getKey());
            }
      }
            obj.append("_id", id);
            itEntry=mapName.entrySet().iterator();
            
      while(itEntry.hasNext()){
          entry=itEntry.next();
          obj.append((String)entry.getKey(), entry.getValue());
            }
      return obj;
    }
    
  private void funzRicorsiva(Element elem,HashMap mapObject,HashMap mapField,HashMap mapAttr,String tmpString){
        
        List<Element> listaElem=elem.getChildren();
        List<Attribute> listAttr=elem.getAttributes();
        Iterator iterElem;
        double value;
        String modifiedString,tagName=elem.getName(),nomeTag;
        
        nomeTag=tagName.replace(".", "<_>");
        
       if(!listAttr.isEmpty()){
            this.addAttribute(listAttr, mapObject,mapAttr,tmpString+"/"+nomeTag);
            }
        if(!listaElem.isEmpty()){
            iterElem=listaElem.iterator();
            while(iterElem.hasNext()){
            this.funzRicorsiva(((Element)iterElem.next()), mapObject,mapField,mapAttr, tmpString+"/"+nomeTag);
                 }
         }
        else{
            try{ 
                modifiedString=elem.getText().replaceAll(",", ".");
                value=Double.parseDouble(modifiedString);
                mapObject.put(tmpString+"/"+nomeTag, value);
                }
            catch(NumberFormatException ex){
            
                mapObject.put(tmpString+"/"+nomeTag, elem.getText());
            
            }
          finally{  
            mapField.put(tmpString+"/"+nomeTag, nomeTag);
                }
            }
    }
    
    private void addAttribute(List<Attribute> listAttr,HashMap map,HashMap mapAttr,String tmpString){
    
      Iterator iterAttr;
      Attribute attribute;
      double value;
      String modifiedString,attrName,xpathAttr;
    
         iterAttr=listAttr.iterator();
    while(iterAttr.hasNext()){
          attribute=(Attribute)iterAttr.next();
          attrName=attribute.getName().replace(".", "<_>");
          xpathAttr=tmpString+"/ATTR_"+attrName;
          try{
                modifiedString=attribute.getValue().replaceAll(",", ".");
                value=Double.parseDouble(modifiedString);
                 map.put(xpathAttr, value);
          
          }
          catch(NumberFormatException ex){
              map.put(xpathAttr, attribute.getValue());
          }
           finally{  
          //  System.out.println("dentro foglia funz ricorsiva re"+tmpString+"/"+nomeTag+","+elem.getText());
            mapAttr.put(xpathAttr, attrName);
            
                }
        }
      }
    
    }
    

