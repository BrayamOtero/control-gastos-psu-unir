import { useCallback, useEffect, useState } from 'react';
import CrearGastoPage from './pages/CrearGastoPage.jsx';
import ListaGastosPage from './pages/ListaGastosPage.jsx';
import { useNavegacion } from './hooks/useNavegacion.js';

export default function App() {
  const { creando, irAlListado } = useNavegacion();
  const [mensaje, setMensaje] = useState('');

  useEffect(() => {
    document.title = `${creando ? 'Crear gasto' : 'Mis gastos'} | Control de gastos`;
    if (creando) setMensaje('');
  }, [creando]);

  const alCrear = useCallback((gasto) => {
    setMensaje(`Gasto #${gasto.id} creado correctamente.`);
    irAlListado();
  }, [irAlListado]);

  return creando
    ? <CrearGastoPage onCreado={alCrear} />
    : <ListaGastosPage mensaje={mensaje} />;
}
