#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
setup_firebase.py  --  Aprovisionamiento COMPLETO de Firestore (modo demo)
==========================================================================
Con UN SOLO archivo (la clave del Admin SDK) hace TODO:

  1. Conecta con tu proyecto de Firebase.
  2. Si la base de datos Firestore NO existe, la CREA (por ti, vía API).
  3. Espera a que esté lista.
  4. Sube los datos sintéticos (especialidades, doctores, horarios, citas, usuario).
  5. Escribe la bandera metadata/seed (siembra idempotente).
  6. Verifica leyendo de vuelta.

OPTIMIZACIÓN DE COSTOS
----------------------
  * Sembrado idempotente: 1 lectura; si ya está, no escribe nada.
  * Escrituras agrupadas en WriteBatch (bloques de 450).
  * Sin listeners ni consultas dentro de bucles.

USO
---
  python3 setup_firebase.py /ruta/a/firebase-admin-sdk.json
  python3 setup_firebase.py /ruta/a/... --check          # solo verifica
  python3 setup_firebase.py /ruta/a/... --force          # re-siembra
  python3 setup_firebase.py /ruta/a/... --location europe-west1

Requisitos:  pip install firebase-admin==7.1.0   (ya viene en el entorno)
"""
import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request

try:
    import firebase_admin
    from firebase_admin import credentials, firestore
except ImportError:
    print("ERROR: falta firebase-admin.  Instala: pip install firebase-admin==7.1.0")
    sys.exit(1)

HERE = os.path.dirname(os.path.abspath(__file__))
SEED = os.path.normpath(os.path.join(HERE, "..", "seed"))
SEED_VERSION = 1
BATCH_LIMIT = 450

# Lugares donde se busca la clave si no se indica una ruta explícita.
KEY_SEARCH_DIRS = [
    "/opt/flutter",
    "/home/user/.firebase_keys",
    "/home/user/uploaded_files",
    "/home/user/Downloads",
    "/home/user",
    "/mnt/user-data/uploads",
    "/mnt/user-data",
]
KEY_HINTS = ("adminsdk", "firebase-admin", "serviceaccount", "service-account")


def looks_like_admin_key(path):
    """True si el JSON tiene las claves típicas de una cuenta de servicio."""
    try:
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        return (data.get("type") == "service_account"
                and "private_key" in data and "project_id" in data)
    except Exception:
        return False


def auto_discover_key():
    """Busca automáticamente la clave del Admin SDK en ubicaciones comunes."""
    # 1) Nombres conocidos primero
    for d in KEY_SEARCH_DIRS:
        if not os.path.isdir(d):
            continue
        for name in os.listdir(d):
            low = name.lower()
            if low.endswith(".json") and any(h in low for h in KEY_HINTS):
                p = os.path.join(d, name)
                if looks_like_admin_key(p):
                    return p
    # 2) Cualquier .json que sea una cuenta de servicio
    for d in KEY_SEARCH_DIRS:
        if not os.path.isdir(d):
            continue
        for name in os.listdir(d):
            if not name.lower().endswith(".json"):
                continue
            p = os.path.join(d, name)
            if looks_like_admin_key(p):
                return p
    return None


# --------------------------------------------------------------------------
#  Utilidades
# --------------------------------------------------------------------------
def load(name):
    path = os.path.join(SEED, name)
    if not os.path.exists(path):
        raise FileNotFoundError(
            "No se encuentra %s. Ejecuta primero: python3 generate_seed.py" % path)
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def chunks(seq, size):
    for i in range(0, len(seq), size):
        yield seq[i:i + size]


def commit_batch(db, items):
    total = 0
    for group in chunks(items, BATCH_LIMIT):
        batch = db.batch()
        for col, doc_id, data in group:
            ref = db.collection(col).document(doc_id) if doc_id \
                else db.collection(col).document()
            batch.set(ref, data)
        batch.commit()
        total += len(group)
    return total


def _access_token(key_path):
    from google.oauth2 import service_account
    import google.auth.transport.requests as tr
    creds = service_account.Credentials.from_service_account_file(
        key_path, scopes=["https://www.googleapis.com/auth/cloud-platform"])
    creds.refresh(tr.Request())
    return creds.token


def create_default_database(key_path, project_id, location):
    """Crea la base Firestore (default) por API. Devuelve (ok, mensaje)."""
    try:
        token = _access_token(key_path)
    except Exception as e:
        return False, "no se pudo obtener token: %s" % e

    url = ("https://firestore.googleapis.com/v1/projects/%s/databases"
           "?databaseId=(default)" % project_id)
    body = json.dumps({"type": "FIRESTORE_NATIVE", "locationId": location}).encode()
    req = urllib.request.Request(
        url, data=body, method="POST",
        headers={"Authorization": "Bearer " + token, "Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=180) as r:
            return True, r.read().decode()[:400]
    except urllib.error.HTTPError as e:
        detail = e.read().decode()[:600]
        # Caso típico: la API de Cloud Firestore aún no está habilitada.
        if e.code == 403 and ("has not been used" in detail
                              or "is disabled" in detail
                              or "PERMISSION_DENIED" in detail):
            print_api_help(project_id)
            return False, "API de Cloud Firestore deshabilitada en el proyecto."
        return False, "HTTP %s: %s" % (e.code, detail)
    except Exception as e:
        return False, str(e)


def print_api_help(project_id):
    """Muestra los enlaces exactos para habilitar Firestore en el navegador."""
    print()
    print("  " + "=" * 66)
    print("  ACCIÓN REQUERIDA (1 minuto, en tu navegador):")
    print("  " + "=" * 66)
    print("  La API de Cloud Firestore está DESHABILITADA en tu proyecto.")
    print("  Abre este enlace y pulsa el botón azul 'HABILITAR':")
    print()
    print("    https://console.cloud.google.com/apis/library/"
          "firestore.googleapis.com?project=%s" % project_id)
    print()
    print("  OPCIÓN MÁS SIMPLE (crea la base y HABILITA la API a la vez):")
    print("    https://console.firebase.google.com/project/%s/"
          "firestore" % project_id)
    print("    -> Pulsa 'Crear base de datos' (modo producción) y elige región.")
    print()
    print("  Cuando termines, vuelve a ejecutar este script y sembrará solo.")
    print("  " + "=" * 66)


# --------------------------------------------------------------------------
#  Main
# --------------------------------------------------------------------------
def main():
    ap = argparse.ArgumentParser(description="Aprovisiona Firestore con datos demo.")
    ap.add_argument("key", nargs="?", default=None,
                    help="Ruta a la clave del Admin SDK (JSON). Si se omite, "
                         "se busca automáticamente en ubicaciones comunes.")
    ap.add_argument("--force", action="store_true", help="Fuerza la re-siembra.")
    ap.add_argument("--check", action="store_true", help="Solo verifica (no escribe).")
    ap.add_argument("--no-create", action="store_true",
                    help="No intentar crear la base de datos automáticamente.")
    ap.add_argument("--location", default="europe-west1",
                    help="Ubicación de Firestore si hay que crearla (def. europe-west1).")
    args = ap.parse_args()

    # --- Localizar la clave del Admin SDK ---
    key = args.key
    if not key or not os.path.exists(key):
        if key:
            print("AVISO: no se encontró '%s'. Buscando automáticamente..." % key)
        found = auto_discover_key()
        if found:
            key = found
            print("OK: clave del Admin SDK detectada automáticamente en: %s" % key)
        else:
            print("ERROR: no se encontró ninguna clave del Admin SDK.")
            print("Buscada en: %s" % ", ".join(KEY_SEARCH_DIRS))
            print()
            print("Descárgala así:")
            print("  Firebase Console > Config. del proyecto > Cuentas de servicio")
            print("  > idioma Python > Generar nueva clave privada.")
            print("Luego súbela con la pestaña de archivos o pásala como argumento:")
            print("  python3 setup_firebase.py /ruta/a/firebase-admin-sdk.json")
            sys.exit(1)

    if not os.path.exists(key):
        print("ERROR: no existe la clave: %s" % key)
        sys.exit(1)

    if not firebase_admin._apps:
        if not looks_like_admin_key(key):
            print("AVISO: '%s' no parece una clave de cuenta de servicio válida."
                  % key)
        firebase_admin.initialize_app(credentials.Certificate(key))
    project_id = firebase_admin.get_app().project_id
    print("OK: clave válida. Proyecto = '%s'." % project_id)

    db = firestore.client()

    # ---- 1) ¿Existe la base de datos? ----
    snap = None
    try:
        snap = db.collection("metadata").document("seed").get()
    except Exception as e:
        msg = str(e)
        not_found = ("NOT_FOUND" in msg or "does not exist" in msg
                     or "404" in msg or "not been used" in msg)
        if not not_found:
            print("FALLO al leer Firestore: %s" % msg)
            sys.exit(3)
        print("\nLa base de datos Firestore NO existe todavía.")
        if args.no_create:
            print("Créala en la consola y vuelve a ejecutar. Abortando.")
            sys.exit(2)
        print("Intentando CREARLA automáticamente en '%s'..." % args.location)
        ok, detail = create_default_database(key, project_id, args.location)
        if not ok:
            # create_default_database ya imprime la ayuda detallada si aplica.
            print("\n>>> No se pudo crear la base automáticamente.")
            print("    Sigue los pasos de arriba y vuelve a ejecutar este script.")
            sys.exit(2)
        print("  OK: %s" % detail)
        # La creación tarda unos segundos en propagarse.
        for _ in range(10):
            time.sleep(6)
            try:
                snap = db.collection("metadata").document("seed").get()
                print("  Base de datos lista.")
                break
            except Exception:
                print("  ...esperando a que Firestore esté disponible")
        if snap is None:
            print("La base aún no responde; espera 1-2 min y reintenta.")
            sys.exit(2)

    exists = snap.exists
    version = snap.to_dict().get("version", 0) if exists else 0

    # ---- 2) Modo verificación ----
    if args.check:
        print("\n--- Estado actual ---")
        print("metadata/seed existe:", exists, "| version:", version)
        for col in ("specialties", "doctors", "slots", "appointments", "users"):
            try:
                it = db.collection(col).limit(BATCH_LIMIT).stream()
                print("  %-14s %d docs" % (col, sum(1 for _ in it)))
            except Exception as e:
                print("  %-14s error: %s" % (col, e))
        return

    # ---- 3) Idempotencia ----
    if exists and version >= SEED_VERSION and not args.force:
        print("\nOK: los datos ya estaban sembrados (v%s)." % version)
        print("    No se escribe nada (ahorro de cuota). Usa --force para re-sembrar.")
        return

    # ---- 4) Sembrar ----
    specialties = load("specialties.json")
    doctors = load("doctors.json")
    slots = load("slots.json")
    users = load("users.json")
    appointments = load("appointments.json")

    items = []
    for s in specialties:
        items.append(("specialties", s["id"], s))
    for d in doctors:
        items.append(("doctors", d["id"], d))
    for sl in slots:
        items.append(("slots", sl["id"], {k: v for k, v in sl.items() if k != "id"}))
    for u in users:
        items.append(("users", u["id"], u))
    for a in appointments:
        items.append(("appointments", None, a))

    print("\nSubiendo %d documentos en lotes de %d..." % (len(items), BATCH_LIMIT))
    written = commit_batch(db, items)

    db.collection("metadata").document("seed").set({
        "version": SEED_VERSION,
        "specialties": len(specialties),
        "doctors": len(doctors),
        "slots": len(slots),
        "users": len(users),
        "appointments": len(appointments),
        "seededAt": firestore.SERVER_TIMESTAMP,
    })

    print("OK: %d documentos escritos + bandera metadata/seed (v%d)." % (written, SEED_VERSION))

    # ---- 5) Verificación final ----
    print("\nVerificación:")
    for col, expected in (("specialties", len(specialties)), ("doctors", len(doctors)),
                          ("slots", len(slots)), ("users", len(users)),
                          ("appointments", len(appointments))):
        it = db.collection(col).limit(BATCH_LIMIT).stream()
        n = sum(1 for _ in it)
        print("  %-14s %d (esperado %d) %s" % (col, n, expected,
                                               "OK" if n == expected else "REVISAR"))
    print("\nListo. Tu base de datos Firestore ya tiene los datos para la demo.")


if __name__ == "__main__":
    main()
