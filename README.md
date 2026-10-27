# Instituto – Backend (Spring Boot + PostgreSQL)

## Cómo correrlo
1. Crear la base en PostgreSQL: `CREATE DATABASE instituto;`
2. Editar usuario/contraseña en `src/main/resources/application.properties`.
3. En NetBeans: File → Open Project → esta carpeta → Run (o `mvn spring-boot:run`).
4. La API queda en `http://localhost:8080`. Las tablas se crean solas (`ddl-auto=update`).

## Seguridad (login y roles)
La API usa **Spring Security con HTTP Basic** (sin sesiones). Todas las rutas `/api/**` exigen
el header `Authorization: Basic base64(usuario:contraseña)`. Las contraseñas se guardan con BCrypt.

- Al arrancar por primera vez se crea el usuario **`admin`** (contraseña en `app.admin.password`,
  por defecto `admin1234`). **Cambiarla.**
- Flujo del frontend: el usuario escribe usuario y contraseña → el frontend arma el header Basic y
  llama a `GET /api/auth/me` → si responde 200 devuelve `{id, username, nombre, rol, activo}`
  (se usa para mostrar/ocultar opciones según el rol); si responde 401 las credenciales son incorrectas.
  Después envía ese mismo header en cada pedido.
- Sin credenciales o con credenciales incorrectas: **401**. Con rol sin permiso: **403**.

### Permisos por rol
| Recurso | ADMINISTRADOR | ADMINISTRATIVO | DOCENTE |
|---|---|---|---|
| Usuarios (`/api/usuarios`) | Todo | – | – |
| Alumnos: consultar | Sí | Sí | Sí |
| Alumnos: crear / modificar / dar de baja | Sí | Sí | – |
| Cuotas y situación administrativa de un alumno | Sí | Sí | – |
| Inscripciones: consultar | Sí | Sí | Sí |
| Inscripciones: registrar | Sí | Sí | – |
| Matrículas, cuotas y pagos (consultar y registrar) | Sí | Sí | – |

## Arquitectura
Controller → Service → Repository → PostgreSQL
- **controller**: recibe HTTP, valida el JSON (`@Valid`) y devuelve la respuesta.
- **service**: reglas de negocio y transacciones.
- **repository**: acceso a datos (Spring Data JPA).
- **model**: entidades JPA (tablas).
- **exception**: errores uniformes (400, 404, 409).

## Endpoints implementados
| Método | URL | Descripción | Códigos |
|---|---|---|---|
| GET | /api/alumnos?q= | Lista alumnos activos; `q` busca por apellido, nombre o DNI | 200 |
| GET | /api/alumnos/{id} | Obtiene un alumno | 200, 404 |
| POST | /api/alumnos | Crea un alumno | 201, 400, 409 (DNI repetido) |
| PUT | /api/alumnos/{id} | Modifica un alumno | 200, 400, 404, 409 |
| DELETE | /api/alumnos/{id} | Baja lógica (estado INACTIVO) | 204, 404 |
| POST | /api/inscripciones | Inscribe `{alumnoId, cursoId}` | 201, 400, 404, 409 |
| GET | /api/inscripciones/curso/{cursoId} | Inscripciones de un curso | 200 |
| POST | /api/matriculas | Registra matrícula `{alumnoId, cicloLectivoId, importe}` (queda PENDIENTE) | 201, 400, 404, 409 |
| GET | /api/matriculas/{id} | Consulta una matrícula | 200, 404 |
| GET | /api/matriculas?cicloId= | Matrículas de un ciclo lectivo | 200, 404 |
| PATCH | /api/matriculas/{id}/estado | Cambia estado `{estado: ACTIVA\|PENDIENTE\|CANCELADA}` | 200, 400, 404, 409 |
| POST | /api/matriculas/{id}/cuotas | Genera cuotas mensuales `{cantidad, importeCuota, primerVencimiento}` | 201, 400, 404, 409 |
| GET | /api/matriculas/{id}/cuotas | Cuotas de una matrícula (con total pagado y saldo) | 200, 404 |
| GET | /api/cuotas/{id} | Consulta una cuota | 200, 404 |
| GET | /api/cuotas/vencidas | Todas las cuotas vencidas | 200 |
| GET | /api/alumnos/{id}/cuotas | Cuotas de un alumno | 200, 404 |
| GET | /api/alumnos/{id}/situacion-administrativa | Matrículas, cuotas, cantidad pendientes/vencidas, importe adeudado y total abonado | 200, 404 |
| POST | /api/pagos | Registra pago `{cuotaId, importe, medioPago}`; el usuario se toma de quien está logueado | 201, 400, 401, 403, 404, 409 |
| GET | /api/auth/me | Usuario autenticado y su rol (sirve como login) | 200, 401 |
| GET | /api/usuarios | Lista usuarios (solo ADMINISTRADOR) | 200, 401, 403 |
| GET | /api/usuarios/{id} | Consulta un usuario | 200, 404 |
| POST | /api/usuarios | Crea usuario `{username, password, nombre, rol}` | 201, 400, 409 |
| PUT | /api/usuarios/{id} | Modifica `{nombre, rol, activo, password?}` | 200, 400, 404, 409 |
| DELETE | /api/usuarios/{id} | Baja lógica (activo = false) | 204, 404, 409 |
| GET | /api/pagos?alumnoId=&cuotaId=&desde=&hasta= | Consulta pagos (filtros opcionales; fechas `AAAA-MM-DD`) | 200 |

