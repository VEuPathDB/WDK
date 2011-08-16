/**
 * 
 */
package org.gusdb.wdk.model.attribute.plugin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.PrimaryKeyAttributeValue;

/**
 * @author jerric
 * 
 */
public class WordCloudAttributePlugin extends AbstractAttributePlugin implements
        AttributePlugin {

    private static final String ATTR_PLUGIN = "plugin";
    private static final String ATTR_CONTENT = "content";
    private static final String ATTR_FREQUENCY = "frequency";

    private static final int RARE_THRESHOLD = 100;
    private static final int MIN_WORD_LENGTH = 3;
    private static final String SEPARATOR = "[^a-z0-9\\-_]+";
    private static final int MIN_FONT = 6 * 255;
    private static final int MAX_FONT = 50 * 255;

    private static final Set<String> COMMON_WORDS = new HashSet<String>();
    static {
        String[] words = { "and", "off", "are", "was", "were" };
        for (String word : words) {
            COMMON_WORDS.add(word);
        }
    }

    private static final Logger logger = Logger
            .getLogger(WordCloudAttributePlugin.class);

    private static class ValueComparator implements Comparator<String> {

        private Map<String, Integer> counts;

        public ValueComparator(Map<String, Integer> counts) {
            this.counts = counts;
        }

        public int compare(String word1, String word2) {
            int diff = counts.get(word2) - counts.get(word1);
            if (diff != 0) return diff;
            return word1.compareTo(word2);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributePlugin#process()
     */
    public Map<String, Object> process() {
        StringBuilder content = new StringBuilder();
        Map<String, Integer> counts = new HashMap<String, Integer>();
        try {
            Map<PrimaryKeyAttributeValue, Object> values = getAttributeValues();
            for (Object value : values.values()) {
                if (value == null) continue;
                content.append(" ").append(value);
                splitWords(value.toString(), counts);
            }
            counts = consolidate(counts);
            scale(counts);
        }
        catch (Exception ex) {
            logger.error(ex);
            throw new RuntimeException(ex);
        }

        // compose the result
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(ATTR_CONTENT, content.toString().trim());
        result.put(ATTR_FREQUENCY, counts);
        result.put(ATTR_PLUGIN, this);
        return result;
    }

    private void splitWords(String content, Map<String, Integer> counts) {
        // logger.debug("content: '" + content + "'");
        // break the words
        String[] words = content.trim().toLowerCase().split(SEPARATOR);
        for (String word : words) {
            // logger.debug("word: '" + word + "'");
            if (word.length() < MIN_WORD_LENGTH) continue;
            if (COMMON_WORDS.contains(word)) continue;

            int count = 1;
            if (counts.containsKey(word)) count = counts.get(word) + 1;
            counts.put(word, count);
            // logger.debug("word count: '" + word + "' = " + count);
        }
    }

    private Map<String, Integer> consolidate(Map<String, Integer> counts) {
        String[] words = new String[counts.size()];
        words = counts.keySet().toArray(words);
        for (String word : words) {
            // look for plural words
            if (word.endsWith("s")) {
                String part = word.substring(0, word.length() - 1);
                if (!counts.containsKey(part) && word.endsWith("es")) {
                    part = word.substring(0, word.length() - 2);
                    if (!counts.containsValue(part) && word.endsWith("ies"))
                        part = word.substring(0, word.length() - 3) + "y";
                }

                if (counts.containsKey(part)) {
                    int count = counts.get(word) + counts.get(part);
                    counts.put(part, count);
                    counts.remove(word);
                }
            }
        }

        // Map<String, Integer> sorted = new TreeMap<String, Integer>(
        //         new ValueComparator(counts));
        // sorted.putAll(counts);
        // return sorted;
        return counts;
    }

    private void scale(Map<String, Integer> counts) {
        String[] words = new String[counts.size()];
        counts.keySet().toArray(words);
        int threshold = (int)Math.round(Math.log10(counts.size()));
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (String word : words) {
            int count = counts.get(word);
            if (count <= threshold) {
                counts.remove(word);
                continue;
            }
            if (count > max) max = count;
            if (count < min) min = count;
        }
        float scale = (MAX_FONT - MIN_FONT + 1F) / (max - min);
        for (String word : words) {
            if (!counts.containsKey(word)) continue;
            int font = Math.round(scale * counts.get(word)) + MIN_FONT - 1;
            counts.put(word, font);
        }
    }
}
