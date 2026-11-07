import { useRef, useState } from 'react';
import { eliminarGasto } from '../services/gastos.js';

export function useEliminarGasto() {
  const enCurso = useRef(false);
  const [eliminando, setEliminando] = useState(false);
  const [error, setError] = useState('');

  async function eliminar(id) {
    if (enCurso.current) return false;
    enCurso.current = true;
    setEliminando(true);
    setError('');
    try {
      await eliminarGasto(id);
      return true;
    } catch (error) {
      setError(error.message);
      return false;
    } finally {
      enCurso.current = false;
      setEliminando(false);
    }
  }

  return { eliminar, eliminando, error };
}
