import java.util.*;

public class Bigrams {

    private static final String INPUT_TEXT = "Hello!\n" +
            "This is the first question.\n" +
            "Read a text file, extract alphabet bi-grams and count them.\n" +
            "Finally select most frequent three bi-grams, and print them with count.";
    private static final Integer TOP_N = 3;

    public static void main(String[] args) {
        extractBigrams(INPUT_TEXT, TOP_N);

        //extractBigramsWithPriorityQueue();
    }

    private static void extractBigrams(String inputText, Integer topN) {
        Map<String, Integer> bigrams = new HashMap<>();

        for (int index = 0; index < inputText.length(); index++) {
            if (Character.isAlphabetic(inputText.charAt(index)) && Character.isAlphabetic(inputText.charAt(index + 1))) {
                String subText = inputText.substring(index, index + 2).toLowerCase();
                bigrams.put(subText, bigrams.getOrDefault(subText, 0) + 1);
            }
        }

        List<String> sortedKeys = sortKeys(bigrams);
        printTopN(bigrams, sortedKeys, topN);
    }

    private static void printTopN(Map<String, Integer> bigrams, List<String> keys, Integer topN) {
        int count = 0;
        for (String key : keys) {
            if (count != topN) {
                System.out.println(key + " " + bigrams.get(key));
                count++;
            }
        }
    }

    private static List<String> sortKeys(Map<String, Integer> bigrams) {
        List<String> keys = new ArrayList<>(bigrams.keySet());
        Collections.sort(keys, (key1, key2) -> {
            if (bigrams.get(key2).compareTo(bigrams.get(key1)) == 0) {
                return key1.compareTo(key2); // 알파벳 순
            } else {
                return bigrams.get(key2).compareTo(bigrams.get(key1)); // value 내림차순
            }
        });

        return keys;
    }
}
