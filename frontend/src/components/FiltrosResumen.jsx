import { useRef, useState } from 'react';
import { MESES } from '../constants/meses.js';
import { validarPeriodo } from '../services/resumenMensual.js';

export default function FiltrosResumen({ iniciales, onConsultar }) {
  const [periodo, setPeriodo] = useState(iniciales);
  const [errores, setErrores] = useState({});
  const formulario = useRef(null);

  function cambiar(event) {
    const { name, value } = event.target;
    setPeriodo((actual) => ({ ...actual, [name]: value }));
    setErrores({});
  }

  function consultar(event) {
    event.preventDefault();
    const nuevosErrores = validarPeriodo(periodo);
    setErrores(nuevosErrores);
    const campo = Object.keys(nuevosErrores)[0];
    if (campo) {
      formulario.current.elements.namedItem(campo).focus();
      return;
    }
    onConsultar({ ...periodo });
  }

  return (
    <form ref={formulario} className="filtros" onSubmit={consultar} noValidate>
      <fieldset>
        <legend className="titulo-filtros">Seleccionar período</legend>
        <div className="fila">
          <div className="campo">
            <label htmlFor="mes">Mes</label>
            <select id="mes" name="mes" value={periodo.mes} onChange={cambiar} required aria-invalid={Boolean(errores.mes)} aria-describedby={errores.mes ? 'mes-error' : undefined}>
              {MESES.map((mes, indice) => <option key={mes} value={indice + 1}>{mes}</option>)}
            </select>
            {errores.mes && <p id="mes-error" className="error-campo">{errores.mes}</p>}
          </div>
          <div className="campo">
            <label htmlFor="anio">Año</label>
            <input id="anio" name="anio" type="text" inputMode="numeric" value={periodo.anio} onChange={cambiar} required aria-invalid={Boolean(errores.anio)} aria-describedby={errores.anio ? 'anio-error' : undefined} />
            {errores.anio && <p id="anio-error" className="error-campo">{errores.anio}</p>}
          </div>
        </div>
        <button type="submit">Consultar resumen</button>
      </fieldset>
    </form>
  );
}
