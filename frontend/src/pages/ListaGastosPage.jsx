import FiltrosGastos from '../components/FiltrosGastos.jsx';
import TablaGastos from '../components/TablaGastos.jsx';
import Paginacion from '../components/Paginacion.jsx';
import { useListaGastos } from '../hooks/useListaGastos.js';

export default function ListaGastosPage({ mensaje }) {
  const { filtros, aplicarFiltros, cargando, error, resultado, cambiarPagina, reintentar } = useListaGastos();

  return (
    <>
      <header className="cabecera cabecera-lista">
        <span>Control de gastos</span>
        <a className="boton-enlace" href="#/crear-gasto">Crear gasto</a>
      </header>
      <main className="contenedor contenedor-lista">
        <section className="tarjeta" aria-labelledby="titulo">
          <h1 id="titulo">Mis gastos</h1>
          <p className="introduccion">Consulta tus gastos. Por defecto se muestra el mes actual.</p>
          {mensaje && <div className="mensaje exito" role="status">{mensaje}</div>}
          <FiltrosGastos iniciales={filtros} onAplicar={aplicarFiltros} />
          <section aria-label="Resultados" aria-busy={cargando}>
            <p role="status" aria-live="polite">
              {cargando ? 'Cargando gastos…' : resultado ? `${resultado.page.totalElements} gastos encontrados.` : ''}
            </p>
            {error && (
              <div className="mensaje error" role="alert">
                <p>{error}</p>
                <button type="button" onClick={reintentar}>Reintentar</button>
              </div>
            )}
            {resultado && (resultado.content.length > 0
              ? <TablaGastos gastos={resultado.content} />
              : <p>No hay gastos que coincidan con los filtros.</p>)}
            {resultado && resultado.page.totalPages > 0 && <Paginacion pagina={resultado.page} onCambiar={cambiarPagina} />}
          </section>
        </section>
      </main>
    </>
  );
}
