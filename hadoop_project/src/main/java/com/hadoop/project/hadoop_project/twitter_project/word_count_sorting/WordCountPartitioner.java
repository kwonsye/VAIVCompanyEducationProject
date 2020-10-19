package com.hadoop.project.hadoop_project.twitter_project.word_count_sorting;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Partitioner;


public class WordCountPartitioner extends Partitioner<WordCountPair, NullWritable> {

    @Override
    public int getPartition(WordCountPair wordCountPair, NullWritable nullWritable, int numPartitions) {
        return (wordCountPair.getWord().hashCode() & 0x7FFFFFFF) % numPartitions; // hashcode() == MIN_VALUE 일 경우 Math.abs(MIN_VALUE)=MIN_VALUE
    }
}
