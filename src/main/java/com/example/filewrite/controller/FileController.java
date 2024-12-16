package com.example.filewrite.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RestController
public class FileController {

    private final Path filePath;

    public FileController(@Value("${file.resource.path:files/demo.txt}") String relativePath) {
        // Resolve the path relative to the current working directory
        String jarDir = new File(System.getProperty("user.dir")).getAbsolutePath();
        this.filePath = Path.of(jarDir, relativePath).normalize();
    }

    @GetMapping("/write")
    public ResponseEntity<String> writeFile() {
        try {
            Files.createDirectories(filePath.getParent());
            String content = "This is a demo file content.";
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return ResponseEntity.ok("File written successfully to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error writing file: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile() {
        try {
            File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            byte[] fileContent = Files.readAllBytes(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
