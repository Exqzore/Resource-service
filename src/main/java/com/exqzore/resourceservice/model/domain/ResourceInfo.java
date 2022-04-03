package com.exqzore.resourceservice.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Table(name = "resources_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResourceInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "bucket_name")
  private String bucketName;

  @Column(name = "file_name")
  private String fileName;
}
