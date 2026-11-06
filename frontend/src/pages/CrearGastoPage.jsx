import { useEffect } from 'react';
import GastoForm from '../components/GastoForm.jsx';
import { useCrearGasto } from '../hooks/useCrearGasto.js';

export default function CrearGastoPage({ onCreado }) {
  const { guardar, guardando, error, gastoCreado, limpiarMensajes } = useCrearGasto();

  useEffect(() => {
    if (gastoCreado) onCreado(gastoCreado);
  }, [gastoCreado, onCreado]);

  return (
    <>
      <header className="cabecera">Control de gastos</header>
      <main className="contenedor">
        <section className="tarjeta" aria-labelledby="titulo">
          <a className="volver" href="#/">Volver a mis gastos</a>
          <h1 id="titulo">Crear gasto</h1>
          <p className="introduccion">Registra tus gastos para llevar el control de tu día a día.</p>
          {error && <div className="mensaje error" role="alert">{error}</div>}
          <GastoForm onGuardar={guardar} guardando={guardando} onEditar={limpiarMensajes} />
        </section>
      </main>
    </>
  );
}
