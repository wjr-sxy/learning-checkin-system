package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件为空");
        }

        try {
            // Create upload directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file
            Path path = Paths.get(uploadDir + File.separator + newFilename);
            Files.write(path, file.getBytes());

            // Return URL (Assuming static resource mapping is configured to /uploads/**)
            // The frontend will append the base URL
            return Result.success("/uploads/" + newFilename);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}
