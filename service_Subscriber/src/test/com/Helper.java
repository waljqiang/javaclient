package test.com;

import java.util.Random;

public class Helper {
	public static String setMac(String mac){
		return mac.substring(0,2) + ":" + mac.substring(2,4) + ":" + mac.substring(4,6) + ":" + mac.substring(6,8) + ":" + mac.substring(8,10) + ":" + mac.substring(10,12);
	}
	
	public static String parseMac(String mac){
		return mac.replace(":","");
	}
	
	public static Long hexdec(String str){
		long sum=0,tmp=0;
		for(int i=0;i<str.length();i++){
			char c = str.charAt(i);
			if(c>='0'&&c<='9'){
				tmp = c-'0';
			}else if(c>='A'&&c<='F'){
				tmp=c-'A'+10;
			}else{
				break;
			}
			sum=sum*16+tmp;
		}
		return sum; 
	}
	
	public static String dechex(long num) {
        return Long.toHexString(num).toUpperCase();
    }
	
	public static String padLeft(String str,int len,char ch){
		int diff = len - str.length();
		if(diff <= 0){
			return str;
		}
		int charLength = len - str.length();
		char[] charr = new char[charLength];
		for(int i = 0;i < charLength;i++){
			charr[i] = ch;
		}
		return new String(charr) + str;
	}
	
	public static String getStr(int length,String source){
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i<length;i++){
			int number = random.nextInt(source.length());
			sb.append(source.charAt(number));
		}
		return sb.toString();
	}
	
	public static int getRandom(int min,int max){
		return min + (int)(Math.random()*(max-min+1));
	}
}
