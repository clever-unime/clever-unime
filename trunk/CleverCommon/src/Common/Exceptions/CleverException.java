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

package org.clever.Common.Exceptions;

public class CleverException extends Exception {
    private Throwable InternalException;
    /**
     * Creates a new instance of <code>CleverException</code> without detail message.
     */
    public CleverException() {
        super();
        InternalException=null;
    }


    public CleverException(Throwable e) {
        super();
        InternalException=e;
    }


    public CleverException(Throwable e, String msg) {
        super(msg);
        InternalException=e;
    }

    /**
     * Constructs an instance of <code>CleverException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CleverException(String msg) {
        super(msg);
        InternalException=null;

    }

    public Throwable getInternalException() {
        return InternalException;
    }
    @Override
    public void printStackTrace()
    {
        super.printStackTrace();
        if(InternalException!=null)
        {
            InternalException.printStackTrace();
        }
    }

    /**
     * @param InternalException the InternalException to set
     */
    public void setInternalException(Throwable InternalException) {
        this.InternalException = InternalException;
    }

}
