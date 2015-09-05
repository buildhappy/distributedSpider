package com.buildhappy.downloaderModel.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author buildhappy
 */
public class StringToSHA1 {
    
    public static String toSHA1(String str) {
        String sha1 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
            
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(StringToSHA1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sha1;
    }
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for(byte b: hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
        
    }
    
    public static void main(String[] args) {
        System.out.println(toSHA1("http://wap.shouji.com.cn/wap/wdown/soft?id=30693"));
    }
    
}