import { useCallback, useEffect, useState } from 'react';

export function useNavegacion() {
  const [creando, setCreando] = useState(() => window.location.hash === '#/crear-gasto');

  useEffect(() => {
    const actualizar = () => setCreando(window.location.hash === '#/crear-gasto');
    window.addEventListener('hashchange', actualizar);
    return () => window.removeEventListener('hashchange', actualizar);
  }, []);

  const irAlListado = useCallback(() => {
    window.location.hash = '/';
    setCreando(false);
  }, []);

  return { creando, irAlListado };
}
