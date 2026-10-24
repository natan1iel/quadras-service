package com.volei.quadras.repository;

import com.volei.quadras.model.Quadra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuadraRepository extends JpaRepository<Quadra, Long> {
    Optional<Quadra> findByCodigoIgnoreCase(String codigo);

    List<Quadra> findByAtivaTrue();
}
