package com.hadoop.project.hadoop_project.twitter_project.word_count_sorting;

import lombok.*;
import org.apache.hadoop.io.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WordCountPair implements WritableComparable<WordCountPair>{

    private String word;
    private Long count;

    @Override
    public int compareTo(WordCountPair wordCountPair) {
        int compareValue = -1 * count.compareTo(wordCountPair.getCount());

        if(compareValue == 0){
            compareValue = word.compareTo(wordCountPair.getWord());
        }
        return compareValue;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeString(out, word);
        WritableUtils.writeVLong(out, count);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        word = WritableUtils.readString(in);
        count = WritableUtils.readVLong(in);
    }
}
