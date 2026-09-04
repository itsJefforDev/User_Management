package com.userManagement.businessLayer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;

    private String cedula;
    private String nombre;
    private String apellido;
    private int edad;
}
