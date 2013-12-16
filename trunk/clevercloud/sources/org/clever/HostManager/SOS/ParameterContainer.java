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
package org.clever.HostManager.SOS;

import org.apache.log4j.Logger;

/**
 *
 * @author alessiodipietro
 */
public class ParameterContainer {
    private static ParameterContainer parameterContainer=null;
    private SOSAgent sosAgent;
    private String dbServer;
    private String dbDriver;
    private String dbUsername;
    private String dbPassword;
    private String dbName;
    private String configurationFile;
    private Logger logger;
    
    private String testDbServer;
    private String testDbDriver;
    private String testDbUsername;
    private String testDbPassword;
    private String testDbName;
    
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
     * @param dbServer the dbServer to set
     */
    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    /**
     * @return the dbDriver
     */
    public String getDbDriver() {
        return dbDriver;
    }

    /**
     * @param dbDriver the dbDriver to set
     */
    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    /**
     * @return the dbUsername
     */
    public String getDbUsername() {
        return dbUsername;
    }

    /**
     * @param dbUsername the dbUsername to set
     */
    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    /**
     * @return the dbPassword
     */
    public String getDbPassword() {
        return dbPassword;
    }

    /**
     * @param dbPassword the dbPassword to set
     */
    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    /**
     * @return the dbName
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param dbName the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * @return the configurationFile
     */
    public String getConfigurationFile() {
        return configurationFile;
    }

    /**
     * @param configurationFile the configurationFile to set
     */
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    /**
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @param logger the logger to set
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * @return the sosAgent
     */
    public SOSAgent getSosAgent() {
        return sosAgent;
    }

    /**
     * @param sosAgent the sosAgent to set
     */
    public void setSosAgent(SOSAgent sosAgent) {
        this.sosAgent = sosAgent;
    }

    /**
     * @return the testDbServer
     */
    public String getTestDbServer() {
        return testDbServer;
    }

    /**
     * @param testDbServer the testDbServer to set
     */
    public void setTestDbServer(String testDbServer) {
        this.testDbServer = testDbServer;
    }

    /**
     * @return the testDbDriver
     */
    public String getTestDbDriver() {
        return testDbDriver;
    }

    /**
     * @param testDbDriver the testDbDriver to set
     */
    public void setTestDbDriver(String testDbDriver) {
        this.testDbDriver = testDbDriver;
    }

    /**
     * @return the testDbUsername
     */
    public String getTestDbUsername() {
        return testDbUsername;
    }

    /**
     * @param testDbUsername the testDbUsername to set
     */
    public void setTestDbUsername(String testDbUsername) {
        this.testDbUsername = testDbUsername;
    }

    /**
     * @return the testDbPassword
     */
    public String getTestDbPassword() {
        return testDbPassword;
    }

    /**
     * @param testDbPassword the testDbPassword to set
     */
    public void setTestDbPassword(String testDbPassword) {
        this.testDbPassword = testDbPassword;
    }

    /**
     * @return the testDbName
     */
    public String getTestDbName() {
        return testDbName;
    }

    /**
     * @param testDbName the testDbName to set
     */
    public void setTestDbName(String testDbName) {
        this.testDbName = testDbName;
    }


    
    
    
}
