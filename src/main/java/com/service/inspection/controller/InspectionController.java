package com.service.inspection.controller;

import com.service.inspection.dto.IdentifiableDto;
import com.service.inspection.dto.inspection.CategoryWithFile;
import com.service.inspection.dto.inspection.InspectionDto;
import com.service.inspection.dto.inspection.InspectionWithIdOnly;
import com.service.inspection.dto.inspection.InspectionWithName;
import com.service.inspection.entities.Category;
import com.service.inspection.entities.Identifiable;
import com.service.inspection.entities.Inspection;
import com.service.inspection.mapper.CategoryMapper;
import com.service.inspection.mapper.CommonMapper;
import com.service.inspection.mapper.InspectionMapper;
import com.service.inspection.service.InspectionService;
import com.service.inspection.service.StorageService;
import com.service.inspection.utils.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/inspections")
@CrossOrigin(allowCredentials = "true", originPatterns = "*")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final InspectionMapper inspectionMapper;
    private final ControllerUtils utils;
    private final CommonMapper commonMapper;
    private final CategoryMapper categoryMapper;


    // TODO заменить обращение к бд для поиска пользователя с email на id для более быстрого поиска

    @PostMapping
    @Operation(summary = "Создание пустой инспекции Без названия")
    public ResponseEntity<InspectionWithIdOnly> createInspection(Authentication authentication) {
        Long userId = utils.getUserId(authentication);
        Long inspectionId = inspectionService.createInspection(userId);

        InspectionWithIdOnly inspectionOnlyIdDto = new InspectionWithIdOnly();
        inspectionOnlyIdDto.setId(inspectionId);

        return ResponseEntity.ok().body(inspectionOnlyIdDto);
    }

    @GetMapping
    @Operation(summary = "Получение всех инспекций")
    public ResponseEntity<Page<InspectionWithName>> getAllInspections(
            @RequestParam(defaultValue = "0", required = false) Integer pageNum,
            @RequestParam(defaultValue = "10", required = false) Integer pageSize,
            Authentication authentication
    ) {
        Long id = utils.getUserId(authentication);
        Page<Inspection> page = inspectionService.getUserInspection(id, pageSize, pageNum);

        return ResponseEntity.ok().body(
                page.map(inspectionMapper::mapToInspectionWithName)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление инспекции")
    public ResponseEntity<Void> deleteInspection(
            @PathVariable @Min(1) Long id, Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        inspectionService.deleteInspection(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление полей инспекции")
    public ResponseEntity<Void> updateInspection(@PathVariable @Min(1) Long id,
                                                 @RequestBody InspectionDto inspectionDto,
                                                 Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        inspectionService.updateInspection(id, userId, inspectionDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/main-photo")
    @Operation(summary = "Загрузка обложки\\главное фотографии проекта\\отчета")
    public ResponseEntity<Void> uploadMainPhoto(
            @PathVariable @Min(1) Long id,
            @RequestParam("file") MultipartFile multipartFile, Authentication authentication) {
        Long userId = utils.getUserId(authentication);
        inspectionService.uploadMainInspectionPhoto(userId, id, multipartFile);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/main-photo")
    @Operation(summary = "Скачать главную фотографию отчета")
    public ResponseEntity<Resource> getInspectionMainPhoto(
            @PathVariable @Min(1) Long id,
            Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        StorageService.BytesWithContentType file = inspectionService.getMainInspectionPhoto(id, userId);
        return utils.getResponseEntityFromFile("main-photo", file);
    }

    // ------------------------------------------------- Категории -------------------------------------------------

    @PostMapping("/{id}/categories")
    @Operation(summary = "Создание категории")
    public ResponseEntity<IdentifiableDto> createNewCategory(
            @PathVariable @Min(1) Long id, @RequestParam(name = "name") String categoryName,
            Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        return ResponseEntity.ok(
                commonMapper.mapToIdentifiableDto(inspectionService.createNewCategory(userId, id, categoryName))
        );
    }

    @GetMapping("/{id}/categories")
    @Operation(summary = "Получение всех категорий инспекций с информацией о фотографиях")
    public ResponseEntity<Set<CategoryWithFile>> getAllCategories(
            Authentication authentication, @PathVariable @Min(1) Long id
    ) {
        Long userId = utils.getUserId(authentication);
        Set<Category> categories = inspectionService.getAllCategories(id, userId);

        return ResponseEntity.ok(categoryMapper.mapToCategoryWithFile(categories));
    }

    @PutMapping("/{id}/categories/{categoryId}")
    @Operation(summary = "Обновление название категории")
    public ResponseEntity<Void> updateCategory(
            @PathVariable @Min(1) Long id, @RequestParam(name = "name") String categoryName,
            @PathVariable @Min(1) Long categoryId, Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        inspectionService.updateCategory(userId, categoryId, id, categoryName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/categories/{categoryId}")
    @Operation(summary = "Удаление категории")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable @Min(1) Long id, @PathVariable @Min(1) Long categoryId,
            Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        inspectionService.deleteCategory(userId, id, categoryId);
        return ResponseEntity.ok().build();
    }

    // ------------------------------------------------- Фотографии -------------------------------------------------

    @PostMapping("/{id}/categories/{categoryId}/photos")
    @Operation(summary = "Добавление фотографии категории")
    public ResponseEntity<IdentifiableDto> addPhotoToCategory(
            @PathVariable @Min(1) Long categoryId, @PathVariable @Min(1) Long id,
            @RequestParam("file") MultipartFile multipartFile, Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        Identifiable ident = inspectionService.addPhotoToCategory(userId, id, categoryId, multipartFile);
        return ResponseEntity.ok(commonMapper.mapToIdentifiableDto(ident));
    }

    @DeleteMapping("/{id}/categories/{categoryId}/photos/{photoId}")
    @Operation(summary = "Удаление фотографий")
    public ResponseEntity<Void> deletePhotoFromCategory(
            @PathVariable @Min(1) Long categoryId, @PathVariable @Min(1) Long id,
            @PathVariable @Min(1) Long photoId, Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        inspectionService.deletePhoto(userId, id, categoryId, photoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/categories/{categoryId}/photos/{photoId}")
    @Operation(summary = "Удаление фотографий")
    public ResponseEntity<Resource> getCategoryPhoto(
            @PathVariable @Min(1) Long categoryId, @PathVariable @Min(1) Long id,
            @PathVariable @Min(1) Long photoId, Authentication authentication
    ) {
        Long userId = utils.getUserId(authentication);
        StorageService.BytesWithContentType file = inspectionService.getCategoryPhoto(userId, id, categoryId, photoId);
        return utils.getResponseEntityFromFile("category-photo", file);
    }
}
