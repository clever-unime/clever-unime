/*
 * The MIT License
 *
 * Copyright 2012 giancarloalteri.
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
package org.clever.Common.Storage;

/**
 *
 * @author giancarloalteri
 */
public class VFSDescription {
    
  public enum TypeVfs
  {
    ftp,
    sftp,
    file,
    jar,
    http,
    zip,
    zip_http
  };  
    
  private UserAuthentication auth;
  private TypeVfs typevfs;
  private String hostname;
  private String port;
  private String path;
  private String path1;
  
  public VFSDescription(){
      
  }
  public VFSDescription( final UserAuthentication auth,final TypeVfs typevfs,final String hostname,final String port,final String path,final String path1)
  {
    this.auth=auth;
    this.typevfs=typevfs;
    this.hostname=hostname;
    this.port=port;
    this.path=path;
    this.path1=path1;
  }
  
    public VFSDescription(final TypeVfs typevfs,final String path,final String path1)
  {

    this.typevfs=typevfs;
    this.path=path;
    this.path1=path1;
  }
  
  public String getHostname(){
    return hostname;
  }
  public void setHostname( String hostname ){
    this.hostname=hostname;
  }
  public String getPort(){
    return port;
  }
  public void setPort( String port ){
    this.port=port;
  }
  public String getPath(){
    return path;
  }
  public void setPath( String path ){
    this.path=path;
  }
    public String getPath1(){
    return path1;
  }
  public void setPath1( String path1 ){
    this.path1=path1;
  }
  public TypeVfs getType(){
    return typevfs;
  }
  public void setType( TypeVfs typevfs ){
    this.typevfs=typevfs;
  }
  public UserAuthentication getAuth(){
    return auth;
  }
  public void setAuth( UserAuthentication auth ){
    this.auth = auth;
  }
}

 


 