package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;


import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils.HttpClientFactory;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.OCCIAuthImpl;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author salvullo
 */
public class OCCIAuth {
  public OCCIAuth(OCCIAuthImpl impl) { 
    this.impl = impl; 
  } 
  public boolean doAuth(HttpRequest request) {
    return impl.doAuth(request);
  }
  public void initClient(HttpClientFactory client) throws Exception
  {
      impl.initClient(client);
  }
  private OCCIAuthImpl impl;
}
