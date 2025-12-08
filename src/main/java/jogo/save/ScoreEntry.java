package jogo.save;

public class ScoreEntry implements Comparable<ScoreEntry> {
    private String name;
    private int score;

    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(ScoreEntry o) {
        // Ordem decrescente (maior score primeiro)
        return Integer.compare(o.score, this.score);
    }

    @Override
    public String toString() {
        return name + ": " + score;
    }
}