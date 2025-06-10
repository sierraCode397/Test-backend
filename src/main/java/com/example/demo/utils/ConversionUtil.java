package com.example.demo.utils;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Utility class for converting between entities and DTOs
 * using ModelMapper.
 */
@Component
@RequiredArgsConstructor
public class ConversionUtil {

  private final ModelMapper modelMapper;

  /**
   * Converts an entity to a DTO.
   *
   * @param <D> the DTO type
   * @param <T> the entity type
   * @param entity the entity to convert
   * @param dtoClass the DTO class
   * @return the converted DTO
   */
  public <D, T> D convertToDto(T entity, Class<D> dtoClass) {
    return modelMapper.map(entity, dtoClass);
  }

  /**
   * Converts a DTO to an entity.
   *
   * @param <D> the DTO type
   * @param <T> the entity type
   * @param dto the DTO to convert
   * @param entityClass the entity class
   * @return the converted entity
   */
  public <D, T> T convertToEntity(D dto, Class<T> entityClass) {
    return modelMapper.map(dto, entityClass);
  }

  /**
   * Converts a list of entities to a list of DTOs.
   *
   * @param <D> the DTO type
   * @param <T> the entity type
   * @param entityList list of entities to convert
   * @param dtoClass the DTO class
   * @return list of converted DTOs
   */
  public <D, T> List<D> convertToDtoList(List<T> entityList, Class<D> dtoClass) {
    return entityList.stream()
            .map(entity -> convertToDto(entity, dtoClass))
            .toList();
  }

  /**
   * Converts a list of DTOs to a list of entities.
   *
   * @param <D> the DTO type
   * @param <T> the entity type
   * @param dtoList list of DTOs to convert
   * @param entityClass the entity class
   * @return list of converted entities
   */
  public <D, T> List<T> convertToEntityList(List<D> dtoList, Class<T> entityClass) {
    return dtoList.stream()
            .map(dto -> convertToEntity(dto, entityClass))
            .toList();
  }

  /**
   * Maps values from a DTO to an existing entity instance.
   *
   * @param <D> the DTO type
   * @param <T> the entity type
   * @param dto the source DTO
   * @param entity the target entity to update
   */
  public <D, T> void mapToExistingEntity(D dto, T entity) {
    modelMapper.map(dto, entity);
  }
}
