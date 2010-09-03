package org.gusdb.wdk.controller.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

public abstract class MapActionForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MapActionForm.class);

    private Map<String, Object> values = new HashMap<String, Object>();
    private Map<String, String[]> arrays = new HashMap<String, String[]>();

    public Object getValue(String key) {
        return values.get(key);
    }

    public void setValue(String key, Object value) {
        logger.debug("set value: key=[" + key + "] value=[" + value + "]");
        values.put(key, value);
    }

    public String[] getArray(String key) {
        return arrays.get(key);
    }

    public void setArray(String key, String[] array) {
        logger.debug("set array: key=[" + key + "] length="+array.length+" array=[" + array[0] + "]");
        arrays.put(key, array);
    }

    public void copyFrom(MapActionForm form) {
        values.clear();
        for (String key : form.values.keySet()) {
            values.put(key, form.values.get(key));
        }

        arrays.clear();
        for (String key : form.arrays.keySet()) {
            arrays.put(key, form.arrays.get(key));
        }
    }
    
    /**
     * The method is required by struts html tag-lib
     */
    public void reset() {
        logger.debug("reset form");
        arrays.clear();
        values.clear();
    }
}
