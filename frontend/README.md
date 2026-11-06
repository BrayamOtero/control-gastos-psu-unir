# Frontend de control de gastos

Listado paginado y creación de gastos en español, con React, Vite y CSS sin librerías de
estilos. Envía solicitudes reales a `GET /api/gastos` y `POST /api/gastos` según el
[contrato del backend](../backend/API.md).

## Desarrollo

Requisito: Node.js 20.19+ o 22.12+ y npm.

```sh
cd frontend
npm install
cp .env.example .env
npm run dev
```

El backend debe estar ejecutándose por separado en `http://localhost:8080`.
Abre la URL indicada por Vite (habitualmente `http://localhost:5173`).
`VITE_API_URL` configura la URL base del backend, sin `/api/gastos`; si no se
define, se usa `http://localhost:8080`. Reinicia Vite después de cambiar `.env`.
No incluyas credenciales en variables `VITE_*`: son públicas.

En desarrollo, las peticiones a `/api` pasan por el proxy de Vite, cuyo destino
es `VITE_API_URL`, para no necesitar cambios de CORS en el backend.
En la compilación de producción (también con `npm run preview`), las peticiones
se envían directamente a `VITE_API_URL`. Si frontend y backend están en orígenes
distintos, configura CORS en el backend para el origen del frontend.
En producción HTTPS, usa también una URL HTTPS para la API.
Las variables se incorporan al compilar; cambiar la URL requiere recompilar.

## Estructura

- `src/components/`: formularios, tabla y paginación accesibles.
- `src/hooks/`: navegación, consultas, estado de envío, errores y confirmación.
- `src/services/`: llamadas HTTP y validación del contrato.
- `src/constants/`: categorías de la API y etiquetas en español.
- `src/pages/`: páginas de listado y creación.
- `src/styles.css`: estilos sencillos y adaptables a móviles.

Se admiten importes con coma o punto, que se envían como números JSON.
El formulario conserva los datos si falla el envío y solo se limpia tras
recibir la confirmación `201` del servidor. No se reintentan solicitudes
automáticamente, para evitar duplicados.

La pantalla principal muestra únicamente fecha, importe y categoría, con diez
gastos por página. Inicialmente consulta desde el primer hasta el último día
del mes actual (fecha local), ordenando por fecha descendente. Los filtros
opcionales de fechas, categoría, descripción e importes se combinan en la API.
Puedes ordenar por fecha, importe o categoría en ambas direcciones.
Pulsa **Aplicar filtros** para consultar; se vuelve a la primera página.
**Restablecer mes actual** restaura los filtros iniciales. Vaciar los límites
de fecha permite consultar otros meses o todos los gastos.

El botón superior **Crear gasto** abre `#/crear-gasto`. Tras recibir `201`,
se vuelve a `#/` y se consulta de nuevo el mes actual, mostrando la confirmación
de creación. Un gasto de otro mes no aparecerá hasta ajustar las fechas.
La navegación usa el fragmento de URL y admite los botones atrás/adelante
del navegador sin configurar rutas en el servidor ni añadir librerías.

## Validación y compilación

```sh
npm test
npm run build
npm run preview
```

Las pruebas unitarias validan límites, fechas, categorías, filtros y parámetros
de paginación y orden; no simulan la API.
