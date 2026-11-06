import { CATEGORIAS } from '../constants/categorias.js';

export default function TablaGastos({ gastos }) {
  return (
    <div className="tabla-contenedor">
      <table>
        <caption className="solo-lectores">Gastos encontrados</caption>
        <thead><tr><th scope="col">Fecha</th><th scope="col" className="importe">Importe</th><th scope="col">Categoría</th></tr></thead>
        <tbody>
          {gastos.map((gasto) => (
            <tr key={gasto.id}>
              <td><time dateTime={gasto.fecha}>{gasto.fecha.split('-').reverse().join('/')}</time></td>
              <td className="importe">{gasto.importe.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
              <td>{CATEGORIAS.find(({ valor }) => valor === gasto.categoria)?.etiqueta || gasto.categoria}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
