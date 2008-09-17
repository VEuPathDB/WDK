package org.gusdb.wdk.model;

import org.gusdb.wdk.model.query.Query;
import org.json.JSONException;
import org.json.JSONObject;

public class Column extends WdkModelBase {

    /**
     * 
     */
    private static final long serialVersionUID = -1895749085919850028L;

    // private static Logger logger = Logger.getLogger( Column.class );

    private String name;
    private Query query;
    private ColumnType type = ColumnType.STRING;
    private int width = 0; // for wsColumns (width of datatype)

    /**
     * The name is used by WSF service.
     */
    private String wsName;

    private boolean lowerCase = false;

    public Column() {}

    public Column(Column column) {
        this.name = column.name;
        this.query = column.query;
        this.type = column.type;
        this.width = column.width;
        this.wsName = column.wsName;
        this.lowerCase = column.lowerCase;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setColumnType(String typeName) throws WdkModelException {
        this.type = ColumnType.parse(typeName);
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public ColumnType getType() {
        return type;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Query getQuery() {
        return query;
    }

    public int getWidth() {
        return (width == 0) ? type.getDefaultWidth() : width;
    }

    /**
     * @return Returns the wsName.
     */
    public String getWsName() {
        return this.wsName;
    }

    /**
     * @param wsName
     *            The wsName to set.
     */
    public void setWsName(String wsName) {
        this.wsName = wsName;
    }

    /**
     * @return the lowerCase
     */
    public boolean isLowerCase() {
        return lowerCase;
    }

    /**
     * @param lowerCase
     *            the lowerCase to set
     */
    public void setLowerCase(boolean lowerCase) {
        this.lowerCase = lowerCase;
    }

    public JSONObject getJSONContent() throws JSONException {
        JSONObject jsColumn = new JSONObject();
        jsColumn.put("name", name);
        jsColumn.put("type", type);
        jsColumn.put("width", width);
        return jsColumn;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        String classnm = this.getClass().getSimpleName();
        StringBuffer buf = new StringBuffer(classnm + ": name='" + name + "', "
                + "  dataTypeName='" + type + "'" + newline);

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // nothing to resolve
    }
}
