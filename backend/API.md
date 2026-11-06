# API de gastos: referencia para frontend

Contrato HTTP del backend. Los ejemplos asumen que la aplicación corre en `http://localhost:8080`.

## Convenciones

- Base de la API: `/api/gastos`
- `Content-Type` para solicitudes JSON: `application/json`
- Las fechas usan formato ISO `yyyy-MM-dd`, por ejemplo `2026-11-05`.
- Los importes son decimales positivos con hasta dos decimales y hasta 10 dígitos enteros (máximo `9999999999.99`). En JSON se envían como números, no como cadenas.
- Las categorías válidas son `ALIMENTACION`, `TRANSPORTE`, `VIVIENDA`, `SERVICIOS`, `SALUD`, `OCIO` y `OTROS`.
- `descripcion` es opcional; admite hasta 255 caracteres.

## Gasto de entrada

Crear y modificar usan el mismo objeto. No se envía `id` al crear ni al modificar.

```json
{
  "fecha": "2026-11-05",
  "importe": 18.75,
  "categoria": "ALIMENTACION",
  "descripcion": "Compra semanal"
}
```

Campos requeridos: `fecha`, `importe` y `categoria`. `descripcion` puede omitirse o ser `null`.

## Gasto de respuesta

```json
{
  "id": 42,
  "fecha": "2026-11-05",
  "importe": 18.75,
  "categoria": "ALIMENTACION",
  "descripcion": "Compra semanal"
}
```

## Endpoints

### Crear gasto

`POST /api/gastos`

Cuerpo: gasto de entrada.

Respuesta `201 Created`: gasto de respuesta. Incluye la cabecera `Location` con la URL del recurso creado, por ejemplo `/api/gastos/42`.

Errores:
- `400 Bad Request`: cuerpo JSON incorrecto o fecha, importe, categoría o descripción inválidos.

### Consultar gasto por identificador

`GET /api/gastos/{id}`

Ejemplo: `GET /api/gastos/42`

Respuesta `200 OK`: gasto de respuesta.

Errores:
- `400 Bad Request`: el identificador no es numérico.
- `404 Not Found`: no existe un gasto con ese identificador.

### Listar y filtrar gastos

`GET /api/gastos`

Todos los filtros son opcionales y se combinan mediante AND. Los límites de fecha e importe son inclusivos. La descripción hace una búsqueda parcial sin distinguir mayúsculas y minúsculas; se eliminan sus espacios iniciales y finales. Una descripción vacía o con solo espacios se ignora.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `fechaDesde` | fecha | Fecha mínima, inclusiva. |
| `fechaHasta` | fecha | Fecha máxima, inclusiva. |
| `importeMinimo` | decimal | Importe mínimo, inclusivo; no negativo y hasta dos decimales. |
| `importeMaximo` | decimal | Importe máximo, inclusivo; no negativo y hasta dos decimales. |
| `categoria` | texto | Una categoría válida, coincidencia exacta. |
| `descripcion` | texto | Fragmento a buscar, ignorando mayúsculas. |
| `page` | entero | Índice de página desde cero. Por defecto: `0`. |
| `size` | entero | Elementos por página. Por defecto: `10`; máximo aplicado: `50`. |
| `sort` | texto | Campo y dirección de orden. Campos permitidos: `id`, `fecha`, `importe`, `categoria`, `descripcion`. Por defecto: `id` ascendente. |

Para ordenar descendente, por ejemplo: `sort=fecha,desc`. Un tamaño solicitado superior a 50 se limita a 50. Sin filtros se listan todos los gastos paginados.

Ejemplo:

```http
GET /api/gastos?fechaDesde=2026-11-01&fechaHasta=2026-11-30&categoria=ALIMENTACION&descripcion=super&page=0&size=20&sort=importe,desc
```

Respuesta `200 OK` (formato de paginación Spring Data `PagedModel`):

```json
{
  "content": [
    {
      "id": 42,
      "fecha": "2026-11-05",
      "importe": 18.75,
      "categoria": "ALIMENTACION",
      "descripcion": "Compra semanal"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

Errores:
- `400 Bad Request`: fecha mal formada, rango de fechas invertido, importes negativos, con más de dos decimales o rango invertido, categoría desconocida o campo de orden no permitido.

### Modificar gasto

`PUT /api/gastos/{id}`

Cuerpo: gasto de entrada completo. Se reemplazan todos los campos editables; el identificador se conserva.

Respuesta `200 OK`: gasto de respuesta actualizado.

Errores:
- `400 Bad Request`: identificador no numérico o no positivo; cuerpo incorrecto, fecha futura/ausente, importe no positivo/ausente o con más de dos decimales, categoría ausente/desconocida o descripción mayor a 255 caracteres.
- `404 Not Found`: no existe un gasto con ese identificador.

### Eliminar gasto

`DELETE /api/gastos/{id}`

Respuesta `204 No Content`: eliminado correctamente; no tiene cuerpo.

Errores:
- `400 Bad Request`: identificador no numérico o no positivo.
- `404 Not Found`: no existe un gasto con ese identificador.

### Resumen mensual

`GET /api/gastos/resumen?mes={mes}&anio={anio}`

Ambos parámetros son requeridos. `mes` debe estar entre 1 y 12; `anio`, entre 1 y 9999.

Ejemplo: `GET /api/gastos/resumen?mes=11&anio=2026`

Respuesta `200 OK`:

```json
{
  "anio": 2026,
  "mes": 11,
  "totalGastado": 40.00,
  "numeroGastos": 3,
  "categorias": [
    {
      "categoria": "ALIMENTACION",
      "total": 30.00,
      "numeroGastos": 2,
      "porcentaje": 75.00
    },
    {
      "categoria": "OCIO",
      "total": 10.00,
      "numeroGastos": 1,
      "porcentaje": 25.00
    },
    {
      "categoria": "TRANSPORTE",
      "total": 0.00,
      "numeroGastos": 0,
      "porcentaje": 0.00
    }
  ]
}
```

La respuesta incluye siempre las siete categorías, ordenadas por importe descendente. Las categorías sin gastos llevan total, número y porcentaje en cero. Si no hay gastos en el mes, o el mes es futuro, el endpoint devuelve `200 OK` con totales y categorías en cero. Los porcentajes se redondean a dos decimales; su suma puede diferir ligeramente de 100.

Errores:
- `400 Bad Request`: falta `mes` o `anio`, no son números enteros o quedan fuera del rango permitido.

## Formato de errores

Los errores manejados por el `RestControllerAdvice` se devuelven como `ProblemDetail` JSON. `status` y `detail` identifican el código HTTP y el mensaje; otros campos estándar como `type`, `title` e `instance` pueden acompañarlos.

```json
{
  "status": 400,
  "detail": "El importe debe ser mayor que cero"
}
```

Ejemplo de recurso inexistente:

```json
{
  "status": 404,
  "detail": "No existe el gasto con id 999"
}
```

Los valores de `detail` dependen del error concreto; la interfaz debería usar el código HTTP y mostrar `detail` cuando esté disponible.
