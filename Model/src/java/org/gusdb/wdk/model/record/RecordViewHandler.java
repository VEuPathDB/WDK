package org.gusdb.wdk.model.record;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author jerric
 * 
 */
public interface RecordViewHandler {

    Map<String, Object> process(RecordInstance recordInstance)
            throws WdkModelException, WdkUserException;
}
