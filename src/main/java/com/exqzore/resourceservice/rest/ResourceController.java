package com.exqzore.resourceservice.rest;

import com.exqzore.resourceservice.model.DeletedEntitiesInfo;
import com.exqzore.resourceservice.model.EntityBase;
import com.exqzore.resourceservice.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resources")
public class ResourceController {

  private final ResourceService resourceService;

  @Autowired
  public ResourceController(ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @PostMapping
  public ResponseEntity<EntityBase> uploadFile(@RequestParam(value = "file") MultipartFile file) {
    return new ResponseEntity<>(resourceService.uploadFile(file), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ByteArrayResource> getOne(@PathVariable Long id) {
    String fileName = resourceService.getFileNameById(id);
    byte[] data = resourceService.downloadFile(id);
    ByteArrayResource resource = new ByteArrayResource(data);
    return ResponseEntity
            .ok()
            .contentLength(data.length)
            .header("Content-type", "application/octet-stream")
            .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
            .body(resource);
  }

  @DeleteMapping
  public ResponseEntity<DeletedEntitiesInfo> removeResources (@RequestParam("id") String ids) {
    return new ResponseEntity<>(resourceService.deleteFiles(ids), HttpStatus.OK);
  }
}
