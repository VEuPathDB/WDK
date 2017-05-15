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

    private final String _underscoredName;
    private final Method _method;
    private final boolean _isRngRequired;

    public FieldSetter(String underscoredName, Method method, boolean isRngRequired) {
      _underscoredName = underscoredName;
      _method = method;
      _isRngRequired = isRngRequired;
    }

    public String getUnderscoredName() { return _underscoredName; }
    public Method getMethod() { return _method; }
    public boolean isRngRequired() { return _isRngRequired; }

    @Override
    public String toString() {
      return "{ " + _underscoredName + ", " + _method.getName() + ", " + _isRngRequired + " }";
    }
  }

  public static List<FieldSetter> getRngFields(Class<?> clazz) {
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

  private static RngProperty determineIfRequired(Class<?> clazz, Method method) {
    String methodName = clazz.getName() + "." + method.getName();
    String duplicationMessage = methodName + "() has more than one RNG Requirement annotation.  There can be only one.";
    String missingMessage = methodName + " has no RNG Requirement annotations.  This class requires one on each single-argument setXxx method.";
    RngProperty prop = null;
    if (method.isAnnotationPresent(RngRequired.class)) {
      prop = RngProperty.REQUIRED;
    }
    if (method.isAnnotationPresent(RngOptional.class)) {
      if (prop != null) {
        throw new WdkRuntimeException(duplicationMessage);
      }
      prop = RngProperty.OPTIONAL;
    }
    if (method.isAnnotationPresent(RngUndefined.class)) {
      if (prop != null) {
        throw new WdkRuntimeException(duplicationMessage);
      }
      prop = RngProperty.UNDEFINED;
    }
    if (prop == null) {
      throw new WdkRuntimeException(missingMessage);
    }
    return prop;
  }
}
