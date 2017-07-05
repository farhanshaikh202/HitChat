package com.farhanapps.HitChat.utils;

/**
 * Created by farhan on 14-04-2016.
 */
public class DatabaseUtils {
    /**
     *
     * While storing in database
     * */

    public static String sqlEncodeString(String aString) {

        String aReturn = "";

        if (null != aString) {
            aReturn = aString.replace("'", "''");
            //aReturn = DatabaseUtils.sqlEscapeString(aString);
            // Remove the enclosing single quotes ...
            //aReturn = aReturn.substring(1, aReturn.length() - 1);
        }

        return aReturn;
    }




    /**
     *
     * While reading from database
     * */

    public static String sqlDecodeString(String aString) {

        String aReturn = "",sub="";

        if (null != aString) {
            aReturn = aString.replace("\"", "\\\"");
        }

        return aReturn;
    }
}
