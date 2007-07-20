package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnumParam extends AbstractEnumParam {

    private List<EnumItemList> enumItemLists = new ArrayList<EnumItemList>();
    private EnumItemList enumItemList;

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void addEnumItemList(EnumItemList enumItemList) {
        this.enumItemLists.add(enumItemList);
    }

    public String[] getDisplay() {
        EnumItem[] enumItems = enumItemList.getEnumItems();
        String[] displays = new String[enumItems.length];
        for (int i = 0; i < displays.length; i++) {
            displays[i] = enumItems[i].getDisplay();
        }
        return displays;
    }
    
    public Map<String, String> getTermDisplayMap() {
        Map<String, String> map = new LinkedHashMap< String, String >();
        EnumItem[] enumItems = enumItemList.getEnumItems();
        for (EnumItem item : enumItems) {
            map.put( item.getTerm(), item.getDisplay() );
        }
        return map;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void initVocabMap() throws WdkModelException {
        if (vocabMap == null) {
            vocabMap = new LinkedHashMap<String, String>();
            EnumItem[] enumItems = enumItemList.getEnumItems();
            for (EnumItem item : enumItems) {
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
        param.enumItemList = new EnumItemList(this.enumItemList);
        return param;
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
        for (EnumItemList itemList : enumItemLists) {
            if (itemList.include(projectId)) {
                itemList.excludeResources(projectId);
                this.enumItemList = itemList;
                // apply the use term only from enumList
                Boolean useTermOnly = itemList.isUseTermOnly();
                if (useTermOnly != null) this.useTermOnly = useTermOnly;
                break;
            }
        }
        if (enumItemList == null)
            throw new WdkModelException(
                    "No EnumItemList available in enumParam " + this.name);
    }
}
