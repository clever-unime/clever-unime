/*
 *  Copyright (c) 2010 Patrizio Filloramo
 *  Copyright (c) 2010 Salvatore Barbera
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
package org.clever.HostManager.NetworkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;



public class IPAddress
{

  private String address = "";



  public IPAddress( String address )
  {
    setAddress( address );
  }



  public String getAddress()
  {
    return this.address;
  }



  private void setAddress( String address )
  {
    if( address.contains( "/" ) )
    {
      StringTokenizer trunk = new StringTokenizer( address, "/" );
      this.address = trunk.nextToken();
      if( this.address.contains( "%" ) )
      {
        StringTokenizer trunk2 = new StringTokenizer( address, "%" );
        this.address = trunk2.nextToken();
      }
    }
    else if( address.contains( "%" ) )
    {
      StringTokenizer trunk = new StringTokenizer( address, "%" );
      this.address = trunk.nextToken();
    }
    else
    {
      this.address = address;
    }
  }



  private Vector toByte()
  {
    Vector vector = new Vector();
    String onebyte;

    if( !( getAddress().contains( ":" ) ) )
    {
      StringTokenizer st = new StringTokenizer( getAddress(), "." );
      while( st.hasMoreTokens() )
      {
        onebyte = Integer.toBinaryString( Integer.parseInt( st.nextToken() ) );
        if( onebyte.length() < 8 )
        {
          while( onebyte.length() < 8 )
          {
            onebyte = "0" + onebyte;
          }
        }
        vector.add( onebyte );
      }
    }

    return vector;
  }



  public String toBinary()
  {
    String toBinary = null;
    Vector vector = new Vector();
    vector = this.toByte();
    if( !vector.isEmpty() )
    {
      toBinary = vector.get( 0 ) + "." + vector.get( 1 ) + "." + vector.get( 2 ) + "." + vector.get( 3 );
    }
    return toBinary;
  }



  public static String hexToIp( String hex )
  {
    List str = new ArrayList();
    int i = 0;
    for( i = 0; i < hex.length(); i++ )
    {
      str.add( new String( hex.substring( i, i + 2 ) ) );
      i++;
    }
    return Integer.parseInt( ( String ) str.get( 3 ), 16 ) + "."
           + Integer.parseInt( ( String ) str.get( 2 ), 16 ) + "."
           + Integer.parseInt( ( String ) str.get( 1 ), 16 ) + "."
           + Integer.parseInt( ( String ) str.get( 0 ), 16 );
  }
}
