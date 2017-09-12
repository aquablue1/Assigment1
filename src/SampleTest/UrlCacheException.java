package SampleTest;


/**
 * UrlCacheException Class
 * 
 * @author 	Majid Ghaderi
 * @version	1.0, Sep 22, 2015
 */
public class UrlCacheException extends Exception {

    /**
     * Constructor calls Exception super class with message
     */
    public UrlCacheException() {
        super("UrlCacheException");
    }

    /**
     * Constructor calls Exception super class with message
     * @param message The message of exception
     */
    public UrlCacheException(String message) {
        super(message);
    }


}
