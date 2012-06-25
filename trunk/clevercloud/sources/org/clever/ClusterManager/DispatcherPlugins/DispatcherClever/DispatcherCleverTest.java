 /*
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
package org.clever.ClusterManager.DispatcherPlugins.DispatcherClever;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.ClusterManager.Dispatcher.DispatcherPlugin;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Exceptions.CleverException;



public class DispatcherCleverTest implements Runnable
{

  private DispatcherPlugin dispatcherToTest = null;
  private  List param = new ArrayList();



  public DispatcherCleverTest( DispatcherPlugin dispatcherToTest )
  {
       this.param.add( "eth0" );
    this.dispatcherToTest = dispatcherToTest;
  }



  @Override
  public void run()
  {

      while(true)
      {
        try
        {
        Thread.sleep( 4000 );
     
        MethodInvoker invoker = new MethodInvoker( "NetworkManagerAgent", "getAdapterInfo", true, param );
        System.out.println( "invio" );
        Object test = dispatcherToTest.dispatchToExtern( invoker, "maurizioPort" );
        System.out.println( test );
        }
        catch (CleverException ex) {

            ex.printStackTrace();
            Logger.getLogger(DispatcherCleverTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        catch( InterruptedException ex )
            {
            ex.printStackTrace();
            Logger.getLogger( DispatcherCleverTest.class.getName() ).log( Level.SEVERE, null, ex );
            }
      }
  }
}
