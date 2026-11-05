package com.unir.gastos.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.unir.gastos.entity.Gasto;

public interface GastoRepository extends JpaRepository<Gasto, Long>, JpaSpecificationExecutor<Gasto> {

    // Solo devuelve categorías con gastos en el rango; las vacías las completa el servicio.
    @Query("""
            select new com.unir.gastos.repository.TotalCategoria(g.categoria, sum(g.importe), count(g))
            from Gasto g
            where g.fecha between :inicio and :fin
            group by g.categoria
            """)
    List<TotalCategoria> totalesPorCategoria(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
