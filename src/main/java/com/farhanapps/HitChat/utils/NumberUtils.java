package com.farhanapps.HitChat.utils;

public class NumberUtils {

	public static String getNumber(String no){
		no=no.replace(" ", "").replace("-", "").replace("+", "").replace("(", "").replace(")", "");
		no=no.trim();
		if(no.length()>=10)
		no=no.substring(no.length()-10,no.length());
		return no;
	}
}
