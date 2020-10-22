package com.minhasfinancas.model.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minhasfinancas.model.entity.Lancamento;
import com.minhasfinancas.model.enums.TipoLancamento;

public interface LancamentoRepository  extends JpaRepository <Lancamento, Long>{
	
	@Query( value = 
			"Select sum(l.valor) from Lancamento l join l.usuario u"
			+ " where u.id = :idUsuario and l.tipo = :tipo group by u" )
	BigDecimal obterSaldoPorTipoLancamentoEUsuario(@Param("idUsuario") Long idUsuario, @Param("tipo") TipoLancamento tipo);
	
}
