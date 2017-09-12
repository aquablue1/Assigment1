package SampleTest;

import java.util.Date;
import java.io.Serializable;
 
public class CatalogObject implements java.io.Serializable {
    private String url;
    private Date lastModifiedDate;
 
    /**
     * @param url
     *          Address of the file.
     * @param date
     *          The date that the file was last updated.
     */
    public CatalogObject(String url, Date date) {
        this.url = url;
        this.lastModifiedDate = date;
    }
 
 
    /**
     * Returns URL.
     */
    public String getHostName() {
    	return this.url;
    }
    
    public Date getLastModified() {
    	return this.lastModifiedDate;
    }
    
    public String getUrl() {
        return url;
    }
 
 
    /** 
     * Returns last modified time.      
     */
    public Date getDate() {
        return lastModifiedDate;
    }
}
 