Roles de usuario: `ADMINISTRADOR`, `ADMINISTRATIVO`, `DOCENTE`.
Medios de pago: `EFECTIVO`, `TRANSFERENCIA`, `TARJETA`, `OTRO`.
Estados de cuota: `PENDIENTE`, `PAGADA`, `VENCIDA`, `ANULADA`.

## Reglas de negocio ya aplicadas
1. No puede haber dos alumnos con el mismo DNI.
2. El alumno no se elimina: baja lógica.
3. Solo alumnos activos pueden inscribirse.
4. El alumno y el curso deben existir.
5. Un alumno no puede tener dos inscripciones en el mismo ciclo lectivo.
6. Un curso no puede superar su capacidad máxima.
7. Una matrícula pertenece a un único alumno y ciclo lectivo; solo alumnos activos pueden matricularse.
8. Una matrícula cancelada no puede reactivarse; al cancelarla se anulan las cuotas sin pagos.
9. Las cuotas se generan una sola vez por matrícula (hasta 12, mensuales desde el primer vencimiento).
10. Una cuota PENDIENTE con vencimiento anterior a hoy pasa a VENCIDA (se actualiza en cada consulta o pago).
11. No se puede pagar más que el saldo de la cuota; se admiten pagos a cuenta.
12. La cuota pasa a PAGADA cuando los pagos cubren todo su importe (y por lo tanto tiene al menos un pago).
13. No se registran pagos en cuotas PAGADAS, ANULADAS ni de matrículas canceladas.
14. El nombre de usuario es único y las contraseñas se guardan cifradas (BCrypt).
15. Los usuarios no se eliminan (baja lógica) y siempre debe quedar al menos un administrador activo.
16. Cada pago registra automáticamente al usuario autenticado que lo cargó.

## Prueba rápida
En Postman: pestaña **Authorization** → tipo **Basic Auth** → usuario `admin` y su contraseña.
```
GET /api/auth/me

POST /api/usuarios
{ "username":"maria", "password":"clave1234", "nombre":"María Gómez", "rol":"ADMINISTRATIVO" }

POST /api/alumnos
{ "dni":"40123456","apellido":"Pérez","nombre":"Juan","fechaNacimiento":"2008-05-14","email":"juan@mail.com" }

POST /api/inscripciones
{ "alumnoId":1, "cursoId":1 }

POST /api/matriculas
{ "alumnoId":1, "cicloLectivoId":1, "importe":30000 }

POST /api/matriculas/1/cuotas
{ "cantidad":10, "importeCuota":50000, "primerVencimiento":"2027-03-10" }

POST /api/pagos
{ "cuotaId":1, "importe":20000, "medioPago":"EFECTIVO" }

GET /api/alumnos/1/situacion-administrativa
```

(`data.sql` crea los ciclos 2026/2027 y dos cursos de prueba.)
