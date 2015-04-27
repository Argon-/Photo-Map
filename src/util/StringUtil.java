package util;

import java.io.File;



/**
 * Various String related utility methods.
 */
public class StringUtil
{
    
    /**
     * @return the basename of a supplied path f
     */
    public static String basename(String f)
    {
        if (f == null || f.equals("") || f.length() < 2) {
            return f;
        }
        
        String s = f;
        
        if (s.contains(".")) 
        {
            int e = s.lastIndexOf('.');
            if (e >= 0 && e != 0) {
                s = s.substring(0, e);
            }
        }
        
        if (s.contains("" + File.separatorChar))
        {
            int i = s.lastIndexOf(File.separatorChar);
            if (i >= 0 && i < s.length() - 1) {
                s = s.substring(i+1, s.length());
            }
        }
        
        return s;
    }
    

    /**
     * Join non-String arrays with "{@code ,}".
     */
    public static <T> String arrayToString(T[] n)
    {
        String s = "";
        for (int i = 0; i < n.length; ++i) {
            s += n[i];

            if (i != n.length - 1)
                s += ", ";
            else
                s += " ";
        }
        return s;
    }


    /**
     * Join non-String arrays with "{@code ,}".
     */
    public static String arrayToString(int[][] n)
    {
        String s = "";
        for (int i = 0; i < n.length; ++i) {
            s += n[i][1];

            if (i != n.length - 1)
                s += ", ";
            else
                s += " ";
        }
        return s;
    }


    /**
     * Join non-String arrays with "{@code ,}".
     */
    public static String arrayToString(int[] n)
    {
        String s = "";
        for (int i = 0; i < n.length; ++i) {
            s += n[i];

            if (i != n.length - 1)
                s += ", ";
            else
                s += " ";
        }
        return s;
    }
    
    
    /**
     * Join non-String arrays with "{@code ,}".
     */
    public static String arrayToString(long[] n)
    {
        String s = "";
        for (int i = 0; i < n.length; ++i) {
            s += n[i];
            
            if (i != n.length - 1)
                s += ", ";
            else
                s += " ";
        }
        return s;
    }

}
