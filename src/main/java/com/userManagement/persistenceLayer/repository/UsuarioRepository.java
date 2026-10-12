package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends CrudRepository<User,Long> {

}
