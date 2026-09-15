package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario,Long> {

}
