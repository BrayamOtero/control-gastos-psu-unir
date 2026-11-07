import { useCallback, useEffect, useState } from 'react';

function leerRuta() {
  const hash = window.location.hash;
  if (hash === '#/crear-gasto') return { nombre: 'crear' };
  if (hash === '#/resumen') return { nombre: 'resumen' };
  const detalle = /^#\/gastos\/([1-9]\d*)$/.exec(hash);
  if (detalle && Number.isSafeInteger(Number(detalle[1]))) {
    return { nombre: 'detalle', id: Number(detalle[1]) };
  }
  return { nombre: 'lista' };
}

export function useNavegacion() {
  const [ruta, setRuta] = useState(leerRuta);

  useEffect(() => {
    const actualizar = () => setRuta(leerRuta());
    window.addEventListener('hashchange', actualizar);
    return () => window.removeEventListener('hashchange', actualizar);
  }, []);

  const navegar = useCallback((destino) => {
    window.location.hash = destino;
    setRuta(leerRuta());
  }, []);

  return { ruta, navegar };
}
