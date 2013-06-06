/*
 *  The MIT License
 * 
 *  Copyright 2011 brady.
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

package org.clever.Common.VEInfo;

/**
 *
 * @author brady
 */
public class DesktopVirtualization {
    
    private String username;
    private String password_user;
    private String password_vnc_vm;
    private String ip_vnc;
    private String port;

    public DesktopVirtualization(String username, String password){
        this.username=username;
        this.password_user=password;
    }
    public DesktopVirtualization(String username){
        this.username=username;
    }

    public DesktopVirtualization(String username, String password_user, String ip_vnc, String port){
        this.username=username;
        this.password_user=password_user;
        this.ip_vnc=ip_vnc;
        this.port=port;
    }

    public void setUsername(String username){
        this.username=username;
    }
    public void setUserPassword(String password_user){
        this.password_user=password_user;
    }
    public void setVmVNCPassword(String password_vnc){
        this.password_vnc_vm=password_vnc;
    }
    public void setIpVNC(String ip_vnc){
        this.ip_vnc=ip_vnc;
    }
    public void setPort(String port){
        this.port=port;
    }

    public String getUsername(){
        return this.username;
    }
    public String getUserPassword(){
        return this.password_user;
    }
    public String getVmVNCPassword(){
        return this.password_vnc_vm;
    }
    public String getIpVNC(){
        return this.ip_vnc;
    }
    public String getPort(){
        return this.port;
    }

}
