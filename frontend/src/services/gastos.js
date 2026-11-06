const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '');
// En desarrollo, el proxy de Vite usa la misma URL sin requerir CORS.
const BASE_URL = import.meta.env.DEV ? '' : API_URL;

export async function crearGasto(datos) {
  let respuesta;
  try {
    respuesta = await fetch(`${BASE_URL}/api/gastos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fecha: datos.fecha,
        importe: Number(datos.importe.replace(',', '.')),
        categoria: datos.categoria,
        descripcion: datos.descripcion || null,
      }),
      signal: AbortSignal.timeout(15000),
    });
  } catch (error) {
    if (error.name === 'TimeoutError') {
      throw new Error('El servidor tardó demasiado en responder. Comprueba si el gasto se guardó antes de volver a enviarlo.');
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
        : `No se pudo guardar el gasto (HTTP ${respuesta.status}).`,
    );
  }
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
