package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.user.QueryFactory;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Param extends WdkModelBase {

    protected static Logger logger = Logger.getLogger(Param.class);

    /**
     * @return Error string if an error. null if no errors.
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     */
    public abstract String validateValue(Object value)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException;

    public abstract Param clone();

    public abstract void resolveReferences(WdkModel model)
            throws WdkModelException;

    protected abstract void appendJSONContent(JSONObject jsParam)
            throws JSONException;

    protected String id;
    protected String name;
    protected String prompt;

    private List<WdkModelText> helps;
    protected String help;

    protected String defaultValue;
    protected String sample;

    private boolean visible;
    private boolean readonly;

    private Group group;

    protected QueryFactory queryFactory;

    private List<ParamSuggestion> suggestions;
    protected boolean allowEmpty;
    protected String emptyValue;

    protected ParamSet paramSet;

    public Param() {
        visible = true;
        readonly = false;
        group = Group.Empty();
        helps = new ArrayList<WdkModelText>();
        suggestions = new ArrayList<ParamSuggestion>();
        allowEmpty = false;
    }

    public Param(Param param) {
        this.id = param.id;
        this.name = param.name;
        this.prompt = param.prompt;
        this.help = param.help;
        this.defaultValue = param.defaultValue;
        this.sample = param.sample;
        this.visible = param.visible;
        this.readonly = param.readonly;
        this.group = param.group;
        this.queryFactory = param.queryFactory;
        this.allowEmpty = param.allowEmpty;
        this.emptyValue = param.emptyValue;
        this.paramSet = param.paramSet;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setParamSet(ParamSet paramSet) {
        this.paramSet = paramSet;
    }

    public String getFullName() {
        return paramSet.getName() + "." + name;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        if (prompt == null) return name;
        return prompt;
    }

    public void addHelp(WdkModelText help) {
        this.helps.add(help);
    }

    public String getHelp() {
        if (help == null) return getPrompt();
        return help;
    }

    public void setDefault(String defaultValue) {
        if (defaultValue == null) return; // use the current one
        this.defaultValue = defaultValue;
    }

    public String getDefault() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        return defaultValue;
    }

    /**
     * @return the sample
     */
    public String getSample() {
        return this.sample;
    }

    /**
     * @return Returns the readonly.
     */
    public boolean isReadonly() {
        return this.readonly;
    }

    /**
     * @param readonly
     *            The readonly to set.
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * @return Returns the visible.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * @param visible
     *            The visible to set.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the allowEmpty
     */
    public boolean isAllowEmpty() {
        return this.allowEmpty;
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    /**
     * @return the emptyValue
     */
    public String getEmptyValue() {
        return (emptyValue == null) ? defaultValue : emptyValue;
    }

    /**
     * @param emptyValue
     *            the emptyValue to set
     */
    public void setEmptyValue(String emptyValue) {
        this.emptyValue = emptyValue;
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    public void addSuggest(ParamSuggestion suggest) {
        this.suggestions.add(suggest);
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        String classnm = this.getClass().getName();
        StringBuffer buf = new StringBuffer(classnm + ": name='" + name + "'"
                + newline + "  prompt='" + prompt + "'" + newline + "  help='"
                + help + "'" + newline + "  default='" + defaultValue + "'"
                + newline + "  readonly=" + readonly + newline + "  visible="
                + visible + newline);
        if (group != null) buf.append("  group=" + group.getName() + newline);

        return buf.toString();
    }

    public String compressValue(Object value) throws WdkModelException,
            NoSuchAlgorithmException {
        // check if the value is already been compressed
        String strValue;
        if (value == null) strValue = "";
        else if (value instanceof String) strValue = (String)value;
        else if (value instanceof String[]) strValue = Arrays.toString((String[])value);
        else strValue = value.toString();

        if (strValue.startsWith(Utilities.COMPRESSED_VALUE_PREFIX))
            return strValue;

        // check if the value needs to be compressed
        if (strValue.length() >= Utilities.MAX_PARAM_VALUE_SIZE) {
            String checksum = queryFactory.makeClobChecksum(strValue);
            strValue = Utilities.COMPRESSED_VALUE_PREFIX + checksum;
        }
        return strValue;
    }

    public Object decompressValue(String value) throws WdkModelException {
        // check if the value is compressed; that is, if it has a compression
        // prefix
        if (!value.startsWith(Utilities.COMPRESSED_VALUE_PREFIX)) return value;

        // decompress the value
        String checksum = value.substring(
                Utilities.COMPRESSED_VALUE_PREFIX.length()).trim();
        return queryFactory.getClobValue(checksum);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        boolean hasHelp = false;
        for (WdkModelText help : helps) {
            if (help.include(projectId)) {
                if (hasHelp) {
                    throw new WdkModelException("The param " + getFullName()
                            + " has more than one help for project "
                            + projectId);
                } else {
                    this.help = help.getText();
                    hasHelp = true;
                }
            }
        }
        helps = null;

        // exclude suggestions
        boolean hasSuggest = false;
        for (ParamSuggestion suggest : suggestions) {
            if (suggest.include(projectId)) {
                if (hasSuggest) {
                    throw new WdkModelException("The param " + getFullName()
                            + " has more than one <suggest> for project "
                            + projectId);
                } else {
                    suggest.excludeResources(projectId);
                    defaultValue = suggest.getDefault();
                    sample = suggest.getSample();
                    allowEmpty = suggest.isAllowEmpty();
                    hasSuggest = true;
                }
            }
        }
        suggestions = null;
    }

    /**
     * Transforms external value into internal value if needed By default
     * returns provided value
     * 
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * 
     * @throws WdkUserException
     */
    public String getInternalValue(Object value) throws WdkModelException,
            SQLException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        String term = (String) value;
        value = decompressValue(term);
        if (value != null && value instanceof String[]) {
            StringBuffer buffer = new StringBuffer();
            String[] array = (String[]) value;
            for (String item : array) {
                if (buffer.length() > 0) buffer.append(',');
                buffer.append(item);
            }
            return buffer.toString();
        } else if (value != null) return value.toString();
        else {
            throw new WdkModelException("The internal value of param '"
                    + getFullName() + "' with given term '" + term
                    + "' cannot be found.");
        }

    }

    public JSONObject getJSONContent() throws JSONException {
        JSONObject jsParam = new JSONObject();
        jsParam.put("name", getFullName());
        appendJSONContent(jsParam);
        return jsParam;
    }

    public void setResources(WdkModel model) throws WdkModelException {
        this.queryFactory = model.getQueryFactory();
    }

    public String replaceSql(String sql, String internalValue)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        String regex = "\\$\\$" + name + "\\$\\$";
        // escape all single quotes in the value
        return sql.replaceAll(regex, Matcher.quoteReplacement(internalValue));
    }
}
