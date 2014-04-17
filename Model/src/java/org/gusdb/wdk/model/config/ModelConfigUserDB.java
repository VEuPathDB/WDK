package org.gusdb.wdk.model.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * An object representation of the {@code <userDB>} tag in the {@code model-config.xml}. Two schema are used
 * for host wdk tables, and the {@link ModelConfigUserDB#userSchema} has the tables used to user specific
 * data, while the {@link ModelConfigUserDB#wdkEngineSchema} has the tables used to store the data shared
 * between users.
 * 
 * @author xingao
 * 
 */
public class ModelConfigUserDB extends ModelConfigDB {

  private static final String CONFIG_USER_SCHEMA_VERSION = "wdk.user.schema.version";

  private String userSchema;

  /**
   * @return the userSchema
   */
  public String getUserSchema() {
    return userSchema;
  }

  /**
   * @param userSchema
   *          the userSchema to set
   */
  public void setUserSchema(String userSchema) {
    this.userSchema = DBPlatform.normalizeSchema(userSchema);
  }

  public void checkSchema(WdkModel wdkModel) throws WdkModelException {
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    ResultSet resultSet = null;
    try {
      PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource, "SELECT " + CONFIF_VALUE_COLUMN +
          " FROM " + userSchema + CONFIG_TABLE + " WHERE " + CONFIG_NAME_COLUMN + "= ?");
      ps.setString(1, CONFIG_USER_SCHEMA_VERSION);
      resultSet = ps.executeQuery();
      if (!resultSet.next())
        throw new WdkModelException("Unable to validate the version of WDK user schema. Please make sure "
            + "the user schema is correct installed with the latest WDK schema script. ");
      String version = resultSet.getString(CONFIF_VALUE_COLUMN);
      if (!WdkModel.USER_SCHEMA_VERSION.equals(version))
        throw new WdkModelException("The version of WDK user schema is not compatible with code base. The " +
            "current WDK code base requires user schema of version '" + WdkModel.USER_SCHEMA_VERSION +
            "', while the database has version '" + version + "'.");
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }
}
