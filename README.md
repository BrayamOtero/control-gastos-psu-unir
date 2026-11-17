# Control de gastos personales

Aplicación web en Python con Flask y SQLite para registrar, consultar, editar y eliminar gastos personales, con un resumen mensual por categoría.

## Requisitos

- Python 3.8 o superior
- `pip`
- Conexión a internet para el gráfico del resumen mensual (Chart.js se carga desde un CDN)

## Instalación

Desde la carpeta del proyecto:

```bash
# 1. Crear el entorno virtual
python3 -m venv .venv

# 2. Activarlo
source .venv/bin/activate          # macOS / Linux
# .venv\Scripts\activate           # Windows (PowerShell o CMD)

# 3. Instalar las dependencias
pip install -r requirements.txt
```

## Arrancar la aplicación

Con el entorno virtual activado:

```bash
python app.py
```

O, de forma equivalente:

```bash
flask --app app run --debug
```

Después abre <http://127.0.0.1:5000> en el navegador.

La base de datos `gastos.db` se crea automáticamente junto a `app.py` la primera vez que se inicia la aplicación. No se sube al repositorio porque está en `.gitignore`.

Para detener el servidor, pulsa `Ctrl + C` en la terminal.

## Pantallas

| Ruta | Descripción |
| --- | --- |
| `/` | Listado de gastos con filtros por mes, categoría y descripción |
| `/gastos/nuevo` | Registro de un gasto nuevo |
| `/gastos/<id>/ver` | Detalle de un gasto |
| `/gastos/<id>/editar` | Edición de un gasto |
| `/resumen-mensual` | Resumen del mes por categoría con gráfico |

## API

Todas las respuestas son JSON.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/gastos?mes=AAAA-MM&categoria=...&q=...` | Lista los gastos; los filtros son opcionales |
| `POST` | `/gastos` | Crea un gasto |
| `GET` | `/gastos/<id>` | Obtiene un gasto |
| `PUT` | `/gastos/<id>` | Actualiza un gasto |
| `DELETE` | `/gastos/<id>` | Elimina un gasto |
| `GET` | `/gastos/resumen?mes=AAAA-MM` | Resumen mensual por categoría |

Ejemplo para crear un gasto:

```bash
curl -X POST http://127.0.0.1:5000/gastos \
  -H "Content-Type: application/json" \
  -d '{"fecha":"2026-11-15","importe":"12.50","categoria":"Ocio","descripcion":"Cine"}'
```

Categorías válidas: Alimentación, Transporte, Vivienda, Servicios, Salud, Ocio y Otros.

## Estructura

```text
app.py                 Backend Flask y acceso a SQLite
requirements.txt       Dependencias
templates/             Plantillas HTML
static/css/styles.css  Estilos
```
