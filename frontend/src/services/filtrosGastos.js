import { CATEGORIAS } from '../constants/categorias.js';
import { esFechaValida } from './validacionGasto.js';

export function filtrosMesActual(hoy = new Date()) {
  const anio = hoy.getFullYear();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const ultimoDia = new Date(anio, hoy.getMonth() + 1, 0).getDate();
  return {
    fechaDesde: `${anio}-${mes}-01`,
    fechaHasta: `${anio}-${mes}-${ultimoDia}`,
    categoria: '',
    descripcion: '',
    importeMinimo: '',
    importeMaximo: '',
    orden: 'fecha',
    direccion: 'desc',
  };
}

export function validarFiltros(filtros) {
  const errores = {};
  for (const campo of ['fechaDesde', 'fechaHasta']) {
    if (filtros[campo] && !esFechaValida(filtros[campo])) {
      errores[campo] = 'Introduce una fecha válida.';
    }
  }
  if (filtros.fechaDesde && filtros.fechaHasta && filtros.fechaDesde > filtros.fechaHasta) {
    errores.fechaHasta = 'La fecha final debe ser igual o posterior a la inicial.';
  }
  for (const campo of ['importeMinimo', 'importeMaximo']) {
    if (filtros[campo] && !/^\d{1,10}([.,]\d{1,2})?$/.test(filtros[campo])) {
      errores[campo] = 'Introduce un importe no negativo, con hasta dos decimales y máximo 9999999999,99.';
    }
  }
  if (
    filtros.importeMinimo && filtros.importeMaximo &&
    Number(filtros.importeMinimo.replace(',', '.')) > Number(filtros.importeMaximo.replace(',', '.'))
  ) {
    errores.importeMaximo = 'El importe máximo debe ser igual o mayor que el mínimo.';
  }
  if (filtros.categoria && !CATEGORIAS.some(({ valor }) => valor === filtros.categoria)) {
    errores.categoria = 'Selecciona una categoría válida.';
  }
  if (!['fecha', 'importe', 'categoria'].includes(filtros.orden)) {
    errores.orden = 'Selecciona un campo de orden válido.';
  }
  if (!['asc', 'desc'].includes(filtros.direccion)) {
    errores.direccion = 'Selecciona una dirección válida.';
  }
  return errores;
}

export function parametrosListado(filtros, pagina) {
  const parametros = new URLSearchParams({ page: String(pagina), size: '10' });
  for (const campo of ['fechaDesde', 'fechaHasta', 'categoria', 'descripcion', 'importeMinimo', 'importeMaximo']) {
    const valor = filtros[campo].trim();
    if (valor) {
      parametros.set(campo, campo.startsWith('importe') ? valor.replace(',', '.') : valor);
    }
  }
  parametros.append('sort', `${filtros.orden},${filtros.direccion}`);
  // El identificador desempata para mantener páginas estables con valores iguales.
  parametros.append('sort', 'id,asc');
  return parametros;
}
