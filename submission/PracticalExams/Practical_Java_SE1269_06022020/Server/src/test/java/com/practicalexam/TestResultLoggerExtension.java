package com.practicalexam;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.Serializable;
import java.util.Map;

public class TestResultLoggerExtension implements TestWatcher, AfterAllCallback {

    private String PROJECT_DIR = System.getProperty("user.dir");
    private String PATH_JAVA_FOLDER_TEST = PROJECT_DIR + File.separator
            + "src" + File.separator
            + "test" + File.separator
            + "java" + File.separator
            + "com" + File.separator
            + "practicalexam";

    private final String PREFIX_METHOD = "()";
    private final String PREFIX_TEST = "EXAM_";
    private final String TXT_RESULT_NAME = "Result.txt";
    public final String SOCKET_SERVER_LOCAL_HOST = "localhost";
    public final int SOCKET_SERVER_LISTENING_PORT = 9997;

    private Map<String, Double> testResultsStatus = new HashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private SocketUtils socketUtils = new SocketUtils();

    public void checkQuestionPoint(String nameQuestionCheck, boolean isCorrect) {
        String questionPointStr = JavaApplicationTests.questionPointStr;
        Double point = 0.0;
        if (questionPointStr != null && !questionPointStr.equals("")) {
            String[] questionArr = questionPointStr.split("-");
            if (questionArr != null) {
                for (int i = 0; i < questionArr.length; i++) {
                    String[] arr = questionArr[i].split(":");
                    if (arr != null) {
                        String questionName = arr[0] + PREFIX_METHOD;
                        if (nameQuestionCheck.equalsIgnoreCase(questionName)) {
                            if (isCorrect) {
                                point = Double.parseDouble(arr[1]);
                            }
                            testResultsStatus.put(questionName, point);
                            break;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("Test Disabled for test: with reason :- " +
                context.getDisplayName() +
                reason.orElse("No reason"));
        checkQuestionPoint(context.getDisplayName(), false);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("Test Successful for test: " + context.getDisplayName());
        checkQuestionPoint(context.getDisplayName(), true);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("Test Aborted for test: " + context.getDisplayName());
        checkQuestionPoint(context.getDisplayName(), false);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.out.println("Test Aborted for test: " + context.getDisplayName());
        checkQuestionPoint(context.getDisplayName(), false);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        StudentPointDto studentPointDto = null;
        try {
            studentPointDto = appendStringToResultFile();
        } catch (Exception e) {
            if (studentPointDto == null) {
                studentPointDto = new StudentPointDto();
            }
            studentPointDto.setStudentCode(getStudentCode());
            studentPointDto.setErrorMsg("System error!");
        } finally {
            try {
                // convert student point object to JSON
                String studentPointJson = objectMapper.writeValueAsString(studentPointDto);

                // send TCP message with port 9997 to localhost
                socketUtils.sendTCPMessage(studentPointJson, SOCKET_SERVER_LOCAL_HOST, SOCKET_SERVER_LISTENING_PORT);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public StudentPointDto appendStringToResultFile() throws Exception {
        // TODO: For re-submit
        String resultPath = PROJECT_DIR.replace("\\Server", "") + File.separator + TXT_RESULT_NAME;
        File file = null;
        PrintWriter writer = null;
        StudentPointDto studentPointDto = null;
        try {
            Map<String, String> listQuestions = new HashMap<>();
            file = new File(resultPath);
            writer = new PrintWriter(new FileWriter(file, true));
            double totalPoint = 0;
            Integer correctQuestionCount = 0;
            String resultText = "";
            resultText += getStudentCode() + "\n";
            for (Map.Entry<String, Double> entry : testResultsStatus.entrySet()) {
                if (entry.getValue() > 0.0) {
                    resultText += entry.getKey() + ": Passed \n";
                    totalPoint += entry.getValue();
                    correctQuestionCount++;
                } else {
                    resultText += entry.getKey() + ": Failed \n";
                }
            }
            resultText += "Time : " + getCurTime() + "\n";
            resultText += "Result : " + correctQuestionCount + " / " + testResultsStatus.size() + "\n";
            resultText += "Total : " + totalPoint + "\n";
            resultText += "end" + getStudentCode() + "\n";

            // Send TCP messages to Lec-app after finish evaluate
            studentPointDto = new StudentPointDto();
            studentPointDto.setStudentCode(getStudentCode());
            studentPointDto.setListQuestions(listQuestions);
            studentPointDto.setTotalPoint(String.valueOf(totalPoint));
            studentPointDto.setEvaluateTime(getCurTime());
            studentPointDto.setResult(correctQuestionCount + "/" + testResultsStatus.size());
//            try {
//                // convert student point object to JSON
//                String studentPointJson = objectMapper.writeValueAsString(studentPointDto);
//
//                // send TCP message with port 9997 to localhost
//                socketUtils.sendTCPMessage(studentPointJson, SOCKET_SERVER_LOCAL_HOST, SOCKET_SERVER_LISTENING_PORT);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }

            writer.println(resultText);
            return studentPointDto;
        } finally {
            writer.close();
        }
    }

    public String getCurTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String getStudentCode() {
        String path = PATH_JAVA_FOLDER_TEST;
        System.out.println(path);
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String s = file.getName();
                if (s.contains(PREFIX_TEST)) {
                    String[] arr = s.split("_");
                    return arr[1];
                }
            }
        }
        return "";
    }

    class StudentPointDto implements Serializable {

        private String studentCode;
        private Map<String, String> listQuestions;
        private String totalPoint;
        private String submitTime;
        private String evaluateTime;
        private Double codingConvention;
        private String result;
        private String errorMsg;

        public StudentPointDto() {
        }

        public StudentPointDto(String studentCode, Map<String, String> listQuestions, String totalPoint, String submitTime, String evaluateTime, Double codingConvention, String result, String errorMsg) {
            this.studentCode = studentCode;
            this.listQuestions = listQuestions;
            this.totalPoint = totalPoint;
            this.submitTime = submitTime;
            this.evaluateTime = evaluateTime;
            this.codingConvention = codingConvention;
            this.result = result;
            this.errorMsg = errorMsg;
        }

        public String getStudentCode() {
            return studentCode;
        }

        public void setStudentCode(String studentCode) {
            this.studentCode = studentCode;
        }

        public Map<String, String> getListQuestions() {
            return listQuestions;
        }

        public void setListQuestions(Map<String, String> listQuestions) {
            this.listQuestions = listQuestions;
        }

        public String getTotalPoint() {
            return totalPoint;
        }

        public void setTotalPoint(String totalPoint) {
            this.totalPoint = totalPoint;
        }

        public String getSubmitTime() {
            return submitTime;
        }

        public void setSubmitTime(String submitTime) {
            this.submitTime = submitTime;
        }

        public String getEvaluateTime() {
            return evaluateTime;
        }

        public void setEvaluateTime(String evaluateTime) {
            this.evaluateTime = evaluateTime;
        }

        public Double getCodingConvention() {
            return codingConvention;
        }

        public void setCodingConvention(Double codingConvention) {
            this.codingConvention = codingConvention;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }

    class SocketUtils {

        public void sendTCPMessage(String message, String serverHost, int serverPort) throws InterruptedException, IOException {
            Socket clientSocket = null;
            BufferedWriter bw = null;
            OutputStream os = null;
            OutputStreamWriter osw = null;

            try {
                // make a connection with server
                clientSocket = new Socket(serverHost, serverPort);

                os = clientSocket.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);

                bw.write(message);
                bw.flush();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (osw != null) {
                        osw.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}