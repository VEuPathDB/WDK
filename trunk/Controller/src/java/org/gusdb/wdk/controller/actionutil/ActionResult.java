package org.gusdb.wdk.controller.actionutil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains all the information needed to process the result of an Action.  This
 * includes whether the result is an html page, stream, or other type of data,
 * whether to look up a response name in the MVC container or use an explicit
 * path, and whether or not to use redirection.
 * 
 * @author rdoherty
 */
public class ActionResult implements Iterable<String> {

  /**
   * Defines ways the container should handle this result.
   * 
   * @author rdoherty
   */
  public enum ResultType {
    /** Look up and use the name of an action in the MVC container */
    VIEW_NAME,
    /** Use an explicit path to a JSP */
    VIEW_PATH,
    /** Stream a result to the user */
    STREAM,
    /** Return an empty result */
    EMPTY;
  }

  /**
   * Defines an empty result.  This value should be used if caller wants to
   * return an empty result; caller should not try to define his own empty result.
   */
  public static final ActionResult EMPTY_RESULT = getEmptyResult();
  
  private ResponseType _responseType;
  private ResultType _type = ResultType.VIEW_NAME;
  private String _viewName = WdkAction.SUCCESS;
	private String _viewPath;
  private boolean _isRedirect = false;
  private String _fileName = "";
  private InputStream _stream;

  private Map<String, Object> _requestAttributes = new HashMap<>();

  /**
   * Empty constructor.  Defaults to html as the response type.  The default
   * result type is VIEW_NAME, and the default name is SUCCESS.
   */
  public ActionResult() {
    this(ResponseType.html);
  }

  /**
   * Creates a result with the given response type.  The default
   * result type is VIEW_NAME, and the default name is SUCCESS.
   * 
   * @param responseType response type for this result
   */
  public ActionResult(ResponseType responseType) {
    _responseType = responseType;
  }
  
  public ResponseType getResponseType() {
    return _responseType;
  }

  public ActionResult setResponseType(ResponseType responseType) {
    _responseType = responseType;
    return this;
  }
  
  public String getViewName() {
		return _viewName;
	}

  /**
   * Sets the view name, and sets result type to VIEW_NAME.
   * 
   * @param viewName name of the view in the MVC container
   * @return this object
   */
	public ActionResult setViewName(String viewName) {
		_viewName = viewName;
		_type = ResultType.VIEW_NAME;
		return this;
	}

  public String getViewPath() {
    return _viewPath;
  }
  
  /**
   * Sets the view path, and sets result type to VIEW_PATH.
   * 
   * @param viewPath explicit path to a JSP
   * @return this object
   */
  public ActionResult setViewPath(String viewPath) {
    _viewPath = viewPath;
    _type = ResultType.VIEW_PATH;
    return this;
  }
  
	public String getFileName() {
    return _fileName;
  }

	/**
	 * Sets the file name of the response.  This is useful for the streaming
	 * result type.
	 * 
	 * @param fileName name of the file to return in the HTTP response header
	 * @return this object
	 */
  public ActionResult setFileName(String fileName) {
    _fileName = fileName;
    return this;
  }

  public InputStream getStream() {
    return _stream;
  }

  /**
   * Sets the input stream for the response and sets result type to STREAM.  The
   * stream should already be open and ready to read.  Responsibility for
   * closing the stream lies with WdkAction.
   * 
   * @param stream open stream which can be passed to the HTTP requester
   * @return this object
   */
  public ActionResult setStream(InputStream stream) {
    _stream = stream;
    _type = ResultType.STREAM;
    return this;
  }
  
	public boolean isRedirect() {
		return _isRedirect;
	}
	
	public ActionResult setRedirect(boolean isRedirect) {
		_isRedirect = isRedirect;
		return this;
	}
	
	public ResultType getType() {
	  return _type;
	}

  /**
   * @return true if result type is STREAM, otherwise false
   */
	public boolean isStream() {
	  return getType().equals(ResultType.STREAM);
	}
	
	/**
	 * @return true if result type is VIEW_PATH, otherwise false
	 */
	public boolean usesExplicitPath() {
		return getType().equals(ResultType.VIEW_PATH);
	}

  /**
   * @return true if result type is EMPTY, otherwise false
   */
  public boolean isEmptyResult() {
    return getType().equals(ResultType.EMPTY);
  }
  
  /**
   * Adds an attribute to be appended to the request.  These values will be
   * accessible to the JSP via the requestScope
   * 
   * @param key key used to access the attribute
   * @param value attribute value
   * @return
   */
	public ActionResult setRequestAttribute(String key, Object value) {
		_requestAttributes.put(key, value);
		return this;
	}

  public Object getRequestAttribute(String name) {
    return _requestAttributes.get(name);
  }
  
	@Override
	public Iterator<String> iterator() {
		return _requestAttributes.keySet().iterator();
	}

  private static ActionResult getEmptyResult() {
    return new ActionResult() {

      @Override
      public ResultType getType() {
        return ResultType.EMPTY;
      }
      
      // disallow calls to any setters or getters
      private UnsupportedOperationException DISALLOWED = new UnsupportedOperationException(
          "Empty results contain no attributes or response information.");

      @Override public ResponseType getResponseType() { throw DISALLOWED; }
      @Override public String getViewPath() { throw DISALLOWED; }
      @Override public String getViewName() { throw DISALLOWED; }
      @Override public String getFileName() { throw DISALLOWED; }
      @Override public InputStream getStream() { throw DISALLOWED; }
      @Override public Object getRequestAttribute(String name) { throw DISALLOWED; }
      @Override public boolean isRedirect() { throw DISALLOWED; }
      
      @Override public ActionResult setResponseType(ResponseType responseType) { throw DISALLOWED; }
      @Override public ActionResult setViewPath(String viewPath) { throw DISALLOWED; }
      @Override public ActionResult setViewName(String viewName) { throw DISALLOWED; }
      @Override public ActionResult setFileName(String fileName) { throw DISALLOWED; }
      @Override public ActionResult setStream(InputStream stream) { throw DISALLOWED; }
      @Override public ActionResult setRequestAttribute(String key, Object value) { throw DISALLOWED; }
      @Override public ActionResult setRedirect(boolean isRedirect) { throw DISALLOWED; }

      @Override public Iterator<String> iterator() { throw DISALLOWED; }
    };
  }
}
