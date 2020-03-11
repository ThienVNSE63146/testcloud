package com.fpt.submission.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticalInfo implements Serializable {
    private String name;
    private String examCode;
}
