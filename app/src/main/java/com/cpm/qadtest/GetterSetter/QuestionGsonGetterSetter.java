package com.cpm.qadtest.GetterSetter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.xml.transform.Result;

public class QuestionGsonGetterSetter {
    @SerializedName("Result")
    @Expose
    private List<Result> result = null;

    public List<Result> getResult() {
        return result;
    }
    public void setResult(List<Result> result) {
        this.result = result;
    }

    //today question
    @SerializedName("Today_Question")
    @Expose
    private List<TodayQuestion> todayQuestion = null;

    public List<TodayQuestion> getTodayQuestion() {
        return todayQuestion;
    }

    public void setTodayQuestion(List<TodayQuestion> todayQuestion) {
        this.todayQuestion = todayQuestion;
    }
}
