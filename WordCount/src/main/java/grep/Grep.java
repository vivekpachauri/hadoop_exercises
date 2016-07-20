package grep;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import wordcount.WordCountJob;

public class Grep extends Configured implements Tool {

	public static class GrepMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		private String searchString;
		private Text outputKey = new Text();
		private final static IntWritable ONE = new IntWritable(1);
		
		@Override
		protected void setup(
				Mapper<LongWritable, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			this.searchString = context.getConfiguration().get("searchString");
		}

		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			String[] words = StringUtils.split(value.toString(), '\\',' ');
			for ( String word : words )
			{
				if ( word.contains(searchString) )
				{
					context.write(new Text(word), ONE);
				}
			}
		}

	}

	@Override
	public int run(String[] args) throws Exception {

		Job job = Job.getInstance(this.getConf(), "GrepJob");
		Configuration config = job.getConfiguration();
		config.set("searchString", args[2]);
		job.setJarByClass(getClass());
		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		out.getFileSystem(config).delete(out, true); //automatically delete the output directory
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		job.setMapperClass(GrepMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		return job.waitForCompletion(true)?0:1;
	
	}

	public static void main(String[] args) {

		int result;
		try {
			result = ToolRunner.run(new Configuration(), new Grep(), args);
			System.exit(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
	}

}
