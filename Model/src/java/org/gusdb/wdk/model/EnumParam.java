package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EnumParam extends AbstractEnumParam {

    private List<EnumItemList> itemLists;
    private EnumItemList itemList;

    public EnumParam() {
        itemLists = new ArrayList<EnumItemList>();
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void addItemList(EnumItemList itemList) {
        this.itemLists.add(itemList);
    }
    
    public String[] getDisplay() {
        EnumItem[] items = itemList.getEnumItems();
        String[] displays = new String[items.length];
        for (int i = 0; i < items.length;i++) {
            displays[i] = items[i].getDisplay();
        }
        return displays;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void initVocabMap() throws WdkModelException {
        if (vocabMap == null) {
            vocabMap = new LinkedHashMap<String, String>();
            EnumItem[] items = itemList.getEnumItems();
            for (EnumItem item : items) {
                vocabMap.put(item.getTerm(), item.getInternal());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        EnumParam param = new EnumParam();
        super.clone(param);
        param.itemList = new EnumItemList(this.itemList);
        return param;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        super.excludeResources(projectId);

        // exclude enum item list
        for (EnumItemList list : itemLists) {
            if (list.include(projectId)) {
                // set param set, since the list might the default value from it
                list.setParamSet(paramSet);
                list.excludeResources(projectId);
                useTermOnly = list.isUseTermOnly();
                this.itemList = list;
                break;
            }
        }
        itemLists = null;
    }
}
