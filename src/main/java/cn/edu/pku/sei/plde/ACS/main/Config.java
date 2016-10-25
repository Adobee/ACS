package cn.edu.pku.sei.plde.ACS.main;

import com.sun.org.apache.regexp.internal.RE;

/**
 * Created by yanrunfa on 5/8/16.
 */
public class Config {
    //---------------Timeout Config------------------
    public static int TOTAL_RUN_TIMEOUT = 3600;

    public static final int SHELL_RUN_TIMEOUT = TOTAL_RUN_TIMEOUT/5;
    public static final int GZOLTAR_RUN_TIMEOUT = TOTAL_RUN_TIMEOUT/10;
    public static final int SEARCH_BOUNDARY_TIMEOUT = TOTAL_RUN_TIMEOUT/5;

    //--------------Result Path Config------------------
    public static final String RESULT_PATH = "resultMessage";
    public static final String PATCH_PATH = RESULT_PATH + "/patch";
    public static final String PATCH_SOURCE_PATH = RESULT_PATH + "/patchSource";
    public static final String LOCALIZATION_PATH = RESULT_PATH + "/localization";
    public static final String RUNTIMEMESSAGE_PATH = RESULT_PATH + "/runtimeMessage";
    public static final String PREDICATE_MESSAGE_PATH = RESULT_PATH + "/predicateMessage";
    public static final String FIX_RESULT_FILE_PATH = RESULT_PATH + "/fixResult.log";


    //--------------Runtime Path Config--------------------
    public static final String TEMP_FILES_PATH = ".temp/";
    public static final String LOCALIZATION_RESULT_CACHE = ".suspicious/";
}