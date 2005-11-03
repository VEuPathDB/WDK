/**
 * WdkProcessServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.gusdb.wdk.service;

public interface WdkProcessServiceService extends javax.xml.rpc.Service {
    public java.lang.String getWdkProcessServiceAddress();

    public org.gusdb.wdk.service.WdkProcessService getWdkProcessService() throws javax.xml.rpc.ServiceException;

    public org.gusdb.wdk.service.WdkProcessService getWdkProcessService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
