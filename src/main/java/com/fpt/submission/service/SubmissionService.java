package com.fpt.submission.service;

import com.fpt.submission.dto.request.UploadFileDto;
import org.springframework.stereotype.Component;

@Component
public interface SubmissionService {
    String submit(UploadFileDto dto);
}
