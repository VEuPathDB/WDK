package org.gusdb.wdk.model.attribute.plugin;

import java.util.Comparator;

public class WordTag {

    static class WordComparator implements Comparator<WordTag> {
        public int compare(WordTag tag1, WordTag tag2) {
            return tag1.word.compareTo(tag2.word);
        }
    }

    static class CountComparator implements Comparator<WordTag> {

        public int compare(WordTag tag1, WordTag tag2) {
            int diff = tag2.count - tag1.count;
            return (diff != 0) ? diff : (tag1.word.compareTo(tag2.word));
        }
    }

    private final String word;
    private int count;
    private float weight;
    private int score;

    public WordTag(String word) {
        this.word = word;
        this.count = 1;
    }

    public String getWord() {
        return this.word;
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

    public float getWeight() {
        return this.weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
