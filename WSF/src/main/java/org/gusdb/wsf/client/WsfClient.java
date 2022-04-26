package org.gusdb.wsf.client;

import org.gusdb.wsf.plugin.DelayedResultException;

public interface WsfClient {
  
  void setResponseListener(WsfResponseListener listener);

  int invoke(ClientRequest request) throws ClientModelException, ClientUserException, DelayedResultException;

}
