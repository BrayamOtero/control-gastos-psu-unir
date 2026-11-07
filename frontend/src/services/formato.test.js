import test from 'node:test';
import assert from 'node:assert/strict';
import { describirGasto, etiquetaCategoria, formatearFecha, formatearImporte, gastoAFormulario } from './formato.js';
import { validarGasto } from './validacionGasto.js';

const gasto = { id: 42, fecha: '2026-11-05', importe: 18.75, categoria: 'ALIMENTACION', descripcion: 'Compra semanal' };

test('formatea fecha, importe y categoría en español', () => {
  assert.equal(formatearFecha('2026-11-05'), '05/11/2026');
  assert.equal(formatearImporte(18.75), '18,75');
  assert.equal(formatearImporte(12345.5), '12.345,50');
  assert.equal(etiquetaCategoria('ALIMENTACION'), 'Alimentación');
  assert.equal(etiquetaCategoria('DESCONOCIDA'), 'DESCONOCIDA');
  assert.equal(describirGasto(gasto), 'del 05/11/2026 por 18,75 (Alimentación)');
});

test('convierte un gasto en datos de formulario válidos para editar', () => {
  assert.deepEqual(gastoAFormulario(gasto), {
    fecha: '2026-11-05', importe: '18,75', categoria: 'ALIMENTACION', descripcion: 'Compra semanal',
  });
  const sinDescripcion = gastoAFormulario({ ...gasto, importe: 1111, descripcion: null });
  assert.equal(sinDescripcion.importe, '1111,00');
  assert.equal(sinDescripcion.descripcion, '');
  assert.equal(gastoAFormulario({ ...gasto, importe: 9999999999.99 }).importe, '9999999999,99');
  assert.deepEqual(validarGasto(sinDescripcion, '2026-11-05'), {});
});
