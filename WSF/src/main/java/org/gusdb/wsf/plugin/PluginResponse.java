package org.gusdb.wsf.plugin;


public interface PluginResponse {

  void addRow(String[] row) throws PluginModelException, PluginUserException;

  void addAttachment(String key, String content) throws PluginModelException, PluginUserException;

  void setMessage(String message) throws PluginModelException, PluginUserException;
}
