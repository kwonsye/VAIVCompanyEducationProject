import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CompoundNoun {

    private static final String DICTIONARY_FILE_PATH = "noun.txt";
    private static final String COMPOUND_WORD = "대학생선교회";

    private enum Option {
        ALL("all"),
        LONGEST("longest"),
        SHORTEST("shortest");

        private String type;

        Option(String type) {
            this.type = type;
        }
    }

    public static void main(String[] args) throws IOException {

        decomposeCompoundNounWithDP(DICTIONARY_FILE_PATH, COMPOUND_WORD, Option.ALL);

        //decomposeCompoundNounWithDP(DICTIONARY_FILE_PATH, COMPOUND_WORD, Option.LONGEST);

        //decomposeCompoundNounWithDP(DICTIONARY_FILE_PATH, COMPOUND_WORD, Option.SHORTEST);

    }

    private static void decomposeCompoundNounWithRecursive(String dictionaryFilePath, String compoundWord, Option optionType) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilePath));
        Set<String> dictionarySet = new HashSet<>();
        List<String> list = new ArrayList<>();
        String word;

        while ((word = reader.readLine()) != null) {
            dictionarySet.add(word);
        }

        for (String dictionaryWord : dictionarySet) {
            if (compoundWord.contains(dictionaryWord)) {
                list.add(dictionaryWord);
            }
        }
    }

    private static void decomposeCompoundNounWithDP(String dictionaryFilePath, String compoundWord, Option optionType) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilePath));
        Set<String> dictionaryWords = new HashSet<>();
        List<String> dictionaryWordsInCompoundWord = new ArrayList<>();
        String word;
        Map<String, List<String>> dp = new LinkedHashMap<>();

        while ((word = reader.readLine()) != null) {
            dictionaryWords.add(word);
        }

        for (String dictionaryWord : dictionaryWords) {
            if (compoundWord.contains(dictionaryWord)) {
                dictionaryWordsInCompoundWord.add(dictionaryWord);
            }
        }

        for (int i = 1; i <= compoundWord.length(); i++) {
            String subTextFromBackward = compoundWord.substring(compoundWord.length() - i);
            for (int j = 1; j <= subTextFromBackward.length(); j++) {
                String subText = subTextFromBackward.substring(0, j);

                if (dictionaryWordsInCompoundWord.contains(subText)) {
                    dynamicProgramming(dp, subTextFromBackward, subText);
                }
            }
        }

        printMap(dp, optionType);
    }

    private static void printMap(Map<String, List<String>> dp, Option optionType) {
        List<String> keysList = List.copyOf(dp.keySet());
        String lastKey = keysList.get(keysList.size()-1);
        List<String> finalCombinations = dp.get(lastKey);

        switch (optionType){
            case ALL:
                System.out.println("[option : all]");
                finalCombinations.forEach(System.out::println);
                break;

            case LONGEST:
                System.out.println("[option : longest]");
                System.out.println(finalCombinations.get(finalCombinations.size()-1));
                break;

            case SHORTEST:
                System.out.println("[option : shortest]");
                System.out.println(finalCombinations.get(0));
                break;
        }
    }

    private static void dynamicProgramming(Map<String, List<String>> dp,
                                           String subTextFromBackward, String subText) {
        String key;
        List<String> combinations = dp.getOrDefault(subTextFromBackward, new ArrayList<>());

        if (subTextFromBackward.equals(subText)) {
            combinations.add(subText);
        } else {
            key = subTextFromBackward.substring(subText.length());
            List<String> precalculatedCombinations = dp.getOrDefault(key, new ArrayList<>());

            if (!precalculatedCombinations.isEmpty()) {
                for (String element : precalculatedCombinations) {
                    combinations.add(subText + "+" + element);
                }
            }
        }

        dp.put(subTextFromBackward, combinations);
    }
}
