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
package org.clever.administration.commands;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.InvocationCallback;
import org.clever.Common.Exceptions.CleverException;



public abstract class CleverCommand implements InvocationCallback
{

  protected Logger logger = Logger.getLogger( "CleverCommand" );



  /**
   * Get the options for the current command
   * @return
   */
  public abstract Options getOptions();



  /**
   * Execute command
   * @param commandLine
   */
  public abstract void exec( final CommandLine commandLine );



  /**
   * 
   * @param response
   */
  public abstract void handleMessage( final Object response );
  public abstract void handleMessageError(CleverException e); 
}
