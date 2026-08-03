package org.gusdb.wdk.model;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.gusdb.fgputil.functional.FunctionalInterfaces.ConsumerWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Procedure;
import org.gusdb.fgputil.functional.FunctionalInterfaces.SupplierWithException;

/**
 * This exception should be thrown out if the cause is not related to user's
 * input. For example, the cause of the exception can be a mistake in the model
 * file, the database resource is unavailable, etc.
 * 
 * @author jerric
 * 
 */
public class WdkModelException extends WdkException {

  private static final long serialVersionUID = 877548355767390313L;

  public WdkModelException() {
    super();
  }

  public WdkModelException(String msg) {
    super(msg);
  }

  public WdkModelException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public WdkModelException(Throwable cause) {
    super(cause);
  }

  public static WdkModelException translateFrom(Exception e) {
    return translateFrom(e, e.getMessage());
  }

  public static WdkModelException translateFrom(Exception e, String newMessage) {
    // if passed message is null, use name of original exception
    if (newMessage == null) newMessage = e.getClass().getSimpleName();
    // if exception is already a WdkModelException, simply return
    if (e instanceof WdkModelException) return (WdkModelException)e;
    // check underlying exception; a WdkModelException may simply have been wrapped
    Throwable t = (e.getCause() != null ? e.getCause() : e);
    // if underlying exception is WdkModelException then return, else wrap
    return (t instanceof WdkModelException ? (WdkModelException)t : new WdkModelException(newMessage, t));
  }

  /**
   * Meant to be used with the Supplier version of unwrap().  This function will
   * take a SupplierWithException, catch the exception and wrap it in a Runtime
   * Exception, returning a Supplier appropriate for "naked" lambdas.
   *
   * @param <T> type returned by the returned supplier
   * @param supplier supplier with exception
   * @return supplier without exception (wrapped in runtime exception)
   */
  public static <T> T wrap(SupplierWithException<T> supplier) {
    try {
      return supplier.get();
    }
    catch (Exception e) {
      throw (e instanceof RuntimeException) ? (RuntimeException)e : new WdkRuntimeException(e);
    }
  }

  /**
   * Meant to be used with wrap().  This function takes a non-throwing Supplier,
   * calls it, catches any RuntimeExceptions, and throws a WdkModelException
   * that is either 1) the underlying cause, if the cause is a WdkModelException,
   * or 2) a new WdkModelException wrapping the exception.
   *
   * @param <T> type returned by the supplier
   * @param supplier a supplier
   * @return the supplied value
   * @throws WdkModelException if an exception occurs
   */
  public static <T> T unwrap(Supplier<T> supplier) throws WdkModelException {
    try {
      return supplier.get();
    }
    catch(Exception e) {
      return unwrap(e);
    }
  }

  public static <T> Consumer<T> wrap(ConsumerWithException<T> consumer) {
    return val -> {
      try {
        consumer.accept(val);
      }
      catch (Exception e) {
        throw (e instanceof RuntimeException) ? (RuntimeException)e : new WdkRuntimeException(e);
      }
    };
  }

  public static <T> void unwrap(Procedure p) throws WdkModelException {
    try {
      p.perform();
    }
    catch (Exception e) {
      unwrap(e);
    }
  }

  public static <T> T unwrap(Exception e) throws WdkModelException {
    throw translateFrom(e, e.getMessage());
  }

  public static <T> T unwrap(Exception e, String newMessage) throws WdkModelException {
    throw translateFrom(e, newMessage);
  }

}
