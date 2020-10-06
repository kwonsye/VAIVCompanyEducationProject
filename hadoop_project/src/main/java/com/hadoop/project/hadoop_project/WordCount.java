package com.hadoop.project.hadoop_project;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class WordCount {

    private static final String INPUT_DATA_DIRECTORY_PATH = "wordcount_data/input";
    private static final String OUTPUT_DATA_DIRECTORY_PATH = "wordcount_data/output/wordcount_ver1";

    //map
    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> { //Mapper<INPUT_KEY_TYPE, INPUT_VALUE_TYPE, OUTPUT_KEY_TYPE, OUTPUT_VALUE_TYPE>

        /*
         *어떤 key 나 어떤 value 도 hadoop mr 에서 사용하려면 type이 writable 로 구현되어야한다.
         *IntWritable & Text implements WritableComparable
         *Writable : DataInput 과 DataOutput 을 기반으로한 시리얼라이즈 오브젝트
         *    - write() : 해당 데이터 객체를 serialize 하여 byte 스트림으로 변경 시켜주는 역할
         *    - readFields() : byte 스트림을 다시 원래 객체로 deserialize 시키는 역할
         */
        private final static IntWritable ONE = new IntWritable(1);
        private Text word = new Text(); //Text : stores text using standard UTF8 encoding. It provides methods to serialize, deserialize, and compare texts at byte level.

        @Override
        //map(INPUT_KEY, INPUT_VALUE, CONTEXT) : Called once for each key/value pair in the input split.
        public void map(Object inputKey, Text inputValue, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer tokenizer = new StringTokenizer(inputValue.toString()); //입력된 한라인(value)을 공백을 기준으로 분할
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, ONE); // Context.write(OUTPUT_KEY, OUTPUT_VALUE) : Generate an output key/value pair.
            }
        }
    }

    //reduce
    public static class IntSumReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> { //Reducer<INPUT_KEY_TYPE, INPUT_VALUE_TYPE, OUTPUT_KEY_TYPE, OUTPUT_VALUE_TYPE>
        // Mapper 의 OUTPUT_KEY_TYPE == Reducer 의 INPUT_KEY_TYPE, Mapper 의 OUTPUT_VALUE_TYPE == Reducer 의 INPUT_VALUE_TYPE
        private IntWritable result = new IntWritable();

        @Override
        //Mapper 의 map()에서 context.write()으로 context에 저장해 둔 (word,ONE) map 들이 key-values 형태로 매핑되서 reduce의 input으로 들어온다.
        public void reduce(Text inputKey, Iterable<IntWritable> inputValues,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable value : inputValues) {
                sum += value.get();
            }
            result.set(sum);
            context.write(inputKey, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Job job = setJobsConfigurations();

        //input과 output path 설정
        FileInputFormat.addInputPath(job, new Path(INPUT_DATA_DIRECTORY_PATH));
        FileOutputFormat.setOutputPath(job, new Path(OUTPUT_DATA_DIRECTORY_PATH));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    private static Job setJobsConfigurations() throws IOException {

        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration, "word count");

        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);

        /*
         * Combiner : Mapper 와 Reducer 사이에서 진행되는 것으로서 선택적으로 적용이 가능
         * Combiner 를 적용하면 map task 가 수행되는 모든 node 에 대해 Combiner class 의 instance 가 적용됨
         * 각각의 node 에서 Mapper instance 가 산출한 데이터를 입력받고 Combiner 가 지정된 작업을 하면 그 결과물을 Reducer 에게 보냄
         * Combiner 는 일종의 “mini-reduce” 프로세스로서 하나의 단위 컴퓨터에서 생성된 데이터만을 대상으로 함
         * node 별로 중간합산 하여 각 단어 당 한번씩만 Reducer 에 전달되므로 shuffle 프로세스에서 요구되는 대역폭을 획기적으로 줄일 수 있게 되고 결과적으로 job 처리속도가 개선됨
         * mapper 의 부분합을 구한다.
         * IO(disk+network), reducer 의 계산량을 줄이기 위함
         * IO를 줄이기 위해서 압축 기술도 많이 발전했다.
         * streaming 을 쓰는 압축 기술도 있다. ex) snappy(https://github.com/google/snappy), Zstandard(https://github.com/facebook/zstd)
         * 대용량 압축 기술 : bzip, 7Z
         */
        job.setCombinerClass(IntSumReducer.class); //setCombinerClass(combiner : Class<? extends Reducer>) Combiner 는 Reducer interface를 확장해서 만들 수 있음
        job.setReducerClass(IntSumReducer.class);

        //job.setPartitionerClass(null); //partition : mapper 의 key 에 대한 hash code 를 구해서 reducer 에게 분배해주는 방법을 정의함

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class); // 최종 OUTPUT_KEY_TYPE == 여기서는 reducer 의 OUTPUT_KEY_TYPE
        job.setOutputValueClass(IntWritable.class); // 최종 OUTPUT_VALUE_TYPE == 여기서는 reducer 의 OUTPUT_VALUE_TYPE

        return job;
    }
}
