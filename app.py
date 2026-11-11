import re
import sqlite3
from datetime import datetime
from decimal import Decimal, InvalidOperation
from pathlib import Path
from datetime import date, datetime

from flask import Flask, jsonify, render_template, request

app = Flask(__name__)
DB_PATH = Path(__file__).with_name("gastos.db")

CATEGORIAS = {"Alimentación", "Transporte", "Vivienda", "Servicios", "Salud", "Ocio", "Otros"}
FECHA_RE = re.compile(r"^\d{4}-\d{2}-\d{2}$")

MES_RE = re.compile(r"^\d{4}-\d{2}$")


def validar_filtros(args):
    errores = {}

    mes = args.get("mes", "").strip()
    if mes:
        if not MES_RE.match(mes):
            errores["mes"] = "El parámetro 'mes' debe tener el formato AAAA-MM."
        else:
            try:
                inicio = datetime.strptime(mes, "%Y-%m").date()
            except ValueError:
                errores["mes"] = "El parámetro 'mes' no es un mes válido (01-12)."
            else:
                hoy = date.today()
                if (inicio.year, inicio.month) > (hoy.year, hoy.month):
                    errores["mes"] = "El parámetro 'mes' debe ser el mes actual o uno anterior."

    categoria = args.get("categoria", "").strip()
    if categoria and categoria not in CATEGORIAS:
        errores["categoria"] = (
            "El parámetro 'categoria' debe ser una de: " + ", ".join(sorted(CATEGORIAS)) + "."
        )

    q = args.get("q", "").strip()
    return errores, mes, categoria, q


@app.get("/gastos")
def listar_gastos():
    errores, mes, categoria, q = validar_filtros(request.args)
    if errores:
        return jsonify({
            "error": "Parámetros inválidos. Corrige los parámetros indicados.",
            "mensajes": errores,
        }), 400

    condiciones, params = [], []
    if mes:
        condiciones.append("substr(fecha, 1, 7) = ?")
        params.append(mes)
    if categoria:
        condiciones.append("categoria = ?")
        params.append(categoria)
    if q:
        condiciones.append("instr(casefold(descripcion), ?) > 0")
        params.append(q.casefold())

    sql = "SELECT id, fecha, importe_centavos, categoria, descripcion FROM gastos"
    if condiciones:
        sql += " WHERE " + " AND ".join(condiciones)
    sql += " ORDER BY fecha DESC, id DESC"

    with get_connection() as conn:
        filas = conn.execute(sql, params).fetchall()

    return jsonify([
        {
            "id": id_,
            "fecha": fecha,
            "importe": centavos / 100,
            "categoria": cat,
            "descripcion": desc,
        }
        for id_, fecha, centavos, cat, desc in filas
    ]), 200

def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.create_function(
        "casefold", 1, lambda s: s.casefold() if isinstance(s, str) else s, deterministic=True
    )
    return conn


