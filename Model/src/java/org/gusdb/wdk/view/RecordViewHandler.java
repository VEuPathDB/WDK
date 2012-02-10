package org.gusdb.wdk.view;

import java.util.Map;

import org.gusdb.wdk.model.RecordInstance;

/**
 * @author jerric
 *
 */
public interface RecordViewHandler {

    Map<String, Object> process(RecordInstance recordInstance);
}
