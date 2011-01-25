package org.gusdb.wdk.controller.service;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.TableValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.AttributeFieldBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/user/{user-signature}/strategy")
public class StrategyResource {

    public static final String PARAM_STRATEGY_TYPE = "type";
    public static final String PARAM_STRATEGY_SIGNATURE = "strategy";
    public static final String PARAM_ATTRIBUTES = "attributes";
    public static final String PARAM_TABLES = "tables";

    private static final int PAGE_SIZE = 500;

    private static final Logger logger = Logger.getLogger(StrategyResource.class);

    @Context
    private ServletContext servletContext;
    @Context
    private UriInfo ui;

    @GET
    @Produces("application/json")
    public String getStrategies(
            @PathParam("user-signature") String userSignature,
            @QueryParam(PARAM_STRATEGY_TYPE) String type) throws Exception {
        logger.debug("list strategies by type: " + type);

        WdkModelBean wdkModel = ActionUtility.getWdkModel(servletContext);
        UserBean user = wdkModel.getUserFactory().getUser(userSignature);
        Map<String, List<StrategyBean>> strategies = user.getStrategiesByCategory();

        List<StrategyBean> list = strategies.get(type);
        JSONArray jsStrategies = new JSONArray();
        if (list != null) {
            for (StrategyBean strategy : list) {
                JSONObject jsStrategy = new JSONObject();
                jsStrategy.put("name", strategy.getName());
                jsStrategy.put("valid", strategy.isValid());
                jsStrategy.put("size", strategy.getEstimateSize());

                // build the uri to the strategy result
                UriBuilder ub = ui.getAbsolutePathBuilder();
                URI resultUri = ub.path(strategy.getSignature()).build();
                jsStrategy.put("uri", resultUri.toASCIIString());

                jsStrategies.put(jsStrategy);
            }
        }
        return jsStrategies.toString();
    }

    @GET
    @Path("{" + PARAM_STRATEGY_SIGNATURE + "}")
    @Produces("application/json")
    public String getResult(
            @PathParam(PARAM_STRATEGY_SIGNATURE) String strategySignature,
            @QueryParam(PARAM_ATTRIBUTES) String attributeNames,
            @QueryParam(PARAM_TABLES) String tableNames)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        logger.debug("Get strategy results of: " + strategySignature);

        if (attributeNames != null && attributeNames.length() == 0)
            attributeNames = null;
        if (tableNames != null && tableNames.length() == 0) tableNames = null;

        WdkModelBean wdkModel = ActionUtility.getWdkModel(servletContext);

        StrategyBean strategy = wdkModel.getStrategy(strategySignature);
        AnswerValueBean answerValueBean = strategy.getLatestStep().getAnswerValue();
        AnswerValue baseAnswer = answerValueBean.getAnswerValue();
        int resultSize = baseAnswer.getResultSize();

        JSONObject jsAnswer = new JSONObject();

        jsAnswer.put("size", resultSize);

        // determine attribute names and their order
        String[] aNames;
        if (attributeNames == null) {
            if (tableNames == null) {
                // attribute list not specified, use summary attributes as
                // default.
                AttributeFieldBean[] summary = answerValueBean.getSummaryAttributes();
                aNames = new String[summary.length];
                for (int i = 0; i < summary.length; i++) {
                    aNames[i] = summary[i].getName();
                }
            } else {
                aNames = new String[0];
            }
        } else {
            aNames = attributeNames.split(",\\s*");
        }

        // determine table names and their order
        String[] tNames = (tableNames == null) ? new String[0]
                : tableNames.split(",\\s*");

        // output attribute names
        JSONArray jsAttributeNames = new JSONArray();
        for (String aName : aNames) {
            jsAttributeNames.put(aName);
        }
        jsAnswer.put("attributes", jsAttributeNames);

        // output table names
        JSONObject jsTableNames = new JSONObject();
        RecordClass recordClass = baseAnswer.getQuestion().getRecordClass();
        for (String tName : tNames) {
            TableField table = recordClass.getTableField(tName);
            Map<String, AttributeField> attributes = table.getAttributeFieldMap(FieldScope.REPORT_MAKER);

            JSONArray jsTable = new JSONArray();
            for (String aName : attributes.keySet()) {
                jsTable.put(aName);
            }
            jsTableNames.put(tName, jsTable);
        }
        jsAnswer.put("tables", jsTableNames);

        // output the result by pages
        JSONArray jsRecords = new JSONArray();
        for (int start = 1; start <= resultSize; start += PAGE_SIZE) {
            int end = Math.min(resultSize, start + PAGE_SIZE - 1);
            AnswerValue answerValue = new AnswerValue(baseAnswer, start, end);
            for (RecordInstance instance : answerValue.getRecordInstances()) {
                JSONObject jsRecord = new JSONObject();

                // output attributes
                JSONArray jsAttributes = new JSONArray();
                for (String attribute : aNames) {
                    Object objValue = instance.getAttributeValue(attribute).getValue();
                    String value = (objValue == null) ? ""
                            : objValue.toString();
                    jsAttributes.put(value);
                }
                jsRecord.put("attributes", jsAttributes);

                JSONObject jsTables = new JSONObject();
                for (String table : tNames) {
                    TableValue tableValue = instance.getTableValue(table);
                    Map<String, AttributeField> attributes = tableValue.getTableField().getAttributeFieldMap(
                            FieldScope.REPORT_MAKER);

                    JSONArray jsTable = new JSONArray();
                    for (Map<String, AttributeValue> row : tableValue) {
                        JSONArray jsRow = new JSONArray();
                        for (String aName : attributes.keySet()) {
                            Object objValue = row.get(aName).getValue();
                            String value = (objValue == null) ? ""
                                    : objValue.toString();
                            jsRow.put(value);
                        }
                        jsTable.put(jsRow);
                    }
                    jsTables.put(table, jsTable);
                }
                jsRecord.put("tables", jsTables);

                jsRecords.put(jsRecord);
            }
        }
        jsAnswer.put("records", jsRecords);

        return jsAnswer.toString();
    }
}
