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

package org.clever.Common.Shared;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;


public class Support
{

  /**
   * Generate random password
   * @param length the length of the password
   * @return returns password
   */
  static public String generatePassword( final int length )
  {
    String password = "";
    Random random = new Random( System.currentTimeMillis() );
    for( int i = 0; i < length; i++ )
    {
      password += (char) ( 97 + random.nextInt( 25 ) );
    }

    return password;
  }

  /**
   * Copy the content of the file to another
   * @param src source file
   * @param dst destination file
   * @throws IOException error during copy
   */
  static public void copy( final InputStream src, final File dst ) throws IOException
  {
    OutputStream out = new FileOutputStream( dst );

    // Transfer bytes from in to out
    byte[] buf = new byte[ 1024 ];
    int len;
    while ( ( len = src.read( buf ) ) > 0 )
    {
      out.write( buf, 0, len );
    }
    
    src.close();
    out.close();
  }
}
