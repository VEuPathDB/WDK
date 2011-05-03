package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.QueryFactory;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         There are four possible inputs to a param:
 * 
 *         raw data: the data retrieved by processQuestion action, which can be
 *         very long, and needs to be compressed.
 * 
 *         user-dependent data: the data used in all public url, which are
 *         short, and can be compressed. User-dependent data is stored in the
 *         steps table. User-dependent data is also used in creating a query.
 * 
 *         user-independent data: It can be converted back into user-dependent
 *         data, which sometimes means to create new entities for the user.
 *         User-independent data is stored into the answers table.
 * 
 *         Internal data: the data used in the SQL.
 */
public abstract class Param extends WdkModelBase {

    protected static Logger logger = Logger.getLogger(Param.class);

    public abstract Param clone();

    /**
     * Convert raw data to dependent data. this method needs to handle the case
     * when the input is already dependent data.
     * 
     * @param user
     * @param rawValue
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     */
    public abstract String rawOrDependentValueToDependentValue(User user,
            String rawValue) throws NoSuchAlgorithmException,
            WdkModelException, WdkUserException, SQLException, JSONException;

    public abstract String dependentValueToRawValue(User user,
            String dependentValue) throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException;

    public abstract String dependentValueToIndependentValue(User user,
            String dependentValue) throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException;

    public abstract String dependentValueToInternalValue(User user,
            String independentValue) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException;

    protected abstract void applySuggection(ParamSuggestion suggest);

    /**
     * The input the method can be either raw data or dependent data
     * 
     * @param user
     * @param rawOrDependentValue
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     */
    protected abstract void validateValue(User user, String rawOrDependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException;

    protected abstract void appendJSONContent(JSONObject jsParam, boolean extra)
            throws JSONException;

    protected String id;
    protected String name;
    protected String prompt;

    private List<WdkModelText> helps;
    protected String help;

    protected String defaultValue;
    protected String sample;

    protected boolean visible;
    protected boolean readonly;

    private Group group;

    private List<ParamSuggestion> suggestions;
    protected boolean allowEmpty;
    protected String emptyValue;

    protected ParamSet paramSet;

    protected QueryFactory queryFactory;
    protected WdkModel wdkModel;

    private List<ParamConfiguration> noTranslations;
    /**
     * if this flag is set to true, the internal value will be the same as
     * dependent value. This flag is useful when the dependent value is sent to
     * other sites to process using ProcessQuery.
     */
    private boolean noTranslation = false;

    protected Question contextQuestion;

    public Param() {
        visible = true;
        readonly = false;
        group = Group.Empty();
        helps = new ArrayList<WdkModelText>();
        suggestions = new ArrayList<ParamSuggestion>();
        noTranslations = new ArrayList<ParamConfiguration>();
        allowEmpty = false;
        emptyValue = "";
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
        this.wdkModel = param.wdkModel;
        this.noTranslation = param.noTranslation;
        this.resolved = param.resolved;
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
        if (prompt == null)
            return name;
        return prompt;
    }

    public void addHelp(WdkModelText help) {
        this.helps.add(help);
    }

    public String getHelp() {
        if (help == null)
            return getPrompt();
        return help;
    }

    public void setDefault(String defaultValue) {
        if (defaultValue == null)
            return; // use the current one
        this.defaultValue = defaultValue;
    }

    public String getDefault() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (defaultValue != null && defaultValue.length() == 0)
            defaultValue = null;
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
        return emptyValue;
    }

    /**
     * @param emptyValue
     *            the emptyValue to set
     */
    public void setEmptyValue(String emptyValue) {
        if (emptyValue != null && emptyValue.length() == 0)
            emptyValue = "";
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
                + visible + newline + "  noTranslation=" + noTranslation
                + newline);
        if (group != null)
            buf.append("  group=" + group.getName() + newline);

        return buf.toString();
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
                if (hasSuggest)
                    throw new WdkModelException("The param " + getFullName()
                            + " has more than one <suggest> for project "
                            + projectId);

                suggest.excludeResources(projectId);
                defaultValue = suggest.getDefault();
                sample = suggest.getSample();
                allowEmpty = suggest.isAllowEmpty();
                emptyValue = suggest.getEmptyValue();

                applySuggection(suggest);

                hasSuggest = true;

            }
        }
        suggestions = null;

        // exclude noTranslations
        boolean hasNoTranslation = false;
        for (ParamConfiguration noTrans : noTranslations) {
            if (noTrans.include(projectId)) {
                if (hasNoTranslation)
                    throw new WdkModelException("The param " + getFullName()
                            + " has more than one <noTranslation> for project "
                            + projectId);
                noTranslation = noTrans.isValue();
                hasNoTranslation = true;
            }
        }
        noTranslations = null;
    }

    public String compressValue(String value) throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException {
        // check if the value is already been compressed
        if (value == null || value.length() == 0)
            return null;

        if (value.startsWith(Utilities.PARAM_COMPRESSE_PREFIX))
            return value;

        // check if the value needs to be compressed
        if (value.length() >= Utilities.MAX_PARAM_VALUE_SIZE) {
            String checksum = queryFactory.makeClobChecksum(value);
            value = Utilities.PARAM_COMPRESSE_PREFIX + checksum;
        }
        return value;
    }

    public String decompressValue(String value) throws WdkModelException,
            WdkUserException {
        if (value == null || value.length() == 0)
            return null;

        // check if the value is compressed; that is, if it has a compression
        // prefix
        if (!value.startsWith(Utilities.PARAM_COMPRESSE_PREFIX))
            return value;

        // decompress the value
        String checksum = value.substring(
                Utilities.PARAM_COMPRESSE_PREFIX.length()).trim();
        String decompressed = queryFactory.getClobValue(checksum);
        if (decompressed != null)
            value = decompressed;
        return value;
    }

    public JSONObject getJSONContent(boolean extra) throws JSONException {
        JSONObject jsParam = new JSONObject();
        jsParam.put("name", getFullName());
        appendJSONContent(jsParam, extra);
        return jsParam;
    }

    public void setResources(WdkModel model) throws WdkModelException {
        this.wdkModel = model;
        this.queryFactory = model.getQueryFactory();
    }

    public String replaceSql(String sql, String internalValue)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        String regex = "\\$\\$" + name + "\\$\\$";
        // escape all single quotes in the value
        return sql.replaceAll(regex, Matcher.quoteReplacement(internalValue));
    }

    public void validate(User user, String dependentValue)
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        // handle the empty case
        if (dependentValue == null || dependentValue.length() == 0) {
            if (!allowEmpty)
                throw new WdkModelException("The parameter '" + getPrompt()
                        + "' does not allow empty value");
            // otherwise, got empty value and is allowd, no need for further
            // validation.
        } else {
            // value is not empty, the sub classes will complete further
            // validation
            validateValue(user, dependentValue);
        }
    }

    public void addNoTranslation(ParamConfiguration noTranslation) {
        this.noTranslations.add(noTranslation);
    }

    public boolean isNoTranslation() {
        return noTranslation;
    }

    public void setNoTranslation(boolean noTranslation) {
        this.noTranslation = noTranslation;
    }

    /**
     * Set the question where the param is used. The params in a question are
     * always cloned when question is initialized, therefore, each param object
     * will refer to one question uniquely.
     * 
     * @param question
     */
    public void setContextQuestion(Question question) {
        this.contextQuestion = question;
    }
}
