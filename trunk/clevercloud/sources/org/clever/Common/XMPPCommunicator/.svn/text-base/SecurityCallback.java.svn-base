/*
 *  The MIT License
 * 
 *  Copyright 2011 Sergio Marino.
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
package org.clever.Common.XMPPCommunicator;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

public class SecurityCallback implements javax.security.auth.callback.CallbackHandler{

    private String userName = null;
    private String password = null;

    public  SecurityCallback(String userName, String password){
        this.userName = userName;
        this.password = password;
    }
    public void handle(Callback[] callbacks) {
        if (callbacks[0] instanceof javax.security.auth.callback.NameCallback) {


            NameCallback pass = (NameCallback) callbacks[0];
            pass.setName(this.userName);
        }

        if (callbacks[1] instanceof javax.security.auth.callback.PasswordCallback) {

            PasswordCallback pass = (PasswordCallback) callbacks[1];
            pass.setPassword(this.password.toCharArray());

        }
    }
}
