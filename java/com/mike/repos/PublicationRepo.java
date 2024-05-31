package com.mike.repos;

import com.mike.domain.Publication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PublicationRepo extends CrudRepository<Publication, Long> {

    Publication findByPublicationName(String publicationName);

    Publication findByPublicationId(Long id);

    List<Publication> findAllByUser_Id(Long id);

    @Transactional
    void deleteByPublicationId(Long publicationId);
}
