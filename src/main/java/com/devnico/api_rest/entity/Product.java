package com.devnico.api_rest.entity;
import com.devnico.api_rest.entity.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    // Importante. Hay validaciones DDL. Y otras como los constrains.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank // <-- Para Strings
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String productName;

    @Size(max = 500)
    @Column( length = 500)
    private String description;

    //Se considera que para precio es sumamente importante el BigDecimal
    @NotNull // <-- Para numbers
    @Column(nullable = false)
    // private Double price; <-- Not!!
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    // private int amount; <-- Not!
    private Integer amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Algo muy importante es marca los nullable, length y pilas con los Enums

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_category", referencedColumnName = "id_category")
    private Category category;

}
