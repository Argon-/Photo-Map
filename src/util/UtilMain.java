package util;

public class UtilMain
{

    public static void main(String[] args)
    {
        StringUtil.basename("/Users/Julian/Documents/Dropbox/Kamera-Uploads/2015-03-15 20.58.39.jpg");
        StringUtil.basename("/Users/Julian/Documents/Dropbox/Kamera-Uploads/2015-03-15 20.58.39");
        StringUtil.basename("2015-03-15 20.58.39.jpg");
        StringUtil.basename("2015");
        StringUtil.basename("/2015-03-15 20.58.39.");
        StringUtil.basename(".");
        StringUtil.basename("/");
        StringUtil.basename("/.");
        StringUtil.basename("/a.");
    }

}
