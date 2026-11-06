import { CATEGORIAS } from '../constants/categorias.js';

export function fechaLocalHoy() {
  const hoy = new Date();
  return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;
}

export function validarGasto(datos, hoy = fechaLocalHoy()) {
  const errores = {};
  const fecha = new Date(`${datos.fecha}T00:00:00Z`);
  if (
    !/^\d{4}-\d{2}-\d{2}$/.test(datos.fecha) ||
    datos.fecha < '0001-01-01' ||
    Number.isNaN(fecha.getTime()) ||
    fecha.toISOString().slice(0, 10) !== datos.fecha
  ) {
    errores.fecha = 'Selecciona una fecha válida.';
  } else if (datos.fecha > hoy) {
    errores.fecha = 'La fecha no puede ser futura.';
  }

  if (
    !/^\d{1,10}([.,]\d{1,2})?$/.test(datos.importe) ||
    Number(datos.importe.replace(',', '.')) <= 0
  ) {
    errores.importe = 'Introduce un importe mayor que cero, con hasta dos decimales y máximo 9999999999,99.';
  }
  if (!CATEGORIAS.some(({ valor }) => valor === datos.categoria)) {
    errores.categoria = 'Selecciona una categoría.';
  }
  if (datos.descripcion.length > 255) {
    errores.descripcion = 'La descripción admite hasta 255 caracteres.';
  }
  return errores;
}
