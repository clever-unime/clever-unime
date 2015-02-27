/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.Common.OpenAm;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 *
 * @author clever
 */
public class TokenExtension implements PacketExtension {
    private String Type;
    private String Data;
    
    public TokenExtension(){
        this.Type = "openAmToken";
    }
    
    public TokenExtension(String type){
        this.Type = type;
    }
    
    public void setType(String Type) {
        this.Type = Type;
    }
    
    public String getType(){
        return this.Type;
    }
    
    public void setData(String Data){
        this.Data = Data;
    }
    
    public String getData(){
        return this.Data;
    }
    
    public String toXML(){
        return "<x xmlns=\"jabber:x:" + this.Type + "\">" + this.Data + "</x>";
    }
    
    public String getElementName() {
        return "x";
    }
    
    public String getNamespace() {
        return "jabber:x:" + this.Type;
    }
}
