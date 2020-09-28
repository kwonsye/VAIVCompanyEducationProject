package com.hadoop.project.hadoop_project;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class WordCountVer2 {

    private static final String INPUT_DATA_DIRECTORY_PATH = "wordcount_data/input";
    private static final String OUTPUT_DATA_DIRECTORY_PATH = "wordcount_data/output/wordcount_ver2";

    private static final String IS_CASE_SENSITIVE_OPTION_KEY = "wordcount.case.sensitive";
    private static final String SKIP_PATTERNS_FILE_OPTION_KEY = "wordcount.skip.patterns";

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        enum CountersEnum { INPUT_WORDS } //Counter : has a long for the value

        private final static IntWritable ONE = new IntWritable(1);
        private Text word = new Text();

        private boolean caseSensitive;
        private Set<String> patternsToSkip = new HashSet<>();

        private Configuration configuration;
        private BufferedReader bufferedReader;

        @Override
        public void setup(Context context) throws IOException{
            configuration = context.getConfiguration();
            caseSensitive = configuration.getBoolean(IS_CASE_SENSITIVE_OPTION_KEY, true); //실행할 때 주는 -Dwordcount.case.sensitive 옵션

            if (configuration.getBoolean(SKIP_PATTERNS_FILE_OPTION_KEY, false)) { //main()에 -skip 옵션으로 준 파일을 job.getConfiguration().setBoolean("wordcount.skip.patterns",value) 설정하는 부분이 있음
                /*
                DistributedCache : job run 상태에서 모든 맵리듀스에 대해 hadoop fs 에 존재하는 같은 input file 을 줄 수 있다.all the tasks share a common input file
                이 예시에서는 -skip patterns.txt 파일이 캐시파일. main()에 addCacheFile 을 하는 부분이 있음
                */
                URI[] patternsURIs = Job.getInstance(configuration).getCacheFiles();

                for (URI patternsURI : patternsURIs) {
                    Path patternsPath = new Path(patternsURI.getPath());
                    String patternsFileName = patternsPath.getName();
                    parseSkipFile(patternsFileName);
                }
            }
        }

        private void parseSkipFile(String fileName) {
            try {
                bufferedReader = new BufferedReader(new FileReader(fileName));
                String pattern;
                while ((pattern = bufferedReader.readLine()) != null) {
                    patternsToSkip.add(pattern);
                }
            } catch (IOException exception) {
                System.err.println("Caught exception while parsing the cached file '"
                        + StringUtils.stringifyException(exception));
            }
        }

        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            String line = caseSensitive ?
                    value.toString() : value.toString().toLowerCase(); //caseSensitive=true : 대소문자 구분, caseSensitive=false : 대소문자 구분 안함

            for (String pattern : patternsToSkip) {
                line = line.replaceAll(pattern, ""); //regex 을 compile 할때 시간이 오래걸린다.
            }

            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, ONE);

                //Counter : tracks the progress of a map/reduce job. 보통 job의 상태를 추적하는 로그를 남기는데 많이 사용한다.
                Counter counter = context.getCounter(CountersEnum.class.getName(),
                        CountersEnum.INPUT_WORDS.toString());
                counter.increment(1);
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable value : values) {
                sum += value.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        //실행 시  -Dwordcount.case.sensitive={value} -skip {file_paths}

        Configuration configuration = new Configuration();

        Job job = setJobConfigurations(configuration);
        parseArgumentsOfJob(job, configuration, args);

        FileInputFormat.addInputPath(job, new Path(INPUT_DATA_DIRECTORY_PATH));
        FileOutputFormat.setOutputPath(job, new Path(OUTPUT_DATA_DIRECTORY_PATH));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    private static void parseArgumentsOfJob(Job job, Configuration configuration, String[] args) throws IOException {
        GenericOptionsParser optionParser = new GenericOptionsParser(configuration, args);

        String[] remainingArgs = optionParser.getRemainingArgs();

        if ((remainingArgs.length != 2) && (remainingArgs.length != 4)) {
            System.err.println("Usage: wordcount <in> <out> [-skip skipPatternFile]");
            System.exit(2);
        }

        for (int i=0; i < remainingArgs.length; i++) {
            if ("-skip".equals(remainingArgs[i])) {
                job.addCacheFile(new Path(remainingArgs[++i]).toUri()); //캐시파일로 설정
                job.getConfiguration().setBoolean(SKIP_PATTERNS_FILE_OPTION_KEY, true);
            }
        }
    }

    private static Job setJobConfigurations(Configuration configuration) throws IOException {
        Job job = Job.getInstance(configuration, "word count ver2");

        job.setJarByClass(WordCountVer2.class);

        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        return job;
    }


}
