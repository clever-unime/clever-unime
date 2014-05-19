/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.fileutils;

import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maurizio
 */
public class InputStreamSplitter {
   static public List<String> split(final InputStream is , final String beginDelim, final String endDelim, final boolean includeDelim) throws IOException {
             
       InputSupplier<Reader> iss = new  InputSupplier<Reader>(){

            @Override
            public Reader getInput() throws IOException {
                return new InputStreamReader(is);
            }
        };
       
        return CharStreams.readLines(
                iss,
                new LineProcessor<List<String>>() {

                            boolean started , delim = false;
                            StringBuilder sb = new StringBuilder();
                            ArrayList<String> result = new ArrayList<String>();
                            @Override
                            public boolean processLine(String string) throws IOException {
                                
                                if(string.matches(beginDelim))
                                {
                                    //TODO: check error based on started == false
                                   
                                    started=true;
                                    delim = true;
                                    
                                }
                                else if(string.matches(endDelim))
                                {
                                    started = false;
                                    if(includeDelim)
                                        sb.append(string).append('\n'); //append della fine delimitatore
                                    
                                    result.add(sb.toString());
                                    sb = new StringBuilder();
                                   
                                }

                                if(started)
                                {
                                    if(delim)
                                    {
                                        if(includeDelim)
                                            sb.append(string).append('\n');
                                        delim=false;
                                    }
                                    else
                                    {
                                        sb.append(string).append('\n');
                                    }
                                    
                                }

                                return true; 
                            }

                            @Override
                            public List<String> getResult() {
                                return result;
                            }
                  }
                );
       
       
       
       
   } 
}
