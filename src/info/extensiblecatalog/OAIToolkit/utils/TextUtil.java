/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */

package info.extensiblecatalog.OAIToolkit.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

//import com.sun.org.apache.xml.internal.utils.FastStringBuffer;

/**
 * Helper class for useful String maniulations. All methods are static here. 
 * @author Peter Kiraly
 */
public class TextUtil {
	
	private static final int BUFFER_SIZE = 1024; 
	
	/**
	 * Create a camel case string (eg. endOfFile) from an underscore-separated 
	 * compounds (eg. "end_of_file") 
	 * @param text The underscore separated string
	 * @return The camelCase string
	 */
	public static String toCamelCase(String text) {
		if (text.indexOf('_') >= 0) {
            StringBuffer buff = new StringBuffer(text.length());
            boolean afterHyphen = false;
            for (int n = 0; n < text.length(); n++) {
                char c = text.charAt(n);
                if (c == '_') {
                    afterHyphen = true;
                } else {
                    if (afterHyphen) {
                        buff.append(Character.toUpperCase(c));
                    } else {
                        buff.append(c);
                    }
                    afterHyphen = false;
                }
            }
            text = buff.toString();
        }
		return text;
	}
	
	/**
	 * Create an underscore-separated compounds (eg. "end_of_file") 
	 * from camel case (eg. endOfFile) string
	 * @param text The camelCase string
	 * @return The underscore separated string
	 */
	public static String fromCamelCase(String text) {
		return text.replaceAll("([A-Z])", "_$1").toLowerCase();
	}
	
	/**
	 * Join the elements of an array of strings with a separator.
	 * @param list The input list
	 * @param separator The separator between two elements
	 * @return The joined elements
	 */
	public static String join(String[] list, String separator) {
		return join(Arrays.asList(list), separator);
	}
	
	/**
	 * Join the elements of a list with a separator.
	 * @param list The input list
	 * @param separator The separator between two elements
	 * @return The joined elements
	 */
	public static String join(List list, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(Object member : list) {
			sb.append(sep).append(member.toString());
			sep = separator;
		}
		return sb.toString();
	}
	
