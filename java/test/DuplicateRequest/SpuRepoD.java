package com.zjk.spring.NotesAndTests.test.DuplicateRequest;

import com.zjk.spring.model.test.SpuTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpuRepoD extends JpaRepository<SpuTest, Long> {
    SpuTest getOneById(Long id);
}
