/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Event.*;
import java.io.*;
import javax.swing.tree.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.apache.xerces.parsers.*;
/**
 * Wizard logical catalog of CLEVER
 * @author giancarloalteri
 */
public class TreeCommand extends CleverCommand{
    private SAXTreeBuilder saxTree = null;
    @Override
    public Options getOptions() {
        Options options = new Options();
     //  options.addOption( "xml", false, "Displays the XML request/response Messages." );
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        {
            FileWriter fw = null;
            try {

                ArrayList params = new ArrayList();
                params.add("/");
                params.add("");
                String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
                String xml=(String) ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "getContentNodeXML", params, commandLine.hasOption( "xml" ) );
                if(xml.isEmpty()){
                    xml="<message>Locical Catalog is empty</message>";
                }
                String localpath=System.getProperty("user.dir")+"/storage.xml";
                File file = new File(localpath);
                fw = new FileWriter(file);
                fw.write(xml);
                fw.flush();
                fw.close();
                JFrame frame = new JFrame("CLEVER STORAGE: [ Logical Catalog ]");
                frame.setSize(500,500);
                frame.addWindowListener(new WindowAdapter(){
                     public void windowClosing(WindowEvent ev){
                        // System.exit(0);
                     }
                });
                frame.getContentPane().setLayout(new BorderLayout());
                DefaultMutableTreeNode top = new DefaultMutableTreeNode(file);
                saxTree = new SAXTreeBuilder(top);
                try {             
                SAXParser saxParser = new SAXParser();
                saxParser.setContentHandler(saxTree);
                saxParser.parse(new InputSource(new FileInputStream(file)));
                }catch(Exception ex){
                   top.add(new DefaultMutableTreeNode(ex.getMessage()));
                }
                JTree tree = new JTree(saxTree.getTree());

                
                
     tree.setCellRenderer(new DefaultTreeCellRenderer()
        {
             public Component getTreeCellRendererComponent(JTree pTree,
                 Object pValue, boolean pIsSelected, boolean pIsExpanded,
                 boolean pIsLeaf, int pRow, boolean pHasFocus)
             {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)pValue;
	    super.getTreeCellRendererComponent(pTree, pValue, pIsSelected,
                     pIsExpanded, pIsLeaf, pRow, pHasFocus);
          setBackgroundSelectionColor(Color.orange);
          
               if (node.isRoot())
	       setBackgroundNonSelectionColor(Color.red);
	    else if (node.getChildCount() > 0)
	       setBackgroundNonSelectionColor(Color.blue);
	    else if (pIsLeaf)
	       setBackgroundNonSelectionColor(Color.green);
          
               return (this);
	}
        });
                
                /*
                 DefaultTreeCellRenderer renderer =
(DefaultTreeCellRenderer) tree.getCellRenderer();
renderer.setTextNonSelectionColor(Color.BLACK);
renderer.setBackgroundSelectionColor(Color.PINK);
renderer.setBorderSelectionColor(Color.ORANGE);
*/
                
                JScrollPane scrollPane = new JScrollPane(tree);
                frame.getContentPane().add("Center",scrollPane);
                frame.setVisible(true);
            
            
            } catch (IOException ex) {
                Logger.getLogger(TreeCommand.class.getName()).log(Level.SEVERE, null, ex);
            }catch (CleverException ex) {
               logger.error(ex);
               if(commandLine.hasOption("debug"))
                    ex.printStackTrace();
               else
                   System.out.println(ex);
           } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(TreeCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    

    @Override
    public void handleMessage(Object response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleMessageError(CleverException response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    


    
    class SAXTreeBuilder extends DefaultHandler{
       
       private DefaultMutableTreeNode currentNode = null;
       private DefaultMutableTreeNode previousNode = null;
       private DefaultMutableTreeNode rootNode = null;

       public SAXTreeBuilder(DefaultMutableTreeNode root){
              rootNode = root;
       }
       public void startDocument(){
              currentNode = rootNode;
       }
       public void endDocument(){
       }
       public void characters(char[] data,int start,int end){
              String str = new String(data,start,end);              
              if (!str.equals("") && Character.isLetter(str.charAt(0)))
                  currentNode.add(new DefaultMutableTreeNode(str));           
       }
       public void startElement(String uri,String qName,String lName,Attributes atts){
              previousNode = currentNode;
              currentNode = new DefaultMutableTreeNode(lName);
              // Add attributes as child nodes //
              attachAttributeList(currentNode,atts);
              previousNode.add(currentNode);              
       }
       public void endElement(String uri,String qName,String lName){
              if (currentNode.getUserObject().equals(lName))
                  currentNode = (DefaultMutableTreeNode)currentNode.getParent();              
       }
       public DefaultMutableTreeNode getTree(){
              return rootNode;
       }
       
       private void attachAttributeList(DefaultMutableTreeNode node,Attributes atts){
              
           
           for (int i=0;i<atts.getLength();i++){
                    String name = atts.getLocalName(i);
                    String value = atts.getValue(name);
                    node.add(new DefaultMutableTreeNode(name + " = " + value));
               }
       }
       
}
    
}    
     

