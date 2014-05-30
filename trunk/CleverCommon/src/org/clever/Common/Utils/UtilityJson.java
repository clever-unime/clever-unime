/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.Common.Utils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


/**
 *
 * @author Antonino Galletta 2014
 */
public class UtilityJson {
    
    
    
    public UtilityJson(){
    
        //logger ed altre inizializzazioni
    }
   /* 
    public String XMLMultiLevelToJSON(String XMLString){
      
      Document document;
      SAXBuilder builder = new SAXBuilder();
      Element rootElement, child;
      String JSONString ="{";
      List<Element> listElem;
      List<Attribute> listAttr;
      Iterator iterElem;
      HashMap map=new HashMap();
    try
    {
      document = builder.build( new StringReader( XMLString ) );
      rootElement = document.getRootElement();
      listElem=rootElement.getChildren();
      JSONString=JSONString+ "\"level0\": \""+rootElement.getName()+"\", ";//\"name\": \""+rootElement.getAttributeValue("name")+"\", ";//\"value\": \""+rootElement.getChildText("value")+"\", \"timestamp\": \""+rootElement.getChildText(XMLString);
      iterElem=listElem.iterator();
      this.funzRicorsiva(rootElement, map, "");
      listAttr=rootElement.getAttributes();
     if(!listAttr.isEmpty()){
        JSONString=JSONString+ this.addAttribute(listAttr);
     }
      while(iterElem.hasNext()){
            child=((Element)iterElem.next());
            listAttr=child.getAttributes();
      if(!listAttr.isEmpty()){
          JSONString=JSONString+  this.addAttribute(listAttr);
            }
      JSONString=JSONString+"\""+child.getName()+"\": \""+child.getText()+"\", ";
        }
      JSONString=JSONString+"}";
      
      Iterator<Map.Entry> itSet=map.entrySet().iterator();
      Entry e;
      while(itSet.hasNext()){
          e=itSet.next();
          System.out.println(e.getKey()+" aaaaaa "+e.getValue());
      }
    }
    
    catch ( JDOMException ex )
    {
 //    logger.error( "Error while opening the file xml: " + ex );
    }
    catch ( IOException ex )
    {
 //    logger.error( "Error while opening the file xml: " + ex );
    }
    catch(Exception ex){
 //    logger.error( "Error while opening the file xml: " + ex );
        ex.printStackTrace();
    }
    
    finally{
        return JSONString;
    }
    
  }
*/
    public BasicDBObject XMLMultiLevelToJSON(String XMLString){
      
      Document document;
      SAXBuilder builder = new SAXBuilder();
      Element rootElement;
      HashMap map=new HashMap();
      BasicDBObject obj=new BasicDBObject();
 //BasicDBObject obj;

   try{
      Entry entry;
      Iterator<Map.Entry> itEntry;
      
      document = builder.build( new StringReader( XMLString ) );
      rootElement = document.getRootElement();
      this.funzRicorsiva(rootElement, map, "");
    obj.putAll(map);
  /*    itEntry=map.entrySet().iterator();
   while(itEntry.hasNext()){
          entry=itEntry.next();
          obj.append((String)entry.getKey(), entry.getValue());
           }*/
        }
    catch ( IOException ex )
    {
 //    logger.error( "Error while opening the file xml: " + ex );
    }
    catch(Exception ex){
 //    logger.error( "Error while opening the file xml: " + ex );
        ex.printStackTrace();
    }
    
    finally{
        obj.append("insertTimestamp", (double)System.currentTimeMillis());
        return obj;
    }
    
  }

