package org.gusdb.wdk.jmx.mbeans;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;

import java.util.Iterator;
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

import org.gusdb.wdk.jmx.mbeans.dbms.AbstractDBInfo;
import org.gusdb.wdk.jmx.mbeans.dbms.OracleDBInfo;

public abstract class AbstractDB extends BeanBase implements DynamicMBean {

  private HashMap<String, String> dbAttrs;

  HashMap<String, String> metaDataMap;
  HashMap<String, String> servernameDataMap;
  ArrayList<Map<String, String>> dblinkList;
  DataSource datasource;
  DBPlatform platform;
  private static final String DBLINKLISTKEY = "DblinkList";
  private static final Logger logger = Logger.getLogger(AbstractDB.class);

  /**
    ServletContext sc
    String type - QueryPlatform or UserPlatform, to match the getter method
                  in WdkModel.
                  
    platformName is derived from the WDK DBPlatform classname minus the package name. A corresponding utility
    DBInfo class is used to generate mbean attributes. For example, when WDK DBPlatform of class
    org.gusdb.wdk.model.dbms.Oracle is found a OracleDBinfo is needed to provide mbean attributes.
  **/
  public AbstractDB(String type) {
    super();
    platform = getPlatform(type);
    datasource = platform.getDataSource();
    init();
  }

  private void init() {
    String platformName = platform.getClass().getSimpleName();
    dbAttrs = new HashMap<String, String>();
    dblinkList = new ArrayList<Map<String, String>>();
    dbAttrs.put("platform", platformName);
    
    AbstractDBInfo dbinfo = initDbInfoClass(platformName);
    if (dbinfo == null) {
        dbAttrs.put("WARN", "no MBean support for this database platform");
        return;
    }

    dbinfo.setDatasource(datasource);
    dbinfo.populateDatabaseMetaDataMap(dbAttrs);
    dbinfo.populateServernameDataMap(dbAttrs);
    dbinfo.populateDblinkList(dblinkList);
  }

  /**
    initialize a suitable DBInfo class for the given platformName (e.g. Oracle or PostgreSQL)
  */
  private AbstractDBInfo initDbInfoClass(String platformName) {
    AbstractDBInfo dbinfo = null;
    try {
     dbinfo = (AbstractDBInfo) Class.forName("org.gusdb.wdk.jmx.mbeans.dbms." + platformName + "DBInfo").newInstance();
    } catch (ClassNotFoundException cfe) {
        logger.warn("Class org.gusdb.wdk.jmx.mbeans.dbms." + platformName + "DBInfo expected but not found.");
    } catch (InstantiationException ie) {
        logger.error("InstantiationException " + ie);
    } catch (IllegalAccessException iae) {
        logger.error("IllegalAccessException " + iae);
    } catch (Exception e) {
        logger.error("Exception " + e);
    }
    return dbinfo;
  }

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

 
  private DBPlatform getPlatform(String type) {
    java.lang.reflect.Method method = null;
    DBPlatform platform = null;
    String methodName = "get" + type;
    try {
      method = wdkModel.getClass().getMethod(methodName);
    } catch (SecurityException se) {
      logger.error(se);
    } catch (NoSuchMethodException nsme) {
      logger.error(nsme);
    }

    try {
      platform = (DBPlatform)method.invoke(wdkModel);
    } catch (IllegalArgumentException iae) {
      logger.error(iae);
    } catch (IllegalAccessException iae) {
      logger.error(iae);
    } catch (InvocationTargetException ite) {
      logger.error(ite);
    }
    
    return platform;
  }


}
