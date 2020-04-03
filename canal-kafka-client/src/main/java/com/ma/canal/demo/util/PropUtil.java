package com.ma.canal.demo.util;

import java.io.*;
import java.util.Properties;

public class PropUtil {
    public static Properties getProp(String filePath){
        InputStream in = null;
        try {
            Properties prop = new Properties();

             in = PropUtil.class.getClassLoader().getResourceAsStream(filePath);
            prop.load(in);
            return prop;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
