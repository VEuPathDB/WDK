/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
    private Map<String, String> params;

    /**
     * 
     */
    public UrlItem(String urlPattern, String urlType) {
        this.urlPattern = urlPattern;
        this.urlType = urlType;
        params = new LinkedHashMap<String, String>();
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public String getUrlType() {
        return urlType;
    }

    public URL getUrl() throws MalformedURLException {
        return new URL(urlPattern);
    }

    public Map<String, String> getParameters() {
        return new LinkedHashMap<String, String>(params);
    }

    public void addParameter(String key, String value) {
        params.put(key, value);
    }

    public HttpURLConnection getConnection() throws IOException {
        URL url = new URL(urlPattern);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)");
        if (!params.isEmpty()) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // add post parameters
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream());
            Set<String> keys = params.keySet();
            boolean firstParam = true;
            for (String key : keys) {
                String value = params.get(key);
                key = URLEncoder.encode(key, "UTF-8");
                value = URLEncoder.encode(value, "UTF-8");
                if (firstParam) firstParam = false;
                else out.write("&");
                out.write(key + "=" + value);
            }
            out.flush();
            // out.close();
        }
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
        sb.append(urlPattern);
        sb.append(" (" + urlType + ") ");
        for (String key : params.keySet()) {
            sb.append("&" + key + "=" + params.get(key));
        }
        return sb.toString();
    }
}
