/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.openam;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.Common.OpenAm.Openam;
import org.clever.administration.api.Configuration;
import org.clever.administration.api.Environment;
import org.clever.administration.api.SessionFactory;
import org.clever.administration.exceptions.CleverClientException;
import org.json.JSONException;

/**
 *
 * @author clever
 */
public class OpenAmSessionClient {

    private String mToken;
    private String mUsername, mPassword;
    private static OpenAmSessionClient sInstance;
    private Calendar mLastRelease;
    private final static int TOKEN_TIMEOUT = 30 * 60 * 1000; //30 min
    private OpenAmSessionClient() {

    }

    public static OpenAmSessionClient getInstance() {
        if (sInstance == null) {
            sInstance = new OpenAmSessionClient();
        }
        return sInstance;
    }

    public void authenticate(String username, String password) {
        Configuration cfg = new Configuration();
        SessionFactory session;
        try {
            session = cfg.buildSessionFactory();
            Properties p = session.getSession().getSettings().getProperties();
            String host = p.getProperty(Environment.OPEN_AM_HOST);
            String port = p.getProperty(Environment.OPEN_AM_PORT);
            String deployUrl = p.getProperty(Environment.OPEN_AM_DEPLOY_URL);
            Openam client = new Openam(host, port, deployUrl);
            mToken = client.authenticate(username, password);
            if(mToken != null && !mToken.isEmpty()){
                mUsername = username;
                mPassword = password;
                mLastRelease = GregorianCalendar.getInstance();
            }
        } catch (CleverClientException cex) {
            Logger.getLogger(OpenAmSessionClient.class.getName()).log(Level.SEVERE, null, cex);
        } catch (IOException ioEx) {
            Logger.getLogger(OpenAmSessionClient.class.getName()).log(Level.SEVERE, null, ioEx);
        } catch (JSONException ex) {
            Logger.getLogger(OpenAmSessionClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void authenticate(String host, int port, String deployUrl, String username, String password) {
        try {
            Openam client = new Openam(host, String.valueOf(port), deployUrl);
            mToken = client.authenticate(username, password);
        } catch (IOException ioEx) {
            Logger.getLogger(OpenAmSessionClient.class.getName()).log(Level.SEVERE, null, ioEx);
        }
        catch (JSONException ex) {
            Logger.getLogger(OpenAmSessionClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getToken() {
        Calendar now = GregorianCalendar.getInstance();
        if(mLastRelease == null || (now.getTimeInMillis() - mLastRelease.getTimeInMillis()) >= TOKEN_TIMEOUT){
            renewToken();                    
        }        
        return mToken;
    }

    public boolean isAuthenticated() {
        return mToken != null && !mToken.equalsIgnoreCase("");
    }

    public void renewToken() {
        Logger.getLogger(OpenAmSessionClient.class.getName()).log(Level.INFO, "Renew token!");
        authenticate(mUsername, mPassword);
    }
}
