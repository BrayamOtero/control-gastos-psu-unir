# Control de gastos

Aplicación para registrar, consultar, editar y eliminar gastos, con filtros,
paginación y resumen mensual por categorías.

## Contenido del proyecto

- **[backend/](./backend/):** API REST en Java 25 y Spring Boot, compilada con
  Gradle Wrapper. `src/main/java/` contiene controladores, servicios,
  repositorios, entidades y DTO; `src/main/resources/` contiene la configuración
  y `src/test/` las pruebas. Utiliza H2 persistente en archivo. El contrato de
  los endpoints está en [API.md](./backend/API.md).
- **[frontend/](./frontend/):** interfaz en React y Vite. `src/pages/` contiene
  las pantallas, `src/components/` los componentes, `src/hooks/` la lógica de
  interacción y `src/services/` las solicitudes HTTP y sus utilidades.
  El Dockerfile compila la interfaz y nginx sirve los archivos resultantes.
- **[docker-compose.yml](./docker-compose.yml):** construye y ejecuta ambos
  servicios, publica la interfaz en el puerto 3000 y conserva la base de datos
  en un volumen.

## Despliegue local con Docker

1. Instala Docker Desktop y ábrelo. Espera a que su motor esté funcionando.
   No necesitas instalar Java, Gradle ni Node.js en tu equipo.

2. Abre una terminal en la raíz de este proyecto y comprueba Docker:

   ```sh
   docker info
   ```

   La sección `Server` debe aparecer sin errores. Si indica que Docker Desktop
   no puede iniciar, resuelve su arranque antes de continuar.

3. Para el despliegue local, deja `VITE_API_URL` sin definir en los archivos
   `.env*` del frontend. Así el navegador consulta `/api` en el mismo origen
   y nginx reenvía las solicitudes a `backend:8080`. Si defines una URL
   externa, se incorpora al compilar y requiere que esa API sea accesible
   desde el navegador.

4. Construye y arranca los servicios en segundo plano:

   ```sh
   docker compose up -d --build
   ```

   La primera construcción necesita Internet y puede tardar varios minutos.
   Espera a que Spring Boot termine de arrancar; el orden de inicio de Compose
   no garantiza que la API ya esté lista.

5. Comprueba el estado y abre **http://localhost:3000**:

   ```sh
   docker compose ps
   docker compose logs --tail=100 backend
   curl --fail "http://localhost:3000/api/gastos?page=0&size=10"
   ```

   La última petición debe devolver JSON. El puerto 8080 del backend no se
   publica: todas las consultas desde el navegador pasan por nginx.

## Comandos útiles

```sh
docker compose logs -f                 # Ver logs; Ctrl+C deja de seguirlos
docker compose down                    # Detener sin borrar los datos
docker compose up -d                    # Arrancar las imágenes existentes
docker compose up -d --build            # Reconstruir después de cambiar código
```

El volumen `gastos-data` guarda H2 en `/app/data` dentro del backend y se
conserva al detener o recrear los contenedores. Es una base independiente de
`backend/data/`: los datos locales existentes no se importan automáticamente.
**No uses `docker compose down -v` si quieres conservar los gastos: elimina
el volumen y su base de datos.**

## Problemas frecuentes

- **Docker no conecta o no inicia:** abre Docker Desktop y revisa su apartado
  de diagnóstico (*Troubleshoot*). Comprueba de nuevo con `docker info`.
- **Solicitudes a `localhost:8080`:** revisa `VITE_API_URL`, reconstruye el
  frontend con `docker compose up -d --build frontend` y recarga el navegador.
  En este despliegue las solicitudes deben ir a `localhost:3000/api/gastos`.
- **502 Bad Gateway:** revisa los logs del backend y espera a que arranque.
- **Puerto 3000 ocupado:** cambia `"3000:80"` en Compose por otro puerto,
  por ejemplo `"3001:80"`, y vuelve a ejecutar `docker compose up -d`.

Este despliegue es para uso local o demostraciones. La aplicación no incluye
autenticación; no la expongas a Internet sin controles de acceso y HTTPS.
