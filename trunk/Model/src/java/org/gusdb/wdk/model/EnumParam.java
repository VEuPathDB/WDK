package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;

public class EnumParam extends AbstractEnumParam {

    private List<EnumItemList> enumItemLists;
    private EnumItemList enumItemList;

    public EnumParam() {
        enumItemLists = new ArrayList<EnumItemList>();
    }

    public EnumParam(EnumParam param) {
        super(param);
        this.enumItemList = param.enumItemList;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void addEnumItemList(EnumItemList enumItemList) {
        this.enumItemLists.add(enumItemList);
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void initVocabMap() throws WdkModelException {
        if (termInternalMap == null) {
            termInternalMap = new LinkedHashMap<String, String>();
            termDisplayMap = new LinkedHashMap<String, String>();

            EnumItem[] enumItems = enumItemList.getEnumItems();
            for (EnumItem item : enumItems) {
                termInternalMap.put(item.getTerm(), item.getInternal());
                termDisplayMap.put(item.getTerm(), item.getDisplay());
            }
        }
    }

    /*
     * (non-Javadoc) the default is always the terms
     * 
     * @see org.gusdb.wdk.model.AbstractEnumParam#getDefault()
     */
    @Override
    public String getDefault() throws WdkModelException {
        StringBuffer sb = new StringBuffer();
        EnumItem[] enumItems = enumItemList.getEnumItems();
        if (enumItems.length == 0) {
           throw new WdkModelException("enumParam '" + this.name 
                    + "' has zero items");
        }
        for (EnumItem item : enumItems) {
            if (item.isDefault()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(item.getTerm());
            }
        }
        if (sb.length() == 0) {
            // get the first item as the default
            EnumItem item = enumItems[0];
            return item.getTerm();
        } else return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        // exclude enum items
        boolean hasEnumList = false;
        for (EnumItemList itemList : enumItemLists) {
            if (itemList.include(projectId)) {
                if (hasEnumList) {
                    throw new WdkModelException("enumParam " + getFullName()
                            + " has more than one <enumList> for project "
                            + projectId);
                } else {
                    itemList.setParam(this);
                    itemList.excludeResources(projectId);
                    this.enumItemList = itemList;

                    // apply the use term only from enumList
                    Boolean useTermOnly = itemList.isUseTermOnly();
                    if (useTermOnly != null) this.useTermOnly = useTermOnly;

                    hasEnumList = true;
                }
            }
        }
        if (enumItemList == null)
            throw new WdkModelException("No enumList available in enumParam "
                    + getFullName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        enumItemList.resolveReferences(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new EnumParam(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam) {
    // do nothing. do not add the enum list into the content, since they may be
    // changed between versions, but we don't want to invalidate a query because
    // of it.
    }
}
