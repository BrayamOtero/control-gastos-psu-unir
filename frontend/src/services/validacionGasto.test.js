import test from 'node:test';
import assert from 'node:assert/strict';
import { CATEGORIAS } from '../constants/categorias.js';
import { validarGasto } from './validacionGasto.js';

const hoy = '2026-11-05';
const valido = { fecha: hoy, importe: '18,75', categoria: 'ALIMENTACION', descripcion: '' };
const validar = (cambios) => validarGasto({ ...valido, ...cambios }, hoy);

test('acepta todas las categorías, coma o punto, importe máximo y descripción límite', () => {
  for (const { valor } of CATEGORIAS) {
    assert.deepEqual(validar({ categoria: valor }), {});
  }
  for (const importe of ['0.01', '18,75', '9999999999.99']) {
    assert.deepEqual(validar({ importe, descripcion: 'a'.repeat(255) }), {});
  }
});

test('rechaza importes vacíos, no positivos, fuera de rango y con más de dos decimales', () => {
  for (const importe of ['', '0', '-1', '0,00', '1.001', '10000000000', '1e2', 'abc']) {
    assert.ok(validar({ importe }).importe, importe);
  }
});

test('rechaza fechas ausentes, inexistentes y futuras, acepta años bisiestos', () => {
  for (const fecha of ['', '0000-01-01', '2026-02-29', '2026-04-31', '2026-11-06', '05/11/2026']) {
    assert.ok(validar({ fecha }).fecha, fecha);
  }
  assert.deepEqual(validar({ fecha: '2024-02-29' }), {});
});

test('rechaza categoría desconocida y descripción demasiado larga', () => {
  assert.ok(validar({ categoria: '' }).categoria);
  assert.ok(validar({ categoria: 'INVALIDA' }).categoria);
  assert.ok(validar({ descripcion: 'a'.repeat(256) }).descripcion);
});
