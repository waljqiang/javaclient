package test.devicereport;

import conf.config;

public class deviceConf extends config{
	public static final String DEV_AC_CHIP = config.getIni("REPORT_DEV_AC_CHIP","MT7915");
	public static final String DEV_AC_CPU = config.getIni("REPORT_DEV_AC_CPU","1GHz");
	public static final String DEV_AC_FLASH = config.getIni("REPORT_DEV_AC_FLASH","16");
	public static final String DEV_AC_RAM = config.getIni("REPORT_DEV_AC_RAM","2048");
	public static final String DEV_AC_VERSION = config.getIni("REPORT_DEV_AC_VERSION","AC80-AC-V3.0-Build20230302155804");
	public static final String DEV_AC_MODE = config.getIni("REPORT_DEV_AC_MODE","1");
	public static final String DEV_AC_ABILITY = config.getIni("REPORT_DEV_AC_ABILITY","[\"00010001\",\"00040001\",\"00040002\",\"00050001\",\"00060001\",\"00070001\",\"00080001\",\"00090001\",\"000C0001\",\"000D0005\",\"000D0006\",\"000D0007\"]");
	public static final String DEV_AC_WEBLOGIN_PWD = config.getIni("REPORT_DEV_AC_WEBLOGIN_PWD","admin");
	public static final String DEV_AC_LOG = config.getIni("REPORT_DEV_AC_LOG","1");
	public static final String DEV_AC_ALARM = config.getIni("REPORT_DEV_AC_ALARM","0");
	public static final String DEV_AC_REMOTE = config.getIni("REPORT_DEV_AC_REMOTE","{\"enable\":\"1\",\"url\":\"http://44d1fae2c582.remote.yowifi.net:30012\"}");
}
