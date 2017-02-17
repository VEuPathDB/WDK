package org.gusdb.wdk.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.FormatUtil;

public class RngAnnotations {

  /**
   * Applied to a setter of a field required by WDK model's RNG
   */
  public static @interface RngRequired { }

  /**
   * Applied to a setter of a field declared optional by WDK model's RNG
   */
  public static @interface RngOptional { }

  /**
   * Applied to a setter of a field undefined by WDK model's RNG
   */
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
  }

  public static List<FieldSetter> getRngFields(Class<?> clazz) throws WdkModelException {
    List<FieldSetter> result = new ArrayList<>();
    Method[] allMethods = clazz.getMethods();
    for (Method method : allMethods) {
      if (method.getName().startsWith("set")) {
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
    RngProperty prop = null;
    for (Annotation annotation : method.getAnnotations()) {
      RngProperty thisProp = null;
      if (annotation.annotationType().equals(RngRequired.class)) thisProp = RngProperty.REQUIRED;
      if (annotation.annotationType().equals(RngOptional.class)) thisProp = RngProperty.OPTIONAL;
      if (annotation.annotationType().equals(RngUndefined.class)) thisProp = RngProperty.UNDEFINED;
      if (thisProp != null) {
        // annotation represents one of our RNG annotations
        if (prop == null) {
          prop = thisProp;
        }
        else {
          throw new WdkModelException(methodName + " has more than one RNG Requirement annotation.  There can be only one.");
        }
      }
    }
    if (prop == null) {
      throw new WdkModelException(methodName + " has no RNG Requirement annotations.  This class requires one on each setXxx method.");
    }
    return prop;
  }
}
