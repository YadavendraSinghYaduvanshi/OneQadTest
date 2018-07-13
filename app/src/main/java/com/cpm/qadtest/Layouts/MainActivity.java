package com.cpm.qadtest.Layouts;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cpm.qadtest.Constant.AlertandMessages;
import com.cpm.qadtest.Constant.CommonFunction;
import com.cpm.qadtest.Constant.CommonString;
import com.cpm.qadtest.Database.QudTestDB;
import com.cpm.qadtest.GetterSetter.QuestionGsonGetterSetter;
import com.cpm.qadtest.GetterSetter.UserDatum;
import com.cpm.qadtest.GetterSetter.UserDatumGetterSetter;
import com.cpm.qadtest.PostApi;
import com.cpm.qadtest.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    TextView dateTxt;
    DatePickerDialog datePickerDialog;
    Button submitBtn;
    private Retrofit adapter;
    String selectedUser,user_Id,visit_date;
    ProgressDialog loading;
    private Context context;
    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;
    String right_answer, rigth_answer_cd = "", qns_cd, ans_cd;
    Spinner spin;
    QudTestDB db;
    int mYear,mMonth,mDay;
    ArrayList<UserDatum> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        db = new QudTestDB(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        downloadUserList();
        declartion();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 visit_date = dateTxt.getText().toString();
                if(visit_date.equals("")){
                    showToast("Please select date");
                }else if(user_Id.equals("")){
                    showToast("Please select user");
                }
                else{
                    if (CommonFunction.checkNetIsAvailable(MainActivity.this)){
                        //Download Todays Questions
                        JSONObject jsonObject = new JSONObject();

                        try {
                           loading = ProgressDialog.show(MainActivity.this, "Processing", "Please wait...", false, false);
                         /*   jsonObject.put("VisitDate", val.toString());*/
                            jsonObject.put("Downloadtype", "Today_Question");
                            jsonObject.put("Username", user_Id+":"+visit_date);
                            String jsonString = jsonObject.toString();
                            final String[] data_global = {""};
                            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                    .readTimeout(20, TimeUnit.SECONDS)
                                    .writeTimeout(20, TimeUnit.SECONDS)
                                    .connectTimeout(20, TimeUnit.SECONDS)
                                    .build();
                            RequestBody questionjsonData = RequestBody.create(MediaType.parse("application/json"),
                                    jsonString);
                            adapter = new Retrofit.Builder().baseUrl(CommonString.URL).client(okHttpClient)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            PostApi api1 = adapter.create(PostApi.class);
                            Call<ResponseBody> callquest = api1.downloadAllQuestionData(questionjsonData);
                            callquest.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    ResponseBody responseBody = response.body();
                                    String data = null;
                                    if (responseBody != null && response.isSuccessful()) {
                                        try {
                                            data = response.body().string();
                                            data = data.substring(1, data.length() - 1).replace("\\", "");
                                            if (data.equalsIgnoreCase(CommonString.MESSAGE_SOCKETEXCEPTION)) {
                                                loading.dismiss();
                                                AlertandMessages.showAlertlogin(MainActivity.this, "Check Your Internet Connection");
                                            } else if (data.contains("No Data")) {
                                                loading.dismiss();
                                                showToast("No Data Found");
                                            }else{
                                                Gson gs = new Gson();
                                                final QuestionGsonGetterSetter userques = gs.fromJson(data.toString().trim(), QuestionGsonGetterSetter.class);
                                           /*     if (preferences.getString(CommonString.KEY_VERSION, "").equals(Integer.toString(versionCode))) {*/
                                                    loading.dismiss();
                                               /*     final String visit_date = preferences.getString(CommonString.KEY_DATE, "");*/
                                                    if (userques.getTodayQuestion().size() > 0 && userques.getTodayQuestion().get(0).getStatus().equals("N") &&
                                                            !preferences.getBoolean(CommonString.KEY_IS_QUIZ_DONE + visit_date, false)) {
                                                        for (int i = 0; i < userques.getTodayQuestion().size(); i++) {
                                                            if (userques.getTodayQuestion().get(i).getRightAnswer().toString().equalsIgnoreCase("true")) {
                                                                right_answer = userques.getTodayQuestion().get(i).getAnswer();
                                                                rigth_answer_cd = userques.getTodayQuestion().get(i).getAnswerId().toString();
                                                                break;
                                                            }
                                                        }
                                                        final AnswerData answerData = new AnswerData();
                                                        final Dialog customD = new Dialog(MainActivity.this);
                                                        customD.setTitle("Todays Question");
                                                        customD.setCancelable(false);
                                                        customD.setContentView(R.layout.show_answer_layout);
                                                        customD.setContentView(R.layout.todays_question_layout);
                                                        ((TextView) customD.findViewById(R.id.tv_qns)).setText(userques.getTodayQuestion().get(0).getQuestion());
                                                        Button btnsubmit = (Button) customD.findViewById(R.id.btnsubmit);
                                                        final TextView txt_timer = (TextView) customD.findViewById(R.id.txt_timer);
                                                        RadioGroup radioGroup = (RadioGroup) customD.findViewById(R.id.radiogrp);
                                                        new CountDownTimer(30000, 1000) {
                                                            public void onTick(long millisUntilFinished) {
                                                                txt_timer.setText("seconds remaining: " + millisUntilFinished / 1000);
                                                                //here you can have your logic to set text to edittext
                                                            }

                                                            public void onFinish() {
                                                                if (answerData.getAnswer_id() == null || answerData.getAnswer_id().equals("")) {
                                                                    txt_timer.setText("done!");
                                                                    customD.cancel();
                                                                    String ansisright = "";
                                                                    ansisright = "Your Time is over";
                                                                    final Dialog ans_dialog = new Dialog(MainActivity.this);
                                                                    ans_dialog.setTitle("Answer");
                                                                    ans_dialog.setCancelable(false);
                                                                    //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                                    ans_dialog.setContentView(R.layout.show_answer_layout);
                                                                    ((TextView) ans_dialog.findViewById(R.id.tv_ans)).setText(ansisright);
                                                                    Button btnok = (Button) ans_dialog.findViewById(R.id.btnsubmit);
                                                                    btnok.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            answerData.setQuestion_id(userques.getTodayQuestion().get(0).getQuestionId().toString());
                                                                            answerData.setUsername(selectedUser);
                                                                            answerData.setVisit_date(visit_date);
                                                                            if (CommonFunction.checkNetIsAvailable(MainActivity.this)) {
                                                                                ans_dialog.cancel();
                                                                                try {
                                                                                    JSONArray answerDetaills = new JSONArray();
                                                                                    JSONObject object = new JSONObject();

                                                                                    //region Deviation_journeyplan Data
                                                                                    object.put("ANSWER_ID", "0");
                                                                                    object.put("QUESTION_ID", answerData.getQuestion_id());
                                                                                    object.put("VISIT_DATE", answerData.getVisit_date());
                                                                                    object.put("USER_NAME", answerData.getUsername());
                                                                                    answerDetaills.put(object);

                                                                                    object = new JSONObject();
                                                                                    object.put("MID", "0");
                                                                                    object.put("Keys", "TODAY_ANSWER");
                                                                                    object.put("JsonData", answerDetaills.toString());
                                                                                    object.put("UserId", user_Id);

                                                                                    String jsonString = object.toString();
                                                                                    if (jsonString != null && !jsonString.equalsIgnoreCase("")) {

                                                                                        loading.setMessage("Uploading answer data..");
                                                                                        RequestBody jsonData = RequestBody.create(MediaType.parse("application/json"), jsonString);
                                                                                        adapter = new Retrofit.Builder().baseUrl(CommonString.URL).client(okHttpClient).
                                                                                                addConverterFactory(GsonConverterFactory.create()).build();
                                                                                        PostApi api = adapter.create(PostApi.class);
                                                                                        Call<ResponseBody> call = api.getUploadJsonDetail(jsonData);
                                                                                        call.enqueue(new Callback<ResponseBody>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                                ResponseBody responseBody = response.body();
                                                                                                String data = null;
                                                                                                if (responseBody != null && response.isSuccessful()) {
                                                                                                    try {
                                                                                                        data = response.body().string();
                                                                                                        if (data.equalsIgnoreCase("")) {
                                                                                                        } else {
                                                                                                            data = data.substring(1, data.length() - 1).replace("\\", "");
                                                                                                            data_global[0] = data;
                                                                                                            if (data.contains("Success")) {
                                                                                                                String visit_date = preferences.getString(CommonString.KEY_DATE, null);
                                                                                                                editor = preferences.edit();
                                                                                                                editor.putBoolean(CommonString.KEY_IS_QUIZ_DONE + visit_date, true);
                                                                                                                editor.commit();
                                                                                                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                                                                                                startActivity(intent);
                                                                                                                finish();
                                                                                                            } else {
                                                                                                                editor = preferences.edit();
                                                                                                                editor.putString(CommonString.KEY_QUESTION_CD +
                                                                                                                        visit_date, qns_cd);
                                                                                                                editor.putString(CommonString.KEY_ANSWER_CD +
                                                                                                                        visit_date, ans_cd);
                                                                                                                editor.commit();
                                                                                                              /*  Intent intent = new Intent(getBaseContext(), MainMenuActivity.class);
                                                                                                                startActivity(intent);
                                                                                                                finish();*/
                                                                                                            }
                                                                                                        }

                                                                                                    } catch (Exception e) {
                                                                                                        loading.dismiss();
                                                                                                        AlertandMessages.showAlertlogin(MainActivity.this,
                                                                                                                CommonString.MESSAGE_SOCKETEXCEPTION);
                                                                                                    }
                                                                                                } else {
                                                                                                    loading.dismiss();
                                                                                                    AlertandMessages.showAlertlogin(
                                                                                                            MainActivity.this, "Check Your Internet Connection");

                                                                                                }
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                                                                if (t instanceof SocketTimeoutException) {
                                                                                                    AlertandMessages.showAlert((Activity) context,
                                                                                                            CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                                                                                                } else {
                                                                                                    AlertandMessages.showAlert((Activity) context,
                                                                                                            CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                                                                                                }

                                                                                            }
                                                                                        });

                                                                                    }
                                                                                    ans_dialog.cancel();
                                                                                } catch (JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            } else {
                                                                                showToast("No internet connection");
                                                                            }
                                                                        }
                                                                    });
                                                                    ans_dialog.show();
                                                                }
                                                            }
                                                        }.start();

                                                        for (int i = 0; i < userques.getTodayQuestion().size(); i++) {
                                                            RadioButton rdbtn = new RadioButton(MainActivity.this);
                                                            rdbtn.setId(i);
                                                            rdbtn.setText(userques.getTodayQuestion().get(i).getAnswer());
                                                            radioGroup.addView(rdbtn);
                                                        }

                                                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                                            @Override
                                                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                                                answerData.setAnswer_id(userques.getTodayQuestion().get(checkedId).getAnswerId().toString());
                                                                answerData.setRight_answer(userques.getTodayQuestion().get(checkedId).getRightAnswer().toString());
                                                            }
                                                        });

                                                        btnsubmit.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (answerData.getAnswer_id() == null || answerData.getAnswer_id().equals("")) {
                                                                    Snackbar.make(submitBtn, "First select an answer", Snackbar.LENGTH_SHORT).show();
                                                                } else {
                                                                    customD.cancel();
                                                                    String ansisright = "";
                                                                    if (answerData.getRight_answer().equalsIgnoreCase("true")) {
                                                                        ansisright = "Your Answer Is Right!";
                                                                    } else {
                                                                        ansisright = "Your Answer is Wrong! Right Answer Is :- " + right_answer;
                                                                    }
                                                                    final Dialog ans_dialog = new Dialog(MainActivity.this);
                                                                    ans_dialog.setTitle("Answer");
                                                                    ans_dialog.setCancelable(false);
                                                                    ans_dialog.setContentView(R.layout.show_answer_layout);
                                                                    ((TextView) ans_dialog.findViewById(R.id.tv_ans)).setText(ansisright);
                                                                    Button btnok = (Button) ans_dialog.findViewById(R.id.btnsubmit);
                                                                    btnok.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            answerData.setQuestion_id(userques.getTodayQuestion().get(0).getQuestionId().toString());
                                                                            answerData.setUsername(selectedUser);
                                                                            answerData.setVisit_date(visit_date);
                                                                            if (CommonFunction.checkNetIsAvailable(MainActivity.this)) {
                                                                                try {
                                                                                    JSONArray answerDetaills = new JSONArray();
                                                                                    JSONObject object = new JSONObject();

                                                                                    //region Deviation_journeyplan Data
                                                                                    object.put("ANSWER_ID", answerData.getAnswer_id());
                                                                                    object.put("QUESTION_ID", answerData.getQuestion_id());
                                                                                    object.put("VISIT_DATE", answerData.getVisit_date());
                                                                                    object.put("USER_NAME", answerData.getUsername());
                                                                                    answerDetaills.put(object);

                                                                                    object = new JSONObject();
                                                                                    object.put("MID", "0");
                                                                                    object.put("Keys", "TODAY_ANSWER");
                                                                                    object.put("JsonData", answerDetaills.toString());
                                                                                    object.put("UserId", user_Id);

                                                                                    String jsonString = object.toString();
                                                                                    if (jsonString != null && !jsonString.equalsIgnoreCase("")) {

                                                                                        loading.setMessage("Uploading answer data..");
                                                                                        RequestBody jsonData = RequestBody.create(MediaType.parse("application/json"), jsonString);
                                                                                        adapter = new Retrofit.Builder().baseUrl(CommonString.URL).client(okHttpClient).
                                                                                                addConverterFactory(GsonConverterFactory.create()).build();
                                                                                        PostApi api = adapter.create(PostApi.class);
                                                                                        Call<ResponseBody> call = api.getUploadJsonDetail(jsonData);
                                                                                        call.enqueue(new Callback<ResponseBody>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                                                ResponseBody responseBody = response.body();
                                                                                                String data = null;
                                                                                                if (responseBody != null && response.isSuccessful()) {
                                                                                                    try {
                                                                                                        data = response.body().string();
                                                                                                        // if (data.equalsIgnoreCase("")) {
                                                                                                        // data = data.substring(1, data.length() - 1).replace("\\", "");
                                                                                                        //  data_global[0] = data;
                                                                                                        if (data.contains("Success")) {
                                                                                                            loading.dismiss();
                                                                                                            String visit_date = preferences.getString(CommonString.KEY_DATE, null);
                                                                                                            editor = preferences.edit();
                                                                                                            editor.putBoolean(CommonString.KEY_IS_QUIZ_DONE + visit_date, true);
                                                                                                            editor.commit();

                                                                                                           // spin.setSelection(0);
                                                                                                            dateTxt.setText("");
                                                                                                           /* Intent intent = new Intent(getBaseContext(), MainMenuActivity.class);
                                                                                                            startActivity(intent);
                                                                                                            finish();*/
                                                                                                        } else {
                                                                                                            loading.dismiss();
                                                                                                            editor = preferences.edit();
                                                                                                            editor.putString(CommonString.KEY_QUESTION_CD + visit_date, qns_cd);
                                                                                                            editor.putString(CommonString.KEY_ANSWER_CD + visit_date, ans_cd);
                                                                                                            editor.commit();
                                                                                                           /* Intent intent = new Intent(getBaseContext(), MainMenuActivity.class);
                                                                                                            startActivity(intent);
                                                                                                            finish();*/
                                                                                                        }


                                                                                                    } catch (Exception e) {
                                                                                                        loading.dismiss();
                                                                                                        AlertandMessages.showAlertlogin(MainActivity.this,
                                                                                                                CommonString.MESSAGE_INTERNET_NOT_AVALABLE + "(" + e.toString() + ")");
                                                                                                    }
                                                                                                }
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                                loading.dismiss();
                                                                                                if (t instanceof SocketTimeoutException) {
                                                                                                    AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                                                                                                } else if (t instanceof IOException) {
                                                                                                    AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                                                                                                } else if (t instanceof SocketException) {
                                                                                                    AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                                                                                                } else {
                                                                                                    AlertandMessages.showAlert((Activity) context, CommonString.MESSAGE_INTERNET_NOT_AVALABLE, true);
                                                                                                }

                                                                                            }
                                                                                        });

                                                                                    }
                                                                                    ans_dialog.cancel();
                                                                                } catch (JSONException e) {
                                                                                    loading.dismiss();
                                                                                    e.printStackTrace();
                                                                                }
                                                                            } else {
                                                                                showToast("No internet connection");
                                                                            }
                                                                        }
                                                                    });
                                                                    ans_dialog.show();
                                                                }
                                                            }
                                                        });
                                                        customD.show();
                                                    } else {
                                                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                               /* } else {
                                                    Intent intent = new Intent(getBaseContext(), AutoUpdateActivity.class);
                                                    intent.putExtra(CommonString.KEY_PATH, preferences.getString(CommonString.KEY_PATH, ""));
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                */


                                            }


                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{
                        Toast.makeText(MainActivity.this, "Network Connection Failure", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                 Calendar c = Calendar.getInstance();
                 mYear = c.get(Calendar.YEAR); // current year
                 mMonth = c.get(Calendar.MONTH); // current month
                 mDay = c.get(Calendar.DAY_OF_MONTH); // current day

                 Calendar c1 = Calendar.getInstance();
                 mYear = c1.get(Calendar.YEAR); // current year
                 mMonth = c1.get(Calendar.MONTH); // current month
                // Set first date of month
                c1.set(Calendar.DAY_OF_MONTH,1);

                Calendar c2 = Calendar.getInstance();
                mYear = c2.get(Calendar.YEAR); // current year
                mMonth = c2.get(Calendar.MONTH); // current month
                // Set last date of month
                c2.set(Calendar.DATE, c2.getActualMaximum(Calendar.DATE));

               // c.set(Calendar.DAY_OF_MONTH,1);

                datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                dateTxt.setText((monthOfYear + 1) + "/"
                                        + dayOfMonth + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
     //           c.add(Calendar.DATE, 0);
                // Set the Calendar new date as minimum date of date picker
               // datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis() - 86400000 * 15);
                //datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis() + 86400000 * 15);

               // datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                datePickerDialog.getDatePicker().setMinDate(c1.getTimeInMillis());
                datePickerDialog.getDatePicker().setMaxDate(c2.getTimeInMillis());
                // date picker dialog

            }
        });
    }

    private void declartion() {

        dateTxt = (TextView) findViewById(R.id.date);
        submitBtn =(Button)findViewById(R.id.submit);
        spin = (Spinner) findViewById(R.id.userSpinner);

        db.open();
        userList = db.getUserData();
        ArrayAdapter userAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        userAdapter.add("Select User");

        for (int i = 0; i < userList.size(); i++) {
            userAdapter.add(userList.get(i).getEmployee());
        }
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(userAdapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0) {
                    selectedUser = String.valueOf(userList.get(position-1).getEmployee());
                    user_Id = String.valueOf(userList.get(position-1).getUserId());
                }else{
                    selectedUser = "";
                    user_Id = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void downloadUserList() {
        //Download User
        JSONObject jsonObject = new JSONObject();
        try {
            loading = ProgressDialog.show(MainActivity.this, "Downloading", "Users", false, false);

            jsonObject.put("Downloadtype", "User_Data");
            jsonObject.put("Username", "");

            String jsonString = jsonObject.toString();
            final String[] data_global = {""};
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();
            RequestBody questionjsonData = RequestBody.create(MediaType.parse("application/json"),
                    jsonString);
            adapter = new Retrofit.Builder().baseUrl(CommonString.URL).client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            PostApi api1 = adapter.create(PostApi.class);
            Call<ResponseBody> callquest = api1.downloadAllQuestionData(questionjsonData);
            callquest.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody responseBody = response.body();
                    String data = null;
                    if (responseBody != null && response.isSuccessful()) {
                        try {
                            data = response.body().string();
                            data = data.substring(1, data.length() - 1).replace("\\", "");
                            if (data.equalsIgnoreCase(CommonString.MESSAGE_SOCKETEXCEPTION)) {
                                loading.dismiss();
                                AlertandMessages.showAlertlogin(MainActivity.this, "Check Your Internet Connection");
                            } else if (data.contains("No Data")) {
                                loading.dismiss();
                                Toast.makeText(MainActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                            }else{
                                Gson gs = new Gson();
                                db.open();
                                final UserDatumGetterSetter userData = gs.fromJson(data.toString().trim(), UserDatumGetterSetter.class);
                                if(userData != null && db.insertUserData(userData) ){
                                    loading.dismiss();
                                    showToast("User Data Download Succesfully");
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    class AnswerData {
        public String question_id, answer_id, username, visit_date, right_answer;

        public String getQuestion_id() {
            return question_id;
        }

        public void setQuestion_id(String question_id) {
            this.question_id = question_id;
        }

        public String getAnswer_id() {
            return answer_id;
        }

        public void setAnswer_id(String answer_id) {
            this.answer_id = answer_id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getVisit_date() {
            return visit_date;
        }

        public void setVisit_date(String visit_date) {
            this.visit_date = visit_date;
        }

        public String getRight_answer() {
            return right_answer;
        }

        public void setRight_answer(String right_answer) {
            this.right_answer = right_answer;
        }
    }


    private void showToast(String message) {
        Snackbar.make(submitBtn, message, Snackbar.LENGTH_LONG).show();
    }
}
