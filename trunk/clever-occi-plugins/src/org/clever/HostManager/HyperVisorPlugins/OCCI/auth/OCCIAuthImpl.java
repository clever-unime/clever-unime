/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils.HttpClientFactory;

/**
 *
 * @author salvullo
 */
public interface OCCIAuthImpl {
  boolean doAuth(HttpRequest request);
  public void initClient(HttpClientFactory client);
}
