package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.Gasto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GastoRepository extends CrudRepository<Gasto,Long> {

}
