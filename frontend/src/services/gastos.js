import { parametrosListado } from './filtrosGastos.js';

const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '');
// En desarrollo, el proxy de Vite usa la misma URL sin requerir CORS.
const BASE_URL = import.meta.env.DEV ? '' : API_URL;
const TIEMPO_MAXIMO_MS = 15000;

async function solicitar(ruta, { signal, ...opciones }, mensajes) {
  const limite = AbortSignal.timeout(TIEMPO_MAXIMO_MS);
  let respuesta;
  try {
    respuesta = await fetch(`${BASE_URL}/api/gastos${ruta}`, {
      ...opciones,
      signal: signal ? AbortSignal.any([signal, limite]) : limite,
    });
  } catch (error) {
    if (error.name === 'TimeoutError') throw new Error(mensajes.tiempo);
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
    const fallo = new Error(
      typeof cuerpo?.detail === 'string' ? cuerpo.detail : `${mensajes.error} (HTTP ${respuesta.status}).`,
    );
    fallo.status = respuesta.status;
    throw fallo;
  }
  return { respuesta, cuerpo };
}

function esGasto(gasto) {
  return Number.isInteger(gasto?.id) && gasto.id > 0 &&
    typeof gasto.fecha === 'string' &&
    typeof gasto.importe === 'number' &&
    typeof gasto.categoria === 'string' &&
    (gasto.descripcion == null || typeof gasto.descripcion === 'string');
}

function cuerpoGasto(datos) {
  return JSON.stringify({
    fecha: datos.fecha,
    importe: Number(datos.importe.replace(',', '.')),
    categoria: datos.categoria,
    descripcion: datos.descripcion || null,
  });
}

const CABECERAS_JSON = { 'Content-Type': 'application/json' };

export async function crearGasto(datos) {
  const { respuesta, cuerpo } = await solicitar('', {
    method: 'POST',
    headers: CABECERAS_JSON,
    body: cuerpoGasto(datos),
  }, {
    error: 'No se pudo guardar el gasto',
    tiempo: 'El servidor tardó demasiado en responder. Comprueba si el gasto se guardó antes de volver a enviarlo.',
  });
  if (respuesta.status !== 201 || !esGasto(cuerpo)) {
    throw new Error('No se pudo confirmar la creación. Comprueba los gastos guardados antes de volver a enviarlo.');
  }
  return cuerpo;
}

export async function obtenerGasto(id, signal) {
  const { cuerpo } = await solicitar(`/${id}`, { signal }, {
    error: 'No se pudo consultar el gasto',
    tiempo: 'El servidor tardó demasiado en responder al consultar el gasto.',
  });
  if (!esGasto(cuerpo) || cuerpo.id !== id) {
    throw new Error('El servidor devolvió un gasto no válido.');
  }
  return cuerpo;
}

export async function modificarGasto(id, datos) {
  const { cuerpo } = await solicitar(`/${id}`, {
    method: 'PUT',
    headers: CABECERAS_JSON,
    body: cuerpoGasto(datos),
  }, {
    error: 'No se pudieron guardar los cambios',
    tiempo: 'El servidor tardó demasiado en responder. Comprueba si los cambios se guardaron antes de volver a intentarlo.',
  });
  if (!esGasto(cuerpo) || cuerpo.id !== id) {
    throw new Error('No se pudo confirmar la modificación. Recarga el gasto para comprobar sus datos.');
  }
  return cuerpo;
}

export async function eliminarGasto(id) {
  await solicitar(`/${id}`, { method: 'DELETE' }, {
    error: 'No se pudo eliminar el gasto',
    tiempo: 'El servidor tardó demasiado en responder. Comprueba si el gasto se eliminó antes de volver a intentarlo.',
  });
}

export async function listarGastos(filtros, pagina, signal) {
  const { cuerpo } = await solicitar(`?${parametrosListado(filtros, pagina)}`, { signal }, {
    error: 'No se pudieron consultar los gastos',
    tiempo: 'El servidor tardó demasiado en responder al consultar los gastos.',
  });
  if (
    !Array.isArray(cuerpo?.content) ||
    !cuerpo.content.every(esGasto) ||
    !Number.isInteger(cuerpo.page?.number) || cuerpo.page.number < 0 ||
    !Number.isInteger(cuerpo.page?.size) || cuerpo.page.size <= 0 ||
    !Number.isInteger(cuerpo.page?.totalElements) || cuerpo.page.totalElements < 0 ||
    !Number.isInteger(cuerpo.page?.totalPages) || cuerpo.page.totalPages < 0
  ) {
    throw new Error('El servidor devolvió un listado de gastos no válido.');
  }
  return cuerpo;
}
