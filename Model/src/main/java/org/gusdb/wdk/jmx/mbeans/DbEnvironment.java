package org.gusdb.wdk.jmx.mbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.jmx.BeanBase;
import org.gusdb.wdk.jmx.mbeans.dbms.DbInfo;
import org.gusdb.wdk.jmx.mbeans.dbms.DbInfoFactory;

public class DbEnvironment extends BeanBase implements DynamicMBean {

  private static final String DBLINKLISTKEY = "DblinkList";

  private DatabaseInstance _database;
  private HashMap<String, String> _dbAttrs;
  private ArrayList<Map<String, String>> _dblinkList;

  /**
    ServletContext sc
                  
    platformName is derived from the WDK DBPlatform classname minus the package name. A corresponding utility
    DBInfo class is used to generate mbean attributes. For example, when WDK DBPlatform of class
    org.gusdb.wdk.model.dbms.Oracle is found a OracleDBinfo is needed to provide mbean attributes.
  */
  public DbEnvironment(DatabaseInstance database) {
    _database = database;
    //_dataSource = database.getDataSource();
    init();
  }

  private void init() {
    _dbAttrs = new HashMap<String, String>();
    _dblinkList = new ArrayList<Map<String, String>>();
    SupportedPlatform platform = _database.getConfig().getPlatformEnum();
    _dbAttrs.put("platform", platform.name());
    
    try {
      DbInfo dbinfo = DbInfoFactory.getDbInfo(_database);
      dbinfo.populateDatabaseMetaDataMap(_dbAttrs);
      dbinfo.populateServernameDataMap(_dbAttrs);
      dbinfo.populateConnectionPoolDataMap(_dbAttrs);
      dbinfo.populateDblinkList(_dblinkList);
    }
    catch (IllegalArgumentException e) {
      // No DBInfo implementation created yet for this vendor
      _dbAttrs.put("WARN", "no MBean support for this database platform");
    }

  }

  @Override
  public AttributeList getAttributes(String[] names) {
      AttributeList list = new AttributeList();
      for (String name : names) {
         if (name.equals(DBLINKLISTKEY)) {
           list.add(new Attribute(name, _dblinkList));
         } else {
           Object value = _dbAttrs.get(name);
           if (value != null)
              list.add(new Attribute(name, value));
        }
      }
      return list;
  }

  @Override
  public void setAttribute(Attribute attribute) 
  throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
    String name = attribute.getName();
    if (_dbAttrs.get(name) == null)
        throw new AttributeNotFoundException(name);
    Object value = attribute.getValue();
    if (!(value instanceof String)) {
        throw new InvalidAttributeValueException(
                "Attribute value not a string: " + value);
    }
    _dbAttrs.put(name, (String) value);
  }

  @Override
  public Object getAttribute(String name) throws AttributeNotFoundException {
    if (name.equals(DBLINKLISTKEY)) {
        return _dblinkList;
    }
    Object value = _dbAttrs.get(name);
    if (value != null)
      return value;
    else
      throw new AttributeNotFoundException("No such attribute: " + name);
  }

  @Override
  public AttributeList setAttributes(AttributeList list) {
    AttributeList retlist = new AttributeList();
    Iterator<?> itr = list.iterator();
    while( itr.hasNext() ) {
      Attribute attr = (Attribute)itr.next();
      String name = attr.getName();
      Object value = attr.getValue();
      if (_dbAttrs.get(name) != null && value instanceof String) {
          _dbAttrs.put(name, (String) value);
          retlist.add(new Attribute(name, value));
      }
    }
    return retlist;
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    ArrayList<String> names = new ArrayList<String>();

    for (Object name : _dbAttrs.keySet()) {
      names.add((String) name);
    }
    names.add(DBLINKLISTKEY);
    
    MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[names.size()];
    Iterator<String> it = names.iterator();
    for (int i = 0; i < attrs.length; i++) {
      String name = it.next();
      attrs[i] = new MBeanAttributeInfo(
              name,
              "java.lang.String",
              name,
              true,    // isReadable
              false,   // isWritable
              false);  // isIs
    }

    MBeanOperationInfo[] opers = {
      new MBeanOperationInfo(
              "reload",
              "Reload attributes",
              null,
              "void",
              MBeanOperationInfo.ACTION)
    };

    return new MBeanInfo(
            this.getClass().getName(),
            "Database MetaInfo MBean",
            attrs,
            null,   // constructors
            opers,  // operators
            null);  // notifications
  }

  @Override
  public Object invoke(String name, Object[] args, String[] sig) 
  throws MBeanException, ReflectionException {
    if (name.equals("reload") &&
            (args == null || args.length == 0) &&
            (sig == null || sig.length == 0)) {
        try {
          init();
          return null;
        } catch (Exception e) {
          throw new MBeanException(e);
        }
    }
    throw new ReflectionException(new NoSuchMethodException(name));
  }

  public void refresh() { init(); }

  public ArrayList<Map<String,String>> getDblinkList() { return _dblinkList; }

}
