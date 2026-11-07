import { useCallback, useEffect, useState } from 'react';
import CrearGastoPage from './pages/CrearGastoPage.jsx';
import DetalleGastoPage from './pages/DetalleGastoPage.jsx';
import ListaGastosPage from './pages/ListaGastosPage.jsx';
import ResumenMensualPage from './pages/ResumenMensualPage.jsx';
import { useNavegacion } from './hooks/useNavegacion.js';
import { filtrosMesActual } from './services/filtrosGastos.js';

const TITULOS = { lista: 'Mis gastos', crear: 'Crear gasto', detalle: 'Detalle del gasto', resumen: 'Resumen mensual' };
const consultaInicial = () => ({ filtros: filtrosMesActual(), pagina: 0 });

export default function App() {
  const { ruta, navegar } = useNavegacion();
  const [mensaje, setMensaje] = useState('');
  const [consultaLista, setConsultaLista] = useState(consultaInicial);

  useEffect(() => {
    document.title = `${TITULOS[ruta.nombre]} | Control de gastos`;
    if (ruta.nombre !== 'lista') setMensaje('');
  }, [ruta.nombre]);

  const alCrear = useCallback((gasto) => {
    setMensaje(`Gasto #${gasto.id} creado correctamente.`);
    setConsultaLista(consultaInicial());
    navegar('/');
  }, [navegar]);

  const alEliminar = useCallback((id) => {
    setMensaje(`Gasto #${id} eliminado correctamente.`);
    navegar('/');
  }, [navegar]);

  const abrirDetalle = useCallback((id) => navegar(`/gastos/${id}`), [navegar]);

  if (ruta.nombre === 'crear') return <CrearGastoPage onCreado={alCrear} />;
  if (ruta.nombre === 'detalle') return <DetalleGastoPage key={ruta.id} id={ruta.id} onEliminado={alEliminar} />;
  if (ruta.nombre === 'resumen') return <ResumenMensualPage />;
  return (
    <ListaGastosPage
      consulta={consultaLista}
      onConsultar={setConsultaLista}
      mensaje={mensaje}
      onAbrir={abrirDetalle}
    />
  );
}
