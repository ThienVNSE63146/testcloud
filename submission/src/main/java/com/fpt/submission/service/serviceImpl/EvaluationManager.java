package com.fpt.submission.service.serviceImpl;

import static com.fpt.submission.constants.CommonConstant.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fpt.submission.dto.request.PathDetails;
import com.fpt.submission.dto.request.StudentPointDto;
import com.fpt.submission.exception.CustomException;
import com.fpt.submission.utils.*;
import com.fpt.submission.dto.request.StudentSubmitDetail;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@EnableAsync
@Service
public class EvaluationManager {

    private Boolean isEvaluating;
    private Queue<StudentSubmitDetail> submissionQueue;
    private List<String> examScriptsList;
    private Path sourceScriptPath = null;
    private Path serverTestScriptPath = null;
    private String PREFIX_EXAM_SCRIPT = "EXAM_";
    private PathDetails pathDetails;
    private static final String START_POINT_ARR = "START_POINT_ARR";
    private static final String END_POINT_ARR = "END_POINT_ARR";
    private static final String COMPILE_ERROR = "COMPILATION ERROR";
    private boolean isNew = true;
    @Autowired
    public EvaluationManager() {
        isEvaluating = false;
        submissionQueue = new PriorityQueue<>();
        pathDetails = PathUtils.pathDetails;
        examScriptsList = getExamScriptsList();
    }