    public ArrayList XMLMultiLevelToJSON2(String XMLString) throws FileNotFoundException, IOException{
      
      Document document;
      SAXBuilder builder = new SAXBuilder();
      Element rootElement;
      HashMap mapObject=new HashMap();
      HashMap mapField=new HashMap();
      
      long inizio, fine;
      String str;
      FileOutputStream fos=new FileOutputStream("parsing.csv", true);
      
      ArrayList<BasicDBObject> lista=new ArrayList();
      inizio=System.currentTimeMillis();
   try{
      
      document = builder.build( new StringReader( XMLString ) );
      rootElement = document.getRootElement();
      this.funzRicorsiva(rootElement, mapObject,mapField, "");
 
        }
    catch ( IOException ex )
    {
 //    logger.error( "Error while opening the file xml: " + ex );
    }
    catch(Exception ex){
 //    logger.error( "Error while opening the file xml: " + ex );
        ex.printStackTrace();
    }
    
    finally{
       lista.add(0, this.createDBOBject(mapObject));
       lista.add(1, this.createDBFieldOBject(mapField));
      
       fine=System.currentTimeMillis();
       str="tempo parsing: "+(fine-inizio)+"\n";
       fos.write(str.getBytes());
       
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
            obj.append("insertTimestamp", (double)System.currentTimeMillis());
            return obj;
    }
    
    //metodo che faccia l'update dell'array
    private BasicDBObject createDBFieldOBject(HashMap mapField){
        Entry entry;
        Iterator<Map.Entry> itEntry;
        BasicDBObject obj=new BasicDBObject();
        //int position=0, index=0;
       // ArrayList<BasicDBList> array=new ArrayList();
        HashMap mapName=new HashMap();
        BasicDBList DbList;
        
         itEntry=mapField.entrySet().iterator();
     
   while(itEntry.hasNext()){
          entry=itEntry.next();
          
          if(mapName.get(entry.getValue())==null){
              //mapName.put(entryObject.getValue(), position);
              DbList=new BasicDBList();
              DbList.add(entry.getKey());
              mapName.put(entry.getValue(), DbList);
              //array.set(position, DbList);
              //position++;
            }
          else {
              //index=(int)mapName.get(entryObject.getValue());
              //DbList=array.get(index);
              DbList=(BasicDBList)mapName.get(entry.getValue());
              DbList.add(entry.getKey());
              System.out.println("controlla eventuale riassegnamento");
          }
         
          //obj.append((String)entryObject.getKey(), entryObject.getValue());
           }
            obj.append("_id", "campi");
            //obj.append("insertTimestamp", (double)System.currentTimeMillis());
            itEntry=mapName.entrySet().iterator();
            
      while(itEntry.hasNext()){
          
          entry=itEntry.next();
          obj.append((String)entry.getKey(), entry.getValue());
            }
            
            return obj;
    }
    
    private String addAttribute(List<Attribute> listAttr){
    
      String stringa="";
      Iterator iterAttr;
      Attribute attribute;
    
         iterAttr=listAttr.iterator();
      while(iterAttr.hasNext()){
          attribute=(Attribute)iterAttr.next();
          stringa=stringa+"\"ATTR_"+attribute.getName()+"\": \""+attribute.getValue()+"\", ";
      
      }
      
    return stringa;
    
    
    }
    
    private void funzRicorsiva(Element elem,HashMap map,String tmpString){
        
        List<Element> listaElem=elem.getChildren();
        List<Attribute> listAttr=elem.getAttributes();
        Iterator iterElem;
        double value;
        String modifiedString;
        
        if(!listAttr.isEmpty()){
            this.addAttribute(listAttr, map,tmpString+"/"+elem.getName());
            }
        
        if(!listaElem.isEmpty()){
            iterElem=listaElem.iterator();
            while(iterElem.hasNext()){
            this.funzRicorsiva(((Element)iterElem.next()), map, tmpString+"/"+elem.getName());
                 }
         }
        else{
            try{ 
                modifiedString=elem.getText().replaceAll(",", ".");
                value=Double.parseDouble(modifiedString);
                map.put(tmpString+"/"+elem.getName(), value);
                }
            catch(NumberFormatException ex){
            
                map.put(tmpString+"/"+elem.getName(), elem.getText());
            
            }
            System.out.println("dentro foglia funz ricorsiva "+tmpString+"/"+elem.getName()+","+elem.getText());
        
        }
    }
    
    private void funzRicorsiva(Element elem,HashMap mapObject,HashMap mapField,String tmpString){
        
        List<Element> listaElem=elem.getChildren();
        List<Attribute> listAttr=elem.getAttributes();
        Iterator iterElem;
        double value;
        String modifiedString;
        
        if(!listAttr.isEmpty()){
            this.addAttribute(listAttr, mapObject,tmpString+"/"+elem.getName());
            }
        
        if(!listaElem.isEmpty()){
            iterElem=listaElem.iterator();
            while(iterElem.hasNext()){
            this.funzRicorsiva(((Element)iterElem.next()), mapObject,mapField, tmpString+"/"+elem.getName());
                 }
         }
        else{
            try{ 
                modifiedString=elem.getText().replaceAll(",", ".");
                value=Double.parseDouble(modifiedString);
                mapObject.put(tmpString+"/"+elem.getName(), value);
                }
            catch(NumberFormatException ex){
            
                mapObject.put(tmpString+"/"+elem.getName(), elem.getText());
            
            }
          finally{  
            System.out.println("dentro foglia funz ricorsiva re"+tmpString+"/"+elem.getName()+","+elem.getText());
            mapField.put(tmpString+"/"+elem.getName(), elem.getName());
            
                }
            }
    }
   
    
    private void addAttribute(List<Attribute> listAttr,HashMap map,String tmpString){
    
      Iterator iterAttr;
      Attribute attribute;
      double value;
      String modifiedString;
    
         iterAttr=listAttr.iterator();
    while(iterAttr.hasNext()){
          attribute=(Attribute)iterAttr.next();
          try{
                modifiedString=attribute.getValue().replaceAll(",", ".");
                value=Double.parseDouble(modifiedString);
                map.put(tmpString+"/ATTR_"+attribute.getName(), value);
          
          }
          catch(Exception ex){
            map.put(tmpString+"/ATTR_"+attribute.getName(), attribute.getValue());
          }
          System.out.println("addatributeeee "+tmpString+"/ATTR_"+attribute.getName());
        }
      }
    
    
    }
    

