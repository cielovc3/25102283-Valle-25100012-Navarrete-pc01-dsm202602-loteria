# PC01 · Lotería — DSM 2026-02

## Integrantes
| Nombre          | Código   |
|-----------------|----------|
| Cielo Valle     | 25102283 |
| Italo Navarrete | 25100012 |

## Descripción
App Android que permite autenticarse, registrar una jugada de 6 números (1 a 45, distintos)
y seguir un sorteo en tiempo real desde Firestore, calculando aciertos y resultado en la app.

## Tecnologías
Kotlin, Jetpack Compose (Material 3), Navigation Compose, Firebase Authentication y Cloud Firestore.

## Estructura en Firestore
- `sorteos/sorteo-prueba` → `nombre`, `fecha`, `estado` (`abierto` | `en_curso` | `finalizado`)
- `sorteos/sorteo-prueba/bolillas/{id}` → `numero`, `orden` (1 a 6)
- `sorteos/sorteo-prueba/jugadas/{uid}` → `uid`, `sorteoId`, `numeros` (6 valores)

## Instrucciones de ejecución
1. Clonar el repositorio y abrirlo en Android Studio.
2. El archivo `app/google-services.json` ya está incluido (proyecto Firebase compartido).
3. Ejecutar la app en un emulador o dispositivo (minSdk 24).
4. Crear una cuenta, registrar la jugada mientras el sorteo esté `abierto` e ir a Seguimiento.
5. Desde la consola de Firebase: cambiar `estado` a `en_curso`, publicar las bolillas
   una por una en `bolillas` (campos `numero` y `orden`) y al final cambiar `estado` a `finalizado`.

## Ramas
- `feature/auth`: login, registro y navegación
- `feature/sorteo`: jugada, seguimiento y escucha en tiempo real
- `feature/readme`: documentación