/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jerric
 * 
 */
public class EnumItemList extends WdkModelBase {

    // need to get default value from param set
    private ParamSet paramSet;

    private List<EnumItem> items;

    private List<ParamConfiguration> useTermOnlies;
    private Boolean useTermOnly;

    public EnumItemList() {
        items = new ArrayList<EnumItem>();
        useTermOnlies = new ArrayList<ParamConfiguration>();
    }

    public EnumItemList(EnumItemList itemList) {
        this.paramSet = itemList.paramSet;
        this.items = new ArrayList<EnumItem>(itemList.items);
        this.useTermOnly = itemList.useTermOnly;
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

    void setParamSet(ParamSet paramSet) {
        this.paramSet = paramSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        // exclude use term only
        for (ParamConfiguration paramConfig : useTermOnlies) {
            if (paramConfig.include(projectId)) {
                this.useTermOnly = paramConfig.isValue();
                break;
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
}
