import test from 'node:test';
import assert from 'node:assert/strict';
import { CATEGORIAS } from '../constants/categorias.js';
import { esResumenMensual, periodoActual, validarPeriodo } from './resumenMensual.js';

const periodo = { mes: '11', anio: '2026' };
const vacio = () => ({
  mes: 11, anio: 2026, totalGastado: 0, numeroGastos: 0,
  categorias: CATEGORIAS.map(({ valor }) => ({ categoria: valor, total: 0, numeroGastos: 0, porcentaje: 0 })),
});

test('usa el mes y año locales actuales, incluidos enero y diciembre', () => {
  assert.deepEqual(periodoActual(new Date(2026, 10, 7)), periodo);
  assert.deepEqual(periodoActual(new Date(2027, 0, 1)), { mes: '1', anio: '2027' });
  assert.deepEqual(periodoActual(new Date(2026, 11, 31)), { mes: '12', anio: '2026' });
});

test('valida límites de mes y año; permite meses futuros', () => {
  for (const mes of ['1', '12']) {
    for (const anio of ['1', '9999']) assert.deepEqual(validarPeriodo({ mes, anio }), {});
  }
  for (const mes of ['', '0', '13', '-1', '1.5', 'abc']) {
    assert.ok(validarPeriodo({ ...periodo, mes }).mes);
  }
  for (const anio of ['', '0', '10000', '-1', '2026.5', '2e3', 'abc']) {
    assert.ok(validarPeriodo({ ...periodo, anio }).anio);
  }
});

test('acepta el resumen vacío y datos reales con porcentajes redondeados', () => {
  assert.equal(esResumenMensual(vacio(), periodo), true);
  const resumen = vacio();
  resumen.totalGastado = 40;
  resumen.numeroGastos = 3;
  resumen.categorias[0] = { categoria: 'ALIMENTACION', total: 40, numeroGastos: 3, porcentaje: 100 };
  assert.equal(esResumenMensual(resumen, periodo), true);
  resumen.categorias.slice(0, 3).forEach((fila) => { fila.porcentaje = 33.33; });
  assert.equal(esResumenMensual(resumen, periodo), true);
});

test('rechaza períodos incorrectos, datos incompletos, duplicados y valores inválidos', () => {
  assert.equal(esResumenMensual(null, periodo), false);
  assert.equal(esResumenMensual({ ...vacio(), mes: 12 }, periodo), false);
  assert.equal(esResumenMensual({ ...vacio(), anio: 2025 }, periodo), false);
  assert.equal(esResumenMensual({ ...vacio(), categorias: [] }, periodo), false);
  for (const totalGastado of [-1, NaN, Infinity, '0']) {
    assert.equal(esResumenMensual({ ...vacio(), totalGastado }, periodo), false);
  }
  assert.equal(esResumenMensual({ ...vacio(), numeroGastos: 1.5 }, periodo), false);
  for (const cambio of [{ categoria: 'INVALIDA' }, { total: -1 }, { numeroGastos: -1 }, { porcentaje: 101 }, { porcentaje: NaN }]) {
    const resumen = vacio();
    Object.assign(resumen.categorias[0], cambio);
    assert.equal(esResumenMensual(resumen, periodo), false);
  }
  const duplicado = vacio();
  duplicado.categorias[1] = duplicado.categorias[0];
  assert.equal(esResumenMensual(duplicado, periodo), false);
});
