package com.hadoop.project.hadoop_project.twitter_project.extract_top_n_word_count;

import lombok.Getter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;

public class ExtractTopN {

    static final Integer TOP_N_COUNT = 100;

    public static void main(String[] args) throws IOException {
        showTopNResult(new Path(args[0]));
    }

    //c++ partial sort의 원리 : heap 사용해서 top n 만 정렬
    private static void showTopNResult(Path subFilesDirPath) throws IOException {
        Queue<Node> heap = new PriorityQueue<>();
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "webhdfs://xx.x.xx.xxx:14000");
        FileSystem fs = subFilesDirPath.getFileSystem(conf);
        FileStatus[] fileLists = fs.globStatus(subFilesDirPath);

        for (FileStatus file : fileLists) {
            Path filePath = file.getPath();
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(filePath), "UTF-8"));

            Node node = new Node(filePath, reader);
            if (node.hasNext()) {
                heap.offer(node);
            }
        }

        int count = 1;
        while (count <= TOP_N_COUNT) {
            //heap이 null일때 예외처리하기
            if(heap.isEmpty()){
                System.out.println("end of heap");
                break;
            }
            if (!heap.peek().isEndOfFile()) {
                Node polledNode = heap.poll(); // polling 하면 다시 힙정렬 siftUp log n

                System.out.println(polledNode.getLastWord() + "\t" + polledNode.getLastCount());
                count++;

                if (polledNode.hasNext()) {
                    heap.offer(polledNode); // offer 하면 siftDown log n 시간이 두배로..
                }
            } else { //if node is end of file
                heap.poll();
            }
        }
    }
}

@Getter
class Node implements Comparable<Node> {
    private Path path;
    private BufferedReader reader;

    private String lastWord;
    private Long lastCount;

    public Node(Path path, BufferedReader reader) {
        this.path = path;
        this.reader = reader;
    }

    @Override
    public int compareTo(Node node) {
        int compareValue = -1 * lastCount.compareTo(node.getLastCount());

        if (compareValue == 0) {
            compareValue = lastWord.compareTo(node.getLastWord());
        }
        return compareValue;
    }

    public boolean hasNext() throws IOException {
        setNext();
        return !isEndOfFile();
    }

    //set next lastWord, lastCount
    public void setNext() throws IOException {
        String line;
        if ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            while(tokenizer.hasMoreTokens()){
                lastWord = tokenizer.nextToken();
                lastCount = Long.valueOf(tokenizer.nextToken());
            }
        } else {
            lastWord = null;
            lastCount = null;
        }
    }

    public boolean isEndOfFile() {
        return lastWord == null && lastCount == null;
    }
}
