import Cabecera from '../components/Cabecera.jsx';
import FiltrosResumen from '../components/FiltrosResumen.jsx';
import ResumenMensual from '../components/ResumenMensual.jsx';
import { useResumenMensual } from '../hooks/useResumenMensual.js';

export default function ResumenMensualPage() {
  const { periodo, consultar, resumen, cargando, error, reintentar } = useResumenMensual();

  return (
    <>
      <Cabecera ancha>
        <nav className="navegacion-cabecera" aria-label="Secciones">
          <a className="enlace-cabecera" href="#/">Mis gastos</a>
          <a className="boton-enlace" href="#/crear-gasto">Crear gasto</a>
        </nav>
      </Cabecera>
      <main className="contenedor contenedor-lista">
        <section className="tarjeta" aria-labelledby="titulo">
          <h1 id="titulo">Resumen mensual</h1>
          <p className="introduccion">Consulta el total de tus gastos y su distribución por categoría. Por defecto se muestra el mes actual.</p>
          <FiltrosResumen iniciales={periodo} onConsultar={consultar} />
          <section aria-label="Resultados del resumen" aria-busy={cargando}>
            <p role="status" aria-live="polite">{cargando ? 'Cargando resumen…' : resumen ? 'Resumen actualizado.' : ''}</p>
            {error && (
              <div className="mensaje error" role="alert">
                <p>{error}</p>
                <button type="button" onClick={reintentar}>Reintentar</button>
              </div>
            )}
            {resumen && <ResumenMensual resumen={resumen} />}
          </section>
        </section>
      </main>
    </>
  );
}
