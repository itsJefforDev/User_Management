package com.userManagement.persistenceLayer.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="gasto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class gasto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGasto;

    private String
}