	public static Date dateToUTCDate(Date d)
    {
	    String strDate = "";
	    GregorianCalendar calendar = new GregorianCalendar();
	    calendar.setTime(d);
	    int utcOffsetInMinutes =  -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000);
	    calendar.add(Calendar.MINUTE, utcOffsetInMinutes);
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    return calendar.getTime();
    }
	
	/** 
	 * Convert string in format "yyyyMMddhhmmss.S" (as in the MARC records)
	 * to java.sql.Timestamp 
	 * @param marcTimestamp The input timestamp string
	 * @return The resulting Timestamp object
	 * @throws ParseException
	 */
	public static Timestamp stringToTimestamp(String marcTimestamp) 
			throws ParseException {
		Timestamp timestamp; 
		Date d = new SimpleDateFormat("yyyyMMddHHmmss.S").parse(marcTimestamp);
		timestamp = new Timestamp(d.getTime());
		timestamp.setNanos(
				Integer.parseInt(
						marcTimestamp.substring(marcTimestamp.length()-1)));
		return timestamp;
	}

	/**
	 * Get the current time in UTC
	 * @return The current time in UTC
	 */
	 public static String nowInUTC() {
		Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(date);
	}

	public static Timestamp utcToTimestamp(String utcTimestamp) 
			throws ParseException {
		Timestamp timestamp;		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		Date d = df.parse(utcTimestamp.replaceFirst("Z$", "+0000"));
		timestamp = new Timestamp(d.getTime());
		return timestamp;
	}
	
	public static Date utcToDate(String utcTimestamp) 
			throws ParseException {	
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = df.parse(utcTimestamp.replaceFirst("Z$", "+0000"));
		return d;
	}
	
	public static Timestamp luceneToTimestamp(String utcTimestamp)
			throws ParseException {
		Timestamp timestamp;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Date d = df.parse(utcTimestamp.replaceFirst("Z$", "+0000"));
		timestamp = new Timestamp(d.getTime());
		return timestamp;
	}

	public static Date luceneToDate(String utcTimestamp)
			throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = df.parse(utcTimestamp.replaceFirst("Z$", "+0000"));
		return d;
	}

	public static String utcToMysqlTimestamp(String utcTimestamp) 
			throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		Date d = df.parse(utcTimestamp.replaceFirst("Z$", "+0000"));
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(d);
	}

	/**
	 * Convert a java.sql.Timestamp object (come from the database) to
	 * string in "yyyyMMddhhmmss.S" format
	 * @param marcTimestamp The Timestamp object
	 * @return The string value
	 */
	public static String timestampToString(Timestamp marcTimestamp) {
		return new SimpleDateFormat("yyyyMMddHHmmss.S").format(marcTimestamp);
	}
		
	/**
	 * Convert a java.sql.Timestamp object (come from the database) to
	 * string in UTCdatetime format "yyyy-MM-dd'T'HH:mm:ssZ" format
	 * @param marcTimestamp The Timestamp object
	 * @return The string value
	 */
	public static String timestampToUTC(Timestamp marcTimestamp) {
		if(null == marcTimestamp) {
			return "no timestamp";
		} else {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(marcTimestamp);
		}
	}
	
	/** 
	 * Read a file content to a string variable
	 * @param filePath The path of the file
	 * @return The file's content
	 * @throws IOException
	 */
	public static String readFileAsString(File filePath) throws IOException {
		return readFileAsString(filePath.getAbsolutePath());
	}

	/**
	 * Read a file content to a string variable
	 * @param filePath The path of the file
	 * @return The file's content
	 * @throws IOException
	 */
	public static String readFileAsString(String filePath) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = new BufferedInputStream(new FileInputStream(filePath));
		byte[] buf = new byte[BUFFER_SIZE];
		int numOfBytes = is.read(buf);
		while(numOfBytes != -1){
			baos.write(buf, 0, numOfBytes);
			numOfBytes = is.read(buf);
		}
		is.close();
		return new String(baos.toByteArray(), "UTF-8");
    }
	
	/**
	 * Write content to file
	 * 
	 * @param file The file name
	 * @param content The content of the new file
	 */
	public static void writeFile(File file, String content) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		out.write(content);
		out.close();
	}
	
	/** Create a string representation of hash map as
	 * key="value" key="value" ...
	 * @param map
	 * @return
	 */
	public static String hashMapToString(HashMap<String, ?> map) {
		StringBuffer text = new StringBuffer();
		boolean isFirst = true;
		for(Map.Entry<String, ?> e : map.entrySet()) {
			if(!isFirst) text.append(' ');
			text.append(e.getKey()).append("=\"")
					.append(e.getValue().toString())
					.append('"');
			isFirst = false;
		}
		return text.toString();
	}
	
	/**
	 * Return the UTF-8 representation ('\\uxxxx') of a character
	 * @param a
	 * @return
	 */
	public static String charToHexa(char a) {
		byte[] bytes;
		try {
			bytes = String.valueOf(a).getBytes("UTF-8");
		} catch(UnsupportedEncodingException e) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("'\\u");
		if(1 == bytes.length){
			sb.append("00");
		}
		String hex;
		for (int j = 0; j < bytes.length; j++) {
			hex = Integer.toHexString(bytes[j] & 0xff).toUpperCase();
			if(1 == hex.length()) {
				sb.append('0');
			}
			sb.append(hex);
		}
		sb.append('\'');
		return sb.toString();
	}
	
	/**
	 * Get the content of a web resource
	 * @param url The URL of the resource
	 * @return The content of the resource
	 */
	public static String getWebContent(String urlString) {

		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
			
			if(url.getProtocol().equals("http")) {
				HttpURLConnection httpConn = (HttpURLConnection)conn;  
				if(httpConn.getResponseCode() != 200) {
					System.out.println("The link (" + url + ") is wrong. "
						+ "The server responses: " + httpConn.getResponseCode() 
						+ " " + httpConn.getResponseMessage());
					return null;
				}
			}
			
			/*
			System.out.println("headers: ");
			Map<String, List<String>> headers = conn.getHeaderFields();
			for(String key : headers.keySet()) {
				System.out.print(key + ": ");
				boolean isFirst = true;
				for(String v : headers.get(key)) {
					if(!isFirst) {
						System.out.print(", ");
					}
					System.out.print(v);
				}
				System.out.println();
			}
			*/

			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(
						conn.getInputStream()));

			String line;
			while((line = bufferedReader.readLine()) != null) {
				content.append(line).append(ApplInfo.LN);
			}
			bufferedReader.close();
		} catch (ConnectException e) {
			System.out.println(e.getMessage());
			//e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e);
			//e.printStackTrace();
		}
		return content.toString();
	}
	
}
