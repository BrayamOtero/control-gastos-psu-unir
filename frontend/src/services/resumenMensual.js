import { CATEGORIAS } from '../constants/categorias.js';

export function periodoActual(hoy = new Date()) {
  return { mes: String(hoy.getMonth() + 1), anio: String(hoy.getFullYear()) };
}

export function validarPeriodo(periodo) {
  const errores = {};
  if (!/^\d+$/.test(periodo.mes) || Number(periodo.mes) < 1 || Number(periodo.mes) > 12) {
    errores.mes = 'Selecciona un mes válido.';
  }
  if (!/^\d+$/.test(periodo.anio) || Number(periodo.anio) < 1 || Number(periodo.anio) > 9999) {
    errores.anio = 'Introduce un año entero entre 1 y 9999.';
  }
  return errores;
}

export function esResumenMensual(resumen, periodo) {
  const noNegativo = (valor) => Number.isFinite(valor) && valor >= 0;
  const contador = (valor) => Number.isInteger(valor) && valor >= 0;
  return resumen?.mes === Number(periodo.mes) &&
    resumen?.anio === Number(periodo.anio) &&
    noNegativo(resumen.totalGastado) &&
    contador(resumen.numeroGastos) &&
    Array.isArray(resumen.categorias) &&
    resumen.categorias.length === CATEGORIAS.length &&
    new Set(resumen.categorias.map((fila) => fila?.categoria)).size === CATEGORIAS.length &&
    resumen.categorias.every((fila) =>
      CATEGORIAS.some(({ valor }) => valor === fila?.categoria) &&
      noNegativo(fila.total) && contador(fila.numeroGastos) &&
      noNegativo(fila.porcentaje) && fila.porcentaje <= 100);
}
