package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class FlatVocabParam extends AbstractEnumParam {

    public static final String PARAM_SERVED_QUERY = "ServedQuery";
    public static final String DEPENDED_VALUE = "depended_value";

    public static final String COLUMN_TERM = "term";
    public static final String COLUMN_INTERNAL = "internal";
    public static final String COLUMN_DISPLAY = "display";
    public static final String COLUMN_PARENT_TERM = "parentTerm";

    private Query query;
    private String queryTwoPartName;
    private String servedQueryName = "unknown";

    public FlatVocabParam() {
    }

    public FlatVocabParam(FlatVocabParam param) {
        super(param);
        this.query = param.query;
        this.queryTwoPartName = param.queryTwoPartName;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void setQueryRef(String queryTwoPartName) {

        this.queryTwoPartName = queryTwoPartName;
    }

    public Query getQuery() {
        return query;
    }

    /**
     * @param servedQueryName
     *            the servedQueryName to set
     */
    public void setServedQueryName(String servedQueryName) {
        this.servedQueryName = servedQueryName;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        this.wdkModel = model;

        // the vocab query is always cloned to keep a reference to the param
        Query query = (Query) model.resolveReference(queryTwoPartName);
        query.resolveReferences(model);
        query = query.clone();

        // add a served query param into flatVocabQuery, if it doesn't exist
        ParamSet paramSet = model.getParamSet(Utilities.INTERNAL_PARAM_SET);
        StringParam param = new StringParam();
        param.setName(PARAM_SERVED_QUERY);
        param.setDefault(servedQueryName);
        param.setAllowEmpty(true);
        param.resolveReferences(model);
        param.setResources(model);
        paramSet.addParam(param);
        query.addParam(param);
        this.query = query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#setResources(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void setResources(WdkModel model) throws WdkModelException {
        super.setResources(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.param.AbstractEnumParam#initVocabMap()
     */
    protected synchronized void initVocabMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Param dependedParam = getDependedParam();
        if (termInternalMap != null
                && (dependedParam == null || !isDependedValueChanged()))
            return;

        termInternalMap = new LinkedHashMap<String, String>();
        termDisplayMap = new LinkedHashMap<String, String>();
        termParentMap = new LinkedHashMap<String, String>();

        if (dependedParam != null && dependedValue == null)
            return;

        // check if the query has "display" column
        boolean hasDisplay = query.getColumnMap().containsKey(COLUMN_DISPLAY);
        boolean hasParent = query.getColumnMap()
                .containsKey(COLUMN_PARENT_TERM);

        // prepare param values
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put(PARAM_SERVED_QUERY, servedQueryName);
        if (dependedParam != null)
            values.put(dependedParam.getName(), dependedValue);

        User user = wdkModel.getSystemUser();

        Map<String, String> context = new LinkedHashMap<String, String>();
        context.put(Utilities.QUERY_CTX_PARAM, getFullName());
        if (contextQuestion != null)
            context.put(Utilities.QUERY_CTX_QUESTION,
                    contextQuestion.getFullName());
        QueryInstance instance = query.makeInstance(user, values, true, 0,
                context);

        ResultList result = instance.getResults();
        while (result.next()) {
            Object objTerm = result.get(COLUMN_TERM);
            Object objInternal = result.get(COLUMN_INTERNAL);
            if (objTerm == null)
                throw new WdkModelException("The term of flatVocabParam ["
                        + getFullName() + "] is null. query ["
                        + query.getFullName() + "].\n" + instance.getSql());
            if (objInternal == null)
                throw new WdkModelException("The internal of flatVocabParam ["
                        + getFullName() + "] is null. query ["
                        + query.getFullName() + "].\n" + instance.getSql());

            String term = objTerm.toString().trim();
            String value = objInternal.toString().trim();
            String display = hasDisplay ? result.get(COLUMN_DISPLAY).toString()
                    .trim() : term;
            String parentTerm = null;
            if (hasParent) {
                Object parent = result.get(COLUMN_PARENT_TERM);
                if (parent != null)
                    parentTerm = parent.toString().trim();
            }

            // escape the term & parentTerm
            // term = term.replaceAll("[,]", "_");
            // if (parentTerm != null)
            // parentTerm = parentTerm.replaceAll("[,]", "_");
            if (term.indexOf(',') >= 0 && dependedParam != null)
                throw new WdkModelException(this.getFullName()
                        + ": The term cannot contain comma: '" + term + "'");
            if (parentTerm != null && parentTerm.indexOf(',') >= 0)
                throw new WdkModelException(this.getFullName()
                        + ": The parent term cannot contain " + "comma: '"
                        + parentTerm + "'");

            termParentMap.put(term, parentTerm);

            termInternalMap.put(term, value);
            termDisplayMap.put(term, display);
        }
        if (termInternalMap.isEmpty()) {
            if (query instanceof SqlQuery)
                logger.debug("vocab query returns 0 rows:"
                        + ((SqlQuery) query).getSql());
            throw new WdkModelException("No item returned by the query ["
                    + query.getFullName() + "] of FlatVocabParam ["
                    + getFullName() + "].");
        }
        initTreeMap();
        applySelectMode();

        setDependedValueChanged(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new FlatVocabParam(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam, boolean extra)
            throws JSONException {
        if (extra) {
            // add underlying query name to it
            jsParam.append("query", query.getFullName());
        }
    }
}
