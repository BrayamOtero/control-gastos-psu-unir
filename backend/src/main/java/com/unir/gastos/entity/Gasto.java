package com.unir.gastos.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import com.unir.gastos.exception.DatosInvalidosException;

@Entity
@Table(name = "gastos")
public class Gasto {

    public static final int MAX_DIGITOS_ENTEROS = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = Gasto.MAX_DIGITOS_ENTEROS, fraction = 2)
    @Column(nullable = false, precision = Gasto.MAX_DIGITOS_ENTEROS + 2, scale = 2)
    private BigDecimal importe;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @Size(max = 255)
    @Column(length = 255)
    private String descripcion;

    protected Gasto() {
    }

    public Gasto(LocalDate fecha, BigDecimal importe, Categoria categoria, String descripcion) {
        setFecha(fecha);
        setImporte(importe);
        setCategoria(categoria);
        setDescripcion(descripcion);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new DatosInvalidosException("La fecha es obligatoria");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new DatosInvalidosException("La fecha no puede ser futura");
        }
        this.fecha = fecha;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        if (importe == null) {
            throw new DatosInvalidosException("El importe es obligatorio");
        }
        if (importe.signum() <= 0) {
            throw new DatosInvalidosException("El importe debe ser mayor que cero");
        }
        if (importe.scale() > 2) {
            throw new DatosInvalidosException("El importe no puede tener mas de dos decimales");
        }
        if (importe.precision() - importe.scale() > MAX_DIGITOS_ENTEROS) {
            throw new DatosInvalidosException("El importe no puede superar " + MAX_DIGITOS_ENTEROS + " digitos enteros");
        }
        this.importe = importe;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new DatosInvalidosException("La categoria es obligatoria");
        }
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion != null && descripcion.length() > 255) {
            throw new DatosInvalidosException("La descripcion no puede superar 255 caracteres");
        }
        this.descripcion = descripcion;
    }
}