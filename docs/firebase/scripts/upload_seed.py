#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
upload_seed.py
==============
Sube los datos sintéticos de ../seed/ a Cloud Firestore usando el Admin SDK.

OPTIMIZACIÓN DE COSTOS (cuenta demo/gratuita)
---------------------------------------------
  * Siembra IDEMPOTENTE: consulta metadata/seed (1 lectura). Si la versión ya
    está aplicada, NO escribe nada.
  * Escrituras AGRUPADAS: usa WriteBatch en bloques de <=450 operaciones
    (cada commit cuenta como 1 operación por documento, no por lote).
  * Sin listeners ni bucles de lectura: sólo lecturas masivas controladas.

Requisitos
----------
  1) Crear la base de datos Firestore (consola > Build > Firestore Database).
  2) Descargar la clave del Admin SDK (Python) y pasarla como argumento o
     dejarla en /opt/flutter/firebase-admin-sdk.json
  3) pip install firebase-admin==7.1.0

Uso
---
  python3 upload_seed.py /ruta/a/firebase-admin-sdk.json
  # o sin argumento, usa /opt/flutter/firebase-admin-sdk.json
"""
import json
import os
import sys

try:
    import firebase_admin
    from firebase_admin import credentials, firestore
except ImportError:
    print("Falta firebase-admin. Instala: pip install firebase-admin==7.1.0")
    sys.exit(1)

HERE = os.path.dirname(os.path.abspath(__file__))
SEED = os.path.normpath(os.path.join(HERE, "..", "seed"))
SEED_VERSION = 1
BATCH_LIMIT = 450
DEFAULT_KEY = "/opt/flutter/firebase-admin-sdk.json"


def load(name):
    with open(os.path.join(SEED, name), encoding="utf-8") as f:
        return json.load(f)


def chunks(seq, size):
    for i in range(0, len(seq), size):
        yield seq[i:i + size]


def commit_batch(db, items):
    """items = lista de (collection, doc_id_or_None, data)."""
    for group in chunks(items, BATCH_LIMIT):
        batch = db.batch()
        for col, doc_id, data in group:
            ref = db.collection(col).document(doc_id) if doc_id \
                else db.collection(col).document()
            batch.set(ref, data)
        batch.commit()


def main():
    key_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_KEY
    if not os.path.exists(key_path):
        print("No se encontró la clave del Admin SDK: %s" % key_path)
        print("Descárgala en Firebase Console > Configuración del proyecto > "
              "Cuentas de servicio > Python.")
        sys.exit(1)

    if not firebase_admin._apps:
        firebase_admin.initialize_app(credentials.Certificate(key_path))
    db = firestore.client()

    # --- 1 lectura: bandera de siembra idempotente ---
    meta_ref = db.collection("metadata").document("seed")
    snap = meta_ref.get()
    if snap.exists:
        current = snap.to_dict().get("version", 0)
        if current >= SEED_VERSION:
            print("Datos ya sembrados (v%s). No se escribe nada." % current)
            return

    docs = load("doctors.json")
    items = []

    for s in load("specialties.json"):
        items.append(("specialties", s["id"], s))
    for d in docs:
        items.append(("doctors", d["id"], d))
    for sl in load("slots.json"):
        items.append(("slots", sl["id"], {k: v for k, v in sl.items() if k != "id"}))
    for u in load("users.json"):
        items.append(("users", u["id"], u))
    for a in load("appointments.json"):
        items.append(("appointments", None, a))

    commit_batch(db, items)

    meta_ref.set({
        "version": SEED_VERSION,
        "specialties": len(load("specialties.json")),
        "doctors": len(docs),
        "slots": len(load("slots.json")),
        "users": len(load("users.json")),
        "appointments": len(load("appointments.json")),
    })
    print("Siembra completada: %d documentos." % (len(items) + 1))


if __name__ == "__main__":
    main()
