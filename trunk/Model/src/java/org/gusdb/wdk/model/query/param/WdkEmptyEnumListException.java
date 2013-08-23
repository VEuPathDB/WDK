package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelException;

/**
 * This exception occurs when the AbstractEnumParam loaded an empty list of
 * param values. Some of the causes of this exception are incorrect use of
 * include/exclude projects in the model, the param query doesn't return any
 * thing, or the depended value of a param yields an empty list of param values.
 * 
 * @author xingao
 */
public class WdkEmptyEnumListException extends WdkModelException {

  private static final long serialVersionUID = -4587879598508535198L;

  public WdkEmptyEnumListException() {
	  super();
  }

  public WdkEmptyEnumListException(String msg) {
    super(msg);
  }

  public WdkEmptyEnumListException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public WdkEmptyEnumListException(Throwable cause) {
    super(cause);
  }

}
