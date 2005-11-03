/**
 * 
 */
package test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.gusdb.wdk.service.IProcessor;
import org.gusdb.wdk.service.WdkServiceException;

import blast.ncbi.NcbiProcessor;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class NcbiProcessorTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Test method for 'NcbiProcessor.invoke(String[], String[], String[])'
     */
    public void testInvoke() {
        // prepare parameters
        String[] params = { NcbiProcessor.PARAM_APPLICATION,
                NcbiProcessor.PARAM_SEQUENCE, "-d" };
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
                        + "CGATGCCGAACGTATTTTTGCCGAACTTTT", "c.parvum.nt" };

        // // no hits
        // String[] values = { "blastn", "QQQQQ", "c.parvum.nt" };

        // prepare the columns
        String[] columns = { NcbiProcessor.COLUMN_ID, NcbiProcessor.COLUMN_ROW,
                NcbiProcessor.COLUMN_BLOCK, NcbiProcessor.COLUMN_HEADER,
                NcbiProcessor.COLUMN_FOOTER };
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], i);
        }

        // invoke the blast process
        IProcessor processor = new NcbiProcessor();
        try {
            String[][] result = processor.invoke(params, values, columns);

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
