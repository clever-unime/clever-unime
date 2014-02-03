/*
 *  The MIT License
 *
 *  Copyright (c) 2012 Tricomi Giuseppe
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package sensoracquisitiongenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.Shared.Support;
import org.jdom.Document;
import org.jdom.Element;
import org.apache.log4j.Logger; 
/**
 *
 * @author Giuseppe Tricomi <giu.tricomi@gmail.com>
 */
public class Takeconfig {
    String cfgPath2="./cfg/configuration.xml";
    String serverIP="";
    String pass="";
    String user="";
    String sensName="";
    String compName="";
    String phenName="";
    int sensid;
    int phenid;
    String sensorstatgeneration="false";
    int genInterval;
    public void init(){
        String cfgdir= System.getProperty("user.dir")+"/cfg";
         File fl=new File(cfgdir);
         if(!fl.exists())
             fl.mkdir();
         File cfgFile = new File( cfgPath2 );
            InputStream inxml=null;
            if( !cfgFile.exists() )
            {
                inxml = getClass().getResourceAsStream( "/sensoracquisitiongenerator/configuration.xml" );
                try
                {
                    Support.copy( inxml, cfgFile );
                }
                catch( IOException ex )
                {
                    System.err.println( "Copy file failed" + ex );
                    System.exit( 1 );
                }
            }
            try
            {
                inxml = new FileInputStream( cfgPath2 );
            }
      catch( FileNotFoundException ex )
      {
          System.err.println( "File not found: " + ex );
      }
      try{
          FileStreamer fs = new FileStreamer();
          ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
          Element dbParams=pXML.getDocument().getRootElement().getChild("dbParams");
          
          this.serverIP=dbParams.getChildText("server");
          this.pass=dbParams.getChildText("password");
          this.user=(dbParams.getChildText("username"));
          Element sensGenParams=pXML.getRootElement().getChild("sensGenParams");
          this.sensName=sensGenParams.getChildText("sensName");
          this.compName=sensGenParams.getChildText("compName");
          this.phenName=sensGenParams.getChildText("phenName");
          this.genInterval=Integer.parseInt(sensGenParams.getChildText("genInterval"));
          //System.out.println(sensGenParams.getChildText("sensId"));
          this.sensid=Integer.parseInt(sensGenParams.getChildText("sensid"));
          this.phenid=Integer.parseInt(sensGenParams.getChildText("phenid"));
          
          this.sensorstatgeneration=sensGenParams.getChildText("sensorstatgeneration");
      } catch (IOException ex) 
      {
            System.err.println("Missing configuration not found");
      }
        
        
    }
}
