package org.clever.HostManager.HyperVisorPlugins.OCCI;


import org.apache.http.HttpRequest;
import org.clever.HostManager.HyperVisorPlugins.OCCI.OCCIAuthImpl;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author salvullo
 */
public class OCCIAuth {
  OCCIAuth(OCCIAuthImpl impl) { 
    this.impl = impl; 
  } 
  public boolean doAuth(HttpRequest request) {
    return impl.doAuth(request);
  }
  private OCCIAuthImpl impl;
}
