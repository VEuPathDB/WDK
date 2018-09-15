package org.gusdb.wdk.service.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface InSchema {
  /**
   * Dot notation path to the value to use for validation.
   *
   * <p>
   *   Paths are relative to the value source directory.
   * </p>
   *
   * <p>
   *   <pre>
   *     &#64;InSchema("users.idList.json")
   *   </pre>
   * </p>
   */
  String value();
}
