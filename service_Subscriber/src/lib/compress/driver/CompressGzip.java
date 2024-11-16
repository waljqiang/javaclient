package lib.compress.driver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lib.compress.CompressException;
import lib.compress.CompressInterface;

public class CompressGzip implements CompressInterface{
	public static String GZIP_ENCODE = "UTF-8";
	
	public byte[] compress(String str) throws CompressException{
		if(str == null || str.length() == 0){
			return null;
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip;
		try{
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes(CompressGzip.GZIP_ENCODE));
			gzip.close();
			return out.toByteArray();
		}catch(IOException e){
			throw new CompressException("Compress failure,cause:"+e.getMessage(),CompressException.COMPRESS_FAILURE);
		}
	}
	
	public String uncompress(byte[] bytes) throws CompressException{
		if(bytes == null || bytes.length == 0){
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		try{
			GZIPInputStream ungzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while((n = ungzip.read(buffer)) >= 0){
				out.write(buffer,0,n);
			}
			return out.toString(CompressGzip.GZIP_ENCODE);
		}catch(IOException e){
			throw new CompressException("Uncompress failure,cause:"+e.getMessage(),CompressException.UNCOMPRESS_FAILURE);
		}
	}
	
}