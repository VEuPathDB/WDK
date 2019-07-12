package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author jerric
 */
public class EnumParamHandler extends AbstractParamHandler {

  public EnumParamHandler() {}

  public EnumParamHandler(EnumParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * the raw value is a String[] of terms, and stable value is a string representation of term list.
   */
  @Override
  public String toStableValue(User user, Object rawValue) {
    if (!(rawValue instanceof String[]))
      new Exception().printStackTrace();
    String[] terms = (String[]) rawValue;
    Arrays.sort(terms);
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (buffer.length() > 0)
        buffer.append(",");
      buffer.append(term);
    }
    return buffer.toString();
  }

  /**
   * the raw value is String[] of terms, and comma separated list of terms in string representation.
   */
  @Override
  public String[] toRawValue(User user, String stableValue) {
    if (stableValue == null)
      return null;
    String[] rawValue = stableValue.split(",+");
    for (int i = 0; i < rawValue.length; i++) {
      rawValue[i] = rawValue[i].trim();
    }
    return rawValue;
  }

  /**
   * return a string representation of a list of the internals. If noTranslation
   * is true, returns a string representation of a list of terms instead. If
   * quoted is true, each individual value will be quoted properly.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxParamVals)
      throws WdkModelException {
    final var stableValue = ctxParamVals.get().get(_param.getName());

    if (stableValue == null || stableValue.isEmpty() || _param.isNoTranslation()) {
      return stableValue;
    }

    final var enumParam = (AbstractEnumParam) _param;
    final var cache = enumParam.getVocabInstance(ctxParamVals);

    // TODO: This validation should be in the param, not the handler
    var terms = enumParam.convertToTerms(stableValue);
    var internals = new LinkedHashSet<String>();
    for (var term : terms) {
      if (!cache.containsTerm(term))
        throw new WdkModelException("The term '" + term + "' is invalid for param " + _param.getPrompt());

      var internal = (_param.isNoTranslation()) ? term : cache.getInternal(term);

      if (enumParam.getQuote() && !(internal.startsWith("'") && internal.endsWith("'")))
        internal = "'" + internal.replaceAll("'", "''") + "'";

      internals.add(internal);
    }
    return _wdkModel.getAppDb()
      .getPlatform()
      .prepareExpressionList(internals.toArray(new String[0]));
  }

  @Override
  public String toEmptyInternalValue() {
    return "''";
  }

  /**
   * the signature is a checksum of sorted stable value.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxParamVals) {

    AbstractEnumParam enumParam = (AbstractEnumParam) _param;
    // EnumParamCache cache = enumParam.getValueCache(user, contextParamValues);

    String[] terms = enumParam.convertToTerms(ctxParamVals.get().get(_param.getName()));
    // jerric - we should use terms to generate signature, not internal value. I don't remember
    // when and why we switched to internal values. I will revert it back to term.
    // Furthermore, I will skip validating the terms here, otherwise, it breaks the deep clone
    // of the steps, which prevents us from revising saved invalid strategies.

    Arrays.sort(terms);
    return EncryptionUtil.encrypt(Arrays.toString(terms));
  }

  @Override
  public ParamHandler clone(Param param) {
    return new EnumParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxParamVals)
      throws WdkModelException {
    AbstractEnumParam aeParam = (AbstractEnumParam) _param;
    Map<String, String> displays = aeParam.getDisplayMap(ctxParamVals.getUser(), ctxParamVals.toMap());
    String[] terms = toRawValue(ctxParamVals.getUser(), ctxParamVals.get(_param.getName()));
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (buffer.length() > 0) buffer.append(", ");
      buffer.append(displays.get(term));
    }
    return buffer.toString();
  }
}
