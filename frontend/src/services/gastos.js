import { parametrosListado } from './filtrosGastos.js';

const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '');
// En desarrollo, el proxy de Vite usa la misma URL sin requerir CORS.
const BASE_URL = import.meta.env.DEV ? '' : API_URL;

async function solicitarGastos(ruta, opciones, mensajeError) {
  let respuesta;
  try {
    respuesta = await fetch(`${BASE_URL}/api/gastos${ruta}`, opciones);
  } catch (error) {
    if (error.name === 'TimeoutError') {
      throw new Error(opciones.method === 'POST'
        ? 'El servidor tardó demasiado en responder. Comprueba si el gasto se guardó antes de volver a enviarlo.'
        : 'El servidor tardó demasiado en responder al consultar los gastos.');
    }
    if (error instanceof TypeError) {
      throw new Error('No se pudo conectar con el servidor. Comprueba la conexión y que el backend esté disponible.');
    }
    throw error;
  }

  const texto = await respuesta.text();
  let cuerpo;
  if (texto) {
    try {
      cuerpo = JSON.parse(texto);
    } catch {
      throw new Error(`El servidor devolvió una respuesta no válida (HTTP ${respuesta.status}).`);
    }
  }
  if (!respuesta.ok) {
    throw new Error(
      typeof cuerpo?.detail === 'string'
        ? cuerpo.detail
        : `${mensajeError} (HTTP ${respuesta.status}).`,
    );
  }
  return { respuesta, cuerpo };
}

export async function crearGasto(datos) {
  const { respuesta, cuerpo } = await solicitarGastos('', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fecha: datos.fecha,
      importe: Number(datos.importe.replace(',', '.')),
      categoria: datos.categoria,
      descripcion: datos.descripcion || null,
    }),
    signal: AbortSignal.timeout(15000),
  }, 'No se pudo guardar el gasto');
  if (
    respuesta.status !== 201 ||
    !Number.isInteger(cuerpo?.id) ||
    cuerpo.id <= 0 ||
    typeof cuerpo.fecha !== 'string' ||
    typeof cuerpo.importe !== 'number' ||
    typeof cuerpo.categoria !== 'string'
  ) {
    throw new Error('No se pudo confirmar la creación. Comprueba los gastos guardados antes de volver a enviarlo.');
  }
  return cuerpo;
}

export async function listarGastos(filtros, pagina, signal) {
  const { cuerpo } = await solicitarGastos(`?${parametrosListado(filtros, pagina)}`, {
    signal: AbortSignal.any([signal, AbortSignal.timeout(15000)]),
  }, 'No se pudieron consultar los gastos');
  if (
    !Array.isArray(cuerpo?.content) ||
    !cuerpo.content.every((gasto) =>
      Number.isInteger(gasto?.id) && typeof gasto.fecha === 'string' &&
      typeof gasto.importe === 'number' && typeof gasto.categoria === 'string') ||
    !Number.isInteger(cuerpo.page?.number) || cuerpo.page.number < 0 ||
    !Number.isInteger(cuerpo.page?.size) || cuerpo.page.size <= 0 ||
    !Number.isInteger(cuerpo.page?.totalElements) || cuerpo.page.totalElements < 0 ||
    !Number.isInteger(cuerpo.page?.totalPages) || cuerpo.page.totalPages < 0
  ) {
    throw new Error('El servidor devolvió un listado de gastos no válido.');
  }
  return cuerpo;
}
