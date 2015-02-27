/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.openam;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.Common.OpenAm.Openam;
import org.clever.administration.api.Configuration;
import org.clever.administration.api.Environment;
import org.clever.administration.api.SessionFactory;
import org.clever.administration.api.Settings;
import org.clever.administration.exceptions.CleverClientException;
import org.json.JSONException;

/**
 *
 * @author clever
 */
public class OpenAmSessionClient {

    private String mToken;
    private static OpenAmSessionClient sInstance;

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
        }
        catch (JSONException ex) {
            Logger.getLogger(OpenAmSessionClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getToken() {
        return mToken;
    }

    public boolean isAuthenticated() {
        return mToken != null && !mToken.equalsIgnoreCase("");
    }

    public boolean renewToken() {
        throw new UnsupportedOperationException();
    }
}
