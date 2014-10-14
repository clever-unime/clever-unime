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
/**
 * The MIT License
 * 
 * @author dott. Riccardo Di Pietro - 2014
 * MDSLab Messina
 * dipcisco@hotmail.com
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
package org.clever.ClusterManager.IdentityServicePlugins.Keystone;

public class Token {

private String id;

private String issued_at;

private String expires;

private String tenant;
    
private String publicUrlSwift;



//########################
// Costruttori
//########################

    
/**
 * Costruttore
 * @param id
 * @param issued_at
 * @param expires
 * @param tenant
 * @param publicUrlSwift 
 */
public Token(String id, String issued_at, String expires,String tenant, String publicUrlSwift) {
        this.id = id;
        this.issued_at = issued_at;
        this.expires = expires;
        this.tenant = tenant;
        this.publicUrlSwift = publicUrlSwift;
    }

    
    
/**
 * Costruttore
 * @param id
 * @param issued_at
 * @param expires
 * @param tenant 
 */
public Token(String id, String issued_at, String expires,String tenant) {
        this.id = id;
        this.issued_at = issued_at;
        this.expires = expires;
        this.tenant = tenant;
        
    }
    
    
    
/**
 * Costruttore di default
 */
public Token() {
        this.id = "";
        this.issued_at = "";
        this.expires = "";
        this.tenant = "";
        this.publicUrlSwift = "";
    }
       

/**
 * Metodo utile per la fase di debugging.
 */
public void debug(){
    
    //debug
    System.out.println("\n debug: ");
    System.out.println("###########################");
    System.out.println("token_id: "+this.getId());
    System.out.println("token_issued_at"+this.getIssued_at());
    System.out.println("token_expires"+this.getExpires());
    System.out.println("publicUrlSwift: "+this.getPublicUrlSwift());
    System.out.println("###########################\n");
    
    
}


    //########################
    //Metodi setter e getter
    //########################


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIssued_at() {
        return issued_at;
    }

    public void setIssued_at(String issued_at) {
        this.issued_at = issued_at;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getPublicUrlSwift() {
        return publicUrlSwift;
    }

    public void setPublicUrlSwift(String publicUrlSwift) {
        this.publicUrlSwift = publicUrlSwift;
    }

    
   
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
   
	
}//class