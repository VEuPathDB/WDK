package org.gusdb.wdk.model.query.param;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class StrategyListRemoteHandler implements RemoteHandler {

    private static final String PROP_USER_RESOURCE_URI = "userResourceUri";
    private static final String PROP_STRATEGY_TYPE = "strategyType";

    private WdkModel wdkModel;
    private String userUri;
    private String type;

    public void setModel(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    public void setProperties(Map<String, String> properties)
            throws WdkModelException {
        if (!properties.containsKey(PROP_USER_RESOURCE_URI))
            throw new WdkModelException("The required property '"
                    + PROP_USER_RESOURCE_URI + "' is not set for "
                    + "StrategyListRemoteHandler");

        if (!properties.containsKey(PROP_STRATEGY_TYPE))
            throw new WdkModelException("The required property '"
                    + PROP_STRATEGY_TYPE + "' is not set for "
                    + "StrategyListRemoteHandler");

        // get properties
        this.userUri = properties.get(PROP_USER_RESOURCE_URI);
        this.type = properties.get(PROP_STRATEGY_TYPE);

        if (!userUri.endsWith("/")) userUri += "/";
    }

    public String getResource(User user, Map<String, String> params)
            throws JSONException {
        // get remote user information
        String uri = userUri + user.getSignature();
        Client client = Client.create();
        WebResource userResource = client.resource(uri);
        String response = userResource.accept(MediaType.APPLICATION_JSON_TYPE).get(
                String.class);
        JSONObject jsUser = new JSONObject(response);

        // get remote strategy list
        String strategiesUri = jsUser.getString("strategies-uri");
        WebResource strategiesResource = client.resource(strategiesUri);
        response = strategiesResource.queryParam("type", type).accept(
                MediaType.APPLICATION_JSON_TYPE).get(String.class);
        JSONArray jsStrategies = new JSONArray(response);

        JSONArray jsTerms = new JSONArray();
        for (int i = 0; i < jsStrategies.length(); i++) {
            JSONObject jsStrategy = jsStrategies.getJSONObject(i);

            // skip invalid strategies
            if (!jsStrategy.getBoolean("valid")) continue;
            String term = jsStrategy.getString("uri");
            String name = jsStrategy.getString("name");
            String size = jsStrategy.getString("size");
            String display = name + " (" + size + " results)";

            // put a timestamp in the term so that the cache will be used as
            // long as the term is not retrieved again
            UriBuilder builder = UriBuilder.fromUri(strategiesUri);
            long timestamp = System.currentTimeMillis();
            URI termUri = builder.queryParam("timestamp", timestamp).build();
            term = termUri.toASCIIString();

            // transform the strategy info into a list term-value map
            JSONObject jsTerm = new JSONObject();
            jsTerm.put("term", term);
            jsTerm.put("display", display);
            jsTerms.put(jsTerm);
        }
        return jsTerms.toString();
    }

}
