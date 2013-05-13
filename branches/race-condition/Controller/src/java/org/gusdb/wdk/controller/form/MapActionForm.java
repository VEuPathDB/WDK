package org.gusdb.wdk.controller.form;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.gusdb.wdk.model.Utilities;

public abstract class MapActionForm extends ActionForm {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MapActionForm.class.getName());

    protected Map<String, Object> values = new LinkedHashMap<String, Object>();
    protected Map<String, String[]> arrays = new LinkedHashMap<String, String[]>();

    public Object getValue(String key) {
        return values.get(key);
    }

    public void setValue(String key, Object value) {
        logger.trace("set value: key=[" + key + "] value=[" + value + "]");
        values.put(key, value);
    }

    public String[] getArray(String key) {
        return arrays.get(key);
    }

    public void setArray(String key, String[] array) {
        logger.trace("set array: key=[" + key + "] length=" + array.length
                + " array=" + Utilities.fromArray(array) + "");
        Set<String> values = new LinkedHashSet<String>();
        for (String value : array) {
            values.add(value);
        }
        arrays.put(key, values.toArray(new String[0]));
    }

    public Object getValueOrArray(String key) {
        // in the case some params set value into array, we need to get it from
        // array too.
        Object value = values.get(key);
        logger.trace("key=" + key + ", value=" + value + ", isNull="
                + (value == null));
        if (value == null) {
            String[] array = arrays.get(key);
            value = Utilities.fromArray(array);
            logger.trace("array_value=" + value + ", isNull=" + (value == null));
        }
        return value;
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

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Printing out mapped form:\n");
        builder.append("Values:\n");
        for (String key : values.keySet()) {
            builder.append("\t" + key + "=" + values.get(key) + "\n");
        }
        builder.append("Arrays:\n");
        for (String key : arrays.keySet()) {
            String[] array = arrays.get(key);
            builder.append("\t" + key + "=[" + Utilities.fromArray(array)
                    + "]\n");
        }
        return builder.toString();
    }
}
