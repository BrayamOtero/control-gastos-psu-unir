import { useRef, useState } from 'react';
import { CATEGORIAS } from '../constants/categorias.js';
import { fechaLocalHoy, validarGasto } from '../services/validacionGasto.js';

const datosVacios = () => ({
  fecha: fechaLocalHoy(),
  importe: '',
  categoria: '',
  descripcion: '',
});

export default function GastoForm({
  iniciales, onGuardar, guardando, onEditar, textoGuardar = 'Guardar gasto', onCancelar,
}) {
  const [datos, setDatos] = useState(() => iniciales ?? datosVacios());
  const [errores, setErrores] = useState({});
  const formulario = useRef(null);

  function cambiar(event) {
    const { name, value } = event.target;
    setDatos((actuales) => ({ ...actuales, [name]: value }));
    setErrores((actuales) => ({ ...actuales, [name]: undefined }));
    onEditar();
  }

  async function enviar(event) {
    event.preventDefault();
    if (guardando) return;
    const nuevosErrores = validarGasto(datos);
    setErrores(nuevosErrores);
    const primerCampo = Object.keys(nuevosErrores)[0];
    if (primerCampo) {
      formulario.current.elements.namedItem(primerCampo).focus();
      return;
    }
    await onGuardar(datos);
  }

  function accesibilidad(campo) {
    return {
      'aria-invalid': Boolean(errores[campo]),
      'aria-describedby': errores[campo] ? `${campo}-error` : undefined,
    };
  }

  return (
    <form ref={formulario} onSubmit={enviar} noValidate aria-busy={guardando}>
      <p className="ayuda">Los campos con * son obligatorios.</p>
      <fieldset disabled={guardando}>
        <legend className="solo-lectores">Datos del gasto</legend>
        <div className="fila">
          <div className="campo">
            <label htmlFor="fecha">Fecha *</label>
            <input id="fecha" name="fecha" type="date" required min="0001-01-01" max={fechaLocalHoy()} value={datos.fecha} onChange={cambiar} {...accesibilidad('fecha')} />
            {errores.fecha && <p id="fecha-error" className="error-campo">{errores.fecha}</p>}
          </div>
          <div className="campo">
            <label htmlFor="importe">Importe *</label>
            <input id="importe" name="importe" type="text" inputMode="decimal" required placeholder="0,00" value={datos.importe} onChange={cambiar} {...accesibilidad('importe')} />
            {errores.importe && <p id="importe-error" className="error-campo">{errores.importe}</p>}
          </div>
        </div>
        <div className="campo">
          <label htmlFor="categoria">Categoría *</label>
          <select id="categoria" name="categoria" required value={datos.categoria} onChange={cambiar} {...accesibilidad('categoria')}>
            <option value="">Selecciona una categoría</option>
            {CATEGORIAS.map(({ valor, etiqueta }) => <option key={valor} value={valor}>{etiqueta}</option>)}
          </select>
          {errores.categoria && <p id="categoria-error" className="error-campo">{errores.categoria}</p>}
        </div>
        <div className="campo">
          <label htmlFor="descripcion">Descripción <span className="opcional">(opcional)</span></label>
          <textarea id="descripcion" name="descripcion" rows={3} maxLength={255} placeholder="¿En qué has gastado?" value={datos.descripcion} onChange={cambiar} {...accesibilidad('descripcion')} aria-describedby={errores.descripcion ? 'descripcion-error descripcion-ayuda' : 'descripcion-ayuda'} />
          <p id="descripcion-ayuda" className="ayuda contador">{datos.descripcion.length}/255 caracteres</p>
          {errores.descripcion && <p id="descripcion-error" className="error-campo">{errores.descripcion}</p>}
        </div>
        <div className="acciones">
          <button type="submit">{guardando ? 'Guardando…' : textoGuardar}</button>
          {onCancelar && <button type="button" className="boton-secundario" onClick={onCancelar}>Cancelar</button>}
        </div>
      </fieldset>
    </form>
  );
}
