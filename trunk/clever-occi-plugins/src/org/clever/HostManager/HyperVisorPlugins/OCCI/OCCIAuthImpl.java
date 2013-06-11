/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI;

import org.apache.http.HttpRequest;

/**
 *
 * @author salvullo
 */
public interface OCCIAuthImpl {
  boolean doAuth(HttpRequest request);

}
