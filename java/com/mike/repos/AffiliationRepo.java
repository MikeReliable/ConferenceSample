package com.mike.repos;

import com.mike.domain.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AffiliationRepo extends JpaRepository<Affiliation, Long> {

    Affiliation findByOrganizationShort(String organizationShort);

    @Transactional
    void deleteByAffiliationId(Long id);
}
