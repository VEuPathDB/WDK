package org.gusdb.wsf.client;

import java.net.URI;

public interface WsfClientFactory {

  WsfClient newClient(WsfResponseListener listener);

  WsfClient newClient(WsfResponseListener listener, URI serviceURI);
}
