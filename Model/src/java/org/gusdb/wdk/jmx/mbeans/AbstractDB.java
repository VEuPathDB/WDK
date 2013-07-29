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
import javax.sql.DataSource;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.jmx.mbeans.dbms.AbstractDBInfo;
import org.gusdb.wdk.jmx.mbeans.dbms.DBInfoFactory;
import org.gusdb.wdk.model.WdkModel;

public abstract class AbstractDB extends BeanBase implements DynamicMBean {

  private HashMap<String, String> dbAttrs;

  HashMap<String, String> metaDataMap;
  HashMap<String, String> servernameDataMap;
  ArrayList<Map<String, String>> dblinkList;
  DatabaseInstance database;
  DataSource datasource;
  private static final String DBLINKLISTKEY = "DblinkList";

  /**
    ServletContext sc
                  
    platformName is derived from the WDK DBPlatform classname minus the package name. A corresponding utility
    DBInfo class is used to generate mbean attributes. For example, when WDK DBPlatform of class
    org.gusdb.wdk.model.dbms.Oracle is found a OracleDBinfo is needed to provide mbean attributes.
  */
  public AbstractDB() {
    super();
    database = getDb(wdkModel);
    datasource = database.getDataSource();
    init();
  }

  protected abstract DatabaseInstance getDb(WdkModel model);
  
  private void init() {
    dbAttrs = new HashMap<String, String>();
    dblinkList = new ArrayList<Map<String, String>>();
    SupportedPlatform platform = database.getConfig().getPlatformEnum();
    dbAttrs.put("platform", platform.name());
    
    try {
      AbstractDBInfo dbinfo = DBInfoFactory.getDbInfo(platform);
      dbinfo.setDatasource(datasource);
      dbinfo.populateDatabaseMetaDataMap(dbAttrs);
      dbinfo.populateServernameDataMap(dbAttrs);
      dbinfo.populateDblinkList(dblinkList);
    }
    catch (IllegalArgumentException e) {
      // No DBInfo implementation created yet for this vendor
      dbAttrs.put("WARN", "no MBean support for this database platform");
    }

  }

  @Override
  public AttributeList getAttributes(String[] names) {
      AttributeList list = new AttributeList();
      for (String name : names) {
         if (name.equals(DBLINKLISTKEY)) {
           list.add(new Attribute(name, dblinkList));
         } else {
           Object value = dbAttrs.get(name);
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
    if (dbAttrs.get(name) == null)
        throw new AttributeNotFoundException(name);
    Object value = attribute.getValue();
    if (!(value instanceof String)) {
        throw new InvalidAttributeValueException(
                "Attribute value not a string: " + value);
    }
    dbAttrs.put(name, (String) value);
  }

  @Override
  public Object getAttribute(String name) throws AttributeNotFoundException {
    if (name.equals(DBLINKLISTKEY)) {
        return dblinkList;
    }
    Object value = dbAttrs.get(name);
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
      if (dbAttrs.get(name) != null && value instanceof String) {
          dbAttrs.put(name, (String) value);
          retlist.add(new Attribute(name, value));
      }
    }
    return retlist;
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    ArrayList<String> names = new ArrayList<String>();

    for (Object name : dbAttrs.keySet()) {
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

  public ArrayList<Map<String,String>> getDblinkList() { return dblinkList; }

}
