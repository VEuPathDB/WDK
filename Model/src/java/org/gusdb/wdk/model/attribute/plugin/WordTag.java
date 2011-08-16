package org.gusdb.wdk.model.attribute.plugin;


public class WordTag implements Comparable<WordTag> {

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

    public int compareTo(WordTag tag) {
        int diff = tag.count - count;
        return (diff != 0) ? diff : (word.compareTo(tag.word));
    }

}
