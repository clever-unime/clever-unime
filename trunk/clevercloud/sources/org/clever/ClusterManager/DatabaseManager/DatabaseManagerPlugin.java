 /*
 *  Copyright (c) 2010 Antonino Longo
 *  Copyright (c) 2011 Luca Ciarniello
 *  Copyright (c) 2011 Alessio Di Pietro
 *  Copyright (c) 2013 Nicola Peditto
 *  Copyright (c) 2013 Carmelo Romeo
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.clever.ClusterManager.DatabaseManager;

import java.util.List;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Plugins.RunnerPlugin;
import org.xmldb.api.base.XMLDBException;


/**
 * This interface describes all the methods needed to interact with
 * the project's database
 *
 */
public interface DatabaseManagerPlugin extends RunnerPlugin {



  public void insertNode(String agentId, String node, String where, String location) throws CleverException;

  public void insertNode(String hostId, String agentId, String node, String where, String location) throws CleverException;
  /**
   * Looks for <hm name="hostId"> tag into database structure
   * @param hostId - the name of HM
   * @return true if the tag exists
   */
  boolean checkHm(String hostId) throws CleverException;
  /**
   * Adds <hm name="hostId"> tag into database structure
   * @param hostId - the name of HM
   *
   */
  void addHm(String hostId) throws CleverException;

  /**
   * Submits a read-only (select) query to the database
   * @param query - the complete query
   * @return a string that will contain the response to the query, "n/a" otherwise
   */
  String query(String query) throws CleverException;
  String query(String hostId, String agentId, String location) throws CleverException;
  String query(String agentId,String location) throws CleverException;
  String querytab(String agentId,String location) throws CleverException;
  String getAttributeNode(String agentId,String location,String tipo) throws XMLDBException;
  boolean existNode(String agentId,String location);
  String getChild(String agentId,String location,String tipo);
  List getContentNode(String agentId,String location) throws XMLDBException;
  String getContentNodeXML(String agentId,String location,String property) throws XMLDBException;
  void updateNode(String agentId, String node, String where, String location) throws CleverException;
  void deleteNode(String agentId, String location) throws CleverException;
  String getContentNodeObject(String agentId,String location) throws XMLDBException;
  boolean checkAgentNode(String agentId, String location) throws CleverException;
  public void setOwner(Agent owner);
  List getNameAttributes(String agentId,String location,String condition);
  //String getFatherAttributeNode(String agentId,String location,String sonCondition,String tipo) throws XMLDBException;
  
  //NEWMONITOR
  boolean checkMeasure();
  void addMeasure();
  void insertMeasure(String measure);
  //NEWMONITOR
  
}