/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.Common.Utils;

/**
 *
 * @author Antonio Galletta 2014
 */
public enum OperationName {
    maximum(1),
    minimum(2),
    average(3),
    first(4),
    last(5),
    sum(6);
     
     
     private final int value;

        private OperationName(int value) {
                this.value = value;
        }
       
        public int getValue(){
            return value;
        }
    
    
}
