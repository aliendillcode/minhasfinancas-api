package com.dill.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dill.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
