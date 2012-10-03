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
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public class SqlQuery extends Query {

    private List<WdkModelText> sqlList;
    private String sql;
    private List<WdkModelText> sqlMacroList;
    private Map<String, String> sqlMacroMap;
    private boolean clobRow;

    private List<WdkModelText> dependentTableList;
    private Map<String, String> dependentTableMap;

    public SqlQuery() {
        super();
        clobRow = false;
        sqlList = new ArrayList<WdkModelText>();
        sqlMacroList = new ArrayList<WdkModelText>();
        sqlMacroMap = new LinkedHashMap<String, String>();
        dependentTableList = new ArrayList<WdkModelText>();
        dependentTableMap = new LinkedHashMap<String, String>();
    }

    public SqlQuery(SqlQuery query) {
        super(query);
        this.clobRow = query.clobRow;
        this.sql = query.sql;
        if (query.sqlList != null) 
          this.sqlList = new ArrayList<>(query.sqlList);
        if (query.sqlMacroMap != null)
          this.sqlMacroMap = new LinkedHashMap<>(query.sqlMacroMap);
        if (query.sqlMacroList != null)
          this.sqlMacroList = new ArrayList<>(query.sqlMacroList);
        if (query.dependentTableMap != null)
          this.dependentTableMap = new LinkedHashMap<>(query.dependentTableMap);
        if (query.dependentTableList != null)
          this.dependentTableList = new ArrayList<>(query.dependentTableList);
    }

    public void addSql(WdkModelText sql) {
        this.sqlList.add(sql);
    }

    public void addSqlParamValue(WdkModelText sqlMacro) {
        this.sqlMacroList.add(sqlMacro);
    }

    public void addSqlParamValue(String macro, String value) {
        this.sqlMacroMap.put(macro, value);
    }

    public String getSql() {
        return replaceMacros(sql);
    }

    /**
     * this method is called by other WDK objects. It is not called by the model
     * xml parser.
     * 
     * @param sql
     */
    public void setSql(String sql) {
        // append new line to the end, in case the last line is a comment;
        // otherwise, all modified sql will fail.
        this.sql = sql + "\n";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#makeInstance()
     */
    @Override
    public QueryInstance makeInstance(User user, Map<String, String> values,
            boolean validate, int assignedWeight, Map<String, String> context)
            throws WdkModelException {
        return new SqlQueryInstance(user, this, values, validate,
                assignedWeight, context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.Query#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsQuery, boolean extra)
            throws JSONException {
        if (extra) {
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
            String sql = getSql().replaceAll("\\s+", " ");
            jsQuery.put("sql", sql);
        }
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
                this.setSql(sql.getText());
                break;
            }
        }
        sqlList = null;

        // exclude sql
        for (WdkModelText dependentTable : dependentTableList) {
            if (dependentTable.include(projectId)) {
                dependentTable.excludeResources(projectId);
                String table = dependentTable.getText();
                this.dependentTableMap.put(table, table);
            }
        }
        dependentTableList = null;

        // exclude macros
        for (WdkModelText macro : sqlMacroList) {
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
     * @see
     * org.gusdb.wdk.model.query.Query#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveQueryReferences(WdkModel wdkModel) throws WdkModelException {
        // apply the sql macros into sql
        if (this.sql == null)
            throw new WdkModelException("null sql in "
                    + getQuerySet().getName() + "." + getName());

        // don't replace the sql here. the macros have to be replaced on the fly
        // in order to inject overridden macros from question.
        String sql = replaceMacros(this.sql);
        
        // verify the all param macros have been replaced
        Matcher matcher = Pattern.compile("&&([^&]+)&&").matcher(sql);
        if (matcher.find())
            throw new WdkModelException("SqlParamValue macro "
                    + matcher.group(1) + " found in <sql> of query "
                    + getFullName() + ", but it's not defined.");
    }
    
    private String replaceMacros(String sql) {
        for (String paramName : sqlMacroMap.keySet()) {
            String pattern = "&&" + paramName + "&&";
            String value = sqlMacroMap.get(paramName);
            // escape the & $ \ chars in the value
            sql = sql.replaceAll(pattern, Matcher.quoteReplacement(value));
        }
        return sql;
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

    /**
     * @return the clobRow
     */
    public boolean isClobRow() {
        return clobRow;
    }

    /**
     * @param clobRow
     *            the clobRow to set
     */
    public void setClobRow(boolean clobRow) {
        this.clobRow = clobRow;
    }

    public void addDependentTable(WdkModelText dependentTable) {
        this.dependentTableList.add(dependentTable);
    }

    public String[] getDependentTables() {
        String[] array = new String[dependentTableMap.size()];
        dependentTableMap.keySet().toArray(array);
        return array;
    }
}
