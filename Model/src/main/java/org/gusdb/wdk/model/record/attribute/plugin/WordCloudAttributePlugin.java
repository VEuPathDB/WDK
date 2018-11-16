package org.gusdb.wdk.model.record.attribute.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.PrimaryKeyValue;

/**
 * @author jerric
 * 
 */
public class WordCloudAttributePlugin extends AbstractAttributePlugin {

  private static final String PROP_SPLIT_PATTERN = "split-pattern";
  // private static final String PROP_MIN_WEIGHT = "min-weight";
  // private static final String PROP_MAX_WEIGHT = "max-weight";
  private static final String PROP_MIN_WORD_LENGTH = "min-word-length";
  private static final String PROP_EXCLUDE_NUMBERS = "exclude-numbers";
  private static final String PROP_COMMON_WORDS = "common-words";

  private static final String ATTR_CONTENT = "content";
  private static final String ATTR_TAGS = "tags";

  private static final String NUMBER_PATTERN = "^(\\-)?[\\d\\.]+";
  protected static final String[] COMMON_WORDS = { "and", "are", "was", "were", "the", "that" };

  private static final Logger logger = Logger.getLogger(WordCloudAttributePlugin.class);

  private boolean propertiesResolved = false;
  private String splitPattern = "[^a-zA-Z0-9_]+";
  // private int minWeight = 7;
  // private int maxWeight = 50;
  private int minWordLength = 3;
  private boolean excludeNumbers = true;
  private Set<String> commonWords;

  @Override
  public Map<String, Object> process(AnswerValue answer) {
    StringBuilder content = new StringBuilder();
    List<WordTag> tags = loadTags(answer, content);

    // compose the result
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(ATTR_CONTENT, content.toString().trim());
    result.put(ATTR_TAGS, tags);
    return result;
  }

  private List<WordTag> loadTags(AnswerValue answer, StringBuilder content) {
    List<WordTag> tags = new ArrayList<>();

    resolveProperties();
    try {
      Map<String, WordTag> tagMap = new HashMap<String, WordTag>();
      Map<PrimaryKeyValue, Object> values = getAttributeValues(answer);
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
    String[] words = getCommonWords();
    if (properties.containsKey(PROP_COMMON_WORDS)) {
      // override common words with property if provided
      words = properties.get(PROP_COMMON_WORDS).split(",");
    }
    commonWords = new HashSet<>();
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

  protected String[] getCommonWords() {
    return COMMON_WORDS;
  }

  private void splitWords(String content, Map<String, WordTag> tags) {
    // break the words
	// The original word is case sensitive whereas the word is not (lower case)
	String[] originalWords = content.trim().split(splitPattern);
    for (String originalWord : originalWords) {
      String word = originalWord.toLowerCase();
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

      if (tag == null) tag = new WordTag(word, originalWord);
        
      else {
        tag.increment();
        
        // In addition to incrementing the overall count for the word, we need to amend the
        // mixedCaseCounter map either by adding a new case sensitive version of the word or
        // incrementing the count for an existing case sensitive version.
        Map<String, Integer> mixedCaseCounter = tag.getMixedCaseCounter();
        Integer count = mixedCaseCounter.get(originalWord);
        if(count == null) mixedCaseCounter.put(originalWord, 1);
        else mixedCaseCounter.put(originalWord, ++count);
      }  
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
      // Will be use to convert any mixed case words into singular form
      String ending = "";
      if (word.endsWith("s")) {
    	    ending = "s";
        String part = word.substring(0, word.length() - 1);
        if (!tags.containsKey(part) && word.endsWith("es")) {
        	  ending = "es";
          part = word.substring(0, word.length() - 2);
          if (!tags.containsKey(part) && word.endsWith("ies"))
        	    ending = "ies";
            part = word.substring(0, word.length() - 3) + "y";
        }

        if (tags.containsKey(part)) {
          WordTag partTag = tags.get(part);
          int count = tag.getCount() + partTag.getCount();
          partTag.setCount(count);
          
          // In addition to absorbing overall plural counts for the case neutral word, we
          // need to absorb plural counts for the case sensitive versions of the word as
          // well.
          Map<String,Integer> mixedCaseCounter = partTag.getMixedCaseCounter();
          for(String key : tag.getMixedCaseCounter().keySet()) {
        	    String singularForm = getSingularForm(key, ending);
        	    if(mixedCaseCounter.containsKey(singularForm)) {
        	    	  Integer nonPluralCount = mixedCaseCounter.get(singularForm);
        	    	  Integer pluralCount = tag.getMixedCaseCounter().get(key);
        	    	  mixedCaseCounter.put(singularForm, nonPluralCount + pluralCount);
        	    }
        	    else {
        	    	  mixedCaseCounter.put(singularForm, 1);
        	    }
          }
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
   
    return  list.stream().map(tag -> {
    	  String dominantCase = tag.getDominantCase();
    	  tag.setWord(dominantCase);
    	  return tag;
    	}).collect(Collectors.toList());
  }
  
  /**
   * Mixed case plurals are stored in the mixed case counter map as plurals since no
   * determination about plurals can be easily be made.  But since the ending of the
   * representative (i.e., lower-case) word has already been identified, we can use the same
   * mechanism to render mixed-case plurals into their singular form.
   * @param plural
   * @param ending
   * @return
   */
  protected String getSingularForm(String plural, String ending) {
    switch(ending) {
      case "s":
        return plural.substring(0, plural.length() - 1);
      case "es":
    	    return plural.substring(0, plural.length() - 2);
      case "ies":
    	    return plural.substring(0, plural.length() - 3) + "y";
    	  default:
    		return plural;
	}
  }
}
