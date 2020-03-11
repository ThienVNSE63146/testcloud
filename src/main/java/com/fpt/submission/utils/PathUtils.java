package com.fpt.submission.utils;

import com.fpt.submission.dto.request.PathDetails;

public class PathUtils {

    public PathUtils() {
    }

    public static PathDetails pathDetails = null;

    public void setPathDetails(PathDetails pathDetails) {
        this.pathDetails = pathDetails;
    }

    public static final String PRACTICAL_INFO_FILE_NAME = "practical-info.json";

    public static final String PROJECT_DIR = System.getProperty("user.dir");


}
