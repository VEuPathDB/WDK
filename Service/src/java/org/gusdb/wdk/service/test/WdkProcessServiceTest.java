/**
 * 
 */
package org.gusdb.wdk.service.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.gusdb.wdk.service.WdkProcessService;
import org.gusdb.wdk.service.WdkServiceException;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WdkProcessServiceTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Test method for 'org.gusdb.wdk.service.ProcessService.invoke(String[],
     * String[], String[])'
     */
    public void testInvoke() {
        // prepare parameters
	String processName = System.getProperty("process.name");
        if (processName == null) processName = "WuBlastProcessor";
	String db = System.getProperty("database.name");
        if (db == null) db = "Cparvum_nt.fsa"; 
        String[] params = { "Application", "Sequence", "Database" };
        String[] values = {
                "blastn",
                "AGCTTTTCATTCTGACTGCAACGGGCAATATGTCTCTGTGTGGATTAAAAAAAGAGTGTCTG"
                        + "ATAGCAGCTTCTGAACTGGTTACCTGCCGTGAGTAAATTAAAATTTTATTGA"
                        + "CTTAGGTCACTAAATACTTTAACCAATATAGGCATAGCGCACAGACAGATAA"
                        + "AAATTACAGAGTACACAACATCCATGAAACGCATTAGCACCACCATTACCAC"
                        + "CACCATCACCATTACCACAGGTAACGGTGCGGGCTGACGCGTACAGGAAACA"
                        + "CAGAAAAAAGCCCGCACCTGACAGTGCGGGCTTTTTTTTTCGACCAAAGGTA"
                        + "ACGAGGTAACAACCATGCGAGTGTTGAAGTTCGGCGGTACATCAGTGGCAAA"
                        + "TGCAGAACGTTTTCTGCGTGTTGCCGATATTCTGGAAAGCAATGCCAGGCAG"
                        + "GGGCAGGTGGCCACCGTCCTCTCTGCCCCCGCCAAAATCACCAACCACCTGG"
                        + "TGGCGATGATTGAAAAAACCATTAGCGGCCAGGATGCTTTACCCAATATCAG"
                        + "CGATGCCGAACGTATTTTTGCCGAACTTTT", db};
        // + "CGATGCCGAACGTATTTTTGCCGAACTTTT", "c.parvum.nt" };

        // prepare the columns
        String[] columns = { "Id", "TabularRow", "Alignment", "Header",
                "Footer" };
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], i);
        }

        WdkProcessService service = new WdkProcessService();
        try {
            String[][] result = service.invoke(processName, params, values,
                    columns);

            // print out the result
            System.out.println("");
            for (int i = 0; i < result.length; i++) {
                System.out.println("================ " + result[i][0]
                        + " ================");
                for (String col : columns) {
                    System.out.println("------------ " + col + " ------------");
                    System.out.println(result[i][map.get(col)]);
                }
                System.out.println();
            }
        } catch (WdkServiceException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

}
