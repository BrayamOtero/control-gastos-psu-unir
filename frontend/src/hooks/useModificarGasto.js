import { useRef, useState } from 'react';
import { modificarGasto } from '../services/gastos.js';

export function useModificarGasto(id) {
  const enCurso = useRef(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');

  async function guardar(datos) {
    if (enCurso.current) return null;
    enCurso.current = true;
    setGuardando(true);
    setError('');
    try {
      return await modificarGasto(id, datos);
    } catch (error) {
      setError(error.message);
      return null;
    } finally {
      enCurso.current = false;
      setGuardando(false);
    }
  }

  return { guardar, guardando, error, limpiarError: () => setError('') };
}
