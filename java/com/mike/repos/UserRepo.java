package com.mike.repos;

import com.mike.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByActivationCode(String code);

    User findByFirstNameAndMiddleNameAndLastName(String firstName, String middleName, String lastName);

    User findByAffiliationAffiliationId(Long id);
}
