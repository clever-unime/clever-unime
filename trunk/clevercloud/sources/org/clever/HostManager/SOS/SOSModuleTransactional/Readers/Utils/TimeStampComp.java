/*
 *  Copyright (c) 2013 Universita' degli studi di Messina
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Utils; 
/**
 *  
 * @author Giuseppe Tricomi 
 */
import java.text.SimpleDateFormat;
import org.clever.Common.Exceptions.CleverException;
import org.apache.log4j.Logger;
public class TimeStampComp 
{
    private String fterm,sterm;
    private Logger logger=Logger.getLogger("TimeStampComp");
    public TimeStampComp(String first,String second)throws CleverException{
        if((first!=null)&&(second!=null))
        {
            this.fterm=first;
            this.sterm=second;
        }
        else 
            throw new CleverException("TimeStampComp can't compare null string");
    }
    
    public int compare(){
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date parsedDate = dateFormat.parse(this.fterm);
            java.sql.Timestamp timestamp1 = new java.sql.Timestamp(parsedDate.getTime());
            parsedDate = dateFormat.parse(this.sterm);
            java.sql.Timestamp timestamp2 = new java.sql.Timestamp(parsedDate.getTime());
            //logger.debug("/&//&/"+timestamp1.toString()+timestamp2.toString());
            if(timestamp1.after(timestamp2))
                return 1;
            else
                return -1;
        }catch(Exception e){return 0;}
    }
    
}