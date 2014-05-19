package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils.HttpClientFactory;
import org.jdom.Element;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Maurizio Paone
 */
public class OCCIAuthX509Impl implements OCCIAuthImpl {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OCCIAuthX509Impl.class);
    private final String certpath;
    private final URL retrieveCertificateURL;
    private final ScheduledExecutorService scheduler;
    private final Runnable retrieveCertificateRunner;
    private HttpClientFactory client;
    private Calendar expiration;
    private ScheduledFuture<?> runningFuture;

    public OCCIAuthX509Impl(Element auth) throws MalformedURLException {
        super();
        this.scheduler = Executors.newScheduledThreadPool(1);
        retrieveCertificateRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    initClient(client);
                } catch (Exception ex) {
                    log.error("Error retrieving certificate: " + ex);
                }
            }
        };

        certpath = auth.getChildText("certificatePath");
        if (auth.getChildText("retrieveCertificateURL") != null) {
            try {
                retrieveCertificateURL = new URL(auth.getChildText("retrieveCertificateURL"));
            } catch (MalformedURLException ex) {
                log.error("Error retrieveCertificateURL tag not valid : ");
                //retrieveCertificateURL = null;
                throw ex;
            }
        } else {
            retrieveCertificateURL = null;
        }

    }

    @Override
    public boolean doAuth(HttpRequest request) {
        if (this.isExpired()) {
            try {
                this.initClient(client);
            } catch (Exception ex) {
                log.error("Error in Certificate fetching");
                return false;
            }
        }
        return !isExpired();
    }

    private boolean isExpired() {



        return expiration.before(Calendar.getInstance());


    }

    @Override
    public void initClient(HttpClientFactory client) throws Exception {
        InputStream certStream;
        this.client = client;
        if (certpath == null) {
            log.info("no Certificate provided ...");
            if (this.retrieveCertificateURL != null) {
                log.info("it is present an URL for HTTP retrieving: retrieving certificate ...");
                certStream = this.retrieveCertificate();
            } else {
                log.info("init exiting ...");
                return;
            }
        } else {
            File f = new File(certpath);
            certStream = new FileInputStream(f);
        }

        try {
            client.addClientCertificate(certStream);

        } catch (KeyStoreException ex) {
            log.error(ex);
            throw ex;
        } catch (CertificateException ex) {
            log.error(ex);
            throw ex;
        } catch (NoSuchAlgorithmException ex) {
            log.error(ex);
            throw ex;
        } catch (KeyManagementException ex) {
            log.error(ex);
            throw ex;
        } catch (UnrecoverableKeyException ex) {
            log.error(ex);
            throw ex;
        } catch (FileNotFoundException ex) {
            log.error(ex);
            throw ex;
        } catch (IOException ex) {
            log.error(ex);
            throw ex;
        }
        Date certificateExpirationTime = client.getClientCertExpirationDate();


        Calendar now = Calendar.getInstance();
        expiration = Calendar.getInstance();
        expiration.setTime(certificateExpirationTime);

        if (runningFuture != null) {
            runningFuture.cancel(true);
        }

        runningFuture = scheduler.schedule(this.retrieveCertificateRunner, 
                                           expiration.getTimeInMillis() - now.getTimeInMillis(),
                                           TimeUnit.MILLISECONDS);

        log.debug("Next certificate fetching: " + new SimpleDateFormat().format(certificateExpirationTime) + " milliseconds: " + (expiration.getTimeInMillis() - now.getTimeInMillis()));
    }

    private InputStream retrieveCertificate() throws Exception {
        if (this.retrieveCertificateURL == null) {
            throw new Exception("No URL suitable to retrieve certificate");
        }

        //HttpClient client = new DefaultHttpClient();
        HttpClient client = new HttpClientFactory(
                this.retrieveCertificateURL.getProtocol(),
                true,
                new Integer[]{this.retrieveCertificateURL.getPort()},
                5 * 60 * 1000,
                5 * 60 * 1000)
                .getThreadSafeClient();
        HttpUriRequest request = new HttpGet(this.retrieveCertificateURL.toString());

        return client.execute(request).getEntity().getContent();


    }
}
