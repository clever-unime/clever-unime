/*
 *  Copyright (c) 2011 Antonio Nastasi
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.RequestExpired;



public class Request implements Runnable
{

  private int id = 0;
  private String src = "";
  public enum Type
  {

    INTERNAL,
    EXTERNAL
  };

  private Type type;
  private Object returnValue = null;
  private boolean expired ;
  private long timeout = 0;




  @Override
  public synchronized void run()
  {
      //FIXME: check the spurious wakeups
    //while( returnValue == null )
    {
      try
      {
        wait(timeout);
      }
      catch( InterruptedException ex )
      {
        Logger.getLogger( Request.class.getName() ).log( Level.SEVERE, null, ex );
      }
    }
  }



  public Request( final int id, final String src )
  {
    this.id = id;
    this.src = src;
    this.type = Type.EXTERNAL;
  }

 public Request( final int id, final String src ,long t)
  {
    this.id = id;
    this.src = src;
    this.type = Type.EXTERNAL;
    this.timeout=t;
  }

  public Request( final int id )
  {
    this.id = id;
    this.type = Type.INTERNAL;
  }

 public Request( final int id , long t)
  {
    this.id = id;
    this.type = Type.INTERNAL;
    this.timeout=t;
  }


  public synchronized void setReturnValue( Object returnValue )
  {
    this.returnValue = returnValue;
    expired=false; //request's reply arrived before timeout
    notifyAll();
  }



  public synchronized Object getReturnValue() throws CleverException
  {
    expired=true; //flag to check request time out
    run();
    if(expired)
    {
        throw new RequestExpired();
    }
    if(returnValue instanceof CleverException)
    {
        throw (CleverException)returnValue;
    }

    return returnValue;
  }



  public Type getType()
  {
    return type;
  }



  public int getId()
  {
    return id;
  }



  public String getSrc()
  {
    return src;
  }
}
