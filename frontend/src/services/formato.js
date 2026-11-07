import { CATEGORIAS } from '../constants/categorias.js';

export function formatearFecha(fecha) {
  return fecha.split('-').reverse().join('/');
}

export function formatearImporte(importe) {
  return importe.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export function etiquetaCategoria(valor) {
  return CATEGORIAS.find((categoria) => categoria.valor === valor)?.etiqueta || valor;
}

export function describirGasto(gasto) {
  return `del ${formatearFecha(gasto.fecha)} por ${formatearImporte(gasto.importe)} (${etiquetaCategoria(gasto.categoria)})`;
}

export function gastoAFormulario(gasto) {
  return {
    fecha: gasto.fecha,
    importe: gasto.importe.toFixed(2).replace('.', ','),
    categoria: gasto.categoria,
    descripcion: gasto.descripcion ?? '',
  };
}
