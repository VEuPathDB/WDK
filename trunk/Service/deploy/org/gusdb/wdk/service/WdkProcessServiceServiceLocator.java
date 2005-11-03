/**
 * WdkProcessServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.gusdb.wdk.service;

public class WdkProcessServiceServiceLocator extends org.apache.axis.client.Service implements org.gusdb.wdk.service.WdkProcessServiceService {

    public WdkProcessServiceServiceLocator() {
    }


    public WdkProcessServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WdkProcessServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WdkProcessService
    private java.lang.String WdkProcessService_address = "http://localhost:8080/axis/WdkProcessService";

    public java.lang.String getWdkProcessServiceAddress() {
        return WdkProcessService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WdkProcessServiceWSDDServiceName = "WdkProcessService";

    public java.lang.String getWdkProcessServiceWSDDServiceName() {
        return WdkProcessServiceWSDDServiceName;
    }

    public void setWdkProcessServiceWSDDServiceName(java.lang.String name) {
        WdkProcessServiceWSDDServiceName = name;
    }

    public org.gusdb.wdk.service.WdkProcessService getWdkProcessService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WdkProcessService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWdkProcessService(endpoint);
    }

    public org.gusdb.wdk.service.WdkProcessService getWdkProcessService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.gusdb.wdk.service.WdkProcessServiceSoapBindingStub _stub = new org.gusdb.wdk.service.WdkProcessServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getWdkProcessServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWdkProcessServiceEndpointAddress(java.lang.String address) {
        WdkProcessService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.gusdb.wdk.service.WdkProcessService.class.isAssignableFrom(serviceEndpointInterface)) {
                org.gusdb.wdk.service.WdkProcessServiceSoapBindingStub _stub = new org.gusdb.wdk.service.WdkProcessServiceSoapBindingStub(new java.net.URL(WdkProcessService_address), this);
                _stub.setPortName(getWdkProcessServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("WdkProcessService".equals(inputPortName)) {
            return getWdkProcessService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("service.wdk.gusdb.org", "WdkProcessServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("service.wdk.gusdb.org", "WdkProcessService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WdkProcessService".equals(portName)) {
            setWdkProcessServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
