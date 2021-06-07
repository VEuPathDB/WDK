package org.gusdb.wdk.model.user.dataset.event.raw;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A collection of {@link UDEvent} deserialization methods.
 */
public final class EventParser
{
  /**
   * Parses the given JSON string into a list of raw {@link UDEvent} instances.
   *
   * @param json JSON string to parse.
   *
   * @return The parsed list of {@link UDEvent} instances.
   *
   * @throws WdkModelException if an exception occurs while parsing the input
   *                           JSON.
   */
  public static List<UDEvent> parseList(String json) throws WdkModelException {
    try {
      return Arrays.asList(JsonUtil.Jackson.readValue(json, UDEvent[].class));
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Parses the contents of the given JSON file into a list of raw
   * {@link UDEvent} instances.
   *
   * @param jsonFile JSON file to parse.
   *
   * @return The parsed list of {@link UDEvent} instances.
   *
   * @throws WdkModelException if an exception occurs while parsing the input
   *                           JSON.
   */
  public static List<UDEvent> parseList(File jsonFile) throws WdkModelException {
    try {
      return Arrays.asList(JsonUtil.Jackson.readValue(jsonFile, UDEvent[].class));
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Parses the given JSON string into a single {@link UDEvent} instance.
   *
   * @param json JSON string to parse.
   *
   * @return The parsed {@link UDEvent} instance.
   *
   * @throws WdkModelException if an exception occurs while parsing the input
   *                           JSON.
   */
  public static UDEvent parseSingle(String json) throws WdkModelException {
    try {
      return JsonUtil.Jackson.readValue(json, UDEvent.class);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
}
