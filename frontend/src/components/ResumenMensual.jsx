import { MESES } from '../constants/meses.js';
import { etiquetaCategoria, formatearImporte } from '../services/formato.js';

export default function ResumenMensual({ resumen }) {
  return (
    <>
      <h2>{MESES[resumen.mes - 1]} de {resumen.anio}</h2>
      <dl className="totales-resumen">
        <div><dt>Total gastado</dt><dd>{formatearImporte(resumen.totalGastado)}</dd></div>
        <div><dt>Número de gastos</dt><dd>{resumen.numeroGastos}</dd></div>
      </dl>
      {resumen.numeroGastos === 0 && <p>No hay gastos registrados en este mes.</p>}
      <div className="tabla-contenedor">
        <table>
          <caption className="solo-lectores">Resumen por categoría</caption>
          <thead>
            <tr>
              <th scope="col">Categoría</th>
              <th scope="col" className="importe">Total</th>
              <th scope="col" className="importe">Gastos</th>
              <th scope="col" className="importe">Porcentaje</th>
            </tr>
          </thead>
          <tbody>
            {resumen.categorias.map((fila) => (
              <tr key={fila.categoria}>
                <td>{etiquetaCategoria(fila.categoria)}</td>
                <td className="importe">{formatearImporte(fila.total)}</td>
                <td className="importe">{fila.numeroGastos}</td>
                <td className="importe">{formatearImporte(fila.porcentaje)} %</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="ayuda">Los porcentajes se redondean a dos decimales; su suma puede diferir ligeramente de 100 %.</p>
    </>
  );
}
