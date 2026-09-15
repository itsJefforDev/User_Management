package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.Producto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends CrudRepository<Producto,Long> {

}
