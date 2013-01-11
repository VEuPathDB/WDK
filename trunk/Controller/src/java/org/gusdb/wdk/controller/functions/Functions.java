package org.gusdb.wdk.controller.functions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.struts.action.ActionMessages;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeValue;

public class Functions {
  
  public static String getStackTrace(Throwable t) {
    ByteArrayOutputStream strStream = new ByteArrayOutputStream();
    t.printStackTrace(new PrintStream(strStream));
    return strStream.toString();
  }

  // override this function
  public static String getMessages(ActionMessages messages) {
    StringBuilder sb = new StringBuilder();
    Iterator<?> iter = messages.properties();
    while (iter.hasNext()) {
      sb.append(iter.next()).append("\n");
    }
    return sb.toString();
  }
  
  /**
   * Takes a collection of records (i.e. key-value pair sets) and groups them
   * by similar source ids
   */
  public static Collection<Collection<Map<String, AttributeValue>>>
      groupAttributeRecordsBySource(Collection<Map<String, AttributeValue>> recordCollection)
          throws WdkModelException {
    
    List<Collection<Map<String, AttributeValue>>> groups =
        new ArrayList<Collection<Map<String, AttributeValue>>>();
    
    String currentSourceId = null;
    List<Map<String, AttributeValue>> tempList = new ArrayList<Map<String, AttributeValue>>();
    
    for (Map<String,AttributeValue> record : recordCollection) {
      String nextSourceId = (String)record.get("source_id").getValue();
      if (currentSourceId == null) {
        // first record
        currentSourceId = nextSourceId;
      }
      else if (!nextSourceId.equals(currentSourceId)) {
        // new group
        groups.add(tempList);
        tempList = new ArrayList<Map<String, AttributeValue>>();
        currentSourceId = nextSourceId;
      }
      tempList.add(record);
    }
    if (!tempList.isEmpty()) {
      groups.add(tempList);
    }
    return groups;
  }
}
