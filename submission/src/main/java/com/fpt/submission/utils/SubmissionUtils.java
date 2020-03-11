package com.fpt.submission.utils;

import com.fpt.submission.dto.request.PathDetails;
import com.fpt.submission.dto.request.UploadFileDto;
import com.fpt.submission.exception.CustomException;
import com.fpt.submission.service.serviceImpl.EvaluationManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EnableAsync
@Service
public class SubmissionUtils {

    private EvaluationManager evaluationManager;

    public SubmissionUtils() {
        evaluationManager = new EvaluationManager();
    }

    @Bean("ThreadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(1000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("[THREAD-SUBMIT]-");
        return executor;
    }

    @Async("ThreadPoolTaskExecutor")
    public Boolean submitSubmission(UploadFileDto dto) {
        try {
            Logger.getLogger(SubmissionUtils.class.getName())
                    .log(Level.INFO, "[SUBMISSION] - File from student: " + dto.getStudentCode());
            MultipartFile file = dto.getFile();
            if (file != null) {
                PathDetails pathDetails = PathUtils.pathDetails;

                String folPath = pathDetails.getPathSubmission();
                Path copyLocation = Paths.get(folPath + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

                return true;
            }
        } catch (Exception ex) {
            Logger.getLogger(SubmissionUtils.class.getName())
                    .log(Level.ERROR, "[SUBMISSION-ERROR] - File from student : " + ex.getMessage());
            throw new CustomException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return false;
    }

    public static boolean deleteFolder(File directory) {
        //make sure directory exists
        if (directory.exists()) {
            File[] allContents = directory.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteFolder(file);
                }
            }
        } else {
            Logger.getLogger(SubmissionUtils.class.getName())
                    .log(Level.WARN, "[DELETE FOLDER] - : Directory does not exist");
        }
        return directory.delete();
    }

    public static void sendTCPMessage(String message, String serverHost, int serverPort) throws InterruptedException, IOException {
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
    public static String getCurTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static boolean saveResultCSubmission(String result, String filePath){
        // TODO: For re-submit
        File file = null;
        PrintWriter writer = null;
        try {
            file = new File(filePath);
            writer = new PrintWriter(new FileWriter(file, true));
            writer.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            writer.close();
        }
        return true;
    }

}
