package com.cpm.qadtest.Constant;

public class CommonString {

    public static final String MESSAGE_SOCKETEXCEPTION = "Network Communication Failure. Please Check Your Network Connection";
    public static final String URL = "http://test.parinaam.in/Webservice/QADWebservice.svc/";
  //  public static final String URL = "http://intelre.parinaam.in/webservice/intelwebservice.svc/";
    public static final String MESSAGE_INTERNET_NOT_AVALABLE = "No Internet Connection.Please Check Your Network Connection";
    public static final String KEY_IS_QUIZ_DONE = "is_quiz_done";
    public static final String KEY_QUESTION_CD = "question_cd";
    public static final String KEY_ANSWER_CD = "answer_cd";
    public static final String KEY_DATE = "DATE";
    public static final String TABLE_USER_DATA = "User_Data";


    public static String KEY_ID = "Id";
    public static final String KEY_USER_ID = "User_Id";
    public static final String KEY_USER_NAME = "User_Name";

/*
    public static final String CREATE_TABLE_USER_DATA = "CREATE TABLE  IF NOT EXISTS "
            + TABLE_USER_DATA + " (" + KEY_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + KEY_USER_ID
            + " VARCHAR , "
            + KEY_USER_NAME + " VARCHAR)";
*/

  public static final String CREATE_TABLE_USER_DATA = "CREATE TABLE "
          + TABLE_USER_DATA + " (" + KEY_ID
          + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
          + KEY_USER_ID + " INTEGER,"
          + KEY_USER_NAME + " VARCHAR)";
}
