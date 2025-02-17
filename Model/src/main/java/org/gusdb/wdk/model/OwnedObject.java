package org.gusdb.wdk.model;

import org.gusdb.oauth2.client.veupathdb.UserInfo;

public interface OwnedObject {

  UserInfo getOwningUser();

}
