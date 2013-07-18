/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: Jerric
 * @created: Mar 21, 2006
 * @modified by: Jerric
 * @modified at: Mar 21, 2006
 * 
 */
public class UrlItem {
    
    private String urlPattern;
    private String urlType;
    
    /**
     * 
     */
    public UrlItem( String urlPattern, String urlType ) {
        this.urlPattern = urlPattern;
        this.urlType = urlType;
    }
    
    public String getUrlPattern() {
        return urlPattern;
    }
    
    public String getUrlType() {
        return urlType;
    }
    
    public URL getUrl() throws MalformedURLException {
        return new URL( urlPattern );
    }
    
    public HttpURLConnection getConnection( String cookies ) throws IOException {
        URL url = new URL( urlPattern );
        HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();
        connection.setRequestProperty( "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)" );
        if ( cookies != null )
            connection.setRequestProperty( "Cookie", cookies );
        return connection;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( urlPattern );
        sb.append( " (" + urlType + ") " );
        return sb.toString();
    }
}
