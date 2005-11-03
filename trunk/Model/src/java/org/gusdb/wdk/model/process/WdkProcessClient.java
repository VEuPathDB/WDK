/**
 * 
 */
package org.gusdb.wdk.model.process;

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.gusdb.wdk.service.WdkProcessService;
import org.gusdb.wdk.service.WdkProcessServiceServiceLocator;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WdkProcessClient implements ProcessClientI {

    private WdkProcessService service;

    /**
     * @throws ServiceException
     * 
     */
    public WdkProcessClient(URL accessPoint) throws ServiceException {
        // create the service stub
        WdkProcessServiceServiceLocator locator = new WdkProcessServiceServiceLocator();
        service = locator.getWdkProcessService(accessPoint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.process.ProcessClientI#invoke(java.lang.String[],
     *      java.lang.String[], java.lang.String[])
     */
    public String[][] invoke(String[] params, String[] values, String[] columns)
            throws RemoteException {
        return service.invoke(params, values, columns);
    }

}
