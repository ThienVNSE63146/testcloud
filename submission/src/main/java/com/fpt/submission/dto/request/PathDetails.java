package com.fpt.submission.dto.request;

import static com.fpt.submission.utils.PathUtils.*;


import java.io.File;
import java.io.Serializable;

public class PathDetails implements Serializable {

    private PracticalInfo practicalInfo;
    private String curPractical;
    private String practicalExamCode;
    private String pathSubmission;
    private String pathServer;
    private String pathLogFile;
    private String pathJavaSubmit;
    private String pathJavaSubmitDelete;
    private String pathJavaTestFol;
    private String javaExecuteCmd;

    public PathDetails(PracticalInfo practicalInfo) {
        this.practicalInfo = practicalInfo;
        curPractical = PROJECT_DIR + File.separator
                + "PracticalExams" + File.separator
                + practicalInfo.getName();
    }

    public String getCurPracticalPath() {
        return curPractical;
    }

    public String getExamCode() {
        return practicalInfo.getExamCode();
    }

    public String getPathSubmission() {
        return curPractical + File.separator + "Submissions";
    }

    public String getPathServer() {
        return curPractical + File.separator + "Server";
    }

    public String getPathServerLogFile() {
        return curPractical + File.separator + "Server" + File.separator + "output.log";
    }

    public String getResultTextFilePath() {
        return curPractical + File.separator + "Result.txt";
    }

    public String getPathTestScripts() {
        return curPractical + File.separator + "TestScripts";
    }


    // For Java
    public String getPathJavaSubmit() {
        return getPathServer() + File.separator
                + "src" + File.separator
                + "main" + File.separator
                + "java" + File.separator
                + "com" + File.separator
                + "practicalexam" + File.separator;
    }

    public String getPathJavaSubmitDelete() {
        return getPathServer() + File.separator
                + "src" + File.separator
                + "main" + File.separator
                + "java" + File.separator
                + "com" + File.separator
                + "practicalexam" + File.separator
                + "student";
    }

    public String getPathTestJavaFol() {
        return getPathServer()
                + File.separator
                + "src" + File.separator
                + "test" + File.separator
                + "java" + File.separator
                + "com" + File.separator
                + "practicalexam" + File.separator;
    }

    public String getPathJavaComFol() {
        return getPathJavaSubmit() + File.separator + "com";
    }

    public String getJavaExecuteCmd() {
        return "cd " + getPathServer() + "&mvn clean package";
    }

    // For C

    public String getPathTestCFol() {
        return getPathServer()
                + File.separator
                + "src";
    }

    public String getPathCXMLResultFile() {
        return getPathServer()
                + File.separator
                + "src" + File.separator +
                "CUnitAutomated-Results.xml";
    }

    public String getPathCSubmit() {
        return getPathTestCFol() + File.separator;
    }

    public String getPathCSubmitDelete() {
        return getPathServer() + File.separator
                + "src" + File.separator;
    }

    public String getCExecuteCmd(String scriptCode) {
        return "cd " + getPathTestCFol() + "&" +
                "gcc " + scriptCode + ".c -lcunit -o app&app.exe";
    }


//     For CSharp

    // Lưu đề thi
    public String getPathTestCSharpFol() {
        return getPathServer()
                + File.separator
                + "Tests" + File.separator
                + "Controllers" + File.separator;
    }

    public String getPathCSharpSubmit() {
        return getPathServer()
                + File.separator
                + "TemplateAutomatedTest" + File.separator;
    }

    public String getPathCSharpSubmitDelete() {
        return getPathServer()
                + File.separator
                + "TemplateAutomatedTest"
                + File.separator
                + "Student";
    }

    public String getCSharpExecuteCmd() {
        return "cd " + getPathServer() + "&dotnet clean&dotnet test";
    }
}
