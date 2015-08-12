package org.gusdb.wdk.model.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors {

  private List<String> _messages = new ArrayList<>();
  private Map<String,List<String>> _paramMessages = new HashMap<>();
  
  public void addMessage(String message) {
    _messages.add(message);
  }
  
  public void addParamMessage(String paramName, String message) {
    List<String> messages = _paramMessages.get(paramName);
    if (messages == null) {
      messages = new ArrayList<String>();
      _paramMessages.put(paramName,  messages);
    }
    messages.add(message);
  }
  
  public List<String> getMessages() {
    return _messages;
  }
  
  public Map<String,List<String>> getParamMessages() {
    return _paramMessages;
  }

  public boolean isEmpty() {
    return _messages.isEmpty() && _paramMessages.isEmpty();
  }
  
}
package org.gusdb.wdk.model.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors {

  private List<String> _messages = new ArrayList<>();
  private Map<String,List<String>> _paramMessages = new HashMap<>();
  
  public void addMessage(String message) {
    _messages.add(message);
  }
  
  public void addParamMessage(String paramName, String message) {
    List<String> messages = _paramMessages.get(paramName);
    if (messages == null) {
      messages = new ArrayList<String>();
      _paramMessages.put(paramName,  messages);
    }
    messages.add(message);
  }
  
  public List<String> getMessages() {
    return _messages;
  }
  
  public Map<String,List<String>> getParamMessages() {
    return _paramMessages;
  }

  public boolean isEmpty() {
    return _messages.isEmpty() && _paramMessages.isEmpty();
  }
  
}
