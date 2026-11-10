# Cliente Flutter CNS

Base de la aplicacion Flutter para el Sistema de Gestion de Citas Medicas del Hospital Obrero.

## Requisitos

- Flutter estable con Dart 3.3 o superior.
- Backend Spring Boot disponible si se ejecuta el cliente REST.

## Ejecucion

Desde esta carpeta:

```bash
flutter pub get
flutter run --dart-define=API_BASE_URL=http://localhost:8080/api/v1
```

`API_BASE_URL` es opcional; por defecto usa `http://localhost:8080/api/v1`.

## Pruebas

```bash
flutter test
```

La prueba usa `FakeAuthApi`, por lo que no necesita backend. La aplicacion real usa `RestAuthApi`.

## Capas

- `lib/models`: DTOs con `fromJson` y `toJson`.
- `lib/services`: cliente HTTP, errores y APIs por dominio.
- `lib/controllers`: estado de pantalla y validacion de formato mediante `ChangeNotifier`.
- `lib/views`: widgets sin llamadas HTTP directas.

La eleccion de `ChangeNotifier` mantiene la base sin introducir una libreria de estado antes de que el equipo cierre esa decision. Las reglas de negocio y autorizacion permanecen en el backend.

## Inicio de sesion y menu

`LoginController` mantiene la sesion en memoria durante la ejecucion. Cuando `AuthApi` autentica correctamente, `LoginPage` abre `MenuPage`. El menu muestra unicamente las opciones cuyos permisos aparecen en `SesionDto.permisos`; la autorizacion definitiva permanece en el backend.

El boton de cerrar sesion elimina el flujo actual y vuelve a la pantalla de login. No se persiste la sesion entre arranques, porque esa decision corresponde a S3-08/PV-11.

El contrato REST de `auth/login` sigue la propuesta documentada en `03_des/02-arquitecturaGeneral/06_contratos_rest_api.md`; los nombres definitivos deben actualizarse cuando el backend publique sus DTOs.