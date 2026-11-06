export default function Paginacion({ pagina, onCambiar }) {
  return (
    <nav className="paginacion" aria-label="Paginación de gastos">
      <button type="button" className="boton-secundario" disabled={pagina.number === 0} onClick={() => onCambiar(pagina.number - 1)}>Anterior</button>
      <span>Página {pagina.number + 1} de {pagina.totalPages}</span>
      <button type="button" className="boton-secundario" disabled={pagina.number + 1 >= pagina.totalPages} onClick={() => onCambiar(pagina.number + 1)}>Siguiente</button>
    </nav>
  );
}
