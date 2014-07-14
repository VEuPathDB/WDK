/**
 * 
 */
package org.gusdb.wdk.model.record.attribute.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.user.Step;

/**
 * @author jerric
 * 
 */
public class WordCloudAttributePlugin extends AbstractAttributePlugin implements
    AttributePlugin {

  private static final String PROP_SPLIT_PATTERN = "split-pattern";
  // private static final String PROP_MIN_WEIGHT = "min-weight";
  // private static final String PROP_MAX_WEIGHT = "max-weight";
  private static final String PROP_MIN_WORD_LENGTH = "min-word-length";
  private static final String PROP_EXCLUDE_NUMBERS = "exclude-numbers";
  private static final String PROP_COMMON_WORDS = "common-words";

  private static final String ATTR_CONTENT = "content";
  private static final String ATTR_TAGS = "tags";

  private static final String NUMBER_PATTERN = "^(\\-)?[\\d\\.]+";
  private static final String[] COMMON_WORDS = { "and", "off", "are", "was",
      "were", "the", "that" };

  private static final Logger logger = Logger.getLogger(WordCloudAttributePlugin.class);

  private boolean propertiesResolved = false;
  private String splitPattern = "[^a-z0-9_]+";
  // private int minWeight = 7;
  // private int maxWeight = 50;
  private int minWordLength = 3;
  private boolean excludeNumbers = true;
  private Set<String> commonWords;

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributePlugin#process()
   */
  @Override
  public Map<String, Object> process(Step step) {
    StringBuilder content = new StringBuilder();
    List<WordTag> tags = loadTags(step, content);

    // compose the result
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(ATTR_CONTENT, content.toString().trim());
    result.put(ATTR_TAGS, tags);
    return result;
  }

  private List<WordTag> loadTags(Step step, StringBuilder content) {
    List<WordTag> tags = new ArrayList<>();

    resolveProperties();
    try {
      Map<String, WordTag> tagMap = new HashMap<String, WordTag>();
      Map<PrimaryKeyAttributeValue, Object> values = getAttributeValues(step);
      for (Object value : values.values()) {
        if (value == null)
          continue;
        content.append(" ").append(value);
        splitWords(value.toString(), tagMap);
      }
      // the tags are sorted by count
      tags = processTags(tagMap);
      return tags;
    } catch (Exception ex) {
      logger.error(ex);
      throw new RuntimeException(ex);
    }
  }

  private void resolveProperties() {
    if (propertiesResolved)
      return;

    // get common words
    String[] words = COMMON_WORDS;
    if (properties.containsKey(PROP_COMMON_WORDS))
      words = properties.get(PROP_COMMON_WORDS).split(",");
    commonWords = new HashSet<String>();
    for (String word : words) {
      commonWords.add(word);
    }

    // check if exclude numbers
    if (properties.containsKey(PROP_EXCLUDE_NUMBERS))
      excludeNumbers = Boolean.valueOf(properties.get(PROP_EXCLUDE_NUMBERS));

    // if (properties.containsKey(PROP_MAX_WEIGHT))
    // maxWeight = Integer.valueOf(properties.get(PROP_MAX_WEIGHT));
    //
    // if (properties.containsKey(PROP_MIN_WEIGHT))
    // minWeight = Integer.valueOf(properties.get(PROP_MIN_WEIGHT));
    //
    if (properties.containsKey(PROP_MIN_WORD_LENGTH))
      minWordLength = Integer.valueOf(properties.get(PROP_MIN_WORD_LENGTH));

    if (properties.containsKey(PROP_SPLIT_PATTERN))
      splitPattern = properties.get(PROP_SPLIT_PATTERN);

    propertiesResolved = true;
  }

  private void splitWords(String content, Map<String, WordTag> tags) {
    // break the words
    String[] words = content.trim().toLowerCase().split(splitPattern);
    for (String word : words) {
      // exclude small words
      if (word.length() < minWordLength)
        continue;
      // exclude common words
      if (commonWords.contains(word))
        continue;
      // exclude numbers
      if (excludeNumbers && word.matches(NUMBER_PATTERN))
        continue;

      WordTag tag = tags.get(word);
      if (tag == null)
        tag = new WordTag(word);
      else
        tag.increment();
      tags.put(word, tag);
      // logger.debug("word count: '" + word + "' = " + count);
    }
  }

  private List<WordTag> processTags(Map<String, WordTag> tags) {
    // remove the plurals
    List<WordTag> list = new ArrayList<WordTag>();
    for (WordTag tag : tags.values()) {
      boolean isPlural = false;
      // look for plural words
      String word = tag.getWord();
      if (word.endsWith("s")) {
        String part = word.substring(0, word.length() - 1);
        if (!tags.containsKey(part) && word.endsWith("es")) {
          part = word.substring(0, word.length() - 2);
          if (!tags.containsKey(part) && word.endsWith("ies"))
            part = word.substring(0, word.length() - 3) + "y";
        }

        if (tags.containsKey(part)) {
          WordTag partTag = tags.get(part);
          int count = tag.getCount() + partTag.getCount();
          partTag.setCount(count);
          isPlural = true;
        }
      }
      // only keep the tags that are not plural
      if (!isPlural)
        list.add(tag);
    }

    if (list.size() > 1) {
      // sort the tags by count, so that the follow-up computation of
      // weights and scores are easier.
      Collections.sort(list);
    }
    return list;
  }
}
