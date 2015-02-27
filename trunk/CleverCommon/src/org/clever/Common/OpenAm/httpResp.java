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
package org.clever.Common.OpenAm;


public class httpResp {
    
    private String httpCode;
    private String json;
    
    private String tokenId;
    private String cookieDomains;
    private String cookieSessionToken;
    private String CookieSessionTokenAndNames;

    private Boolean tokenValidity;
    private Boolean uriAutho;
    
    
    public httpResp() {
        this.httpCode = "";
        this.json = "";
        this.tokenId = "";
        this.cookieDomains = "";
        this.cookieSessionToken = "";
        this.CookieSessionTokenAndNames = "";
        this.tokenValidity = false;
        this.uriAutho= false;
    }

    
    public String getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getCookieDomains() {
        return cookieDomains;
    }

    public void setCookieDomains(String cookieDomains) {
        this.cookieDomains = cookieDomains;
    }

    public String getCookieSessionToken() {
        return cookieSessionToken;
    }

    public void setCookieSessionToken(String cookieSessionToken) {
        this.cookieSessionToken = cookieSessionToken;
    }

    public String getCookieSessionTokenAndNames() {
        return CookieSessionTokenAndNames;
    }

    public void setCookieSessionTokenAndNames(String CookieSessionTokenAndNames) {
        this.CookieSessionTokenAndNames = CookieSessionTokenAndNames;
    }

    public Boolean getTokenValidity() {
        return tokenValidity;
    }

    public void setTokenValidity(Boolean tokenValidity) {
        this.tokenValidity = tokenValidity;
    }

    public Boolean getUriAutho() {
        return uriAutho;
    }

    public void setUriAutho(Boolean uriAutho) {
        this.uriAutho = uriAutho;
    }
    
    
}
