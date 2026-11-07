import { describirGasto, etiquetaCategoria, formatearFecha, formatearImporte } from '../services/formato.js';

export default function TablaGastos({ gastos, onAbrir, onEliminar, eliminando }) {
  function abrirDesdeFila(event, id) {
    if (!event.target.closest('a, button')) onAbrir(id);
  }

  return (
    <div className="tabla-contenedor">
      <table className="tabla-gastos">
        <caption className="solo-lectores">Gastos encontrados. Selecciona un gasto para ver su detalle.</caption>
        <thead>
          <tr>
            <th scope="col">Fecha</th>
            <th scope="col" className="importe">Importe</th>
            <th scope="col">Categoría</th>
            <th scope="col"><span className="solo-lectores">Acciones</span></th>
          </tr>
        </thead>
        <tbody>
          {gastos.map((gasto) => (
            <tr key={gasto.id} className="fila-enlace" onClick={(event) => abrirDesdeFila(event, gasto.id)}>
              <td>
                <a href={`#/gastos/${gasto.id}`} aria-label={`Ver detalle del gasto ${describirGasto(gasto)}`}>
                  <time dateTime={gasto.fecha}>{formatearFecha(gasto.fecha)}</time>
                </a>
              </td>
              <td className="importe">{formatearImporte(gasto.importe)}</td>
              <td>{etiquetaCategoria(gasto.categoria)}</td>
              <td className="acciones-fila">
                <button
                  type="button"
                  className="boton-peligro boton-pequeno"
                  disabled={eliminando}
                  onClick={() => onEliminar(gasto)}
                  aria-label={`Eliminar gasto ${describirGasto(gasto)}`}
                >
                  Eliminar
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
