# PeruTalent - versión híbrida local + Render/Netlify

Esta versión está preparada para dos usos:

1. Probar localmente con Docker + Angular.
2. Desplegar en Render + Netlify usando variables de entorno.

No incluye carpetas generadas ni archivos pesados como `node_modules`, `dist`, `.angular`, `target`, `uploads` o `.env`.

## Probar localmente

### Backend con Docker

Desde la carpeta `backend`:

```powershell
docker build -t perutalent-api .
docker rm -f perutalent-api

docker run --name perutalent-api `
  --dns=8.8.8.8 `
  --dns=1.1.1.1 `
  -p 8081:8081 `
  -e DATABASE_URL="jdbc:postgresql://TU_HOST_SUPABASE:6543/postgres?sslmode=require&prepareThreshold=0" `
  -e DATABASE_USERNAME="TU_USUARIO_SUPABASE" `
  -e DATABASE_PASSWORD="TU_PASSWORD_SUPABASE" `
  -e JWT_SECRET="TU_JWT_SECRET" `
  -e JWT_EXPIRATION="86400000" `
  -e CORS_ALLOWED_ORIGINS="http://localhost:4200" `
  -e SENDGRID_API_KEY="TU_API_KEY_SENDGRID" `
  -e SENDGRID_FROM_EMAIL="TU_CORREO_VERIFICADO_SENDGRID" `
  -e SENDGRID_FROM_NAME="PeruTalent" `
  -e UPLOAD_DIR="/tmp/perutalent/uploads/cvs/postulaciones" `
  perutalent-api
```

### Frontend local

Desde la carpeta `frontend`:

```powershell
npm install
ng serve
```

El archivo `frontend/src/enviroments/enviroment.ts` apunta a:

```text
http://localhost:8081/
```

## Backend en Render

Render puede usar el archivo `render.yaml` incluido en la raíz del proyecto.

Configuración esperada:

- Root Directory: `backend`
- Build Command: `./mvnw clean package -DskipTests`
- Start Command: `java -jar target/Up-Work-Perusalen-0.0.1-SNAPSHOT.jar`
- Health Check Path: `/actuator/health`

Variables necesarias en Render:

```text
DATABASE_URL=jdbc:postgresql://...supabase...?sslmode=require&prepareThreshold=0
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
JWT_SECRET=...
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://TU-SITIO.netlify.app
SENDGRID_API_KEY=...
SENDGRID_FROM_EMAIL=correo_verificado_en_sendgrid
SENDGRID_FROM_NAME=PeruTalent
UPLOAD_DIR=/tmp/perutalent/uploads/cvs/postulaciones
```

Nota sobre CVs: para demo, Render puede usar `UPLOAD_DIR`. En producción real conviene migrar los CVs a S3 o Supabase Storage porque el almacenamiento del servidor puede ser efímero.

## Frontend en Netlify

Este repo incluye `netlify.toml` en la raíz y también en `frontend/`.

Si Netlify toma el repo desde la raíz, puede usar:

- Base directory: `frontend`
- Build command: `npm install && npm run build -- --configuration production`
- Publish directory: `dist/perutalent-frontend`

El archivo `frontend/public/_redirects` también está incluido para que Angular funcione con rutas directas.

## URL del backend para producción

Antes de desplegar, revisar:

```text
frontend/src/enviroments/enviroment.prod.ts
```

Ese archivo debe apuntar al backend real de Render:

```ts
export const environment = {
  production: true,
  apiUrl: 'https://TU-BACKEND.onrender.com/'
};
```

El archivo `enviroment.ts` puede quedarse apuntando a `localhost` porque solo se usa en desarrollo local. Angular reemplaza ese archivo por `enviroment.prod.ts` durante el build de producción.
