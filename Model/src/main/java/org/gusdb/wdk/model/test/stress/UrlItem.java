package org.gusdb.wdk.model.test.stress;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.gusdb.wdk.model.Utilities;

/**
 * @author Jerric
 */
public class UrlItem {
    
    private String urlPattern;
    private String urlType;
    
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
        return Utilities.newURL( urlPattern );
    }
    
    public HttpURLConnection getConnection( String cookies ) throws IOException {
        URL url = Utilities.newURL( urlPattern );
        HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();
        connection.setRequestProperty( "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)" );
        if ( cookies != null )
            connection.setRequestProperty( "Cookie", cookies );
        return connection;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( urlPattern );
        sb.append( " (" + urlType + ") " );
        return sb.toString();
    }
}
