package org.gusdb.wdk.service.statustype;

import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.core.Response.StatusType;

/**
 * Extending StatusType to accommodate the http status code of 422.  This is
 * considered a more appropriate choice for data validation issues that have to be
 * handled on the server side than 400.
 * @author crisl-adm
 *
 */
public class UnprocessableEntityStatusType implements StatusType {
  
  /** Get the class of the status code  */
  @Override
  public Family getFamily() {
    return Family.CLIENT_ERROR;
  }

  /** Get the short description for the status code */
  @Override
  public String getReasonPhrase() {
    return "Unprocessable Entity";
  }

  /** Get the status code */
  @Override
  public int getStatusCode() {
    return 422;
  }

}
