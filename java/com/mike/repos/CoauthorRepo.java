package com.mike.repos;

import com.mike.domain.Coauthor;
import org.springframework.data.repository.CrudRepository;

public interface CoauthorRepo extends CrudRepository<Coauthor, Long> {

    Coauthor findByFirstNameAndMiddleNameAndLastName(String firstName, String middleName, String LastName);
}