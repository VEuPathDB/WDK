package org.gusdb.wdk.model.query;

import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelText;

public class PostCacheUpdateSql extends WdkModelBase {
	
	private WdkModelText sql;

	public String getSql() {
		return sql.getText();
	}
	
	public void setSql(WdkModelText sql) {
		this.sql = sql;
	}
}
