package com.fpt.submission.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPointDto implements Serializable {

    private String studentCode;
    private Map<String, String> listQuestions;
    private String totalPoint;
    private String submitTime;
    private String evaluateTime;
    private Double codingConvention;
    private String result;
    private String errorMsg;

}