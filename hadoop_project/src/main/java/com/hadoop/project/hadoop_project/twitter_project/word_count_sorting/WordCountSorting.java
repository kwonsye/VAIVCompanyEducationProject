package com.hadoop.project.hadoop_project.twitter_project.word_count_sorting;

import lombok.Getter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.beans.ConstructorProperties;
import java.io.*;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;

public class WordCountSorting {

    public static class WordCountSortingMapper extends Mapper<LongWritable, Text, WordCountPair, NullWritable> {

        private WordCountPair wordCountPair = new WordCountPair();
        private NullWritable nullValue = NullWritable.get();

        @Override
        public void map(LongWritable inputKey, Text inputValue, Context context) throws IOException, InterruptedException {
            StringTokenizer tokenizer = new StringTokenizer(inputValue.toString());

            //split 을 쓰는게 더 나을 수도 있다.
            while (tokenizer.hasMoreTokens()){

                wordCountPair.setWord(tokenizer.nextToken());
                wordCountPair.setCount(Long.parseLong(tokenizer.nextToken()));

                context.write(wordCountPair, nullValue);
            }
        }
    }

    public static class WordCountSortingReducer extends Reducer<WordCountPair, NullWritable, Text, LongWritable> {

        private Text word = new Text();
        private LongWritable count = new LongWritable();

        @Override
        public void reduce(WordCountPair key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            word.set(key.getWord());
            count.set(key.getCount());

            context.write(word, count);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Job job = setJobConfigurations();

        Path path = new Path(args[0]);
        System.err.println("path log : "+ path.getFileSystem(job.getConfiguration()));

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        boolean isSuccess = job.waitForCompletion(true);

        System.exit(isSuccess ? 0 : 1);
    }

    private static Job setJobConfigurations() throws IOException {

        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration, "twitter_word_count_sort_by_count");

        job.setJarByClass(WordCountSorting.class);

        job.setMapperClass(WordCountSortingMapper.class);
        job.setReducerClass(WordCountSortingReducer.class);

        job.setNumReduceTasks(1);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapOutputKeyClass(WordCountPair.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        return job;
    }
}