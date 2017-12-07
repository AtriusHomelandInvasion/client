package com.jukusoft.mmo.utils;

import java.io.File;

public class LogUtils {

    /**
    * get path to log directory
     *
     * @param appName name of application
     *
     * @return log directory path, in most cases user.home/.APPNAME/logs/
    */
    public static String getLogPath (String appName) {
        return FileUtils.getAppHomeDir(appName) + "/logs/";
    }

    /**
    * create logs directory, if directory doesnt exists
    */
    public static void createLogDirIfAbsent () {
        String path = getLogPath(AppUtils.getAppName());

        //create directory, if not exists
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
    }

}
