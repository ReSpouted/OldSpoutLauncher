package org.respouted.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    public static final String VERSION = "1.1";

    public static String readEntireStream(InputStream is) throws IOException {
        if(is == null) return null;
        byte[] bytes = new byte[is.available()];
        DataInputStream dataInputStream = new DataInputStream(is);
        dataInputStream.readFully(bytes);
        return new String(bytes);
    }

    public static String getResponseBody(int responseCode, HttpsURLConnection connection) throws IOException {
        if(responseCode >= 200 && responseCode < 300) {
            return readEntireStream(connection.getInputStream());
        } else {
            return readEntireStream(connection.getErrorStream());
        }
    }
}
