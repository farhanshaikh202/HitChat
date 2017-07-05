package com.farhanapps.HitChat.utils;

/**
 * to andi db
 */
public class StringUtils {
	
	public static String filterAndi(String str)
	{
		str=str.replace("'", "''");
		return str;
	}

	/***
     * for php or to PHP
	 */
	public static String filterPHP(String str)
	{
		str=str.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "''");
		return str;
	}
}
