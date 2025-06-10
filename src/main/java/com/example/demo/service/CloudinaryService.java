package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.exception.FileUploadException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing files on Cloudinary.
 * Provides methods to upload files asynchronously and delete files
 * using the Cloudinary API.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryService {

  private final Cloudinary cloudinary;

  /**
   * Uploads a file to Cloudinary asynchronously to a specific folder.
   *
   * @param file   file to upload (MultipartFile)
   * @param folder destination folder in Cloudinary where the file will be stored
   * @return a {@link CompletableFuture} containing the upload result
   *         as a {@code Map<String, Object>}
   * @throws FileUploadException if an error occurs reading or uploading the file
   */
  @Async
  @SuppressWarnings("unchecked")
  public CompletableFuture<Map<String, Object>> uploadAsync(MultipartFile file, String folder) {
    try {
      Map<String, Object> params = (Map<String, Object>) ObjectUtils.asMap("folder", folder);
      Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
      return CompletableFuture.completedFuture(uploadResult);
    } catch (IOException e) {
      CompletableFuture<Map<String, Object>> failedFuture = new CompletableFuture<>();
      failedFuture.completeExceptionally(
              new FileUploadException("Error al subir archivo asíncrono", e));
      return failedFuture;
    }
  }

  /**
   * Uploads a file to Cloudinary asynchronously to a specific folder with a custom public ID.
   *
   * @param file     file to upload (MultipartFile)
   * @param folder   destination folder in Cloudinary where the file will be stored
   * @param publicId public identifier that will be assigned to the file in Cloudinary
   * @return a {@link CompletableFuture} containing the upload result
   *         as a {@code Map<String, Object>}
   * @throws FileUploadException if an error occurs reading or uploading the file
   */
  @Async
  @SuppressWarnings("unchecked")
  public CompletableFuture<Map<String, Object>> uploadAsync(
          MultipartFile file, String folder, String publicId) {
    try {
      Map<String, Object> params = ObjectUtils.asMap(
              "folder", folder,
              "public_id", publicId,
              "resource_type", "raw",
              "use_filename", true,
              "unique_filename", false
      );
      Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
      return CompletableFuture.completedFuture(uploadResult);
    } catch (IOException e) {
      CompletableFuture<Map<String, Object>> failedFuture = new CompletableFuture<>();
      failedFuture.completeExceptionally(
              new FileUploadException("Error al subir archivo asíncrono", e));
      return failedFuture;
    }
  }

  /**
   * Deletes a file in Cloudinary given its public URL.
   *
   * @param url public URL of the file in Cloudinary
   * @return a {@code Map<String, Object>} with the result of the deletion
   * @throws FileUploadException if an error occurs deleting the file or if the URL is invalid
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> deleteByUrl(String url) {
    try {
      String publicId = extractPublicIdFromUrl(url);
      return (Map<String, Object>) cloudinary.uploader().destroy(
              publicId, ObjectUtils.asMap("invalidate", true));
    } catch (IOException e) {
      throw new FileUploadException("Error al eliminar imagen en Cloudinary", e);
    }
  }

  /**
   * Extracts the public identifier (public_id) from a Cloudinary public URL.
   *
   * @param url public Cloudinary URL
   * @return the extracted public identifier from the URL
   * @throws FileUploadException if the URL is invalid or the public_id cannot be extracted
   */
  private String extractPublicIdFromUrl(String url) {
    try {
      String[] parts = url.split("/");
      int uploadIndex = -1;
      for (int i = 0; i < parts.length; i++) {
        if (parts[i].equals("upload")) {
          uploadIndex = i;
          break;
        }
      }
      if (uploadIndex == -1 || uploadIndex + 1 >= parts.length) {
        throw new IllegalArgumentException("URL de Cloudinary inválida");
      }
      StringBuilder publicIdBuilder = new StringBuilder();
      for (int i = uploadIndex + 2; i < parts.length; i++) {
        String part = parts[i];
        if (i == parts.length - 1 && part.contains(".")) {
          part = part.substring(0, part.lastIndexOf('.'));
        }
        publicIdBuilder.append(part);
        if (i < parts.length - 1) {
          publicIdBuilder.append("/");
        }
      }
      return publicIdBuilder.toString();
    } catch (Exception e) {
      throw new FileUploadException("No se pudo extraer el public_id desde la URL", e);
    }
  }

}
