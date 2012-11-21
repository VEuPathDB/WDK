package org.gusdb.wdk.controller.functions;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.WdkModelException;
import org.junit.Test;

public class TestFunctions {

  private static final String SOURCE_ID = "source_id";
  private static final String REC_VALUE = "rec_value";
  
  private static class StringValue extends AttributeValue {
    
    private String _str;
    
    public StringValue(String str) {
      super(null);
      _str = str;
    }

    @Override
    public Object getValue() throws WdkModelException {
      return _str;
    }
    
    @Override
    public String toString() {
      return "StringValue { value = '" + _str + "' }"; 
    }
  }
  
  private static class RecordBuilder {
    
    private Map<String,AttributeValue> _map = new HashMap<String, AttributeValue>();
    
    public RecordBuilder addRecord(String key, String value) {
      _map.put(key, new StringValue(value));
      return this;
    }
    
    public RecordBuilder addSource(String value) { return addRecord(SOURCE_ID, value); }
    public RecordBuilder addValue(String value) { return addRecord(REC_VALUE, value); }
    
    public Map<String, AttributeValue> toMap() {
      return _map;
    }
  }

  private static Map<String, AttributeValue> buildRec(String source, String value) {
    return new RecordBuilder().addSource(source).addValue(value).toMap();
  }
 
  @Test
  public void testGroupMapper() {
    List<Map<String, AttributeValue>> recordCollection = new ArrayList<Map<String, AttributeValue>>();
    recordCollection.add(buildRec("source_1","record_0"));
    recordCollection.add(buildRec("source_1","record_1"));
    recordCollection.add(buildRec("source_2","record_2"));
    recordCollection.add(buildRec("source_3","record_3"));
    recordCollection.add(buildRec("source_3","record_4"));
    recordCollection.add(buildRec("source_3","record_5"));
    recordCollection.add(buildRec("source_4","record_6"));
    recordCollection.add(buildRec("source_5","record_7"));
    recordCollection.add(buildRec("source_5","record_8"));
    recordCollection.add(buildRec("source_5","record_9"));
    
    try {
      Collection<Collection<Map<String, AttributeValue>>> result =
          Functions.groupAttributeRecordsBySource(recordCollection);

      assertEquals(result.size(), 5);
      
      Iterator<Collection<Map<String, AttributeValue>>> iter = result.iterator();
      assertEquals(iter.next().size(), 2);
      assertEquals(iter.next().size(), 1);
      assertEquals(iter.next().size(), 3);
      assertEquals(iter.next().size(), 1);
      assertEquals(iter.next().size(), 3);
      
      /* uncomment to view results */ /*
      for (Collection<Map<String, AttributeValue>> group : result) {
        System.out.println("New Group:");
        for (Map<String, AttributeValue> record : group) {
          String out = "Record { ";
          for (String key : record.keySet()) {
            out += "( " + key + ", " + record.get(key) + " ) ";
          }
          out += "}";
          System.out.println("  " + out);
        }
      } */
    }
    catch (WdkModelException e) {
      throw new RuntimeException(e);
    }
    
  }
  

  @Test
  public void testStackTraceGetter() {
    System.out.println(Functions.getStackTrace(new Exception("Expected exception. Yay!")));
  }
}
