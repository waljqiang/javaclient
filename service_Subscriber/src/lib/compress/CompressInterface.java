package lib.compress;

public interface CompressInterface{
	byte[] compress(String str) throws CompressException;
	String uncompress(byte[] bytes) throws CompressException;
}