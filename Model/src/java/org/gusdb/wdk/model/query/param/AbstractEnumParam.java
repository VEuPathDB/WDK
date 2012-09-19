package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         raw data: a comma separated list of terms;
 * 
 *         user-dependent data: same as user-independent data, can be either a
 *         comma separated list of terms or a compressed checksum;
 * 
 *         user-independent data: same as user-dependent data;
 * 
 *         internal data: a comma separated list of internals, quotes are
 *         properly escaped or added
 * 
 *         Note about dependent params: AbstractEnumParams can be dependent on
 *         other parameter values. Thus, this class provides two versions of
 *         many methods: one that takes a dependent value, and one that doesn't.
 *         If the version is called without a depended value, or the version
 *         requiring a depended value is called with null, yet this param
 *         requires a value, the default value of the depended param is used.
 */
public abstract class AbstractEnumParam extends Param {

    public static final String DISPLAY_TYPE_AHEAD = "typeAhead";
    public static final String DISPLAY_TREE_BOX = "treeBox";

    static final String SELECT_MODE_NONE = "none";
    static final String SELECT_MODE_ALL = "all";
    static final String SELECT_MODE_FIRST = "first";

    protected boolean multiPick = false;
    protected boolean quote = true;

    protected String dependedParamRef;
    private String displayType;

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
        this.quote = param.quote;
        this.dependedParamRef = param.dependedParamRef;
        this.displayType = param.displayType;
        this.selectMode = param.selectMode;
        this.suppressNode = param.suppressNode;
    }

    protected abstract EnumParamCache createEnumParamCache(
            String dependedParamVal) throws WdkModelException;

    private EnumParamCache getEnumParamCache(String dependedParamVal) {
        if (isDependentParam() && dependedParamVal == null) {
            throw new NoDependedValueException(
                    "Attempt made to retrieve values in dependent param "
                            + getName() + " without setting depended value.");
        }
        try {
            return createEnumParamCache(dependedParamVal);
        }
        catch (WdkModelException wme) {
            throw new WdkRuntimeException(
                    "Unable to create EnumParamCache for param " + getName()
                            + " with depended value " + dependedParamVal, wme);
        }
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    // used only to initially set this property
    public void setMultiPick(Boolean multiPick) {
        this.multiPick = multiPick.booleanValue();
    }

    public Boolean getMultiPick() {
        return new Boolean(multiPick);
    }

    public boolean isSkipValidation() {
        return (displayType != null && displayType.equals(DISPLAY_TYPE_AHEAD));
    }

    public void setQuote(boolean quote) {
        this.quote = quote;
    }

    public boolean getQuote() {
        return quote;
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

    public boolean isDependentParam() {
        return (dependedParamRef != null);
    }

    public Param getDependedParam() throws WdkModelException {
        if (!isDependentParam()) return null;
        String paramName = dependedParamRef.split("\\.")[1];
        if (contextQuestion != null) {
            return contextQuestion.getParamMap().get(paramName);
        } else if (contextQuery != null) {
            return contextQuery.getParamMap().get(paramName);
        }
        return (Param) wdkModel.resolveReference(dependedParamRef);
    }

    public void setDependedParamRef(String dependedParamRef) {
        this.dependedParamRef = dependedParamRef;
    }

    /**
     * Returns the default value. In the case that this is a dependent param,
     * uses the default value of the depended param as the depended value
     * (recursively).
     */
    @Override
    public String getDefault() throws WdkModelException {
        String dependedValue = null;
        if (isDependentParam()) {
            dependedValue = getDependedParam().getDefault();
            logger.debug("param=" + getFullName() + " getting default with depended=" + dependedValue);
        }
        return getDefault(dependedValue);
    }

    public String getDefault(String dependedParamVal) throws WdkModelException {
        if (isDependentParam()) {
            if (dependedParamVal == null) {
                logger.warn("Retrieving default value for dependent param "
                        + getName()
                        + " without depended param value.  Ensure this is intentional.");
                dependedParamVal = getDependedParam().getDefault();
            }
            return getEnumParamCache(dependedParamVal).getDefaultValue();
        }
        return getEnumParamCache(null).getDefaultValue();
    }

    public EnumParamCache getValueCache() {
        return getValueCache(null);
    }

    public EnumParamCache getValueCache(String dependedParamVal) {
        return getEnumParamCache(dependedParamVal);
    }

    public String[] getVocab() {
        return getVocab(null);
    }

    public String[] getVocab(String dependedParamVal)
            throws WdkRuntimeException {
        return getEnumParamCache(dependedParamVal).getVocab();
    }

    public EnumParamTermNode[] getVocabTreeRoots() {
        return getVocabTreeRoots(null);
    }

    public EnumParamTermNode[] getVocabTreeRoots(String dependedParamVal) {
        return getEnumParamCache(dependedParamVal).getVocabTreeRoots();
    }

    public String[] getVocabInternal() {
        return getVocabInternal(null);
    }

    public String[] getVocabInternal(String dependedParamVal) {
        return getEnumParamCache(dependedParamVal).getVocabInternal();
    }

    public String[] getDisplays() {
        return getDisplays(null);
    }

    public String[] getDisplays(String dependedParamVal) {
        return getEnumParamCache(dependedParamVal).getDisplays();
    }

    public Map<String, String> getVocabMap() {
        return getVocabMap(null);
    }

    public Map<String, String> getVocabMap(String dependedParamVal) {
        return getEnumParamCache(dependedParamVal).getVocabMap();
    }

    public Map<String, String> getDisplayMap() {
        return getDisplayMap(null);
    }

    public Map<String, String> getDisplayMap(String dependedParamVal) {
        return getEnumParamCache(dependedParamVal).getDisplayMap();
    }

    public Map<String, String> getParentMap() {
        return getParentMap(null);
    }

    public Map<String, String> getParentMap(String dependedParamVal) {
        return getEnumParamCache(dependedParamVal).getParentMap();
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void initTreeMap(EnumParamCache cache) throws WdkModelException {

        // construct index
        Map<String, EnumParamTermNode> indexMap = new LinkedHashMap<String, EnumParamTermNode>();
        for (String term : cache.getTerms()) {
            EnumParamTermNode node = new EnumParamTermNode(term);
            node.setDisplay(cache.getDisplay(term));
            indexMap.put(term, node);

            // check if the node is root
            String parentTerm = cache.getParent(term);
            if (parentTerm != null && !cache.containsTerm(parentTerm))
                parentTerm = null;
            if (parentTerm == null) {
                cache.addParentNodeToTree(node);
                cache.unsetParentTerm(term);
            }
        }
        // construct the relationships
        for (String term : cache.getTerms()) {
            String parentTerm = cache.getParent(term);
            // skip if parent doesn't exist
            if (parentTerm == null) continue;

            EnumParamTermNode node = indexMap.get(term);
            EnumParamTermNode parent = indexMap.get(parentTerm);
            parent.addChild(node);
        }

        if (suppressNode) suppressChildren(cache, cache.getTermTreeListRef());
    }

    private void suppressChildren(EnumParamCache cache, List<EnumParamTermNode> children) {
        boolean suppressed = false;
        if (children.size() == 1) {
            // has only one child, suppress it in the tree if it has
            // grandchildren
            EnumParamTermNode child = children.get(0);
            EnumParamTermNode[] grandChildren = child.getChildren();
            if (grandChildren.length > 0) {
                logger.debug(child.getTerm() + " suppressed.");
                children.remove(0);
                for (EnumParamTermNode grandChild : grandChildren) {
                    children.add(grandChild);
                }
                // Also remove the suppressed node from term & internal map.
                // Disable the cache change, to have a correct tree on portal.
                // cache.removeTerm(child.getTerm());
                
                // need to suppress children
                suppressChildren(cache, children);
                suppressed = true;
            }
        }
        if (!suppressed) {
            for (EnumParamTermNode child : children) {
                suppressChildren(cache, child.getChildrenList());
            }
        }
    }

    public String[] getTerms(String termList) {
        // the input is a list of terms
        if (termList == null) return new String[0];

        String[] terms;
        if (multiPick) {
            terms = termList.split("[,]+");
            for (int i = 0; i < terms.length; i++) {
                terms[i] = terms[i].trim();
            }
        } else {
            terms = new String[] { termList.trim() };
        }

        // disable the validation - it prevented the revising of invalid step
        // if a strategy has more than one invalid steps.
        // if (!isSkipValidation()) {
        // initVocabMap(dependedParamVal);
        // for (String term : terms) {
        // if (!termInternalMap.containsKey(term))
        // throw new WdkModelException(" - Invalid term '" + term
        // + "' for parameter '" + name + "'");
        // }
        // }
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
            String dependentValue) throws WdkUserException, WdkModelException {
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
    public String dependentValueToInternalValue(User user, String dependedValue)
            throws WdkUserException, WdkModelException {
        return dependentValueToInternalValue(user, dependedValue, null);
    }

    public String dependentValueToInternalValue(User user,
            String dependedValue, String dependedParamValue)
            throws WdkUserException, WdkModelException {
        EnumParamCache cache = getEnumParamCache(dependedParamValue);

        String rawValue = decompressValue(dependedValue);
        if (rawValue == null || rawValue.length() == 0) rawValue = emptyValue;

        String[] terms = getTerms(rawValue);
        StringBuffer buf = new StringBuffer();
        for (String term : terms) {
            String internal = (isNoTranslation()) ? term
                    : cache.getInternal(term);
            if (!cache.containsTerm(term)) {
                // doesn't validate term, if it doesn't exist in the list, just
                // use it as internval value. This is for wildcard support in
                // type-ahead params.
                if (isSkipValidation()) {
                    internal = term;
                } else {
                    // term doesn't exists need to correct it later
                    throw new WdkUserException("param " + getFullName()
                            + " encountered an invalid term from user #"
                            + user.getUserId() + ": " + term);
                }
            }
            if (quote && !(internal.startsWith("'") && internal.endsWith("'")))
                internal = "'" + internal.replaceAll("'", "''") + "'";
            if (buf.length() != 0) buf.append(", ");
            buf.append(internal);
        }
        return buf.toString();
    }

    @Override
    public String getInternalValue(User user, String dependentValue)
            throws WdkModelException, WdkUserException {
        return getInternalValue(user, dependentValue, null);
    }

    public String getInternalValue(User user, String dependentValue,
            String dependedParamValue) throws WdkModelException,
            WdkUserException {
        String internalValue = dependentValueToInternalValue(user,
                dependentValue, dependedParamValue);
        if (handler != null)
            internalValue = handler.transform(user, internalValue);
        return internalValue;
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
            throws WdkModelException, WdkUserException {
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
            throws WdkModelException, WdkUserException {
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
    protected void validateValue(User user, String userDependentValue)
             throws WdkModelException, WdkUserException {
        String dependedValue = null;
        if (isDependentParam()) {
            dependedValue = getDependedParam().getDefault();
        }
        validateValue(user, userDependentValue, dependedValue);
    }

    public void validateValue(User user, String userDependentValue, String dependedValue)
            throws WdkModelException, WdkUserException {
        // handle the empty case
        if (userDependentValue == null || userDependentValue.length() == 0) {
            if (!allowEmpty)
                throw new WdkModelException("The parameter '" + getPrompt()
                        + "' does not allow empty value");
            // otherwise, got empty value and is allowed, no need for further
            // validation.
        }

        if (!isSkipValidation()) {
            String rawValue = decompressValue(userDependentValue);
            logger.debug("param=" + getFullName() + " - validating: " + rawValue);
            String[] terms = getTerms(rawValue);
            if (terms.length == 0 && !allowEmpty)
                throw new WdkUserException(
                        "The value to enumParam/flatVocabParam "
                                + getFullName() + " cannot be empty");
            Map<String, String> map = getVocabMap(dependedValue);
            boolean error = false;
            StringBuilder message = new StringBuilder();
            for (String term : terms) {
                if (!map.containsKey(term)) {
                    error = true;
                    message.append("Invalid term for param [" + getFullName()
                            + "]: " + term + ". ");
                }
            }
            if (error) throw new WdkUserException(message.toString());
        } else {
            logger.debug("param=" + getFullName() + " - skip validation");
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

    /**
     * Builds the default value of the "current" enum values
     * 
     * @throws WdkUserException
     * @throws WdkModelException
     */
    protected void applySelectMode(EnumParamCache cache)
            throws WdkModelException {
        logger.debug("applySelectMode(): select mode: '" + selectMode
                + "', default from model = " + super.getDefault());
        String defaultFromModel = super.getDefault();

        String errorMessage = "The default value from model, '"
                + defaultFromModel + "', is not a valid term for param "
                + getFullName() + ", please double check this default value.";
        if (defaultFromModel != null) {
            // default defined in the model, validate default values, and set it
            // to the cache.
            String[] defaults = getMultiPick() ? defaultFromModel.split("\\s*,\\s*")
                    : new String[] { defaultFromModel };
            for (String def : defaults) {
                if (!cache.getTerms().contains(def)) {
                    // the given default doesn't match any term
                    if (isDependentParam()) {
                        // need to investigate and make sure the default is as
                        // intended.
                        // Cannot throws exception here, since the default might
                        // not be valid for a different depended value.
                        logger.warn(errorMessage);
                    } else {
                        // param doesn't depend on anything. The default must be
                        // wrong.
                        logger.warn(errorMessage);
                        throw new WdkModelException(errorMessage);

                    }
                }
            }
            cache.setDefaultValue(defaultFromModel);
            return;
        }

        // single pick can only select one value
        if (selectMode == null || !multiPick) selectMode = SELECT_MODE_FIRST;
        if (selectMode.equalsIgnoreCase(SELECT_MODE_ALL)) {
            StringBuilder builder = new StringBuilder();
            for (String term : cache.getTerms()) {
                if (builder.length() > 0) builder.append(",");
                builder.append(term);
            }
            cache.setDefaultValue(builder.toString());
        } else if (selectMode.equalsIgnoreCase(SELECT_MODE_FIRST)) {
            StringBuilder builder = new StringBuilder();
            Stack<EnumParamTermNode> stack = new Stack<EnumParamTermNode>();
            if (cache.getTermTreeListRef().size() > 0)
                stack.push(cache.getTermTreeListRef().get(0));
            while (!stack.empty()) {
                EnumParamTermNode node = stack.pop();
                if (builder.length() > 0) builder.append(",");
                builder.append(node.getTerm());
                for (EnumParamTermNode child : node.getChildren()) {
                    stack.push(child);
                }
            }
            cache.setDefaultValue(builder.toString());
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#appendJSONContent(org.json.JSONObject
     * , boolean)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam, boolean extra)
            throws JSONException {
        // TODO Auto-generated method stub

    }
}
