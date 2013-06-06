package org.clever.Common.Exceptions;


public class StopException extends HyperVisorException {
    public StopException (String string){
        super(string);
    }
}