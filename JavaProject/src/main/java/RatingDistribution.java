import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class RatingDistribution {

    private static final String PROJECT_DATA_DIRECTORY = "rating_distribution_data/";
    private static final String OUTPUT_SUB_DATA_DIRECTORY = "output_sub_data/";
    private static final String FINAL_OUTPUT_RESULT_DIRECTORY = "output_result/";

    private static final String FINAL_OUTPUT_RESULT_FILE_NAME = "final_result.txt";
    private static final String REVIEW_DATA_JSON_FILE_NAME = "test.json";

    private static final Long FLUSH_UNIT_OF_MAP_SIZE = 5L;
    private static final Long INITIAL_VALUE_OF_COUNT = 0L;

    public static void main(String[] args) throws IOException {

        parseInputFileAndWriteUnitMapToFiles(PROJECT_DATA_DIRECTORY + REVIEW_DATA_JSON_FILE_NAME);

        KWayMergingOfAllOutputFiles(PROJECT_DATA_DIRECTORY + OUTPUT_SUB_DATA_DIRECTORY);
    }

    private static void parseInputFileAndWriteUnitMapToFiles(String inputFilePath) throws IOException {

        Map<Long, List<Long>> ratingsMap = new HashMap<>(); //key:bookId - value : list(rating1 count,rating2 count,rating3 count..)

        BufferedReader reader = FileIOUtil.getBufferedReader(inputFilePath);
        String json;
        ObjectMapper mapper = new ObjectMapper();
        long fileCount = 0;

        while ((json = reader.readLine()) != null) {
            ReviewData reviewData = mapper.readValue(json, ReviewData.class);
            Long bookId = reviewData.getBookId();
            Integer rating = reviewData.getRating();

            initializeCountingList(ratingsMap, bookId);
            updateCountingList(ratingsMap, bookId, rating);

            //divide
            if (ratingsMap.size() == FLUSH_UNIT_OF_MAP_SIZE) {
                writeMapDataToFile(fileCount, ratingsMap, sortingMapKeys(ratingsMap.keySet()));
                ratingsMap.clear();

                fileCount++;
            }
        }

        //write last remain data
        if(!ratingsMap.isEmpty()){
            writeMapDataToFile(fileCount, ratingsMap, sortingMapKeys(ratingsMap.keySet()));
            ratingsMap.clear();
        }
    }

    private static void KWayMergingOfAllOutputFiles(String outputFilePath) throws IOException {
        Queue<RateCountingData> heap = new PriorityQueue<>();
        long lineCountingIndex = 0;
        long maxLineIndex = 0;

        File outputFolder = new File(outputFilePath);
        File[] listOfFiles = outputFolder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                long fileLineCount = Files.lines(file.toPath()).count();
                maxLineIndex = Math.max(maxLineIndex, fileLineCount);
            }
        }

        while (lineCountingIndex < maxLineIndex) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    readFileAndOfferOneDataToHeap(file, lineCountingIndex, heap);
                }
            }
            pollingNElementsOfHeap(heap, 1);

            lineCountingIndex++;
        }

        pollingNElementsOfHeap(heap, heap.size());
    }

    private static void pollingNElementsOfHeap(Queue<RateCountingData> heap, int pollingCount) throws IOException {
        BufferedWriter writer = FileIOUtil.getBufferedWriter(PROJECT_DATA_DIRECTORY + FINAL_OUTPUT_RESULT_DIRECTORY + FINAL_OUTPUT_RESULT_FILE_NAME);
        int count = 0;

        while(!heap.isEmpty() && count < pollingCount){
            RateCountingData rateCountingData = heap.poll();
            String result = FileIOUtil.buildResultFormat(rateCountingData.getBookId(), rateCountingData.getCountingList());
            writer.write(result);

            count++;
        }
        writer.close();
    }

    private static void readFileAndOfferOneDataToHeap(File file, long lineCountingIndex, Queue<RateCountingData> heap) throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            Optional<String> line = lines.skip(lineCountingIndex).findFirst();
            line.ifPresent(lineData -> {
                RateCountingData offeringData = parseLineToDataObject(lineData);
                Long bookId = offeringData.getBookId();
                boolean isExistData = false;

                Iterator<RateCountingData> iterator = heap.iterator();
                while (iterator.hasNext()) {
                    RateCountingData iteratingData = iterator.next();
                    if (iteratingData.getBookId() == bookId) {
                        isExistData = true;
                        heap.remove(iteratingData);

                        RateCountingData updatedData = getUpdatedRateCountingData(offeringData, iteratingData);
                        heap.offer(updatedData);

                        break;
                    }
                }
                if(!isExistData){
                    heap.offer(offeringData);
                }
            });
        }
    }

    private static RateCountingData getUpdatedRateCountingData(RateCountingData offeringData, RateCountingData previousData) {
        List<Long> newList = offeringData.getCountingList();
        List<Long> previousList = previousData.getCountingList();

        for (int index = 0; index < previousList.size(); index++) {
            newList.set(index, previousList.get(index) + newList.get(index));
        }

        RateCountingData data = new RateCountingData(offeringData.getBookId(), newList);

        return data;
    }

    private static RateCountingData parseLineToDataObject(String lineData) {
        StringTokenizer tokenizer = new StringTokenizer(lineData);

        Long bookId = Long.valueOf(tokenizer.nextToken());
        List<Long> countingList = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            countingList.add(Long.valueOf(tokenizer.nextToken()));
        }

        return new RateCountingData(bookId, countingList);
    }


    private static void writeMapDataToFile(long fileIndex, Map<Long, List<Long>> ratingsMap, Long[] sortedKeys) throws IOException {
        BufferedWriter writer = FileIOUtil.getBufferedWriter(PROJECT_DATA_DIRECTORY + OUTPUT_SUB_DATA_DIRECTORY + fileIndex);

        for (Long key : sortedKeys) {
            String result = FileIOUtil.buildResultFormat(key, ratingsMap.get(key));
            writer.write(result);
        }
        writer.close();
    }

    private static Long[] sortingMapKeys(Set<Long> keySet) {
        Long[] keyArray = keySet.stream().toArray(Long[] :: new);
        Arrays.sort(keyArray);
        return keyArray;
    }

    private static void updateCountingList(Map<Long, List<Long>> ratingsMap, Long bookId, Integer rating) {
        List<Long> countingList = ratingsMap.get(bookId);

        long previousCount = countingList.get(rating);
        countingList.set(rating, previousCount + 1);

        ratingsMap.put(bookId, countingList);
    }

    private static void initializeCountingList(Map<Long, List<Long>> ratingsMap, Long bookId) {
        List<Long> valueList = ratingsMap.getOrDefault(bookId, new ArrayList<Long>() {{
            add(INITIAL_VALUE_OF_COUNT);
            add(INITIAL_VALUE_OF_COUNT);
            add(INITIAL_VALUE_OF_COUNT);
            add(INITIAL_VALUE_OF_COUNT);
            add(INITIAL_VALUE_OF_COUNT);
            add(INITIAL_VALUE_OF_COUNT);
        }});

        ratingsMap.put(bookId, valueList);
    }
}

@Getter
@AllArgsConstructor
class RateCountingData implements Comparable<RateCountingData> {

    private Long bookId;
    private List<Long> countingList;

    @Override
    public int compareTo(RateCountingData data) {
        if (bookId > data.getBookId()) {
            return 1;
        } else if (bookId < data.getBookId()) {
            return -1;
        } else {
            return 0;
        }
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class ReviewData {

    @JsonProperty("book_id")
    private Long bookId;

    @JsonProperty("rating")
    private Integer rating;

}

class FileIOUtil {

    public static BufferedReader getBufferedReader(String filePath) throws FileNotFoundException { return new BufferedReader(new FileReader(filePath)); }

    public static BufferedWriter getBufferedWriter(String filePath) throws IOException { return new BufferedWriter(new FileWriter(filePath, true)); }

    public static String buildResultFormat(Long id, Collection<Long> collection) {
        StringBuilder builder = new StringBuilder();

        builder.append(id);
        collection.forEach(element -> builder.append(" " + element));
        builder.append("\n");

        return builder.toString();
    }
}