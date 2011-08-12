/**
 * 
 */
package org.gusdb.wdk.model.attribute.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.PrimaryKeyAttributeValue;

/**
 * @author jerric
 * 
 */
public class WordCloudAttributePlugin extends AbstractAttributePlugin implements
        AttributePlugin {

    private static final String ATTR_PLUGIN = "plugin";
    private static final String ATTR_CONTENT = "content";

    private static final Logger logger = Logger
            .getLogger(WordCloudAttributePlugin.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributePlugin#process()
     */
    public Map<String, Object> process() {
        StringBuilder content = new StringBuilder();
        try {
            Map<PrimaryKeyAttributeValue, Object> values = getAttributeValues();
            for (Object value : values.values()) {
                if (value == null) continue;
                content.append(" ").append(value);
            }
        }
        catch (Exception ex) {
            logger.error(ex);
            throw new RuntimeException(ex);
        }

        // compose the result
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(ATTR_CONTENT, content.toString().trim());
        result.put(ATTR_PLUGIN, this);
        return result;
    }
}
