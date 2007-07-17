package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.user.QueryFactory;

public abstract class Param extends WdkModelBase {

    protected static Logger logger = Logger.getLogger(Param.class);

    protected String name;
    protected String id;
    protected String prompt;

    private List<WdkModelText> helps;
    protected String help;

    protected String defaultValue;
    protected String sample;
    protected String fullName;

    private boolean visible;
    private boolean readonly;

    private Group group;

    protected QueryFactory queryFactory;

    private List<ParamSuggestion> suggestions;
    protected boolean allowEmpty;
    
    protected ParamSet paramSet;

    public Param() {
        visible = true;
        readonly = false;
        group = Group.Empty();
        helps = new ArrayList<WdkModelText>();
        suggestions = new ArrayList<ParamSuggestion>();
        allowEmpty = false;
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
        return fullName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Assumes that the name of this param has already been set. Note this is
     * slightly different than a simple accessor in that the full name of the
     * param is <code>paramSetName</code> concatenated with ".paramName".
     * 
     * @param paramSetName
     *        name of the paramSet to which this param belongs.
     */
    public void setFullName(String paramSetName) {
        this.fullName = paramSetName + "." + name;
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

    public String getDefault() throws WdkModelException {
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
     *        The readonly to set.
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
     *        The visible to set.
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
    
    void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * @param group
     *        the group to set
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
                + ": id='" + id + "'" + newline + "  prompt='" + prompt + "'"
                + newline + "  help='" + help + "'" + newline + "  default='"
                + defaultValue + "'" + newline + "  readonly=" + readonly
                + newline + "  visible=" + visible + newline);
        if (group != null) buf.append("  group=" + group.getName() + newline);

        return buf.toString();
    }

    /**
     * @return Error string if an error. null if no errors.
     */
    public abstract String validateValue(Object value) throws WdkModelException;

    public String compressValue(Object value) throws WdkModelException {
        // check if the value is already been compressed
        String strValue = (value != null) ? value.toString() : "";

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
        // exclude helps
        for (WdkModelText help : helps) {
            if (help.include(projectId)) {
                this.help = help.getText();
                break;
            }
        }
        helps = null;

        // exclude suggestions
        for (ParamSuggestion suggest : suggestions) {
            if (suggest.include(projectId)) {
                suggest.excludeResources(projectId);
                defaultValue = suggest.getDefault();
                sample = suggest.getSample();
                allowEmpty = suggest.isAllowEmpty();
                break;
            }
        }
        suggestions = null;
    }

    // ////////////////////////////////////////////////////////////////////
    // protected methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Transforms external value into internal value if needed By default
     * returns provided value
     * 
     * @throws WdkUserException
     */
    protected String getInternalValue(String value) throws WdkModelException {
        return (String) decompressValue(value);
    }

    protected abstract void resolveReferences(WdkModel model)
            throws WdkModelException;

    protected void setResources(WdkModel model) throws WdkModelException {
        this.queryFactory = model.getQueryFactory();
    }

    protected void clone(Param param) {
        param.name = name;
        param.id = id;
        param.prompt = prompt;
        param.help = help;
        param.defaultValue = defaultValue;
        param.sample = sample;
        param.fullName = fullName;
        param.visible = visible;
        param.readonly = readonly;
        param.queryFactory = this.queryFactory;
        param.group = this.group;
        param.allowEmpty = this.allowEmpty;
        param.paramSet = this.paramSet;
    }

    public abstract Param clone();
}