def init_db():
    with get_connection() as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS gastos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT NOT NULL,
                importe_centavos INTEGER NOT NULL,
                categoria TEXT NOT NULL,
                descripcion TEXT NOT NULL DEFAULT ''
            )
            """
        )


def validar(data):
    errores = {}

    fecha = data.get("fecha")
    if not isinstance(fecha, str) or not FECHA_RE.match(fecha):
        errores["fecha"] = "La fecha es obligatoria y debe tener el formato AAAA-MM-DD."
    else:
        try:
            datetime.strptime(fecha, "%Y-%m-%d")
        except ValueError:
            errores["fecha"] = "La fecha no existe en el calendario. Corrígela (AAAA-MM-DD)."

    centavos = None
    importe = data.get("importe")
    if importe is None or isinstance(importe, bool) or not isinstance(importe, (int, float, str)):
        errores["importe"] = "El importe es obligatorio y debe ser numérico."
    else:
        try:
            valor = Decimal(str(importe).strip())
            if not valor.is_finite():
                raise InvalidOperation
        except InvalidOperation:
            errores["importe"] = "El importe debe ser un número válido."
        else:
            if valor <= 0:
                errores["importe"] = "El importe debe ser mayor que cero."
            elif valor != valor.quantize(Decimal("0.01")):
                errores["importe"] = "El importe debe tener como máximo dos decimales."
            else:
                centavos = int(valor * 100)

    categoria = data.get("categoria")
    if categoria not in CATEGORIAS:
        errores["categoria"] = (
            "La categoría debe ser una de: " + ", ".join(sorted(CATEGORIAS)) + "."
        )

    descripcion = data.get("descripcion", "")
    if descripcion is None:
        descripcion = ""
    if not isinstance(descripcion, str):
        errores["descripcion"] = "La descripción debe ser texto."

    return errores, fecha, centavos, categoria, descripcion

def leer_gasto_valido():
    """Devuelve (datos, None) si el JSON es válido o (None, respuesta_400) si no."""
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return None, (jsonify({
            "error": "Datos inválidos",
            "mensajes": {"cuerpo": "El cuerpo debe ser un objeto JSON válido."},
        }), 400)

    errores, fecha, centavos, categoria, descripcion = validar(data)
    if errores:
        return None, (jsonify({
            "error": "Datos inválidos. Corrige los campos indicados.",
            "mensajes": errores,
        }), 400)

    return (fecha, centavos, categoria, descripcion.strip()), None


def gasto_no_encontrado(gasto_id):
    return jsonify({"error": f"No existe ningún gasto con ID {gasto_id}."}), 404

@app.get("/gastos/<int:gasto_id>")
def obtener_gasto(gasto_id):
    with get_connection() as conn:
        fila = conn.execute(
            "SELECT id, fecha, importe_centavos, categoria, descripcion "
            "FROM gastos WHERE id = ?",
            (gasto_id,),
        ).fetchone()

    if fila is None:
        return gasto_no_encontrado(gasto_id)

    id_, fecha, centavos, categoria, descripcion = fila
    return jsonify({
        "id": id_,
        "fecha": fecha,
        "importe": centavos / 100,
        "categoria": categoria,
        "descripcion": descripcion,
    }), 200

@app.post("/gastos")
def crear_gasto():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({
            "error": "Datos inválidos",
            "mensajes": {"cuerpo": "El cuerpo debe ser un objeto JSON válido."},
        }), 400

    errores, fecha, centavos, categoria, descripcion = validar(data)
    if errores:
        return jsonify({
            "error": "Datos inválidos. Corrige los campos indicados.",
            "mensajes": errores,
        }), 400

    descripcion = descripcion.strip()
    with get_connection() as conn:
        cur = conn.execute(
            "INSERT INTO gastos (fecha, importe_centavos, categoria, descripcion) "
            "VALUES (?, ?, ?, ?)",
            (fecha, centavos, categoria, descripcion),
        )
        gasto_id = cur.lastrowid

    return jsonify({
        "id": gasto_id,
        "fecha": fecha,
        "importe": centavos / 100,
        "categoria": categoria,
        "descripcion": descripcion,
    }), 201

@app.put("/gastos/<int:gasto_id>")
def actualizar_gasto(gasto_id):
    datos, error = leer_gasto_valido()
    if error:
        return error
    fecha, centavos, categoria, descripcion = datos

    with get_connection() as conn:
        cur = conn.execute(
            "UPDATE gastos SET fecha = ?, importe_centavos = ?, categoria = ?, descripcion = ? "
            "WHERE id = ?",
            (fecha, centavos, categoria, descripcion, gasto_id),
        )
        if cur.rowcount == 0:
            return gasto_no_encontrado(gasto_id)

    return jsonify({
        "id": gasto_id,
        "fecha": fecha,
        "importe": centavos / 100,
        "categoria": categoria,
        "descripcion": descripcion,
    }), 200


@app.delete("/gastos/<int:gasto_id>")
def eliminar_gasto(gasto_id):
    with get_connection() as conn:
        cur = conn.execute("DELETE FROM gastos WHERE id = ?", (gasto_id,))
        if cur.rowcount == 0:
            return gasto_no_encontrado(gasto_id)

    return "", 204

@app.get("/gastos/resumen")
def resumen_gastos():
    errores, mes, _, _ = validar_filtros({
        "mes": request.args.get("mes", "")
    })

    if not mes:
        errores["mes"] = (
            "El parámetro 'mes' es obligatorio y debe tener formato AAAA-MM."
        )

    if errores:
        return jsonify({
            "error": "Parámetros inválidos. Corrige los parámetros indicados.",
            "mensajes": errores,
        }), 400

    with get_connection() as conn:
        filas = conn.execute(
            """
            SELECT categoria, SUM(importe_centavos)
            FROM gastos
            WHERE substr(fecha, 1, 7) = ?
            GROUP BY categoria
            """,
            (mes,),
        ).fetchall()

    importes = dict(filas)
    total_centavos = sum(importes.values())
    categorias = []

    for categoria in sorted(CATEGORIAS):
        centavos = importes.get(categoria, 0)
        porcentaje = 0

        if total_centavos:
            porcentaje = float(
                (
                    Decimal(centavos)
                    / Decimal(total_centavos)
                    * Decimal(100)
                ).quantize(Decimal("0.01"))
            )

        categorias.append({
            "categoria": categoria,
            "total_centavos": centavos,
            "porcentaje": porcentaje,
        })

    respuesta = {
        "mes": mes,
        "total_centavos": total_centavos,
        "categorias": categorias,
    }

    if not filas:
        respuesta["mensaje"] = "No hay gastos registrados en este mes"

    return jsonify(respuesta), 200

@app.get("/")
def index():
    return render_template(
        "index.html",
        categorias=sorted(CATEGORIAS),
        mes_actual=date.today().strftime("%Y-%m"),
    )

@app.get("/gastos/nuevo")
def nuevo_gasto():
    return render_template(
        "nuevo_gasto.html",
        categorias=sorted(CATEGORIAS),
    )

@app.errorhandler(404)
def ruta_no_encontrada(_error):
    return jsonify({"error": "Recurso no encontrado. Revisa la URL y el ID del gasto."}), 404
init_db()

if __name__ == "__main__":
    app.run(debug=True)