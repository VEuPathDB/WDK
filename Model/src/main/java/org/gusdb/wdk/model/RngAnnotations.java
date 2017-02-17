package org.gusdb.wdk.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.FormatUtil;

public class RngAnnotations {

  /**
   * Applied to a setter of a field required by WDK model's RNG
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface RngRequired { }

  /**
   * Applied to a setter of a field declared optional by WDK model's RNG
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface RngOptional { }

  /**
   * Applied to a setter of a field undefined by WDK model's RNG
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface RngUndefined { }

  private static enum RngProperty {
    REQUIRED,
    OPTIONAL,
    UNDEFINED;
  }

  public static class FieldSetter {

    public final String underscoredName;
    public final Method setter;
    public final boolean isRngRequired;

    public FieldSetter(String underscoredName, Method setter, boolean isRngRequired) {
      this.underscoredName = underscoredName;
      this.setter = setter;
      this.isRngRequired = isRngRequired;
    }

    @Override
    public String toString() {
      return "{ " + underscoredName + ", " + setter.getName() + ", " + isRngRequired + " }";
    }
  }

  public static List<FieldSetter> getRngFields(Class<?> clazz) throws WdkModelException {
    List<FieldSetter> result = new ArrayList<>();
    Method[] allMethods = clazz.getMethods();
    for (Method method : allMethods) {
      if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
        RngProperty prop = determineIfRequired(clazz, method);
        if (!prop.equals(RngProperty.UNDEFINED)) {
          result.add(new FieldSetter(FormatUtil.toUnderscoreFormat(method.getName().substring(3)),
              method, prop.equals(RngProperty.REQUIRED)));
        }
      }
    }
    return result;
  }

  private static RngProperty determineIfRequired(Class<?> clazz, Method method) throws WdkModelException {
    String methodName = clazz.getName() + "." + method.getName();
    String duplicationMessage = methodName + "() has more than one RNG Requirement annotation.  There can be only one.";
    String missingMessage = methodName + " has no RNG Requirement annotations.  This class requires one on each single-argument setXxx method.";
    RngProperty prop = null;
    if (method.isAnnotationPresent(RngRequired.class)) {
      prop = RngProperty.REQUIRED;
    }
    if (method.isAnnotationPresent(RngOptional.class)) {
      if (prop != null) {
        throw new WdkModelException(duplicationMessage);
      }
      prop = RngProperty.OPTIONAL;
    }
    if (method.isAnnotationPresent(RngUndefined.class)) {
      if (prop != null) {
        throw new WdkModelException(duplicationMessage);
      }
      prop = RngProperty.UNDEFINED;
    }
    if (prop == null) {
      throw new WdkModelException(missingMessage);
    }
    return prop;
  }
}