    // Get all exams code in TestScript folder
    private List<String> getExamScriptsList() {
        PathDetails pathDetails = PathUtils.pathDetails;
        List<String> result = null;
        if (pathDetails != null) {
            try {
                result = new ArrayList<>();
                String s = pathDetails.getPathTestScripts() + File.separator;
                File folder = new File(s);
                if (folder != null) {
                    for (final File file : folder.listFiles()) {
                        if (file.isFile()) {
                            result.add(file.getName());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Async
    @EventListener
    public void evaluate(StudentSubmitDetail submissionEvent) {
        System.out.println(Thread.currentThread().getName() + "-" + submissionEvent.getStudentCode() +":"+isEvaluating);
        submissionQueue.add(submissionEvent);
        if (!isEvaluating && submissionQueue.size() > 0) {
            isEvaluating = true;
            switch (pathDetails.getExamCode()) {
                case CODE_PRACTICAL_C:
                    evaluateSubmissionC(submissionQueue.remove());
                    break;
                case CODE_PRACTICAL_JAVA_WEB:
                    evaluateSubmissionJavaWeb(submissionQueue.remove());
                    break;
                case CODE_PRACTICAL_CSharp:
                    evaluateSubmissionCSharp(submissionQueue.remove());
                    break;
                case CODE_PRACTICAL_JAVA:
                    evaluateSubmissionJava(submissionQueue.remove());
                    break;
                default:
                    throw new CustomException(HttpStatus.NOT_FOUND, "Not found Path Details Exam code");
            }
        } else {
            Logger.getLogger(SubmissionUtils.class.getName())
                    .log(Level.ERROR, "[EVALUATE] Waiting - : " + submissionEvent.getStudentCode());
        }
    }


    private void evaluateSubmissionC(StudentSubmitDetail dto) {
        try {
            Logger.getLogger(SubmissionUtils.class.getName())
                    .log(Level.INFO, "[EVALUATE] Student code : " + dto.getStudentCode());

            sourceScriptPath = null;
            serverTestScriptPath = null;
            if (examScriptsList.size() == 0)
                throw new CustomException(HttpStatus.NOT_FOUND, "No exam codes");
            for (String scriptCode : examScriptsList) {
                if (dto.getScriptCode().contains(scriptCode.replace(EXTENSION_C, ""))) {
                    sourceScriptPath = Paths.get(pathDetails.getPathTestScripts() + File.separator + scriptCode);
                    serverTestScriptPath = Paths.get(pathDetails.getPathTestCFol() + File.separator + PREFIX_EXAM_SCRIPT + dto.getStudentCode() + "_" + scriptCode);
                    break;
                }
            }

            if (sourceScriptPath == null && serverTestScriptPath == null) {
                System.out.println("[PATH-SCRIPT-ERROR]" + dto.getStudentCode() + "-" + dto.getScriptCode());
                return;
            }
            ZipFile.unzip(pathDetails.getPathSubmission() + File.separator + dto.getStudentCode() + EXTENSION_ZIP, pathDetails.getPathCSubmit());
            Files.copy(sourceScriptPath, serverTestScriptPath);

            // Chạy CMD file test
            CmdExcution.execute(pathDetails.getCExecuteCmd(PREFIX_EXAM_SCRIPT + dto.getStudentCode() + "_" + dto.getScriptCode()));


            String questionPointArrStr = "";
            List<String> content = Files.readAllLines(serverTestScriptPath);
            for (String line : content) {
                if (line.contains(START_POINT_ARR) && line.contains(END_POINT_ARR)) {
                    questionPointArrStr = line.replace(START_POINT_ARR, "")
                            .replace(END_POINT_ARR, "").trim();

                    break;
                }
            }
            generateCTestResult(questionPointArrStr, dto.getStudentCode());

            if (submissionQueue.size() > 0) {
                deleteAllFile(dto.getStudentCode(), pathDetails.getPathCSubmitDelete());
                evaluateSubmissionC(submissionQueue.remove());
            } else {
                isEvaluating = false;
            }
            // Trả status đã chấm xong về app lec winform (mssv)
            System.out.println("Trả response cho giảng viên");
        } catch (Exception e) {
            Logger.getLogger(EvaluationManager.class.getName())
                    .log(Level.ERROR, "[EVALUATE-ERROR] Student code : " + dto.getStudentCode());
            e.printStackTrace();
        } finally {
            deleteAllFile(dto.getStudentCode(), pathDetails.getPathCSubmitDelete());
            isEvaluating = false;
        }
    }

    private void generateCTestResult(String questionPointArrStr, String studentCode) {
        StudentPointDto studentPointDto = new StudentPointDto();
        try {
            // Get evaluated submission result
            File xml = new File(pathDetails.getPathCXMLResultFile());
            if (xml.isFile()) {

                String[] questions = questionPointArrStr.split("-");
                if (questions == null) {
                    throw new CustomException(HttpStatus.NOT_FOUND, "Not found Question point array");
                }

                // Get question point array
                Map<String, Double> questionPointMap = new HashMap<>();

                for (int i = 0; i < questions.length; i++) {
                    String[] arr = questions[i].split(":");
                    String questionName = arr[0];
                    Double point = Double.valueOf(arr[1]);
                    questionPointMap.put(questionName, point);
                }


                ObjectWriter w = new ObjectMapper().writerWithDefaultPrettyPrinter();
                Object o = null;
                o = new XmlMapper()
                        .registerModule(new SimpleModule().addDeserializer(Object.class, new JsonUtils()))
                        .readValue(xml, Object.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(w.writeValueAsString(o));
                if (rootNode != null) {
                    List<JsonNode> nodeSuiteSuccessList = rootNode.findValues("CUNIT_RUN_TEST_SUCCESS");
                    List<JsonNode> nodeSuiteFailedList = rootNode.findValues("CUNIT_RUN_TEST_FAILURE");


                    Map<String, String> listQuestions = new HashMap<>();
                    double totalPoint = 0;
                    Integer correctQuestionCount = 0;
                    String resultText = "";
                    resultText += studentCode + ":\n";
                    String time = TimeUtils.getCurTime();

                    if (nodeSuiteSuccessList != null && nodeSuiteSuccessList.size() > 0) {
                        for (JsonNode node : nodeSuiteSuccessList) {
                            JsonNode testNameNode = node.findValue("TEST_NAME");
                            if (testNameNode != null) {
                                String testName = testNameNode.textValue();
                                for (Map.Entry<String, Double> entry : questionPointMap.entrySet()) {
                                    if (testName.trim().equalsIgnoreCase(entry.getKey().trim())) {
                                        String key = entry.getKey();
                                        Double value = entry.getValue();
                                        resultText += key + ": Passed \n";
                                        totalPoint += value;
                                        correctQuestionCount++;
                                        listQuestions.put(key, value + "/" + value);
                                    }
                                }
                            }
                        }
                    }
                    if (nodeSuiteFailedList != null && nodeSuiteFailedList.size() > 0) {
                        for (JsonNode node : nodeSuiteFailedList) {
                            JsonNode testNameNode = node.findValue("TEST_NAME");
                            if (testNameNode != null) {
                                String testName = testNameNode.textValue();
                                for (Map.Entry<String, Double> entry : questionPointMap.entrySet()) {
                                    if (testName.equalsIgnoreCase(entry.getKey())) {
                                        String key = entry.getKey();
                                        Double value = entry.getValue();
                                        resultText += key + ": Failed \n";
                                        listQuestions.put(key, "0/" + value);
                                    }
                                }
                            }
                        }
                    }

                    resultText += "Time : " + time + "\n";
                    resultText += "Result : " + correctQuestionCount + " / " + questionPointMap.size() + "\n";
                    resultText += "Total : " + totalPoint + "\n";
                    if (SubmissionUtils.saveResultCSubmission(resultText, pathDetails.getResultTextFilePath())) {
                        System.out.println("Successfully");
                    }

                    studentPointDto.setStudentCode(studentCode);
                    studentPointDto.setListQuestions(listQuestions);
                    studentPointDto.setTotalPoint(String.valueOf(totalPoint));
                    studentPointDto.setEvaluateTime(time);
                    studentPointDto.setResult(correctQuestionCount + "/" + questionPointMap.size());
                    String jsonStr = mapper.writeValueAsString(studentPointDto);
                    // Send json to Lecturer App
                    System.out.println(jsonStr);
                }
            }
        } catch (IOException e) {
            studentPointDto.setErrorMsg("[EVALUATE-ERROR] - " + studentCode + ": System error");
            Logger.getLogger(EvaluationManager.class.getName())
                    .log(Level.INFO, "[EVALUATE] Student code : " + studentCode);
            e.printStackTrace();
        }
    }

    private void evaluateSubmissionJava(StudentSubmitDetail dto) {
        try {
            Logger.getLogger(EvaluationManager.class.getName())
                    .log(Level.INFO, "[EVALUATE] Student code : " + dto.getStudentCode());
            sourceScriptPath = null;
            serverTestScriptPath = null;
            if (examScriptsList.size() == 0)
                throw new CustomException(HttpStatus.NOT_FOUND, "No exam codes");
            for (String scriptCode : examScriptsList) {
                if (dto.getScriptCode().contains(scriptCode.replace(EXTENSION_JAVA, ""))) {
                    sourceScriptPath = Paths.get(pathDetails.getPathTestScripts() + File.separator + scriptCode);
                    serverTestScriptPath = Paths.get(pathDetails.getPathTestJavaFol() + PREFIX_EXAM_SCRIPT + dto.getStudentCode() + "_" + scriptCode);
                    break;
                }
            }
            //copy source to target using Files Class
            if (sourceScriptPath == null && serverTestScriptPath == null) {
                System.out.println("[PATH-SCRIPT-ERROR]" + dto.getStudentCode() + "-" + dto.getScriptCode());
                return;
            }
            Files.copy(sourceScriptPath, serverTestScriptPath);
            String a = pathDetails.getPathSubmission() + File.separator + dto.getStudentCode() + ".zip";
            String b = pathDetails.getPathJavaSubmit();
            ZipFile.unzip(pathDetails.getPathSubmission() + File.separator + dto.getStudentCode() + ".zip", pathDetails.getPathJavaSubmit());

            // Chạy CMD file test
            CmdExcution.execute(pathDetails.getJavaExecuteCmd());
            if (submissionQueue.size() > 0) {
                deleteAllFile(dto.getStudentCode(), pathDetails.getPathJavaSubmitDelete());
                evaluateSubmissionJava(submissionQueue.remove());
            } else {
                isEvaluating = false;
            }

            // Trả status đã chấm xong về app lec winform (mssv)
            System.out.println("Trả response cho giảng viên");
        } catch (Exception e) {
            Logger.getLogger(EvaluationManager.class.getName())
                    .log(Level.ERROR, "[EVALUATE-ERROR] Student code : " + dto.getStudentCode());
            e.printStackTrace();
        } finally {
            deleteAllFile(dto.getStudentCode(), pathDetails.getPathJavaSubmitDelete());
            isEvaluating = false;
        }
    }

    private void evaluateSubmissionJavaWeb(StudentSubmitDetail dto) {

    }


    private void evaluateSubmissionCSharp(StudentSubmitDetail dto) {
        try {
            Logger.getLogger(SubmissionUtils.class.getName())
                    .log(Level.INFO, "[EVALUATE] Student code : " + dto.getStudentCode());

            sourceScriptPath = null;
            serverTestScriptPath = null;
            if (examScriptsList.size() == 0)
                throw new CustomException(HttpStatus.NOT_FOUND, "No exam codes");
            for (String scriptCode : examScriptsList) {
                if (dto.getScriptCode().contains(scriptCode.replace(EXTENSION_CSHARP, ""))) {
                    sourceScriptPath = Paths.get(pathDetails.getPathTestScripts() + File.separator + scriptCode);
                    serverTestScriptPath = Paths.get(pathDetails.getPathTestCSharpFol() + PREFIX_EXAM_SCRIPT + dto.getStudentCode() + "_" + scriptCode);
                    break;
                }
            }
            //copy source to target using Files Class
            if (sourceScriptPath == null && serverTestScriptPath == null) {
                System.out.println("[PATH-SCRIPT-ERROR]" + dto.getStudentCode() + "-" + dto.getScriptCode());
                return;
            }
            Files.copy(sourceScriptPath, serverTestScriptPath);
            ZipFile.unzip(pathDetails.getPathSubmission() + File.separator + dto.getStudentCode() + ".zip", pathDetails.getPathCSharpSubmit());

            // Chạy CMD file test
            CmdExcution.execute(pathDetails.getCSharpExecuteCmd());

            if (submissionQueue.size() > 0) {
                deleteAllFile(dto.getStudentCode(), pathDetails.getPathCSharpSubmitDelete());
                evaluateSubmissionJava(submissionQueue.remove());
            } else {
                isEvaluating = false;
            }
            // Trả status đã chấm xong về app lec winform (mssv)
            System.out.println("Trả response cho giảng viên");
        } catch (Exception e) {
            Logger.getLogger(EvaluationManager.class.getName())
                    .log(Level.ERROR, "[EVALUATE-ERROR] Student code : " + dto.getStudentCode());
            e.printStackTrace();
        } finally {
            deleteAllFile(dto.getStudentCode(), pathDetails.getPathCSharpSubmitDelete());
        }
    }


    private void deleteAllFile(String studentCode, String pathSubmit) {

        File file = new File(pathSubmit);
        if (file != null && SubmissionUtils.deleteFolder(file)) {
            System.out.println("[DELETE SUBMISSION - SERVER] - " + studentCode);
        }

        if (serverTestScriptPath != null) {
            File scriptFile = new File(serverTestScriptPath.toString());
            if (scriptFile != null && scriptFile.delete()) {
                System.out.println("[DELETE SCRIPT - SERVER] - " + studentCode);
            }
        }
    }

    @Bean
    TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("[THREAD-EVALUATE]-");
        executor.initialize();
        return executor;
    }

    private Boolean checkCompileIsError(String outputLogPath) {
        List<String> content = null;
        try {
            content = Files.readAllLines(Paths.get(outputLogPath));
            for (String line : content) {
                if (line.contains(COMPILE_ERROR)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
