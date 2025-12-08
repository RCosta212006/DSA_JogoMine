package jogo.save;

import java.io.*;
import java.util.*;

public class HighScoreManager {
    private static final String FILE_NAME = "highscores.txt";
    private static final int MAX_SCORES = 5;

    public static void saveScore(String playerName, int score) {
        List<ScoreEntry> scores = loadScores();
        scores.add(new ScoreEntry(playerName, score));

        // Ordenar e manter apenas os top 5
        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }

        // Escrever no ficheiro
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.getName() + ":" + entry.getScore());
            }
        } catch (Exception e) {
            System.out.println("Erro ao guardar scores: " + e.getMessage());
        }
    }

    public static List<ScoreEntry> loadScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return scores;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    scores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Erro ao ler scores: " + e.getMessage());
        }

        Collections.sort(scores);
        return scores;
    }
}