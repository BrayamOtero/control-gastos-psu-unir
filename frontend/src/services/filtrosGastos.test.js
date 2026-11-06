import test from 'node:test';
import assert from 'node:assert/strict';
import { filtrosMesActual, parametrosListado, validarFiltros } from './filtrosGastos.js';

const iniciales = filtrosMesActual(new Date(2026, 10, 5));

test('el rango inicial cubre el mes local completo, incluidos meses bisiestos y diciembre', () => {
  assert.equal(iniciales.fechaDesde, '2026-11-01');
  assert.equal(iniciales.fechaHasta, '2026-11-30');
  assert.equal(filtrosMesActual(new Date(2024, 1, 15)).fechaHasta, '2024-02-29');
  assert.equal(filtrosMesActual(new Date(2026, 11, 31)).fechaHasta, '2026-12-31');
  assert.deepEqual(validarFiltros(iniciales), {});
});

test('permite filtros vacíos, límites iguales, cero y máximo permitido', () => {
  assert.deepEqual(validarFiltros({ ...iniciales, fechaDesde: '', fechaHasta: '', importeMinimo: '0', importeMaximo: '9999999999,99' }), {});
  assert.deepEqual(validarFiltros({ ...iniciales, fechaHasta: iniciales.fechaDesde, importeMinimo: '1.25', importeMaximo: '1,25' }), {});
});

test('rechaza rangos invertidos, fechas inexistentes, importes inválidos y orden desconocido', () => {
  assert.ok(validarFiltros({ ...iniciales, fechaHasta: '2026-10-31' }).fechaHasta);
  assert.ok(validarFiltros({ ...iniciales, fechaDesde: '2026-02-29' }).fechaDesde);
  assert.ok(validarFiltros({ ...iniciales, importeMinimo: '20', importeMaximo: '10' }).importeMaximo);
  for (const importeMinimo of ['-1', '1.001', '10000000000', '1e2', 'abc']) {
    assert.ok(validarFiltros({ ...iniciales, importeMinimo }).importeMinimo);
  }
  assert.ok(validarFiltros({ ...iniciales, categoria: 'INVALIDA' }).categoria);
  assert.ok(validarFiltros({ ...iniciales, orden: 'descripcion' }).orden);
  assert.ok(validarFiltros({ ...iniciales, direccion: 'invalid' }).direccion);
});

test('envía todos los filtros, normaliza decimales y usa página cero y diez elementos', () => {
  const parametros = parametrosListado({
    ...iniciales, categoria: 'SALUD', descripcion: '  farmacia & crema  ',
    importeMinimo: '0', importeMaximo: '25,50', orden: 'importe', direccion: 'asc',
  }, 0);
  assert.equal(parametros.get('page'), '0');
  assert.equal(parametros.get('size'), '10');
  assert.equal(parametros.get('fechaDesde'), '2026-11-01');
  assert.equal(parametros.get('fechaHasta'), '2026-11-30');
  assert.equal(parametros.get('categoria'), 'SALUD');
  assert.equal(parametros.get('descripcion'), 'farmacia & crema');
  assert.equal(parametros.get('importeMinimo'), '0');
  assert.equal(parametros.get('importeMaximo'), '25.50');
  assert.deepEqual(parametros.getAll('sort'), ['importe,asc', 'id,asc']);
});

test('omite filtros vacíos y conserva página y orden solicitados', () => {
  for (const orden of ['fecha', 'importe', 'categoria']) {
    const parametros = parametrosListado({ ...iniciales, fechaDesde: '', fechaHasta: '', descripcion: ' ', orden }, 2);
    for (const campo of ['fechaDesde', 'fechaHasta', 'descripcion', 'categoria', 'importeMinimo', 'importeMaximo']) {
      assert.equal(parametros.has(campo), false);
    }
    assert.equal(parametros.get('page'), '2');
    assert.deepEqual(parametros.getAll('sort'), [`${orden},desc`, 'id,asc']);
  }
});
