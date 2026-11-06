import GastoForm from '../components/GastoForm.jsx';
import { CATEGORIAS } from '../constants/categorias.js';
import { useCrearGasto } from '../hooks/useCrearGasto.js';

export default function CrearGastoPage() {
  const { guardar, guardando, error, gastoCreado, limpiarMensajes } = useCrearGasto();
  const categoria = CATEGORIAS.find(({ valor }) => valor === gastoCreado?.categoria);

  return (
    <>
      <header className="cabecera">Control de gastos</header>
      <main className="contenedor">
        <section className="tarjeta" aria-labelledby="titulo">
          <h1 id="titulo">Crear gasto</h1>
          <p className="introduccion">Registra tus gastos para llevar el control de tu día a día.</p>
          <div role="status" aria-live="polite" aria-atomic="true">
            {gastoCreado && (
              <div className="mensaje exito">
                <strong>Gasto #{gastoCreado.id} creado correctamente.</strong>
                <p>{gastoCreado.fecha} · {categoria?.etiqueta || gastoCreado.categoria} · Importe: {gastoCreado.importe.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
              </div>
            )}
          </div>
          {error && <div className="mensaje error" role="alert">{error}</div>}
          <GastoForm onGuardar={guardar} guardando={guardando} onEditar={limpiarMensajes} />
        </section>
      </main>
    </>
  );
}
