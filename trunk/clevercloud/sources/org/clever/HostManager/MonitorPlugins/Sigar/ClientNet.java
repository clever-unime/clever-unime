/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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

package org.clever.HostManager.MonitorPlugins.Sigar;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientNet{

    private List net = new ArrayList(4);
    private List network;

    public List getNet(BufferedReader in) {

        String message;
        int MAX_NET_INFO = 5;

        try {
            if (net.size() == MAX_NET_INFO) {
                net.clear();
            } else {
                while (net.size() < MAX_NET_INFO) {
                    message = in.readLine();
                    net.add(message);
                }
            }

            //out.close();
            //in.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return net;
    }

    public List Connect(){

        BufferedReader in = null;
        PrintStream out = null;
        Socket socket = null;

        try {
                // open a socket connection
                InetAddress host = InetAddress.getLocalHost();
                socket = new Socket(host, 4000);

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintStream(socket.getOutputStream(), true);

                network = getNet(in);
                for(int i=0;i<network.size();i++){
                    if(network.get(i)==null)
                        System.exit(0);
                }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return network;
    }

  /*  public static void main(String argv[]) throws IOException {

        BufferedReader in = null;
        PrintStream out = null;
        Socket socket = null;

        try {
            // open a socket connection
            InetAddress host = InetAddress.getLocalHost();
            socket = new Socket(host, 4000);

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream(), true);

            while (true) {
                List network = getNet(in);
                for(int i=0;i<network.size();i++){
                    if(network.get(i)==null)
                        System.exit(0);
                    else
                        System.out.println(network);
                }
                
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }*/
}
