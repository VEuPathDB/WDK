package org.gusdb.wdk.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.web.ApplicationContext;

public class ServletApplicationContext implements ApplicationContext {

  private final ServletContext _servletContext;

  public ServletApplicationContext(ServletContext servletContext) {
    _servletContext = servletContext;
  }

  @Override
  public String getInitParameter(String key) {
    return _servletContext.getInitParameter(key);
  }

  @Override
  public String getRealPath(String path) {
    return _servletContext.getRealPath(path);
  }

  @Override
  public int size() {
    return keySet().size();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return keySet().contains(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  @Override
  public Object get(Object key) {
    return (key instanceof String) ?
      _servletContext.getAttribute((String)key) : null;
  }

  @Override
  public Object put(String key, Object value) {
    Object o = _servletContext.getAttribute(key);
    _servletContext.setAttribute(key, value);
    return o;
  }

  @Override
  public Object remove(Object key) {
    Object o = get(key);
    if (o == null) return null;
    _servletContext.removeAttribute((String)key);
    return o;
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    for (Entry<? extends String, ? extends Object> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    for (String key : keySet()) {
      _servletContext.removeAttribute(key);
    }
  }

  @Override
  public Set<String> keySet() {
    Enumeration<String> names = _servletContext.getAttributeNames();
    Set<String> keys = new HashSet<>();
    while (names.hasMoreElements()) {
      keys.add(names.nextElement());
    }
    return keys;
  }

  @Override
  public Collection<Object> values() {
    return keySet().stream().map(key -> get(key)).collect(Collectors.toList());
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return keySet().stream().map(key -> new TwoTuple<>(key, get(key))).collect(Collectors.toSet());
  }

  @Override
  public void close() throws IOException {
    // by default, do nothing
  }

}
