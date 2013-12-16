/*
 * The MIT License
 *
 * Copyright 2012 alessiodipietro.
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
package org.clever.ClusterManager.SAS;



/**
 *
 * @author alessiodipietro
 */
public class ParameterContainer {
    private static ParameterContainer parameterContainer=null;
    private static String dbServer;
    private static String dbDriver;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbName;
    
    
    public static ParameterContainer getInstance(){
        if(parameterContainer==null){
            parameterContainer=new ParameterContainer();
        }
        return parameterContainer;
    }


    /**
     * @return the dbServer
     */
    public String getDbServer() {
        return dbServer;
    }

    /**
     * @param aDbServer the dbServer to set
     */
    public void setDbServer(String aDbServer) {
        dbServer = aDbServer;
    }

    /**
     * @return the dbDriver
     */
    public String getDbDriver() {
        return dbDriver;
    }

    /**
     * @param aDbDriver the dbDriver to set
     */
    public void setDbDriver(String aDbDriver) {
        dbDriver = aDbDriver;
    }

    /**
     * @return the dbUsername
     */
    public String getDbUsername() {
        return dbUsername;
    }

    /**
     * @param aDbUsername the dbUsername to set
     */
    public void setDbUsername(String aDbUsername) {
        dbUsername = aDbUsername;
    }

    /**
     * @return the dbPassword
     */
    public String getDbPassword() {
        return dbPassword;
    }

    /**
     * @param aDbPassword the dbPassword to set
     */
    public void setDbPassword(String aDbPassword) {
        dbPassword = aDbPassword;
    }

    /**
     * @return the dbName
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param aDbName the dbName to set
     */
    public void setDbName(String aDbName) {
        dbName = aDbName;
    }
    
}
