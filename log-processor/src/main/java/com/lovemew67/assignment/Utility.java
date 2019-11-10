package com.lovemew67.assignment;

import akka.event.LoggingAdapter;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import java.io.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Utility {

    //# https://howtodoinjava.com/core-java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
    public static String getFileSha256Checksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };
        fis.close();
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String getFileMimeType(File file) throws MagicParseException, MagicException, MagicMatchNotFoundException {
        return Magic.getMagicMatch(file, true).getMimeType();
    }

    public static int readIntegerFromFile(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        int i = (int) in.readObject();
        in.close();
        fileIn.close();
        return i;
    }

    public static void writeIntegerToFile(Integer i, String fileName) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(i);
        out.close();
        fileOut.close();
    }

    public static HashMap<String, Integer> readHashMapFromFile(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        HashMap m = (HashMap<String, Integer>) in.readObject();
        in.close();
        fileIn.close();
        return m;
    }

    public static void writeHashMapToFile(HashMap<String, Integer> map, String fileName) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(map);
        out.close();
        fileOut.close();
    }

    public static void printOutHashMapBeautifully(HashMap<String, Integer> map, String fileName, LoggingAdapter log) {
        List<String> keys = new ArrayList(map.keySet());
        Collections.sort(keys);
        log.info("Word count of file: [{}]", fileName);
        for (String key : keys) {
            log.info("\tCount:[{}]\tWord:[{}]", map.get(key), key);
        }
    }

}
