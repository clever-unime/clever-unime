/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.Common.OpenAm;

import org.clever.Common.Exceptions.CleverException;

/**
 *
 * @author clever
 */
public class AuthorizationException extends CleverException {

    public AuthorizationException() {
        super();
    }

    public AuthorizationException(Throwable e) {
        super(e);
    }

    public AuthorizationException(Throwable e, String msg) {
        super(e, msg);
    }

    /**
     * Constructs an instance of <code>CleverException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public AuthorizationException(String msg) {
        super(msg);
    }
}
