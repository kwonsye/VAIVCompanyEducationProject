package com.hadoop.project.hadoop_project.twitter_project.word_count;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class TwitterWordCount {

    public static class JsonParsingTokenizerMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static LongWritable ONE = new LongWritable(1);
        private Text word = new Text();
        private ObjectMapper mapper = new ObjectMapper();

        @Override
        public void map(Object inputKey, Text inputJson, Context context) throws IOException, InterruptedException {

            TwitterPost twitterPost = mapper.readValue(inputJson.toString(), TwitterPost.class);
            String content = twitterPost.getContent();

            StringTokenizer tokenizer = new StringTokenizer(content);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, ONE);
            }
        }
    }

    public static class CountingSumReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private LongWritable result = new LongWritable();

        @Override
        public void reduce(Text word, Iterable<LongWritable> countingValues, Context context) throws IOException, InterruptedException {
            long countingSum = 0;
            for (LongWritable countingValue : countingValues) {
                countingSum += countingValue.get();
            }

            result.set(countingSum);
            context.write(word, result);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = setJobConfigurations();

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    private static Job setJobConfigurations() throws IOException {
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration, "twitter_word_count");

        job.setJarByClass(TwitterWordCount.class);

        job.setMapperClass(JsonParsingTokenizerMapper.class);
        job.setCombinerClass(CountingSumReducer.class);
        job.setReducerClass(CountingSumReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        return job;
    }
}
