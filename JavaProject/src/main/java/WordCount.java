import java.io.*;
import java.util.*;

public class WordCount {

    private static final String BIGDATA_FILE_NAME = "words.txt";
    private static final String SMALL_TEST_DATA_FILE_NAME = "small_size_test.txt";
    private static final String UNIT_FILE_PREFIX = "unit_words_file_";
    private static final String OUT_OF_MEMORY_FILE_PREFIX = "out_of_memory_words_file_";
    private static final String FILE_EXTENSION = ".txt";
    private static final Long PRINT_INTERVAL = 1_000_000L;
    private static final Long FLUSH_UNIT_OF_MAP_SIZE = 5_000_000L;

    public static void main(String[] args) throws IOException {
        // Out of memory with JVM 512Xmx
        wordCountForOutOfMemory(BIGDATA_FILE_NAME);

        // Divide and conquer
        //wordCountSolution(BIGDATA_FILE_NAME);
    }

    private static void wordCountSolution(String dataFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
        Map<String, Integer> map = new HashMap<>();
        String word;
        long lineCount = 1;
        int unitFileCount = 1;

        while ((word = bufferedReader.readLine()) != null) {
            map.put(word, map.getOrDefault(word, 0) + 1);

            //divide
            if (map.size() == FLUSH_UNIT_OF_MAP_SIZE) {
                String[] sortedKeys = sortingKeySet(map.keySet());
                writeMapDataToFile(UNIT_FILE_PREFIX + unitFileCount + FILE_EXTENSION, map, sortedKeys);
                map.clear();

                if (unitFileCount % 2 == 0) {
                    merge(unitFileCount - 1, unitFileCount); //idx 에 해당하는 두 개의 파일을 비교해서 하나의 파일로 합친다.
                    deleteSubFiles(unitFileCount - 1, unitFileCount);
                    unitFileCount++;
                }
                unitFileCount++;
            }

            if (lineCount % PRINT_INTERVAL == 0) {
                showMemoryStatus();
            }
            lineCount++;

        }

        //map 의 남은 데이터들을 파일에 쓴다.
        if (!map.isEmpty()) {
            String[] sortedKeys = sortingKeySet(map.keySet());
            writeMapDataToFile(UNIT_FILE_PREFIX + unitFileCount + FILE_EXTENSION, map, sortedKeys);
            merge(unitFileCount - 1, unitFileCount);
        }

        bufferedReader.close();
    }

    private static void deleteSubFiles(int... files) {
        for (int fileIdx : files) {
            File file = new File(UNIT_FILE_PREFIX + fileIdx + FILE_EXTENSION);
            file.delete();
        }
    }

    private static void merge(int firstFileIdx, int secondFileIdx) throws IOException {
        BufferedReader firstFileReader = new BufferedReader(new FileReader(UNIT_FILE_PREFIX + firstFileIdx + FILE_EXTENSION));
        BufferedReader secondFileReader = new BufferedReader(new FileReader(UNIT_FILE_PREFIX + secondFileIdx + FILE_EXTENSION));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(UNIT_FILE_PREFIX + (secondFileIdx + 1) + FILE_EXTENSION, true));

        String firstFilesLine = firstFileReader.readLine();
        String secondFilesLine = secondFileReader.readLine();

        while (true) {

            if (firstFilesLine == null || secondFilesLine == null) {
                break;
            }

            String firstFilesWord = firstFilesLine.split(":")[0];
            String secondFilesWord = secondFilesLine.split(":")[0];

            //firstFilesWord == secondFilesWord 일때 단어의 개수를 합쳐서 파일에 쓴다.
            if (firstFilesWord.compareTo(secondFilesWord) == 0) {
                int firstFilesWordCount = Integer.parseInt(firstFilesLine.split(":")[1]);
                int secondFilesWordCount = Integer.parseInt(secondFilesLine.split(":")[1]);

                bufferedWriter.write(firstFilesWord + ":" + (firstFilesWordCount + secondFilesWordCount) + "\n");
                firstFilesLine = firstFileReader.readLine();
                secondFilesLine = secondFileReader.readLine();
                continue;
            }

            //firstFilesWord < secondFilesWord 일때 firstFiles 의 데이터를 파일에 쓴다.
            if (firstFilesWord.compareTo(secondFilesWord) < 0) {
                bufferedWriter.write(firstFilesLine + "\n");
                firstFilesLine = firstFileReader.readLine();
                continue;
            }

            //firstFilesWord > secondFilesWord 일때 secondFiles 의 데이터를 파일에 쓴다.
            if (firstFilesWord.compareTo(secondFilesWord) > 0) {
                bufferedWriter.write(secondFilesLine + "\n");
                secondFilesLine = secondFileReader.readLine();
                continue;
            }
        }

        //남은 데이터를 모두 파일에 쓴다.
        if (firstFilesLine != null) {
            bufferedWriter.write(firstFilesLine + "\n");
            while ((firstFilesLine = firstFileReader.readLine()) != null) {
                bufferedWriter.write(firstFilesLine + "\n");
            }
        }

        if (secondFilesLine != null) {
            bufferedWriter.write(secondFilesLine + "\n");
            while ((secondFilesLine = secondFileReader.readLine()) != null) {
                bufferedWriter.write(secondFilesLine + "\n");
            }
        }

        bufferedWriter.close();
        firstFileReader.close();
        secondFileReader.close();
    }

    private static void writeMapDataToFile(String fileName, Map<String, Integer> map, String[] sortedKeys) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));

        for (String key : sortedKeys) {
            bufferedWriter.write(key + ":" + map.get(key) + "\n");
        }

        bufferedWriter.close();
    }

    private static String[] sortingKeySet(Set<String> keySet) {
        String[] sortedKeys = keySet.toArray(String[]::new); //java 11
        Arrays.sort(sortedKeys);
        return sortedKeys;
    }

    private static void wordCountForOutOfMemory(String dataFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
        Map<String, Integer> map = new HashMap<>();
        String data;
        long lineCount = 1;

        while ((data = bufferedReader.readLine()) != null) {
            map.put(data, map.getOrDefault(data, 0) + 1);
            /*if (map.size() % PRINT_MAP_SIZE_INTERVAL == 0) {
                System.out.println(map.size()); //map size 최대 사이즈가 20000000으로 고정되어있음
                showMemoryStatus();
            }*/
            if (lineCount % PRINT_INTERVAL == 0) {
                showMemoryStatus();
            }
            lineCount++;
        }

        String[] sortedKeys = sortingKeySet(map.keySet());
        writeMapDataToFile(OUT_OF_MEMORY_FILE_PREFIX + FILE_EXTENSION, map, sortedKeys);

        bufferedReader.close();
    }

    private static void showMemoryStatus() {
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Total Memory : " + runtime.totalMemory() + "\n" +
                "Used Memory : " + (runtime.totalMemory() - runtime.freeMemory()) + "\n" +
                "Free Memory : " + runtime.freeMemory() + "\n" +
                "Max Memory : " + runtime.maxMemory());
        System.out.println("============================");
    }
}
