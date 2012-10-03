package org.gusdb.wdk.controller.actionutil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ActionResult implements Iterable<String> {

  public enum ResultType {
    VIEW_NAME,
    VIEW_PATH,
    STREAM,
    EMPTY;
  }

  public static final ActionResult EMPTY_RESULT = getEmptyResult();
  
  private ResultType _type = ResultType.VIEW_NAME;
  private String _viewName = WdkAction.SUCCESS;
	private String _viewPath;
  private boolean _isRedirect = false;
  private String _fileName = "";
  private InputStream _stream;

  private Map<String, Object> _requestAttributes = new HashMap<>();
	
	public String getViewName() {
		return _viewName;
	}
	
	public ActionResult setViewName(String viewName) {
		_viewName = viewName;
		_type = ResultType.VIEW_NAME;
		return this;
	}

  public String getViewPath() {
    return _viewPath;
  }
  
  public ActionResult setViewPath(String viewPath) {
    _viewPath = viewPath;
    _type = ResultType.VIEW_PATH;
    return this;
  }
  
	public String getFileName() {
    return _fileName;
  }

  public ActionResult setFileName(String fileName) {
    _fileName = fileName;
    return this;
  }

  public InputStream getStream() {
    return _stream;
  }

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
	
	public boolean isStream() {
	  return getType().equals(ResultType.STREAM);
	}
	
	public boolean usesExplicitPath() {
		return getType().equals(ResultType.VIEW_PATH);
	}

  public boolean isEmptyResult() {
    return getType().equals(ResultType.EMPTY);
  }
  
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

  public static ActionResult getEmptyResult() {
    return new ActionResult() {

      @Override
      public ResultType getType() {
        return ResultType.EMPTY;
      }
      
      // disallow calls to any setters or getters
      private UnsupportedOperationException DISALLOWED = new UnsupportedOperationException(
          "Empty results contain no attributes or response information.");

      @Override public String getViewPath() { throw DISALLOWED; }
      @Override public String getViewName() { throw DISALLOWED; }
      @Override public String getFileName() { throw DISALLOWED; }
      @Override public InputStream getStream() { throw DISALLOWED; }
      @Override public Object getRequestAttribute(String name) { throw DISALLOWED; }
      @Override public boolean isRedirect() { throw DISALLOWED; }
      
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
