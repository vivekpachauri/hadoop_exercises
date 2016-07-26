package average;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;

public class StockInputFormat extends FileInputFormat<Stock, StockPrices> {

	@Override
	public RecordReader<Stock, StockPrices> createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException,
			InterruptedException {
		return new StockReader();
	}

	public static class StockReader extends RecordReader<Stock, StockPrices>{



	    private Stock key = new Stock();
	    private StockPrices value = new StockPrices();
	    private LineReader in;
	    private long start;
	    private long end;
	    private long currentPos;
	    private Text line = new Text();

		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			FileSplit fSplit = (FileSplit)split;
			
			Configuration conf = context.getConfiguration();
			Path path = fSplit.getPath();
			FSDataInputStream is = path.getFileSystem(conf).open(path);
			in = new LineReader(is, conf);
			 
			/*in = new LineReader(fSplit.getPath().getFileSystem(context.getConfiguration()).open(fSplit.getPath()), context.getConfiguration());*/
			start = fSplit.getStart();
			end = start + fSplit.getLength();
			is.seek(start);
			if ( start !=  0)
			{
				start += in.readLine(new Text(), 0, (int)Math.min(Integer.MAX_VALUE, end - start));
			}
			currentPos = start;
			
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {

			if ( currentPos > end ) return false;
			currentPos += in.readLine(line);
			if (line.getLength() == 0 )
			{
				return false;
			}
			if ( line.toString().startsWith("exchange"))
			{
				currentPos += in.readLine(line);
			}
			String[] values = StringUtils.split(line.toString(), ',');
			key.setSymbol(values[1]);
			key.setDate(values[2]);
			value.setOpen(Double.parseDouble(values[3]));
			value.setHigh(Double.parseDouble(values[4]));
			value.setLow(Double.parseDouble(values[5]));
			value.setClose(Double.parseDouble(values[6]));
			value.setVolume(Integer.parseInt(values[7]));
			value.setAdjustedClose(Double.parseDouble(values[8]));
			return true;		
		}

		@Override
		public Stock getCurrentKey() throws IOException, InterruptedException {

			return key;
		}

		@Override
		public StockPrices getCurrentValue() throws IOException,
				InterruptedException {

			return value;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			in.close();
		}
		
	}

}
