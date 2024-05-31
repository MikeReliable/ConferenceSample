package com.mike.repos;

import com.mike.domain.Affiliation;
import com.mike.domain.Coauthor;
import com.mike.domain.CoauthorAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CoauthorAffiliationRepo extends JpaRepository<CoauthorAffiliation, Long> {

    CoauthorAffiliation findByCoauthorAndAffiliation(Coauthor coauthor, Affiliation affiliation);

    @Transactional
    void deleteAllByAffiliation_AffiliationId(Long affiliationId);
}
