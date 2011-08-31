/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

/**
 * @author Jerric
 * @created Feb 16, 2006
 */
public class ParamReference extends Reference {

    public static Param resolveReference(WdkModel wdkModel,
            ParamReference paramRef, String servedQueryName)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        String twoPartName = paramRef.getTwoPartName();
        Param param = (Param) wdkModel.resolveReference(twoPartName);
        // clone the param to have different default values
        param = param.clone();

        // if the param has customized default value
        String defaultValue = paramRef.getDefault();
        if (defaultValue != null) param.setDefault(defaultValue);

        // if the param has customized allowEmpty
        Boolean allowEmpty = paramRef.isAllowEmpty();
        if (allowEmpty != null) {
            param.setAllowEmpty(allowEmpty);

            // if the param has customized allowEmpty
            String emptyValue = paramRef.getEmptyValue();
            if (emptyValue != null) param.setEmptyValue(emptyValue);
        }
        Boolean noTranslation = paramRef.getNoTranslation();
        if (noTranslation != null) param.setNoTranslation(noTranslation);

        // if the visible is set
        Boolean visible = paramRef.getVisible();
        if (visible != null) param.setVisible(visible);

        // set help if exists
        String help = paramRef.getHelp();
        if (help != null) param.setHelp(help);

        Boolean number = paramRef.getNumber();

        Boolean quote = paramRef.getQuote();
        Boolean multiPick = paramRef.isMultiPick();
        String displayType = paramRef.getDisplayType();
        String selectMode = paramRef.getSelectMode();
        String queryRef = paramRef.getQueryRef();
        Boolean suppressNode = paramRef.getSuppressNode();
        if (param instanceof AbstractEnumParam) {
            AbstractEnumParam enumParam = (AbstractEnumParam) param;
            // check those invalid properties
            if (number != null)
                throw new WdkModelException("The 'number' property is not "
                        + "allowed in param '" + twoPartName + "'");

            if (param instanceof FlatVocabParam)
                ((FlatVocabParam) param).setServedQueryName(servedQueryName);

            // if the param has customized multi pick
            if (multiPick != null) enumParam.setMultiPick(multiPick);

            // if the queryRef is set for FlatVocabParam
            if (queryRef != null) {
                if (param instanceof FlatVocabParam) {
                    ((FlatVocabParam) param).setQueryRef(queryRef);
                } else throw new WdkModelException("The paramRef to '"
                        + twoPartName + "' is not a flatVocabParam. The "
                        + "'queryRef' property can only be applied to "
                        + "paramRefs of flatVocabParams.");
            }

            // if quote is set, it overrides the value of the param
            if (quote != null) enumParam.setQuote(quote);

            // if displayType is set, overrides the value in param
            if (displayType != null) enumParam.setDisplayType(displayType);

            if (selectMode != null) enumParam.setSelectMode(selectMode);

            if (suppressNode != null) enumParam.setSuppressNode(suppressNode);

        } else { // or other param types
            if (multiPick != null || quote != null || displayType != null
                    || selectMode != null || queryRef != null)
                throw new WdkModelException("The paramRef to '" + twoPartName
                        + "' is not a flatVocabParam nor enumParam. The "
                        + "'multiPick', 'displayType', 'quote',"
                        + " 'selectMode', 'queryRef' properties can only be "
                        + "applied to paramRefs of flatVocabParams or "
                        + "enumParams.");
            if (param instanceof StringParam) {
                // if quote is set, it overrides the value of the param
                if (number != null) ((StringParam) param).setNumber(number);
            } else if (number != null) {
                throw new WdkModelException("The paramRef to '" + twoPartName
                        + "' is not a stringParam. The 'number' property can "
                        + "only be applied to paramRefs of stringParams.");
            }
        }

        // resolve the group reference
        String groupRef = paramRef.getGroupRef();
        if (groupRef != null) {
            Group group = (Group) wdkModel.resolveReference(groupRef);
            param.setGroup(group);
        }
        param.resolveReferences(wdkModel);
        param.setResources(wdkModel);
        return param;
    }

    private String defaultValue;
    private Boolean allowEmpty;
    private Boolean multiPick;
    private String queryRef;
    private Boolean quote;
    private Boolean number;
    private String emptyValue;
    private String displayType;
    private Boolean visible;
    private String selectMode;
    private Boolean noTranslation;
    private Boolean suppressNode;

    private List<WdkModelText> helps = new ArrayList<WdkModelText>();
    private String help;

    public ParamReference() {}

    /**
     * @param twoPartName
     * @throws WdkModelException
     */
    public ParamReference(String twoPartName) throws WdkModelException {
        super(twoPartName);
        // TODO Auto-generated constructor stub
    }

    /**
     * @return Returns the defaultValue.
     */
    public String getDefault() {
        return this.defaultValue;
    }

    /**
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the allowEmpty
     */
    public Boolean isAllowEmpty() {
        return this.allowEmpty;
    }

    /**
     * @param allowEmpty
     *            the allowEmpty to set
     */
    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    /**
     * @return the multiPick
     */
    public Boolean isMultiPick() {
        return this.multiPick;
    }

    /**
     * @param multiPick
     *            the multiPick to set
     */
    public void setMultiPick(boolean multiPick) {
        this.multiPick = multiPick;
    }

    /**
     * @return the queryRef
     */
    public String getQueryRef() {
        return this.queryRef;
    }

    /**
     * @param queryRef
     *            the queryRef to set
     */
    public void setQueryRef(String queryRef) {
        this.queryRef = queryRef;
    }

    /**
     * @return the quote
     */
    public Boolean getQuote() {
        return quote;
    }

    /**
     * @param quote
     *            the quote to set
     */
    public void setQuote(Boolean quote) {
        this.quote = quote;
    }

    public String getEmptyValue() {
        return emptyValue;
    }

    public void setEmptyValue(String emptyValue) {
        this.emptyValue = emptyValue;
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

    /**
     * @return the visible
     */
    public Boolean getVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the selectMode
     */
    public String getSelectMode() {
        return selectMode;
    }

    /**
     * @param selectMode
     *            the selectMode to set
     */
    public void setSelectMode(String selectMode) {
        this.selectMode = selectMode;
    }

    /**
     * @return the noTranslation
     */
    public Boolean getNoTranslation() {
        return noTranslation;
    }

    /**
     * @param noTranslation
     *            the noTranslation to set
     */
    public void setNoTranslation(Boolean noTranslation) {
        this.noTranslation = noTranslation;
    }

    /**
     * @return the number
     */
    public Boolean getNumber() {
        return number;
    }

    /**
     * @param number
     *            the number to set
     */
    public void setNumber(Boolean number) {
        this.number = number;
    }

    /**
     * @return the suppressNode
     */
    public Boolean getSuppressNode() {
        return suppressNode;
    }

    /**
     * @param suppressNode
     *            the suppressNode to set
     */
    public void setSuppressNode(Boolean suppressNode) {
        this.suppressNode = suppressNode;
    }

    public void addHelp(WdkModelText help) {
        this.helps.add(help);
    }

    public String getHelp() {
        return help;
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        for (WdkModelText help : helps) {
            if (help.include(projectId)) {
                if (this.help != null)
                    throw new WdkModelException("More than one <help> are "
                            + "defined in the paramRef '"
                            + this.getTwoPartName());

                help.excludeResources(projectId);
                this.help = help.getText();
            }
        }
        helps = null;
    }
}
