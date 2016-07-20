package average;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class AverageJob extends Configured implements Tool {
	
	public enum Counters{
		MAP, COMBINE, REDUCE
	}

	public static class AverageMapper extends Mapper<LongWritable, Text, Text, Text> {

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] vals = StringUtils.split(value.toString(), ',');
			Text state = new Text(vals[1].trim());
			Text income = new Text(vals[9].trim()+",1");
			context.getCounter(Counters.MAP).increment(1);
			context.write(state, income);
		}

		@Override
		protected void cleanup(Context context)
				throws IOException, InterruptedException {

		}


	}

	public static class AverageCombiner extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			long sum=0;
			long count=0;
			for ( Text value : values )
			{
				String vals[] = StringUtils.split(value.toString(), ',');
				try
				{
					long currSum  = Long.valueOf(vals[0]);
					long currCount  = Long.valueOf(vals[1]);
					sum += currSum;
					count += currCount;
				}
				catch (NumberFormatException e)
				{
					System.err.println("unable to parse: " + vals[0] + " " + vals[1]);
				}
			}
			context.getCounter(Counters.COMBINE).increment(1);
			context.write(key, new Text(sum + "," + count));
		}

		@Override
		protected void cleanup(Context context)
				throws IOException, InterruptedException {

		}
	}

	public static class AverageReducer extends Reducer<Text, Text, Text, DoubleWritable> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			double sum = 0;
			long count = 0;
			for ( Text value : values )
			{
				String[] vals = StringUtils.split(value.toString(), ',');
				try
				{
					double currSum = Double.valueOf(vals[0]);
					long currCount = Long.valueOf(vals[1]);
					sum += currSum;
					count += currCount;
				}
				catch (NumberFormatException e)
				{
					System.err.println("unable to parse: " + vals[0] + " " + vals[1]);
				}
			}
			context.getCounter(Counters.REDUCE).increment(1);
			context.write(key, new DoubleWritable(sum/count));
		}

		@Override
		protected void cleanup(Context context)
				throws IOException, InterruptedException {

		}
	}
	
	public static class AveragePartitioner extends org.apache.hadoop.mapreduce.Partitioner<Text, Text> {

		@Override
		public int getPartition(Text key, Text value, int numPartitions) {
//			return key.toString().substring(0, 1).hashCode() % numPartitions;
			String firstChar = key.toString().substring(0,1);
			switch (firstChar)
			{
			case "A":case "B":case "C":case "D":case "E":return 0;
			case "F": case "G": case "H": case "I": case "J": return 1;
			case "K": case "L": case "M": case "N": case "O": return 2;
			case "P": case "Q": case "R": case "S": case "T": return 3;
			default: return 4;
//			case "U": case "V": case "W": case "X": case "Y": case "Z": return 4;
			}
		}
	}

	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = super.getConf();
		Job job = Job.getInstance(conf, "AverageJob");
		job.setJarByClass(AverageJob.class);

		Path out = new Path("counties/output");
		FileInputFormat.setInputPaths(job, "counties");
		FileOutputFormat.setOutputPath(job, out);
		out.getFileSystem(conf).delete(out, true);

		job.setMapperClass(AverageMapper.class);
		job.setReducerClass(AverageReducer.class);
		job.setCombinerClass(AverageCombiner.class);
		job.setPartitionerClass(AveragePartitioner.class);
		job.setNumReduceTasks(5);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);


		return job.waitForCompletion(true)?0:1;

	}


	public static void main(String[] args) {
		int result = 0;
		try {
			result = ToolRunner.run(new Configuration(),  new AverageJob(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(result);

	}

}
