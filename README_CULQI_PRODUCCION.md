# Patch Culqi para PeruTalent

Este patch prepara la integración recurrente para una prueba controlada con llaves de Culqi.

## Incluye

- Migración del frontend desde Checkout v4 a Culqi Checkout Custom.
- Validación de que llave privada, token, tarjeta, cliente, plan y suscripción pertenezcan al mismo ambiente (`test` o `live`).
- Validación de la respuesta de suscripción activa y del plan configurado.
- Bloqueo pesimista por usuario para evitar suscripciones simultáneas duplicadas.
- Cancelación compensatoria en Culqi si la suscripción externa se crea pero falla el guardado local.
- Webhook protegido con un token secreto configurado por variable de entorno.
- Procesamiento idempotente de eventos y almacenamiento de payloads sanitizados.
- Eliminación de logs que exponían tokens, correos o respuestas completas.
- Conservación de Premium durante el periodo ya pagado cuando Culqi notifica un intento de renovación fallido.
- Confirmación visible antes de iniciar un cobro real con llaves `live`.
- Pruebas automatizadas de validación de ambiente, sanitización y autorización del webhook.

## Aplicación

Ejecutar desde la raíz del proyecto:

```powershell
$zip = "$env:USERPROFILE\Downloads\perutalent_culqi_produccion_segura_patch.zip"
Expand-Archive -Path $zip -DestinationPath "." -Force
```

## Comprobación

```powershell
powershell -ExecutionPolicy Bypass -File .\ejecutar-pruebas-completas.ps1
```

También se puede comprobar por separado:

```powershell
cd backend
mvn clean test

cd ..\frontend
npm ci
npm run build
```

## Importante

El patch no incluye llaves ni IDs reales y no modifica `render.yaml`.
No habilitar el primer cobro hasta configurar en Render las variables de Culqi y registrar el webhook protegido.
El SQL de suscripciones recurrentes debe ejecutarse en Supabase antes de probar el pago si aún no fue aplicado.
