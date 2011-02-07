package org.gusdb.wdk.model.query.param;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class TDRTargetListRemoteHandler implements RemoteHandler {

    private static final String PROP_QUERY_LIST_URI = "listUri";

    private WdkModel wdkModel;
    private String listUri;

    public void setModel(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    public void setProperties(Map<String, String> properties)
            throws WdkModelException {
        if (!properties.containsKey(PROP_QUERY_LIST_URI))
            throw new WdkModelException("The required property '"
                    + PROP_QUERY_LIST_URI + "' is not set for "
                    + "TDRTargetListRemoteHandler");

        // get properties
        this.listUri = properties.get(PROP_QUERY_LIST_URI);
    }

    public String getResource(User user, Map<String, String> params)
            throws JSONException {
        // get remote user information
        Client client = Client.create();
        WebResource userResource = client.resource(listUri);
        String response = userResource.accept(MediaType.TEXT_HTML_TYPE).get(
                String.class);

        // parse the query list

        // go to the start marker
        int pos = response.indexOf("Queries in this set");

        // <a\\s+[^>]*href=\"([^\"]+)\"[^>]>(.+)</a>(.*)<a\\s+
        Pattern pattern = Pattern.compile("<a\\s+[^>]*href=\"([^\"]+)\"[^>]*>(.+)</a>(.*)<a\\s+");
        JSONArray jsTerms = new JSONArray();
        while (true) {
            // locate the checkbox
            pos = response.indexOf("<input", pos);
            if (pos < 0) break;

            // get the content after the <input> but before </p>
            int start = response.indexOf(">", pos) + 1;
            int end = response.indexOf("</p>", start);
            String content = response.substring(start, end).trim();
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String queryUri = matcher.group(1).trim();
                String name = matcher.group(2).trim();
                String count = matcher.group(3).trim();

                // transform the query info into a list term-value map
                JSONObject jsTerm = new JSONObject();
                jsTerm.put("term", queryUri);
                jsTerm.put("display", name + " " + count);
                jsTerms.put(jsTerm);
            }

            pos = end + 1;
        }
        return jsTerms.toString();
    }

}
