package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.gusdb.fgputil.FormatUtil;

/**
 * @author xingao
 * 
 *         raw data: a comma separated list of terms;
 * 
 *         user-dependent data: same as user-independent data, can be either a a
 *         comma separated list of terms or a compressed checksum;;
 * 
 *         user-independent data: same as user-dependent data;
 * 
 *         internal data: a comma separated list of internals, quotes are
 *         properly escaped or added
 */
public abstract class AbstractEnumParam extends Param {

    protected abstract void initVocabMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException;

    public static final String DISPLAY_TYPE_AHEAD = "typeAhead";
    public static final String DISPLAY_TREE_BOX = "treeBox";

    static final String SELECT_MODE_NONE = "none";
    static final String SELECT_MODE_ALL = "all";
    static final String SELECT_MODE_FIRST = "first";

    protected boolean multiPick = false;
    protected Map<String, String> termInternalMap;
    protected Map<String, String> termDisplayMap;
    protected Map<String, String> termParentMap;
    protected List<EnumParamTermNode> termTreeList;

    protected boolean quote = true;

    private String displayType;
    protected String dependedParamRef;
    protected String dependedValue;
    protected boolean dependedValueChanged = false;

    /**
     * this property is only used by abstractEnumParams, but have to be
     * initialized from suggest.
     */
    protected String selectMode;

    /**
     * collapse single-child branches if set to true
     */
    private boolean suppressNode = false;

    public AbstractEnumParam() {}

    public AbstractEnumParam(AbstractEnumParam param) {
        super(param);
        this.multiPick = param.multiPick;
        if (param.termDisplayMap != null)
            this.termDisplayMap = new LinkedHashMap<String, String>(
                    param.termDisplayMap);
        if (param.termInternalMap != null)
            this.termInternalMap = new LinkedHashMap<String, String>(
                    param.termInternalMap);
        if (param.termParentMap != null)
            this.termParentMap = new LinkedHashMap<String, String>(
                    param.termParentMap);
        if (param.termTreeList != null) {
            this.termTreeList = new ArrayList<EnumParamTermNode>(
                    param.termTreeList);
        }
        this.quote = param.quote;
        this.displayType = param.displayType;
        this.dependedParamRef = param.dependedParamRef;
        this.dependedValue = param.dependedValue;
        this.selectMode = param.selectMode;
        this.suppressNode = param.suppressNode;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void setMultiPick(Boolean multiPick) {
        this.multiPick = multiPick.booleanValue();
    }

    public Boolean getMultiPick() {
        return new Boolean(multiPick);
    }

    public boolean isSkipValidation() {
        return (displayType != null && displayType.compareTo(DISPLAY_TYPE_AHEAD) == 0);
    }

    public void setQuote(boolean quote) {
        this.quote = quote;
    }

    public boolean getQuote() {
        return quote;
    }

    public String[] getVocab() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        String[] array = new String[termInternalMap.size()];
        termInternalMap.keySet().toArray(array);
        logger.debug("getting vocab: " + FormatUtil.arrayToString(array));
        return array;
    }

