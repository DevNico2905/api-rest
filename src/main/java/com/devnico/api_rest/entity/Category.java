package com.devnico.api_rest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id_category")
   private Long idCategory;

   @NotBlank(message = "Name is mandatory")
   @Size(max = 100)
   @Column(nullable = false, length = 100, unique = true)
    private String categoryName;
}
