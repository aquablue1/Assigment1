package SampleTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * UrlCache Class
 * 
 * @author Majid Ghaderi, Modified by Rayce Rossum
 * @version 1.0, Sep 22, 2015
 *
 */
public class UrlCache {
	ArrayList<CatalogObject> catalog;

	/**
	 * Default constructor to initialize data structures used for caching/etc If
	 * the cache already exists then load it. If any errors then throw
	 * exception.
	 *
	 * @throws UrlCacheException
	 *             if encounters any errors/exceptions
	 */
	public UrlCache() throws UrlCacheException {
		try {   // if the file exists, then use it.
			FileInputStream cataIn = new FileInputStream(System.getProperty("user.dir") + File.separator + "catalog.ser");
			ObjectInputStream objIn = new ObjectInputStream(cataIn);
			catalog = (ArrayList<CatalogObject>) objIn.readObject();
			cataIn.close();
			objIn.close();
		}
		catch(IOException i) { // if the file does not exist, create it
			System.out.println("catalog not found");
			catalog = new ArrayList<CatalogObject>();
		}
		catch(ClassNotFoundException i) {
			i.printStackTrace();
			throw new UrlCacheException();
		}
		//		System.out.println(catalog.toString());
	}

	/**
	 * Downloads the object specified by the parameter url if the local copy is
	 * out of date.
	 *
	 * @param url
	 *            URL of the object to be downloaded. It is a fully qualified
	 *            URL. hostname[:port]/pathname
	 * @throws UrlCacheException
	 *             if encounters any errors/exceptions
	 */
	public void getObject(String url) throws UrlCacheException {
		Socket socket;
		String fileUrl = url.replace('/', '-');
		fileUrl = fileUrl.replace(':', '=');
		
		HashMap<String, String> headerData = new HashMap<String, String>();
		String[] urlWords = parseURL(url);
		String[] portSplit = urlWords[0].split(":");
		
		String hostname = null;
		int port; // Parse port number
		if(portSplit.length > 1) { // means there is a given port number.
			port = Integer.parseInt(portSplit[1]);
			hostname = portSplit[0];
		} else {
			port = 80;
			hostname = urlWords[0];
		}
		String lastModDate = msToStr(getLastModified(url));
		
		// Textbook P115, examples of request and reply
		String request = "GET /" + urlWords[1] + " HTTP/1.0\r\nIf-Modified-Since: " + lastModDate + "\r\n\r\n";
		System.out.println(request);
		
		byte[] data = new byte[10240];
		int byteIn = -1;
		try {
			socket = new Socket(hostname, port);
			
			OutputStream outStream = socket.getOutputStream();
			InputStream inStream = socket.getInputStream();
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			// Make request
			outStream.write(request.getBytes());
			outStream.flush();
			// Read data
			while((byteIn = inStream.read(data)) != -1) {
				System.out.println(byteIn);
				byteOut.write(data, 0, byteIn);
			}

			headerData = parseHTTPHeader(byteOut.toString());
			System.out.println("====================");
			System.out.println(byteOut.toString());
			if(!headerData.get("http").equals("304 Not Modified")) {
				Date date = strToDate(headerData.get("lastMod"));
				CatalogObject catObj = new CatalogObject(url, date);
				
				catalog.add(catObj);

				System.out.println(catalog);
				FileOutputStream catOut = new FileOutputStream("catalog.ser");
				ObjectOutputStream objOut = new ObjectOutputStream(catOut);
				
				objOut.writeObject(catalog);
				objOut.close();
				catOut.close();
				
				System.out.println(fileUrl);
				FileOutputStream fileOut = new FileOutputStream(fileUrl);
				
				// Remove header form byte array
				String headerSep = "\r\n\r\n";
				byte[] byteSep = headerSep.getBytes();
				byte[] byteArray = byteOut.toByteArray();
				System.out.println(Arrays.toString(byteSep));
				for(int i=0; i<byteArray.length; i++) {
					if(byteArray[i] == byteSep[0] && 
							byteArray[i + 1] == byteSep[1] && 
							byteArray[i + 2] == byteSep[2] &&
							byteArray[i + 3] == byteSep[3]) {
						byteArray = Arrays.copyOfRange(byteArray, i + 4, byteArray.length);	
					}
				}
				fileOut.write(byteArray);
				fileOut.flush();
				fileOut.close();
				
			}
			inStream.close();
			outStream.close();
			
			socket.close();

			}
			catch (UnknownHostException e) {
				e.printStackTrace();
				throw new UrlCacheException();
			}
			catch(IOException e) {
				e.printStackTrace();
				throw new UrlCacheException();
			}
		
	}

