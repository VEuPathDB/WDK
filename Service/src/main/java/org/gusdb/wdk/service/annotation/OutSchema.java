package org.gusdb.wdk.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OutSchema {
  /**
   * Dot notation path to the value to use for validation.
   *
   * <p>
   *   Paths are relative to the value source directory.
   * </p>
   *
   * <pre>
   *   &#64;InSchema("users.idList.json")
   * </pre>
   */
  String value();
}
