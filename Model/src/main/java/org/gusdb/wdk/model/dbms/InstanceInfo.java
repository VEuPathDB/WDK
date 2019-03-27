package org.gusdb.wdk.model.dbms;

import java.util.Date;
import java.util.Optional;

public class InstanceInfo {

  private final long _instanceId;
  private final String _tableName;
  private final String _queryName;
  private final String _checksum;
  private final Optional<String> _resultMessage;
  private final long _creationDate;

  public InstanceInfo(long instanceId, String tableName, String queryName, String checksum, Optional<String> resultMessage) {
    _instanceId = instanceId;
    _tableName = tableName;
    _queryName = queryName;
    _checksum = checksum;
    _resultMessage = resultMessage;
    _creationDate = new Date().getTime();
  }

  public long getInstanceId() { return _instanceId; }
  public String getTableName() { return _tableName; }
  public String getQueryName() { return _queryName; }
  public String getChecksum() { return _checksum; }
  public Optional<String> getResultMessage() { return _resultMessage; }

  /**
   * @return timestamp for when this Java object was created (NOT the instance in the DB)
   */
  public long getCreationDate() { return _creationDate; }

}
