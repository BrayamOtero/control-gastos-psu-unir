import { useState } from 'react';
import Cabecera from '../components/Cabecera.jsx';
import DialogoConfirmacion from '../components/DialogoConfirmacion.jsx';
import GastoForm from '../components/GastoForm.jsx';
import { useDetalleGasto } from '../hooks/useDetalleGasto.js';
import { useEliminarGasto } from '../hooks/useEliminarGasto.js';
import { useModificarGasto } from '../hooks/useModificarGasto.js';
import { describirGasto, etiquetaCategoria, formatearFecha, formatearImporte, gastoAFormulario } from '../services/formato.js';

export default function DetalleGastoPage({ id, onEliminado }) {
  const { cargando, error, noEncontrado, gasto, reintentar, actualizar } = useDetalleGasto(id);
  const { guardar, guardando, error: errorGuardar, limpiarError } = useModificarGasto(id);
  const { eliminar, eliminando, error: errorEliminar } = useEliminarGasto();
  const [editando, setEditando] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [aviso, setAviso] = useState('');

  function empezarEdicion() {
    setAviso('');
    limpiarError();
    setEditando(true);
  }

  function cancelarEdicion() {
    limpiarError();
    setEditando(false);
  }

  async function guardarCambios(datos) {
    const actualizado = await guardar(datos);
    if (!actualizado) return;
    actualizar(actualizado);
    setEditando(false);
    setAviso('Gasto actualizado correctamente.');
  }

  async function confirmarEliminacion() {
    if (await eliminar(id)) {
      onEliminado(id);
    } else {
      setConfirmando(false);
    }
  }

  return (
    <>
      <Cabecera />
      <main className="contenedor">
        <section className="tarjeta" aria-labelledby="titulo">
          <a className="volver" href="#/">Volver a mis gastos</a>
          <h1 id="titulo">{editando ? `Editar gasto #${id}` : `Detalle del gasto #${id}`}</h1>
          {cargando && <p role="status">Cargando gasto…</p>}
          {error && (
            <div className="mensaje error" role="alert">
              <p>{error}</p>
              {!noEncontrado && <button type="button" onClick={reintentar}>Reintentar</button>}
            </div>
          )}

          {gasto && !editando && (
            <>
              {aviso && <div className="mensaje exito" role="status">{aviso}</div>}
              {errorEliminar && <div className="mensaje error" role="alert">{errorEliminar}</div>}
              <dl className="detalle">
                <dt>Fecha</dt>
                <dd><time dateTime={gasto.fecha}>{formatearFecha(gasto.fecha)}</time></dd>
                <dt>Importe</dt>
                <dd>{formatearImporte(gasto.importe)}</dd>
                <dt>Categoría</dt>
                <dd>{etiquetaCategoria(gasto.categoria)}</dd>
                <dt>Descripción</dt>
                <dd>{gasto.descripcion || <span className="ayuda">Sin descripción</span>}</dd>
              </dl>
              <div className="acciones">
                <button type="button" onClick={empezarEdicion}>Editar</button>
                <button type="button" className="boton-peligro" onClick={() => setConfirmando(true)}>Eliminar</button>
              </div>
            </>
          )}

          {gasto && editando && (
            <>
              {errorGuardar && <div className="mensaje error" role="alert">{errorGuardar}</div>}
              <GastoForm
                iniciales={gastoAFormulario(gasto)}
                onGuardar={guardarCambios}
                guardando={guardando}
                onEditar={limpiarError}
                textoGuardar="Guardar cambios"
                onCancelar={cancelarEdicion}
              />
            </>
          )}
        </section>
      </main>
      <DialogoConfirmacion
        abierto={confirmando}
        titulo="Eliminar gasto"
        textoConfirmar="Eliminar"
        textoProcesando="Eliminando…"
        procesando={eliminando}
        onConfirmar={confirmarEliminacion}
        onCancelar={() => setConfirmando(false)}
      >
        {gasto && <p>¿Seguro que quieres eliminar el gasto {describirGasto(gasto)}? Esta acción no se puede deshacer.</p>}
      </DialogoConfirmacion>
    </>
  );
}
