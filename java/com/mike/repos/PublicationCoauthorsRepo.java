package com.mike.repos;

import com.mike.domain.Publication;
import com.mike.domain.PublicationCoauthor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface PublicationCoauthorsRepo extends CrudRepository<PublicationCoauthor, Long> {

    Set<PublicationCoauthor> findByPublication(Publication publication);

    Set<PublicationCoauthor> findByCoauthorAffiliation_Affiliation_AffiliationId(Long affiliationId);

    @Transactional
    void deleteAllByPublication(Publication publication);

}
