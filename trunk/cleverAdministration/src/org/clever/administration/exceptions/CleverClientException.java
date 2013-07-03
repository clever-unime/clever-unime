/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.exceptions;

import org.clever.Common.Exceptions.CleverException;

/**
 *
 * @author maurizio
 */
public class CleverClientException extends CleverException {
    public CleverClientException() {
        super();
       
    }


    public CleverClientException(Throwable e) {
        super(e);
        
    }


    public CleverClientException(Throwable e, String msg) {
        super(e, msg);
        
    }
    
    public CleverClientException(String string) {
        super(string);
    }
    
}
