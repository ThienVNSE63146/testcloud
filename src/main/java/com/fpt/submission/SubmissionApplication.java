package com.fpt.submission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.submission.dto.request.PathDetails;
import com.fpt.submission.utils.PathUtils;
import com.fpt.submission.dto.request.PracticalInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class SubmissionApplication {

    public static void main(String[] args) {
        getPracticalInfo();
        SpringApplication.run(SubmissionApplication.class, args);
    }

    private static void getPracticalInfo() {

        ObjectMapper objectMapper = new ObjectMapper();
        PathUtils pathUtils = new PathUtils();
        PracticalInfo info = null;
        try {
            String practicalInfoPath = PathUtils.PROJECT_DIR
                    + File.separator
                    + PathUtils.PRACTICAL_INFO_FILE_NAME;
            File file = new File(practicalInfoPath);
            if (file != null) {
                info = objectMapper.readValue(file, PracticalInfo.class);
                if (info != null) {
                    PathDetails pathDetails = new PathDetails(info);
                    pathUtils.setPathDetails(pathDetails);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //print customer details
        System.out.println(info);
    }
}
