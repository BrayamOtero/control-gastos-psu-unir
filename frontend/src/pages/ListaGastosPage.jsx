import { useState } from 'react';
import Cabecera from '../components/Cabecera.jsx';
import DialogoConfirmacion from '../components/DialogoConfirmacion.jsx';
import FiltrosGastos from '../components/FiltrosGastos.jsx';
import Paginacion from '../components/Paginacion.jsx';
import TablaGastos from '../components/TablaGastos.jsx';
import { useEliminarGasto } from '../hooks/useEliminarGasto.js';
import { useListaGastos } from '../hooks/useListaGastos.js';
import { describirGasto } from '../services/formato.js';

export default function ListaGastosPage({ consulta, onConsultar, mensaje, onAbrir }) {
  const { cargando, error, resultado, aplicarFiltros, cambiarPagina, recargar } = useListaGastos(consulta, onConsultar);
  const { eliminar, eliminando, error: errorEliminar } = useEliminarGasto();
  const [porEliminar, setPorEliminar] = useState(null);
  const [aviso, setAviso] = useState(mensaje);

  async function confirmarEliminacion() {
    const gasto = porEliminar;
    const eliminado = await eliminar(gasto.id);
    setPorEliminar(null);
    if (eliminado) {
      setAviso(`Gasto #${gasto.id} eliminado correctamente.`);
      recargar();
    }
  }

  return (
    <>
      <Cabecera ancha>
        <nav className="navegacion-cabecera" aria-label="Secciones">
          <a className="enlace-cabecera" href="#/resumen">Resumen mensual</a>
          <a className="boton-enlace" href="#/crear-gasto">Crear gasto</a>
        </nav>
      </Cabecera>
      <main className="contenedor contenedor-lista">
        <section className="tarjeta" aria-labelledby="titulo">
          <h1 id="titulo">Mis gastos</h1>
          <p className="introduccion">Consulta tus gastos. Por defecto se muestra el mes actual. Selecciona un gasto para ver su detalle.</p>
          {aviso && <div className="mensaje exito" role="status">{aviso}</div>}
          {errorEliminar && <div className="mensaje error" role="alert">{errorEliminar}</div>}
          <FiltrosGastos iniciales={consulta.filtros} onAplicar={aplicarFiltros} />
          <section aria-label="Resultados" aria-busy={cargando}>
            <p role="status" aria-live="polite">
              {cargando ? 'Cargando gastos…' : resultado ? `${resultado.page.totalElements} gastos encontrados.` : ''}
            </p>
            {error && (
              <div className="mensaje error" role="alert">
                <p>{error}</p>
                <button type="button" onClick={recargar}>Reintentar</button>
              </div>
            )}
            {resultado && (resultado.content.length > 0
              ? <TablaGastos gastos={resultado.content} onAbrir={onAbrir} onEliminar={setPorEliminar} eliminando={eliminando} />
              : <p>No hay gastos que coincidan con los filtros.</p>)}
            {resultado && resultado.page.totalPages > 0 && <Paginacion pagina={resultado.page} onCambiar={cambiarPagina} />}
          </section>
        </section>
      </main>
      <DialogoConfirmacion
        abierto={Boolean(porEliminar)}
        titulo="Eliminar gasto"
        textoConfirmar="Eliminar"
        textoProcesando="Eliminando…"
        procesando={eliminando}
        onConfirmar={confirmarEliminacion}
        onCancelar={() => setPorEliminar(null)}
      >
        {porEliminar && <p>¿Seguro que quieres eliminar el gasto {describirGasto(porEliminar)}? Esta acción no se puede deshacer.</p>}
      </DialogoConfirmacion>
    </>
  );
}
