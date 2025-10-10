package com.dreamhouse.ai.authentication.repository;

import com.dreamhouse.ai.authentication.model.entity.AddressEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends CrudRepository<AddressEntity, Long> {
}
