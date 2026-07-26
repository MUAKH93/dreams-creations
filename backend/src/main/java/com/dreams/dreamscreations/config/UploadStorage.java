package com.dreams.dreamscreations.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class UploadStorage {

    private final Path root;

    public UploadStorage(@Value("${app.upload.dir}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public Path getRoot() {
        return root;
    }

    public Path resolve(String fileName) {
        return root.resolve(fileName).normalize();
    }

    public void save(InputStream inputStream, String fileName) throws IOException {
        Path target = resolve(fileName);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteIfExists(String fileNameOrPath) throws IOException {
        if (fileNameOrPath == null || fileNameOrPath.isBlank()) return;
        Path path = Paths.get(fileNameOrPath);
        if (!path.isAbsolute()) {
            path = resolve(fileNameOrPath);
        }
        Files.deleteIfExists(path);
    }
}
