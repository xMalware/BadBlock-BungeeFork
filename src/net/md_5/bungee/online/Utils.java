package net.md_5.bungee.online;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.net.ssl.HttpsURLConnection;

public class Utils
{
  public static final Charset fileCharset = Charset.forName("ISO-8859-1");
  
  public static boolean isSessionServerOnline()
  {
    try
    {
        HttpsURLConnection connection = (HttpsURLConnection)new URL("https://sessionserver.mojang.com").openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("HEAD");
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        HttpsURLConnection connection2 = (HttpsURLConnection)new URL("https://authserver.mojang.com").openConnection();
        connection2.setConnectTimeout(10000);
        connection2.setReadTimeout(10000);
        connection2.setRequestMethod("HEAD");
        int responseCode2 = connection2.getResponseCode();
        connection2.disconnect();
      
      return (200 <= responseCode) && (responseCode <= 399) && (200 <= responseCode2) && (responseCode2 <= 399);
    }
    catch (IOException exception) {}
    return false;
  }
  
}

