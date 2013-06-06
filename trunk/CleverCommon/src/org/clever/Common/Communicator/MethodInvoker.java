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
package org.clever.Common.Communicator;

import java.util.List;



public class MethodInvoker
{

  String module;
  String methodName;
  List params;
  boolean hasReturn;



  public MethodInvoker( String module, String methodName, boolean hasReturn, List params )
  {
    this.module = module;
    this.methodName = methodName;
    this.params = params;
    this.hasReturn = hasReturn;
  }



  public String getModule()
  {
    return ( module );
  }



  public void setModule( String module )
  {
    this.module = module;
  }



  public String getMethodName()
  {
    return ( methodName );
  }



  public void setMethodName( String methodName )
  {
    this.methodName = methodName;
  }



  public List getParams()
  {
    return ( params );
  }



  public void setList( List params )
  {
    this.params = params;
  }



  public boolean getHasReturn()
  {
    return ( hasReturn );
  }



  public void setHasReturn( boolean hasReturn )
  {
    this.hasReturn = hasReturn;
  }
}
