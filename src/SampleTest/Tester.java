package SampleTest;

import java.io.IOException;
import java.net.UnknownHostException;


/**
 * A simple test driver
 * 
 * @author 	Majid Ghaderi
 * @version	3.0, Oct 2, 2015
 *
 */
public class Tester {
	
	public static void main(String[] args) throws UnknownHostException, IOException {

		// include whatever URL you like
		// these are just some samples
		String[] url = {"people.ucalgary.ca/~mghaderi/test/test.html",
						"people.ucalgary.ca/~mghaderi/test/uc.gif"};
		
		// this is a very basic tester
		// the TAs will use more a comprehensive set of tests
		try {
			UrlCache cache = new UrlCache();
			
			for (int i = 0; i < url.length; i++)
				cache.getObject(url[i]);
			
			System.out.println("Last-Modified for " + url[0] + " is: " + cache.getLastModified(url[0]));
			cache.getObject(url[0]);
			System.out.println("Last-Modified for " + url[0] + " is: " + cache.getLastModified(url[0]));
		}
		catch (UrlCacheException e) {
			System.out.println("There was a problem: " + e.getMessage());
		}
		System.out.println("Successful end");
	}
	
}