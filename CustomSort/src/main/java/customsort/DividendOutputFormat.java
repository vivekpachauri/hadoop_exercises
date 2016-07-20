package customsort;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DividendOutputFormat extends FileOutputFormat<NullWritable, DividendChange> {

	@Override
	public RecordWriter<NullWritable, DividendChange> getRecordWriter(
			TaskAttemptContext job) throws IOException, InterruptedException {
		int partition = job.getTaskAttemptID().getTaskID().getId();
		
		Path outputDir = FileOutputFormat.getOutputPath(job);
		
		Path file = new Path(outputDir.getName() + Path.SEPARATOR + job.getJobName() + "_"  + partition);
		
		FileSystem fs = file.getFileSystem(job.getConfiguration());
		
		FSDataOutputStream outFile = fs.create(file);
		
		return new DividendRecordWriter(outFile);
	}
	
	public static class DividendRecordWriter extends RecordWriter<NullWritable, DividendChange>
	{
		private final String SEPARATOR=",";
		private DataOutputStream out;
//		private FSDataOutputStream outStream;
		
		public DividendRecordWriter(FSDataOutputStream outStream)
		{
			this.out = outStream;
		}

		@Override
		public void close(TaskAttemptContext context) throws IOException,
				InterruptedException {
			if ( this.out != null )
			{
				this.out.close();
			}
			
		}

		@Override
		public void write(NullWritable key, DividendChange value)
				throws IOException, InterruptedException {
			StringBuilder sb = new StringBuilder();
			sb.append("yippykayay" + value.getSymbol() + SEPARATOR + value.getDate() + SEPARATOR + value.getChange()).append("\n");
			out.write(sb.toString().getBytes());
		}
	}




}
