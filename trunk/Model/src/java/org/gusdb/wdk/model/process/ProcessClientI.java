/**
 * 
 */
package org.gusdb.wdk.model.process;

import java.rmi.RemoteException;

/**
 * @author Jerric
 * @created Nov 1, 2005
 */
public interface ProcessClientI {

    /**
     * invokes a process query, and returns the result in a 2D string array
     * 
     * @param params names of the parameters for the prcoess to be invoked. It
     *        should have the same size and the values array, and names are
     *        mapped with values one by one, in order. In BLAST process query, a
     *        parameter of name "Application" must be specified to tell the
     *        service which BLAST program to be excuted
     * @param values values of the parameters for the process to be invoked.
     *        They are mapped with parameter names one by one, in order. In
     *        BLAST process query, the value of parameter "Application" can be
     *        "blastn", "blastp", "blastx"
     * @param columns specify the names and the order of the columns in the
     *        result.
     * @return A 2D string array are returned. In BLAST process query, it has
     *         following columns: ID, header, footer, tabular row, alignment;
     *         only the first row has header filled, and only last row has
     *         footer filled. If the BLAST gets no hit, a single row will be
     *         returned, with header and footer fields, but other fields are
     *         empty.
     * @throws RemoteException
     */
    public String[][] invoke(String processName, String[] params, String[] values, String[] columns)
            throws RemoteException;
}
