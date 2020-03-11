package com.fpt.submission.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fpt.submission.utils.JsonUtils;
import com.fpt.submission.utils.PathUtils;
import com.fpt.submission.dto.request.PracticalInfo;
import com.fpt.submission.dto.request.UploadFileDto;
import com.fpt.submission.service.SubmissionService;
import com.fpt.submission.utils.SubmissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.fasterxml.jackson.databind.MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionUtils submissionUtils;
    private final SubmissionService submissionService;

    @Autowired
    public SubmissionController(SubmissionUtils submissionUtils, SubmissionService submissionService) {
        this.submissionUtils = submissionUtils;
        this.submissionService = submissionService;
    }

    @GetMapping("/test")
    public void test() throws IOException {
//        File xml = new File("test2.xml");
//        ObjectWriter w = new ObjectMapper().writerWithDefaultPrettyPrinter();
//        Object o = new XmlMapper()
//                .registerModule(new SimpleModule().addDeserializer(Object.class, new JsonUtils()))
//                .readValue(xml, Object.class);
//
//        System.out.println(w.writeValueAsString(o));
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode node = mapper.readTree(w.writeValueAsString(o));
//        JsonNode nodeSuiteName = node.findPath("SUITE_NAME");
//        List<JsonNode> nodeSuiteSuccess = node.findValues("CUNIT_RUN_TEST_SUCCESS");
//        List<JsonNode> nodeSuiteFailed = node.findValues("CUNIT_RUN_TEST_FAILURE");



        String sourceFile = "C:\\Users\\HP\\Desktop\\SQL_Update\\SE63155\\student";
        FileOutputStream fos = new FileOutputStream("SE63155.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
    @PostMapping("/submission")
    public String uploadFile(@ModelAttribute UploadFileDto file) throws IOException {
        return submissionService.submit(file);
    }

}