	/**
	 * Returns the Last-Modified time associated with the object specified by
	 * the parameter url.
	 *
	 * @param url
	 *            URL of the object
	 * @return the Last-Modified time in millisecond as in Date.getTime()
	 * @throws UrlCacheException
	 *             if the specified url is not in the cache, or there are other
	 *             errors/exceptions
	 */
	public long getLastModified(String url) throws UrlCacheException {
		try {
			Iterator<CatalogObject> iterator = catalog.iterator();
			CatalogObject obj = null;
			while(iterator.hasNext()) {
				obj = iterator.next();
				if (obj.getHostName().equals(url)){
					return obj.getLastModified().getTime();
				}
			}
			throw new UrlCacheException("URL is not in cache");
		} 
		catch(UrlCacheException e) {
			return -1;
		}

	}
	/**
	 * Downloads the object specified by the parameter url if the local copy is
	 * out of date.
	 *
	 * @param url
	 *            URL of the object to be split. It is a fully qualified
	 *            URL.
	 * 
	 * @return urlWords
	 * 			  an array of URL words with the hostname and path split
	 * 
	 */
	public String[] parseURL(String url) {
		String[] urlWords = url.split("/", 2);

		return urlWords;
	}
	
	/**
	 * Takes data from the header string and stores it in a hashmap
	 *
	 * @param http
	 *            String containing the HTTPHeader
	 * 
	 * @return httpHeader
	 * 			  a hashmap of the http header's data
	 * 
	 */

	public HashMap<String, String> parseHTTPHeader(String http) { // Take data from header and store in hashmap
		String[] headerData = new String[10];
		HashMap<String, String> httpHeader = new HashMap<String, String>();
		headerData = http.split("\r\n\r\n");
		if (headerData.length > 1) {
			headerData[1] = null;
		}
		// Strip data
		String str = headerData[0];
		//System.out.println(str);
		headerData = str.split("\r\n");
		// Strip label
		for (String s : headerData) {
			String[] sData = s.split(" ", 2);
			switch (sData[0]) {
			case "HTTP/1.1":
				httpHeader.put("http", sData[1]);
				break;
			case "Date:":
				httpHeader.put("date", sData[1]);
				break;
			case "Server:":
				httpHeader.put("server", sData[1]);
				break;
			case "Last-Modified:":
				httpHeader.put("lastMod", sData[1]);
				break;
			case "ETAG":
				httpHeader.put("ETAG", sData[1]);
				break;
			case "Accept-Ranges:":
				httpHeader.put("acceptRange", sData[1]);
				break;
			case "Content-Length:":
				httpHeader.put("length", sData[1]);
				break;
			case "Connection":
				httpHeader.put("connection", sData[1]);
				break;
			case "Content-Type":
				httpHeader.put("content", sData[1]);
				break;
			}
		}
		//		System.out.println(httpHeader.toString());
		return httpHeader;
	}
	
	/**
	 * Converts a string representing the date to a Date object
	 *
	 * @param dateStr
	 *            String containing the date
	 * 
	 * @return date
	 * 			  Date object from the dateStr
	 * 
	 */

	public Date strToDate(String dateStr) { // Convert from String to Date
		DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = null;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
//		System.out.println(format.format(date));
		return date;
	}

	/**
	 * Converts a long representing the date to a String object
	 *
	 * @param time
	 * 			  Time in ms reporesenting the date
	 *            
	 * 
	 * @return dateStr
	 * 			  String containing the date
	 * 
	 */
	public String msToStr(long time) { // Convert from long to tring
		DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date(time);

		String dateStr = (format.format(date)).toString();
		return dateStr;
	}
}
