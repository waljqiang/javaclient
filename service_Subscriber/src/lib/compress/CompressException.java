package lib.compress;

public class CompressException extends Exception{
	
	public static final long COMPRESS_FAILURE = 600201100;//压缩失败
	public static final long UNCOMPRESS_FAILURE = 600201101;//解压失败
	
	private long code;
	
	public CompressException(){
		super();
	}
	
	public CompressException(String message){
		super(message);
	}
	
	public CompressException(String message,long code){
		super(message);
		this.code = code;
	}
	
	public long getCode(){
		return this.code;
	}
}