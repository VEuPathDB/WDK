/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public class SqlQuery extends Query {

    private List<WdkModelText> sqlList;
    protected String sql;
    private List<SqlParamValue> sqlMacroList;
    private Map<String, String> sqlMacroMap;

    public SqlQuery() {
        super();
        sqlList = new ArrayList<WdkModelText>();
        sqlMacroList = new ArrayList<SqlParamValue>();
        sqlMacroMap = new LinkedHashMap<String, String>();
    }

    public SqlQuery(SqlQuery query) {
        super(query);
        this.sql = query.sql;
        this.sqlMacroMap = new LinkedHashMap<String, String>(query.sqlMacroMap);
    }

    public void addSql(WdkModelText sql) {
        this.sqlList.add(sql);
    }

    public void addSqlParamValue(SqlParamValue sqlMacro) {
        this.sqlMacroList.add(sqlMacro);
    }

    public String getSql() {
        return sql;
    }

    /**
     * this method is called by other WDK objects. It is not called by the model
     * xml parser.
     * 
     * @param sql
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#makeInstance()
     */
    @Override
    public QueryInstance makeInstance(Map<String, Object> values)
            throws WdkModelException {
        return new SqlQueryInstance(this, values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsQuery) throws JSONException {
        // add macro into the content
        String[] macroNames = new String[sqlMacroMap.size()];
        sqlMacroMap.keySet().toArray(macroNames);
        Arrays.sort(macroNames);
        JSONObject jsMacros = new JSONObject();
        for (String macroName : macroNames) {
            jsMacros.put(macroName, sqlMacroMap.get(macroName));
        }
        jsQuery.put("macros", jsMacros);

        // add sql
        String sql = this.sql.replaceAll("\\s+", " ");
        jsQuery.put("sql", sql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        // exclude sql
        for (WdkModelText sql : sqlList) {
            if (sql.include(projectId)) {
                sql.excludeResources(projectId);
                this.sql = sql.getText();
                break;
            }
        }
        sqlList = null;

        // exclude macros
        for (SqlParamValue macro : sqlMacroList) {
            if (macro.include(projectId)) {
                macro.excludeResources(projectId);
                String name = macro.getName();
                if (sqlMacroMap.containsKey(name))
                    throw new WdkModelException("The macro " + name
                            + " is duplicated in query " + getFullName());

                sqlMacroMap.put(macro.getName(), macro.getText());
            }
        }
        sqlMacroList = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        super.resolveReferences(wdkModel);

        // apply the sql macros into sql
        for (String paramName : sqlMacroMap.keySet()) {
            String pattern = "&&" + paramName + "&&";
            String value = sqlMacroMap.get(paramName);
            // escape the & $ \ chars in the value
            sql = sql.replaceAll(pattern, Matcher.quoteReplacement(value));
        }
        // verify the all param macros have been replaced
        Matcher matcher = Pattern.compile("&&([^&]+)&&").matcher(sql);
        if (matcher.find())
            throw new WdkModelException("SqlParamValue macro "
                    + matcher.group(1) + " found in <sql> of query "
                    + getFullName() + ", but it's not defined.");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#clone()
     */
    @Override
    public Query clone() {
        return new SqlQuery(this);
    }
}
