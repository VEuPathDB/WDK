package org.gusdb.wdk.model.report.reporter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class WordTag implements Comparable<WordTag> {

    private String word;
    private int count;
    
    // Holds the various combinations of case for the word this wordTag represents, along
    // with a count of each occurrence.
    private Map<String,Integer> _mixedCaseCounter;

    // private float weight;
    // private int score;

    public WordTag(String word) {
        this.word = word;
        this.count = 1;
        _mixedCaseCounter = new HashMap<>();
    }
    
    /**
     * When constructing a new word tag, we also want to start a new mixedCaseCounter
     * map holding the first occurrence of a case sensitive version of the word represented. 
     * @param word - case neutral (i.e., lower case) version of the word
     * @param originalWord - case sensitive version of the word
     */
    public WordTag(String word, String originalWord) {
    	  this.word = word;
      this.count = 1;
      _mixedCaseCounter = new HashMap<>();
      _mixedCaseCounter.put(originalWord, 1);
    }

    public String getWord() {
        return this.word;
    }
    
    // Used as a way to munge in the dominant case version of the word for display.
    public void setWord(String word) {
    	  this.word = word;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increment() {
        this.count++;
    }
    
    /**
     * Returns the map holding the various case versions of the word along with its frequency
     * of appearance.
     * @return - map keyed by different case versions of the word.  Values are counts of appearance.
     */
    public Map<String,Integer> getMixedCaseCounter() {
    	  return _mixedCaseCounter;
    }
    
    /**
     * Iterates over the mixedCaseCounter mappings to find a case combination for the
     * word with the most occurrences.
     * @return - string representation of the mixed case version of the word with
     * the most occurrences.
     */
    public String getDominantCase() {
    	  Comparator<Entry<String, Integer>> compareByCounts =
    	      (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue());
    	  Optional<Entry<String, Integer>> dominantCase = _mixedCaseCounter
        .entrySet().stream().sorted(compareByCounts).findFirst();
    	  return dominantCase.get().getKey();
    }

    //
    // public float getWeight() {
    // return this.weight;
    // }
    //
    // public void setWeight(float weight) {
    // this.weight = weight;
    // }
    //
    // public int getScore() {
    // return this.score;
    // }
    //
    // public void setScore(int score) {
    // this.score = score;
    // }

    @Override
    public int compareTo(WordTag tag) {
        int diff = tag.count - count;
        return (diff != 0) ? diff : (word.compareTo(tag.word));
    }

}
