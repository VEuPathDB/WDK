package org.gusdb.wdk.model;

import org.gusdb.wdk.model.RngAnnotations.FieldSetter;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.junit.Test;

public class RngAnnotationTest {

  @Test
  public void testRngAnnotations() {
    dumpRngFields(Column.class);
    dumpRngFields(QueryColumnAttributeField.class);
  }

  private void dumpRngFields(Class<?> clazz) {
    System.out.println(clazz.getSimpleName() + ":");
    for (FieldSetter fs : RngAnnotations.getRngFields(clazz)) {
      System.out.println(fs);
    }
  }
}
