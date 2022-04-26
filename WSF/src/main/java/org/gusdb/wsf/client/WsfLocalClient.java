package org.gusdb.wsf.client;

import org.gusdb.wsf.plugin.DelayedResultException;
import org.gusdb.wsf.plugin.PluginExecutor;
import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginResponse;
import org.gusdb.wsf.plugin.PluginUserException;

/**
 * @author Jerric
 */
public class WsfLocalClient implements WsfClient, PluginResponse {

  private WsfResponseListener listener;

  protected WsfLocalClient() {}

  @Override
  public void addRow(String[] row) throws PluginModelException, PluginUserException {
    try {
      listener.onRowReceived(row);
    }
    catch (ClientModelException ex) {
      throw new PluginModelException(ex);
    }
    catch (ClientUserException ex) {
      throw new PluginUserException(ex);
    }
  }

  @Override
  public void addAttachment(String key, String attachment) throws PluginModelException, PluginUserException {
    try {
      listener.onAttachmentReceived(key, attachment);
    }
    catch (ClientModelException ex) {
      throw new PluginModelException(ex);
    }
    catch (ClientUserException ex) {
      throw new PluginUserException(ex);
    }
  }

  @Override
  public void setMessage(String message) throws PluginModelException, PluginUserException {
    try {
      listener.onMessageReceived(message);
    }
    catch (ClientModelException ex) {
      throw new PluginModelException(ex);
    }
    catch (ClientUserException ex) {
      throw new PluginUserException(ex);
    }
  }

  @Override
  public void setResponseListener(WsfResponseListener listener) {
    this.listener = listener;
  }

  @Override
  public int invoke(ClientRequest request) throws ClientModelException, ClientUserException, DelayedResultException {
    PluginExecutor executor = new PluginExecutor();
    String pluginClassName = request.getPluginClass();
    try {
      return executor.execute(pluginClassName, request, this);
    }
    catch (PluginModelException ex) {
      throw new ClientModelException(ex);
    }
    catch (PluginUserException ex) {
      throw new ClientUserException(ex);
    }
  }
}
