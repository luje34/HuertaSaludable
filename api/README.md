# API Huerta Saludable

API REST de gestión de usuarios con Java 17, Spring Boot 3, Spring Data JPA y MySQL. Este proyecto es independiente de la aplicación Android y parte del esqueleto Maven entregado en `APIHuertaSaludable`.

## Requisitos

- JDK 17 o superior
- MySQL en ejecución
- Maven, o el wrapper incluido (`mvnw.cmd` en Windows)

## Configuración

Crea la base de datos en MySQL:

```sql
CREATE DATABASE huerta_saludable;
```

La API usa por defecto `localhost:3306`, la base `huerta_saludable` y el usuario `root`. Configura credenciales u otro servidor mediante las variables de entorno `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. `SERVER_PORT` permite cambiar el puerto (por defecto `8080`). Hibernate actualiza el esquema automáticamente para facilitar el desarrollo local.

Desde esta carpeta ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

## Endpoints

La base de la API es `/api/usuarios`:

| Método | Ruta | Acción |
| --- | --- | --- |
| `GET` | `/api/usuarios` | Lista usuarios |
| `GET` | `/api/usuarios/{id}` | Consulta un usuario |
| `POST` | `/api/usuarios` | Crea un usuario |
| `PUT` | `/api/usuarios/{id}` | Actualiza sus datos |
| `DELETE` | `/api/usuarios/{id}` | Elimina un usuario |

Ejemplo de creación:

```json
{
  "nombre": "Camila Muñoz",
  "email": "camila@huerta.cl",
  "password": "una-clave-segura",
  "rol": "COLABORADOR",
  "tallerGrupo": "Taller 5° Básico A",
  "activo": true
}
```

Los roles disponibles son `ADMIN` y `COLABORADOR`. En una actualización, `password` puede omitirse o enviarse vacío para conservar la clave actual. Las contraseñas se almacenan con BCrypt y nunca aparecen en las respuestas. Los correos se normalizan a minúsculas y no pueden repetirse. Errores de validación devuelven `400`, duplicados `409` y usuarios inexistentes `404`.

La API todavía no incluye autenticación ni autorización; antes de exponerla fuera de un entorno local, protege las operaciones y limita quién puede asignar roles.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria; no requieren una instancia MySQL.
