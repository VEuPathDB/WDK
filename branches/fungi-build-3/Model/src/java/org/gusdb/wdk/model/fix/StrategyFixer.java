/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wsf.util.BaseCLI;

/**
 * @author xingao
 * 
 *         this code generates the signature for the strategy (the old system
 *         doesn't create the signature automatically, but the new one does.
 *         Therefore, this code is considered deprecated.
 * 
 */
@Deprecated
public class StrategyFixer extends BaseCLI {

    private static final Logger logger = Logger.getLogger(StrategyFixer.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        StrategyFixer fixer = new StrategyFixer(cmdName);
        try {
            fixer.invoke(args);
        } finally {
            logger.info("strategy fixer finished.");
            System.exit(0);
        }
    }

    /**
     * @param command
     * @param description
     */
    protected StrategyFixer(String command) {
        super((command == null) ? command : "strategyFixer",
                "generate unique strategy signature");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
     */
    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_PROJECT_ID, true, null, "A comma-separated"
                + " list of ProjectIds, which should match the directory name "
                + "under $GUS_HOME, where model-config.xml is stored.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#execute()
     */
    @Override
    protected void execute() throws Exception {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        String strProject = (String) getOptionValue(ARG_PROJECT_ID);
        String[] projects = strProject.split(",");
        for (String projectId : projects) {
            logger.info("Fixng strategies for project " + projectId);
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            generateSignatures(wdkModel);
        }
    }

    private void generateSignatures(WdkModel wdkModel) throws SQLException,
            NoSuchAlgorithmException, WdkModelException {
        String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();

        StringBuffer sqlSelect = new StringBuffer(
                "SELECT strategy_id, user_id ");
        sqlSelect.append("FROM ").append(schema).append("strategies ");
        sqlSelect.append("WHERE signature IS NULL AND project_id = ?");

        StringBuffer sqlUpdate = new StringBuffer("UPDATE ");
        sqlUpdate.append(schema).append("strategies SET signature = ? ");
        sqlUpdate.append("WHERE strategy_id = ?");

        StepFactory factory = wdkModel.getStepFactory();
        ResultSet resultSet = null;
        PreparedStatement psSelect = null, psUpdate = null;
        DataSource src = wdkModel.getUserPlatform().getDataSource();
        try {
            psSelect = SqlUtils.getPreparedStatement(src, sqlSelect.toString());
            psUpdate = SqlUtils.getPreparedStatement(src, sqlUpdate.toString());

            psSelect.setString(1, wdkModel.getProjectId());
            resultSet = psSelect.executeQuery();
            while (resultSet.next()) {
                int strategyId = resultSet.getInt("strategy_id");
                int userId = resultSet.getInt("user_id");
                String sig = factory.getStrategySignature(userId, strategyId);

                psUpdate.setString(1, sig);
                psUpdate.setInt(2, strategyId);
                psUpdate.executeUpdate();
            }
        } finally {
            SqlUtils.closeStatement(psUpdate);
            SqlUtils.closeStatement(psSelect);
            SqlUtils.closeResultSet(resultSet);
        }
    }
}
