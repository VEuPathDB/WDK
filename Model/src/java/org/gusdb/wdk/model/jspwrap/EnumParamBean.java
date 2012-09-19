package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.TreeNode;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamTermNode;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * A wrapper on a {@link AbstractEnumParam} that provides simplified access for
 * consumption by a view.
 * 
 * Note on dependent params: if this is a dependent param and depended param is
 * set, will access values based on that value; otherwise will access values
 * based on the default value of the depended param (i.e. are assuming caller
 * knows what they are doing).
 */
public class EnumParamBean extends ParamBean<AbstractEnumParam> {

	private static final Logger logger = Logger.getLogger(EnumParamBean.class.getName());
	
    private String[] currentValues;

    // if this obj wraps a dependent param, holds depended values
    private String _dependedValue;
    private boolean _dependedValueChanged = false;
    private EnumParamCache _cache;
    
    public EnumParamBean(AbstractEnumParam param) {
        super(param);
    }

    public Boolean getMultiPick() {
        return param.getMultiPick();
    }
    
	public boolean getQuote() {
		return param.getQuote();
	}

	public boolean isSkipValidation() {
		return param.isSkipValidation();
	}

    public String getDisplayType() {
        return param.getDisplayType();
    }

    public String getDependedValue() {
        return _dependedValue;
    }

	public boolean isDependentParam() {
		return param.isDependentParam();
	}

    public void setDependedValue(String dependedValue) {
    	if ((_dependedValue == null && dependedValue != null) ||
    	    (_dependedValue != null && !_dependedValue.equals(dependedValue))) {
    		_dependedValue = dependedValue;
    		_dependedValueChanged = true;
    	}
    }
    
    @Override
    public String getDefault() throws WdkUserException, WdkModelException {
        return getCache().getDefaultValue();
    }
    
    // NOTE: not threadsafe!  This class is expected only to be used in a single thread
    private EnumParamCache getCache() throws WdkModelException {
    	if (_cache == null || _dependedValueChanged) {
    		_cache = param.getValueCache(_dependedValue);
    		_dependedValueChanged = false;
    	}
    	return _cache;
    }
    
    public String[] getVocabInternal() throws WdkModelException {
        return getCache().getVocabInternal();
    }

    public String[] getVocab() throws WdkModelException, WdkUserException {
        return getCache().getVocab();
    }

    public Map<String, String> getVocabMap() throws WdkModelException, WdkUserException {
        return getCache().getVocabMap();
    }

    public String[] getDisplays() throws WdkModelException, WdkUserException {
        return getCache().getDisplays();
    }

    public Map<String, String> getDisplayMap() throws WdkModelException, WdkUserException {
        return getCache().getDisplayMap();
    }

    public Map<String, String> getParentMap() throws WdkModelException, WdkUserException {
        return getCache().getParentMap();
    }

	public String getInternalValue(User user, String dependentValue)
			throws WdkModelException, WdkUserException {
		return param.getInternalValue(user, dependentValue, _dependedValue);
	}

	public ParamBean<?> getDependedParam() throws WdkModelException {
        Param dependedParam = param.getDependedParam();
        if (dependedParam != null) {
            return ParamBeanFactory.createBeanFromParam(user, dependedParam);
        }
        return null;
    }
    
    public EnumParamTermNode[] getVocabTreeRoots() throws Exception {
    	return getCache().getVocabTreeRoots();
    }

    public String[] getTerms(String termList) throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        return param.getTerms(termList);
    }

    public String getRawDisplayValue() throws WdkModelException, WdkUserException {
        String rawValue = getRawValue();
        if (rawValue == null) rawValue = "";
        if (!param.isSkipValidation()) {
            String[] terms = rawValue.split(",");
            Map<String, String> displays = getDisplayMap();
            StringBuffer buffer = new StringBuffer();
            for (String term : terms) {
                if (buffer.length() > 0) buffer.append(", ");
                String display = displays.get(term.trim());
                if (display == null) display = term;
                buffer.append(display);
            }
            return buffer.toString();
        } else {
            return rawValue;
        }
    }

    /**
     * Sets the currently selected values (as set on a user form) on the bean.
     * 
     * @param currentValues currently selected values
     */
    public void setCurrentValues(String[] currentValues) {
        this.currentValues = currentValues;
    }

    /**
     * Returns map where keys are vocab values and values are booleans telling
     * whether each value is currently selected or not.
     * 
     * @return map from value to selection status
     * @throws Exception
     */
    public Map<String, Boolean> getCurrentValues() throws Exception {
        if (currentValues == null) return new LinkedHashMap<String, Boolean>();
        Map<String, Boolean> values = new LinkedHashMap<String, Boolean>();
        Map<String, String> terms = getVocabMap();
        // ignore the validation for type-ahead params.
        String displayType = getDisplayType();
        if (displayType == null) displayType = "";
        boolean typeAhead = displayType.equals(AbstractEnumParam.DISPLAY_TYPE_AHEAD);
        for (String term : currentValues) {
            boolean valid = typeAhead || terms.containsKey(term);
            values.put(term, valid);
        }
        return values;
    }

    /**
     * Returns a TreeNode containing all values for this tree param,
     * with the "currently selected" values checked
     * 
     * @return up-to-date tree of this param
     * @throws Exception
     */
    public TreeNode getParamTree() throws Exception {
        TreeNode root = new TreeNode(getName(), "top");
        for (EnumParamTermNode paramNode : getVocabTreeRoots()) {
            if (paramNode.getChildren().length == 0) {
                root.addChildNode(new TreeNode(paramNode.getTerm(),
                        paramNode.getDisplay(), paramNode.getDisplay()));
            } else {
                root.addChildNode(paramNode.toTreeNode());
            }
        }

        if (currentValues != null && currentValues.length > 0) {
            List<String> currentValueList = Arrays.asList(currentValues);
            root.turnOnSelectedLeaves(currentValueList);
            root.setDefaultLeaves(currentValueList);
        }
        return root;
    }

    /**
     * Temporary method to allow easy on/off of checkbox tree for value
     * selection.
     * 
     * @return whether checkbox tree should be used (columns layout otherwise)
     */
    public boolean getUseCheckboxTree() {
        return true;
    }

    @Override
    public void validate(UserBean user, String rawOrDependentValue)
            throws WdkModelException, WdkUserException {
      logger.debug("Validating param=" + getName() + ", value=" + rawOrDependentValue + ", dependedValue=" + _dependedValue);
      param.validateValue(user.getUser(), rawOrDependentValue, _dependedValue); 
    }
}
