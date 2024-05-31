package com.mike.repos;

import com.mike.domain.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PaperRepo extends JpaRepository<Paper, Long> {

    Paper findByPaperId(Long id);

    Paper findByPaperName(String paperName);

    @Transactional
    void deleteByPaperId(Long paperId);

}
