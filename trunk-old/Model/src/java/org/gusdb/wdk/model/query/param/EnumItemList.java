/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * 
 */
public class EnumItemList extends WdkModelBase {

    // need to get default value from param set
    private EnumParam param;

    private List<EnumItem> items;

    private List<ParamConfiguration> useTermOnlies;
    private Boolean useTermOnly;

    private String dependedValue;

    public EnumItemList() {
        items = new ArrayList<EnumItem>();
        useTermOnlies = new ArrayList<ParamConfiguration>();
    }

    public EnumItemList(EnumItemList itemList) {
        this.param = itemList.param;
        this.items = new ArrayList<EnumItem>(itemList.items);
        this.useTermOnly = itemList.useTermOnly;
    }

    public void setDependedValue(String dependedValue) {
	this.dependedValue = dependedValue;
    }

    public String getDependedValue() {
	return dependedValue;
    }

    public void addEnumItem(EnumItem enumItem) {
        items.add(enumItem);
    }

    public EnumItem[] getEnumItems() {
        EnumItem[] array = new EnumItem[items.size()];
        items.toArray(array);
        return array;
    }

    public void addUseTermOnly(ParamConfiguration paramConfig) {
        useTermOnlies.add(paramConfig);
    }

    /**
     * @return the useTermOnly
     */
    public Boolean isUseTermOnly() {
        return this.useTermOnly;
    }

    void setParam(EnumParam param) {
        this.param = param;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude use term only
        boolean hasUseTermOnly = false;
        for (ParamConfiguration paramConfig : useTermOnlies) {
            if (paramConfig.include(projectId)) {
                if (hasUseTermOnly) {
                    throw new WdkModelException("the <enumList> of enumParam "
                            + param.getFullName() + " has more <useTermOnly> "
                            + "for project " + projectId);
                } else {
                    this.useTermOnly = paramConfig.isValue();
                    hasUseTermOnly = true;
                }
            }
        }
        useTermOnlies = null;

        // exclude enum items
        List<EnumItem> newItems = new ArrayList<EnumItem>();
        for (EnumItem item : items) {
            if (item.include(projectId)) {
                item.excludeResources(projectId);
                newItems.add(item);
            }
        }
        items = newItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
        for (EnumItem item : items) {
            item.resolveReferences(wodkModel);
        }
    }
}
