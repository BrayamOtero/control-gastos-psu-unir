import { useEffect, useRef } from 'react';

export default function DialogoConfirmacion({
  abierto, titulo, children, textoConfirmar, textoProcesando, procesando, onConfirmar, onCancelar,
}) {
  const dialogo = useRef(null);

  useEffect(() => {
    const elemento = dialogo.current;
    if (abierto && !elemento.open) elemento.showModal();
    if (!abierto && elemento.open) elemento.close();
  }, [abierto]);

  function cancelar(event) {
    event.preventDefault();
    if (!procesando) onCancelar();
  }

  // No todos los navegadores emiten "cancel" al pulsar Escape, por eso también se atiende la tecla.
  function alPulsarTecla(event) {
    if (event.key === 'Escape') cancelar(event);
  }

  // Si el navegador cierra el diálogo por su cuenta, se sincroniza el estado.
  function alCerrar() {
    if (abierto && !procesando) onCancelar();
  }

  return (
    <dialog
      ref={dialogo}
      className="dialogo"
      aria-labelledby="dialogo-titulo"
      aria-describedby="dialogo-mensaje"
      onCancel={cancelar}
      onKeyDown={alPulsarTecla}
      onClose={alCerrar}
    >
      <h2 id="dialogo-titulo">{titulo}</h2>
      <div id="dialogo-mensaje">{children}</div>
      <div className="acciones">
        <button type="button" className="boton-secundario" onClick={onCancelar} disabled={procesando}>Cancelar</button>
        <button type="button" className="boton-peligro" onClick={onConfirmar} disabled={procesando}>
          {procesando ? textoProcesando : textoConfirmar}
        </button>
      </div>
    </dialog>
  );
}
