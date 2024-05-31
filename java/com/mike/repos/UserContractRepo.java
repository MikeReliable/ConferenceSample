package com.mike.repos;

import com.mike.domain.UserContract;
import org.springframework.data.repository.CrudRepository;

public interface UserContractRepo extends CrudRepository<UserContract, Long> {
}
