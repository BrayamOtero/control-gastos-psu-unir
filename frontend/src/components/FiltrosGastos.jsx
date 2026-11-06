import { useRef, useState } from 'react';
import { CATEGORIAS } from '../constants/categorias.js';
import { filtrosMesActual, validarFiltros } from '../services/filtrosGastos.js';

export default function FiltrosGastos({ iniciales, onAplicar }) {
  const [filtros, setFiltros] = useState(iniciales);
  const [errores, setErrores] = useState({});
  const formulario = useRef(null);

  function cambiar(event) {
    const { name, value } = event.target;
    setFiltros((actuales) => ({ ...actuales, [name]: value }));
    setErrores({});
  }

  function aplicar(event) {
    event.preventDefault();
    const nuevosErrores = validarFiltros(filtros);
    setErrores(nuevosErrores);
    const primerCampo = Object.keys(nuevosErrores)[0];
    if (primerCampo) {
      formulario.current.elements.namedItem(primerCampo).focus();
      return;
    }
    onAplicar({ ...filtros });
  }

  function restablecer() {
    const nuevos = filtrosMesActual();
    setFiltros(nuevos);
    setErrores({});
    onAplicar(nuevos);
  }

  function atributos(campo) {
    return {
      id: campo,
      name: campo,
      value: filtros[campo],
      onChange: cambiar,
      'aria-invalid': Boolean(errores[campo]),
      'aria-describedby': errores[campo] ? `${campo}-error` : undefined,
    };
  }

  const errorCampo = (campo) => errores[campo] && <p id={`${campo}-error`} className="error-campo">{errores[campo]}</p>;

  return (
    <form ref={formulario} onSubmit={aplicar} noValidate className="filtros">
      <fieldset>
        <legend className="titulo-filtros">Filtrar y ordenar</legend>
        <div className="rejilla-filtros">
          <div className="campo">
            <label htmlFor="fechaDesde">Fecha desde</label>
            <input type="date" min="0001-01-01" max="9999-12-31" {...atributos('fechaDesde')} />
            {errorCampo('fechaDesde')}
          </div>
          <div className="campo">
            <label htmlFor="fechaHasta">Fecha hasta</label>
            <input type="date" min="0001-01-01" max="9999-12-31" {...atributos('fechaHasta')} />
            {errorCampo('fechaHasta')}
          </div>
          <div className="campo">
            <label htmlFor="categoria">Categoría</label>
            <select {...atributos('categoria')}>
              <option value="">Todas las categorías</option>
              {CATEGORIAS.map(({ valor, etiqueta }) => <option key={valor} value={valor}>{etiqueta}</option>)}
            </select>
            {errorCampo('categoria')}
          </div>
          <div className="campo">
            <label htmlFor="descripcion">Palabra en la descripción</label>
            <input type="search" placeholder="Buscar en la descripción" {...atributos('descripcion')} />
          </div>
          <div className="campo">
            <label htmlFor="importeMinimo">Importe mínimo</label>
            <input type="text" inputMode="decimal" placeholder="Sin mínimo" {...atributos('importeMinimo')} />
            {errorCampo('importeMinimo')}
          </div>
          <div className="campo">
            <label htmlFor="importeMaximo">Importe máximo</label>
            <input type="text" inputMode="decimal" placeholder="Sin máximo" {...atributos('importeMaximo')} />
            {errorCampo('importeMaximo')}
          </div>
          <div className="campo">
            <label htmlFor="orden">Ordenar por</label>
            <select {...atributos('orden')}>
              <option value="fecha">Fecha</option>
              <option value="importe">Importe</option>
              <option value="categoria">Categoría</option>
            </select>
            {errorCampo('orden')}
          </div>
          <div className="campo">
            <label htmlFor="direccion">Dirección</label>
            <select {...atributos('direccion')}>
              <option value="desc">Descendente</option>
              <option value="asc">Ascendente</option>
            </select>
            {errorCampo('direccion')}
          </div>
        </div>
        <div className="acciones">
          <button type="submit">Aplicar filtros</button>
          <button type="button" className="boton-secundario" onClick={restablecer}>Restablecer mes actual</button>
        </div>
        <p className="ayuda">Deja los límites vacíos para consultar sin restricciones. Los filtros se combinan.</p>
      </fieldset>
    </form>
  );
}
