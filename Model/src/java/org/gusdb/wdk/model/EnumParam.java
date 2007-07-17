package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EnumParam extends AbstractEnumParam {

    private List<EnumItem> enumItems = new ArrayList<EnumItem>();

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void addEnumItem(EnumItem enumItem) {
        this.enumItems.add(enumItem);
    }

    public String[] getDisplay() {
        String[] displays = new String[enumItems.size()];
        for (int i = 0; i < displays.length; i++) {
            displays[i] = enumItems.get(i).getDisplay();
        }
        return displays;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void initVocabMap() throws WdkModelException {
        if (vocabMap == null) {
            vocabMap = new LinkedHashMap<String, String>();
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
        param.enumItems = new ArrayList<EnumItem>(enumItems);
        return param;
    }

    /*
     * (non-Javadoc)
     * the default is always the terms
     * @see org.gusdb.wdk.model.AbstractEnumParam#getDefault()
     */
    @Override
    public String getDefault() throws WdkModelException {
        StringBuffer sb = new StringBuffer();
        for (EnumItem item : enumItems) {
            if (item.isDefault()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(item.getTerm());
            }
        }
        if (sb.length() == 0) {
            // get the first item as the default
            EnumItem item = enumItems.get(0);
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
        List<EnumItem> newItems = new ArrayList<EnumItem>();
        for (EnumItem item : enumItems) {
            if (item.include(projectId)) {
                item.excludeResources(projectId);
                newItems.add(item);
            }
        }
        enumItems = newItems;
        if (enumItems.size() == 0)
            throw new WdkModelException(
                    "None of the enum values is available in enumParam "
                            + this.name);
    }
}