    public EnumParamTermNode[] getVocabTreeRoots()
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        initVocabMap();
        if (termTreeList != null) {
            EnumParamTermNode[] array = new EnumParamTermNode[termTreeList.size()];
            termTreeList.toArray(array);
            return array;
        } else return new EnumParamTermNode[0];
    }

    public String[] getVocabInternal() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        String[] array = new String[termInternalMap.size()];
        if (isNoTranslation()) termInternalMap.keySet().toArray(array);
        else termInternalMap.values().toArray(array);
        return array;
    }

    public String[] getDisplays() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Map<String, String> displayMap = getDisplayMap();
        String[] displays = new String[displayMap.size()];
        displayMap.values().toArray(displays);
        return displays;
    }

    public Map<String, String> getVocabMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        return new LinkedHashMap<String, String>(termInternalMap);
    }

    /**
     * @return
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     */
    public Map<String, String> getDisplayMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        return new LinkedHashMap<String, String>(termDisplayMap);
    }

    public Map<String, String> getParentMap() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initVocabMap();
        return new LinkedHashMap<String, String>(termParentMap);
    }

    @Override
    public String getDefault() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        initVocabMap();
        if (defaultValue != null && defaultValue.length() == 0)
            defaultValue = null;
        return defaultValue;
    }

    /**
     * @return the displayType
     */
    public String getDisplayType() {
        return displayType;
    }

    /**
     * @param displayType
     *            the displayType to set
     */
    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public Param getDependedParam() throws WdkModelException {
        if (dependedParamRef == null) return null;
        if (contextQuery != null) {
            String paramName = dependedParamRef.split("\\.")[1];
            return contextQuery.getParamMap().get(paramName);
        }
        return (Param) wdkModel.resolveReference(dependedParamRef);
    }

    public void setDependedParamRef(String dependedParamRef) {
        this.dependedParamRef = dependedParamRef;
    }

    public String getDependedValue() {
        return dependedValue;
    }

    public void setDependedValue(String dependedValue) {
        this.dependedValue = dependedValue;
        setDependedValueChanged(true);
    }

    protected void setDependedValueChanged(boolean dependedValueChanged) {
        this.dependedValueChanged = dependedValueChanged;
    }

    protected boolean isDependedValueChanged() {
        return dependedValueChanged;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void initTreeMap() throws WdkModelException {

        termTreeList = new ArrayList<EnumParamTermNode>();

        // construct index
        Map<String, EnumParamTermNode> indexMap = new LinkedHashMap<String, EnumParamTermNode>();
        for (String term : termParentMap.keySet()) {
            EnumParamTermNode node = new EnumParamTermNode(term);
            node.setDisplay(termDisplayMap.get(term));
            indexMap.put(term, node);

            // check if the node is root
            String parentTerm = termParentMap.get(term);
            if (parentTerm != null && !termInternalMap.containsKey(parentTerm))
                parentTerm = null;
            if (parentTerm == null) {
                termTreeList.add(node);
                termParentMap.put(term, parentTerm);
            }
        }
        // construct the relationships
        for (String term : termParentMap.keySet()) {
            String parentTerm = termParentMap.get(term);
            // skip if parent doesn't exist
            if (parentTerm == null) continue;

            EnumParamTermNode node = indexMap.get(term);
            EnumParamTermNode parent = indexMap.get(parentTerm);
            parent.addChild(node);
        }

        if (suppressNode) suppressChildren(termTreeList);
    }

    private void suppressChildren(List<EnumParamTermNode> children) {
        boolean suppressed = false;
        if (children.size() == 1) {
            // has only on child, suppress it in the tree if it has grand
            // children
            EnumParamTermNode child = children.get(0);
            EnumParamTermNode[] grandChildren = child.getChildren();
            if (grandChildren.length > 0) {
                logger.debug(child.getTerm() + " suppressed.");
                children.remove(0);
                for (EnumParamTermNode grandChild : grandChildren) {
                    children.add(grandChild);
                }
                // need to suppress children
                suppressChildren(children);
                suppressed = true;
            }
        }
        if (!suppressed) {
            for (EnumParamTermNode child : children) {
                suppressChildren(child.getChildrenList());
            }
        }
    }

    public String[] getTerms(String termList) throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        // the input is a list of terms
        if (termList == null) return new String[0];

        String[] terms;
        if (multiPick) {
            terms = termList.split("[,]+");
            for (int i = 0; i < terms.length; i++)
                terms[i] = terms[i].trim();
        } else terms = new String[] { termList.trim() };

        if (!isSkipValidation()) {
            initVocabMap();
            // disable the validation - it prevent the revising of invalid step
            // if a strategy has more than one invalis steps.

            // for (String term : terms) {
            // if (!termInternalMap.containsKey(term))
            // throw new WdkModelException(" - Invalid term '" + term
            // + "' for parameter '" + name + "'");
            // }
        }
        return terms;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue
     * (org.gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String dependentValueToIndependentValue(User user,
            String dependentValue) throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        return dependentValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToInternalValue
     * (org.gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String dependentValueToInternalValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        initVocabMap();

        String rawValue = decompressValue(dependentValue);
        if (rawValue == null || rawValue.length() == 0) rawValue = emptyValue;

        String[] terms = getTerms(rawValue);
        StringBuffer buf = new StringBuffer();
        for (String term : terms) {
            String internal = (isNoTranslation()) ? term
                    : termInternalMap.get(term);
            if (!termInternalMap.containsKey(term)) {
                // term doesn't exists need to correct it later
                throw new WdkUserException("param " + getFullName()
                        + " encountered an " + "invalid term from user #"
                        + user.getUserId() + ": " + term);
                // internal = term;
            }
            if (quote && !(internal.startsWith("'") && internal.endsWith("'")))
                internal = "'" + internal.replaceAll("'", "''") + "'";
            if (buf.length() != 0) buf.append(", ");
            buf.append(internal);
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(org.
     * gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String dependentValueToRawValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        return decompressValue(dependentValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(org.
     * gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String rawOrDependentValueToDependentValue(User user, String rawValue)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        return compressValue(rawValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model
     * .user.User, java.lang.String)
     */
    @Override
    protected void validateValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        if (!isSkipValidation()) {
            String rawValue = decompressValue(dependentValue);
            String[] terms = getTerms(rawValue);
            if (terms.length == 0 && !allowEmpty)
                throw new WdkUserException(
                        "The value to enumParam/flatVocabParam "
                                + getFullName() + " cannot be empty");
        }
    }

    /**
     * @param selectMode
     *            the selectMode to set
     */
    public void setSelectMode(String selectMode) {
        this.selectMode = selectMode;
    }

    /**
     * @return the selectMode
     */
    public String getSelectMode() {
        return selectMode;
    }

    protected void applySelectMode() {
        logger.trace("select mode: '" + selectMode + "'");
        if (defaultValue != null && defaultValue.length() > 0) return;

        // single pick can only select one value
        if (selectMode == null || !multiPick) selectMode = SELECT_MODE_FIRST;
        if (selectMode.equalsIgnoreCase(SELECT_MODE_ALL)) {
            StringBuilder builder = new StringBuilder();
            for (String term : termInternalMap.keySet()) {
                if (builder.length() > 0) builder.append(",");
                builder.append(term);
            }
            this.defaultValue = builder.toString();
        } else if (selectMode.equalsIgnoreCase(SELECT_MODE_FIRST)) {
            StringBuilder builder = new StringBuilder();
            Stack<EnumParamTermNode> stack = new Stack<EnumParamTermNode>();
            if (termTreeList.size() > 0) stack.push(termTreeList.get(0));
            while (!stack.empty()) {
                EnumParamTermNode node = stack.pop();
                if (builder.length() > 0) builder.append(",");
                builder.append(node.getTerm());
                for (EnumParamTermNode child : node.getChildren()) {
                    stack.push(child);
                }
            }
            this.defaultValue = builder.toString();
        }
    }

    @Override
    protected void applySuggection(ParamSuggestion suggest) {
        selectMode = suggest.getSelectMode();
    }

    /**
     * @return the suppressNode
     */
    public boolean isSuppressNode() {
        return suppressNode;
    }

    /**
     * @param suppressNode
     *            the suppressNode to set
     */
    public void setSuppressNode(boolean suppressNode) {
        this.suppressNode = suppressNode;
    }

}
