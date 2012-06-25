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

import org.clever.HostManager.MonitorPlugins.Sigar.Database;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Random;

public class ConsumerNetJms implements MessageListener{
    private static int ackMode;
    private static String clientQueueName;

    private boolean transacted = false;
    private MessageProducer producer;
    private static String messageText = null;
    private boolean InfoNet;
    
    public static final String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";

    static {
        clientQueueName = "client.messages";
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }

    public ConsumerNetJms(boolean flag) throws JMSException {

        this.InfoNet=flag;

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(transacted, ackMode);
            Destination adminQueue = session.createQueue(clientQueueName);

             //Setup a message producer to send message to the queue the server is consuming from
            this.producer = session.createProducer(adminQueue);
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //Create a temporary queue that this client will listen for responses on then create a consumer
            //that consumes message from this temporary queue...for a real application a client should reuse
            //the same temp queue for each message to the server...one temp queue per client
            Destination tempDest = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(tempDest);

            //This class will handle the messages to the temp queue as well
            responseConsumer.setMessageListener(this);

            //Now create the actual message you want to send
            TextMessage txtMessage = session.createTextMessage();
            txtMessage.setText("MyProtocolMessage");

            //Set the reply to field to the temp queue you created above, this is the queue the server
            //will respond to
            txtMessage.setJMSReplyTo(tempDest);

            //Set a correlation ID so when you get a response you know which sent message the response is for
            //If there is never more than one outstanding message to the server then the
            //same correlation ID can be used for all the messages...if there is more than one outstanding
            //message to the server you would presumably want to associate the correlation ID with this
            //message somehow...a Map works good
            String correlationId = this.createRandomString();
            txtMessage.setJMSCorrelationID(correlationId);
            this.producer.send(txtMessage);

        } catch (JMSException e) {
            //Handle the exception appropriately
        }
    }

    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    public void onMessage(Message message) {
        //String messageText = null;
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageText = textMessage.getText();
                //System.out.println("messageText = " + messageText);

                /*String separator=",";

                String[] pieces = messageText.split(separator);
                for (int i = pieces.length - 1; i >= 0; i--) {
                pieces[i] = pieces[i].trim();
                }

                List net = new ArrayList(Arrays.asList(pieces));
                for(int i=0;i<net.size();i++){
                    if(net.get(i).toString().startsWith("["))
                        System.out.println("messageText = " + net.get(i).toString().substring(1,net.get(i).toString().length()));
                    else if(net.get(i).toString().endsWith("]"))
                        System.out.println("messageText = " + net.get(i).toString().substring(0,net.get(i).toString().length()-1));
                    else
                        System.out.println("messageText = " + net.get(i).toString());
                }*/

            }
        } catch (JMSException e) {
            //Handle the exception appropriately
        }

        if(this.InfoNet==true)
                Message();


        try {
            RegisterDb();
        } catch (SQLException ex) {
            Logger.getLogger(ConsumerNetJms.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConsumerNetJms.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String Message(){

        System.out.println(this.messageText);
        return this.messageText;
    }


    public void RegisterDb() throws SQLException,Exception{

        Statement stat;

        stat = Database.istance().conn().createStatement();

        String separator=",";

        String[] pieces = this.messageText.split(separator);
        for (int i = pieces.length - 1; i >= 0; i--) {
        pieces[i] = pieces[i].trim();
        }

        List net = new ArrayList(Arrays.asList(pieces));
        for(int i=0;i<net.size();i++){
            if(net.get(i).toString().startsWith("["))
                messageText = net.get(i).toString().substring(1,net.get(i).toString().length());
                //System.out.println("messageText = " + net.get(i).toString().substring(1,net.get(i).toString().length()));
            else if(net.get(i).toString().endsWith("]"))
                messageText = net.get(i).toString().substring(0,net.get(i).toString().length()-1);
                //System.out.println("messageText = " + net.get(i).toString().substring(0,net.get(i).toString().length()-1));
            else
                messageText = net.get(i).toString();
                //System.out.println("messageText = " + net.get(i).toString());
         }
                
            stat.executeUpdate("insert into NetState " +
                        "(totpkts, totpktsize, average, throughput, throughputpkts,Time) values " +
                        "('"+ net.get(0) +"', " +
                        "'"+net.get(1)+"'," +
                        "'"+net.get(2)+"'," +
                        "'"+net.get(3)+"'," +
                        "'"+net.get(4)+"'," +
                        "'"+LastNotification()+"');");
    }

    public static String LastNotification() {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            return sdf.format(cal.getTime());
    }

     public Message postProcessMessage(Message message) throws JMSException {
            return message;
     }
}
