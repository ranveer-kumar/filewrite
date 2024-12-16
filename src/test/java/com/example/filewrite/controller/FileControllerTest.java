package com.example.filewrite.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${file.resource.path:files/demo.txt}")
    private String testFilePath;

    private Path resolvedTestFilePath;

    @BeforeEach
    void setUp() throws Exception {
        // Resolve the file path relative to the working directory
        resolvedTestFilePath = Path.of(System.getProperty("user.dir"), testFilePath).normalize();
        Files.createDirectories(resolvedTestFilePath.getParent());
    }

    @AfterEach
    void cleanUp() throws Exception {
        // Clean up created test files and directories
        Files.deleteIfExists(resolvedTestFilePath);
        Files.deleteIfExists(resolvedTestFilePath.getParent());
    }

    @Test
    void testWriteFile() throws Exception {
        // Perform the write operation
        mockMvc.perform(get("/write"))
                .andExpect(status().isOk())
                .andExpect(content().string("File written successfully to: " + resolvedTestFilePath.toAbsolutePath()));

        // Verify file content
        String content = Files.readString(resolvedTestFilePath);
        assert content.equals("This is a demo file content.");
    }

    @Test
    void testDownloadFile() throws Exception {
        // Arrange: Write a file with the expected content
        Files.writeString(resolvedTestFilePath, "This is a demo file content.");

        // Perform the download operation
        mockMvc.perform(get("/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=demo.txt"))
                .andExpect(content().string("This is a demo file content."));
    }

    @Test
    void testDownloadFileNotFound() throws Exception {
        // Ensure the file does not exist
        Files.deleteIfExists(resolvedTestFilePath);

        // Perform the download operation
        mockMvc.perform(get("/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testWriteFileError() throws Exception {
        // Simulate an error by creating a directory at the file's location
        Files.createDirectories(resolvedTestFilePath);

        // Perform the write operation and expect an error
        mockMvc.perform(get("/write"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error writing file:")));
    }
}
