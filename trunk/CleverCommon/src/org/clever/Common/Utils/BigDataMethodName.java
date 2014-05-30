/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.Common.Utils;

/**
 *
 * @author Antonio Galletta
 */
public enum BigDataMethodName {
     findLessThan(1),
     findGreaterThan(2),
     findLessOrEqualThan(3),
     findGreaterOrEqualThan(4),
     findOnRangeWithBorder(5),
     findOnRangeWithoutBorder(6),
     findOnRangeWithBorderSup(7),
     findOnRangeWithBorderInf(8),
     equal(9);
     
     private int value;

        private BigDataMethodName(int value) {
                this.value = value;
        }
       
        public int getValue(){
            return value;
        }
    
}