package hdfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;

public class InputCounties {


	public static void main(String[] args) {
		Configuration config = new Configuration();
		
		try {
			FileSystem fs = FileSystem.get(config);
			Path path = new Path("counties");
			FileSystem.mkdirs(fs, path, FsPermission.getDirDefault());
			
			for ( int i = 1; i < 5; i++ )
			{
				String fileName = "counties_"+i+".csv";
				Path dest = new Path("counties/"+fileName);
				fs.copyFromLocalFile(dest, dest);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
