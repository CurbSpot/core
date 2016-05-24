package com.foodtruckopt.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.foodtruckopt.dto.AppUser;

@Repository
public interface UserRepository extends CrudRepository<AppUser,Long>{
}
