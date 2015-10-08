/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.Common.Digest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 *
 * @author agalletta
 */
public class Digest {
    
    /**
 * Funzione secondaria. Viene richiamata all'interno di getMD5Checksum()
 * @param filename
 * @return
 * @throws Exception 
 */
private static byte[] createChecksum(String filename) throws Exception {
       InputStream fis =  new FileInputStream(filename);

       byte[] buffer = new byte[1024];
       MessageDigest complete = MessageDigest.getInstance("MD5");
       int numRead;

       do {
           numRead = fis.read(buffer);
           if (numRead > 0) {
               complete.update(buffer, 0, numRead);
           }
       } while (numRead != -1);

       fis.close();
       return complete.digest();
   }


/**
 * Questa funzione restituisce l'md5 del file di input.
 * @param filename
 * @return
 * @throws Exception 
 */
public static String getMD5Checksum(String filename) throws Exception {
       byte[] b = createChecksum(filename);
       String result = "";
       for (int i=0; i < b.length; i++) {
           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
       }
       return result;
   }
    
}
