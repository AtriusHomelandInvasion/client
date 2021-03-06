/*
* Coypright (c) 2015 Justin Kuenzel
* Apache 2.0 License
*
* This file doesnt belongs to the Pentaquin Project.
* This class is owned by Justin Kuenzel and licensed under the Apache 2.0 license.
* Many projects use this class.
*/

package com.jukusoft.mmo.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Justin on 26.01.2015.
 */
public class HashUtils {

    protected static final Logger LOGGER = Logger.getLogger("HashUtils");
    protected static final String LOG_MESSAGE = "an exception was thrown";

    /**
     * private constructor, so other classes cannot create an instance of HashUtils
     */
    protected HashUtils () {
        //
    }

    /**
    * convert byte data to hex
     *
     * @deprecated because it isnt used anymore
    */
    @Deprecated
    private static String convertToHex(byte[] data) {
        //create new instance of string buffer
        StringBuilder stringBuffer = new StringBuilder();
        String hex = "";

        //encode byte data with base64
        hex = Base64.getEncoder().encodeToString(data);
        stringBuffer.append(hex);

        //return string
        return stringBuffer.toString();
    }

    /**
    * converts an byte array to an hex string
     *
     * @param data byte array
     *
     * @return hex string
    */
    public static String toHex (byte[] data) {
        StringBuilder hash = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            String h = Integer.toHexString(0xFF & data[i]);
            while (h.length() < 2)
                h = "0" + h;
            hash.append(h);
        }

        return hash.toString();
    }

    /**
     * generates SHA-512 Hash for passwords
     *
     * @param password text
     *
     * @return hash
     */
    public static String computePasswordSHAHash(String password) {
        MessageDigest mdSha1 = null;
        String shaHash = "";

        try
        {
            mdSha1 = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e1) {
            LOGGER.log(Level.SEVERE, LOG_MESSAGE, e1);
            throw new RuntimeException("NoSuchAlgorithmException: " + e1.getLocalizedMessage());
        }
        try {
            mdSha1.update(password.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, LOG_MESSAGE, e);

            throw new RuntimeException("UnsupportedEncodingException: " + e.getLocalizedMessage());
        }
        byte[] data = mdSha1.digest();
        shaHash = convertToHex(data);

        return shaHash;
    }

    /**
    * generate an SHA256 hash compatible to PHP 5
     *
     * @param password password
     * @param salt salt
     *
     * @return sha hash
    */
    public static String computeSHA256Hash (String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] data = digest.digest((password + salt).getBytes("UTF-8"));
            return toHex(data);
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.log(Level.SEVERE, LOG_MESSAGE, e);
            return null;
        }
    }

    /**
     * generates MD5 hash
     *
     * This method is compatible to PHP 5 and Java 8.
     *
     * @param password text
     * @return hash
    */
    public static String computeMD5Hash(String password) {
        StringBuilder md5Hash = new StringBuilder();

        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes("UTF-8"));
            byte messageDigest[] = digest.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                md5Hash.append(h);
            }

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, LOG_MESSAGE, e);
        }

        return md5Hash.toString();
    }

    /**
    * generates an MD5 file hash, like an file checksum
     *
     * @param file file
     * @return hash
    */
    public static String computeMD5FileHash (File file) throws Exception {
        if (file == null) {
            throw new NullPointerException("file cannot be null.");
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException("cannot compute file hash of an directory: " + file.getAbsolutePath());
        }

        //read content of file
        String content = FileUtils.readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);

        content = content.replace(System.lineSeparator(), "BR");

        return computeMD5Hash(content);

        /*byte[] b = createFileChecksum(file);
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }

        return result;*/
    }

    /*private static byte[] createFileChecksum(File file) throws Exception {
        if (file == null) {
            throw new NullPointerException("file cannot be null.");
        }

        InputStream fis = null;

        try {
            fis =  new FileInputStream(file);

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
        } catch (Exception e) {
            if (fis != null) {
                fis.close();
            }

            throw e;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }*/

    public static Map<String,String> listFileHashesOfDirectory (File file, File baseDir) throws Exception {
        if (file == null) {
            throw new NullPointerException("dir / file cannot be null.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("directory / file doesnt exists: " + file.getAbsolutePath());
        }

        if (baseDir == null) {
            throw new NullPointerException("base dir cannot be null.");
        }

        if (!baseDir.exists()) {
            throw new IllegalArgumentException("base dir doesnt exists: " + file.getAbsolutePath());
        }

        if (file.getAbsolutePath().contains(".git")) {
            //dont calculate file hashes of git directory
            return new HashMap<>();
        }

        file = new File(FileUtils.removeDoubleDotInDir(file.getCanonicalPath()));
        baseDir = new File(FileUtils.removeDoubleDotInDir(baseDir.getCanonicalPath()));

        //map with all file hashes (file path - file checksum)
        Map<String,String> hashMap = new HashMap<>();

        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                if (c.isDirectory()) {
                    Map<String,String> hashes = listFileHashesOfDirectory(c, baseDir);

                    //add all generated hashes to map
                    hashMap.putAll(hashes);
                } else {
                    //it is an file

                    //generate file hash
                    String fileHash = HashUtils.computeMD5FileHash(c);

                    //compute relative file path
                    String relPath = FileUtils.getRelativeFile(c, baseDir).getPath().replace("\\", "/");

                    //put file hash to map
                    hashMap.put(relPath, fileHash);
                }
            }
        } else {
            //generate file hash
            String fileHash = HashUtils.computeMD5FileHash(file);

            //compute relative file path
            String relPath = FileUtils.getRelativeFile(file, baseDir).getPath().replace("\\", "/");

            //put file hash to map
            hashMap.put(relPath, fileHash);
        }

        return hashMap;
    }

}
