package com.example.demo.controller;

import com.example.demo.exception.FileUploadException;
import com.example.demo.service.CloudinaryService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * Controller for file upload operations.
 */
@RestController
@RequiredArgsConstructor
public class UploadController {

  private final CloudinaryService cloudinaryService;

  /**
   * Uploads a file asynchronously to Cloudinary under a user-specific folder.
   *
   * @param file   the multipart file to upload
   * @param userId the ID of the user uploading the file
   * @return a map with the upload result data
   */
  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(
          @RequestParam MultipartFile file,
          @RequestParam String userId) {
    try {
      String folder = "usuarios/" + userId;
      String publicId = folder + "/foto-" + UUID.randomUUID();

      Map<String, Object> result = cloudinaryService
              .uploadAsync(file, folder, publicId)
              .get();

      return ResponseEntity.ok(result);

    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new FileUploadException("Error al subir el archivo", e);
    }
  }
}