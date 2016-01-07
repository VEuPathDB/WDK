package org.gusdb.wdk.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.HttpMethod;

/**
 * Indicates that the annotated method responds to HTTP PATCH requests.  Needed
 * since Jersey does not include a native PATCH annotation.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("PATCH")
@Documented
public @interface PATCH {
}
