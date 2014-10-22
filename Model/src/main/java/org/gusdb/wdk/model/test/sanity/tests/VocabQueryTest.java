package org.gusdb.wdk.model.test.sanity.tests;

import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.user.User;

public class VocabQueryTest extends QueryTest {

  public VocabQueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet) {
    super(user, querySet, query, paramValuesSet);
  }
}
