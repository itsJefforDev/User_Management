package com.userManagement.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    private String nombreProducto;
    private String precio;
    private String description;
}
