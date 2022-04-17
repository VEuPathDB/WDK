package org.gusdb.wsf.client;



public interface WsfResponseListener {

  void onRowReceived(String[] row) throws ClientModelException, ClientUserException;
  
  void onAttachmentReceived(String key, String content) throws ClientModelException, ClientUserException;
  
  void onMessageReceived(String message) throws ClientModelException, ClientUserException;
}
