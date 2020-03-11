package com.fpt.submission.dto.request;


import org.springframework.context.ApplicationEvent;

import java.io.Serializable;


public class StudentSubmitDetail extends ApplicationEvent implements Serializable, Comparable<StudentSubmitDetail> {
    private String studentCode;
    private String scriptCode;

    public StudentSubmitDetail(Object source, String studentCode,String scriptCode) {
        super(source);
        this.studentCode = studentCode;
        this.scriptCode = scriptCode;
    }


    public String getScriptCode() {
        return scriptCode;
    }

    public String getStudentCode() {
        return studentCode;
    }

    @Override
    public int compareTo(StudentSubmitDetail o) {
        return 0;
    }
}
