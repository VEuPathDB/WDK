package org.gusdb.wdk.model.query.param;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;

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
    if (!(rawValue instanceof String[])) {
      throw new IllegalStateException("toStableValue() called with wrong type of Object");
    }
    String[] terms = (String[]) rawValue;
    Arrays.sort(terms);
    return new JSONArray(terms).toString();
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

    if (_param == null) return null;                 // emptyValue may be filled in
    if (_param.isEmptyValue(stableValue)) return ""; // no values; will be an empty in clause

    final var enumParam = (AbstractEnumParam) _param;
    final var cache = enumParam.getVocabInstance(ctxParamVals);

    var terms = AbstractEnumParam.convertToTerms(stableValue);
    var internals = new LinkedHashSet<String>();
    for (var term : terms) {

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
    List<String> terms = AbstractEnumParam.convertToTerms(ctxParamVals.get().get(_param.getName()));
    Collections.sort(terms);
    return EncryptionUtil.encrypt(Arrays.toString(terms.toArray()));
  }

  @Override
  public ParamHandler clone(Param param) {
    return new EnumParamHandler(this, param);
  }
}
