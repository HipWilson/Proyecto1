# FindMySpot UVG

Una aplicación Android nativa para la gestión de espacios de estacionamiento en la Universidad Valle de Guatemala, desarrollada con Jetpack Compose y arquitectura moderna.

## Descripción General

FindMySpot es una solución integral que permite a los estudiantes de UVG consultar disponibilidad de estacionamiento en tiempo real, hacer reservas simbólicas, confirmar llegadas y visualizar historial de reservas. La aplicación utiliza simulación de datos en tiempo real con actualizaciones cada 5 segundos.

## Arquitectura y Decisiones de Modelado

### Estructura de Capas

La aplicación sigue una arquitectura clean de tres capas:

**Capa de Presentación (presentation/):** Contiene screens, ViewModels y componentes Compose reutilizables. Organizada por features con sus respectivos subdirectorios (login, register, parkinglist, etc.).

**Capa de Dominio (domain/):** Define modelos de datos y casos de uso sin dependencias de Android. Contiene la lógica de negocio pura e independiente de frameworks.

**Utilidades (ui/theme, common):** Componentes visuales compartidos, temas de color y tipografía centralizados en una única fuente de verdad.

### Modelos de Dominio

Los modelos principales reflejan entidades clave del negocio:

**ParkingSpot:** Representa un sótano con capacidad total, espacios disponibles y coordenadas GPS. Incluye propiedades calculadas `status` (AVAILABLE, FEW_SPOTS, FULL) y `occupancyPercentage` que se derivan del estado actual, evitando datos redundantes.

**Reservation:** Encapsula una reserva activa con tiempos de inicio y expiración. Las propiedades `remainingMinutes` y `isExpired` se calculan dinámicamente contra el reloj del sistema, permitiendo que la UI refleje cambios sin actualizaciones constantes de la base de datos.

**ReservationHistory:** Registra apartados pasados con confirmación para análisis de patrones de uso.

**User:** Modelo simple con identificación y contacto, preparado para extensión futura con roles o preferencias.

Esta separación permite que los ViewModels mantengan lógica agnóstica del almacenamiento, facilitando testing y cambios futuros en la persistencia.

## Estrategia de Cache y Paginación

### Flujo de Datos en Tiempo Real

El caso de uso `GetParkingSpotsUseCase` implementa un flujo continuo mediante `Flow<List<ParkingSpot>>`:

```
emit(spots) → delay(5s) → emit(nuevos_spots) → ciclo continuo
```

Esta estrategia proporciona actualizaciones pseudo-reales sin polling tradicional. Los disponibles se recalculan aleatoriamente cada 5 segundos en la capa de dominio, simulando cambios dinámicos de ocupación.

### Ausencia de Caché Persistente

La aplicación **no implementa caché en disco** por diseño. Las razones incluyen:

- **Consistencia:** Los datos de estacionamiento cambian constantemente; un caché envejecido sería más dañino que útil.
- **Simplicidad:** Sin base de datos local, se eliminan dependencias de Room o SQLite y complejidad de sincronización.
- **UX:** El usuario espera información actual; mostrar datos cacheados de minutos atrás contradice el propósito de la app.

En cambio, se mantiene un caché **en memoria** a través del estado de Compose, optimizado por la reactividad del framework.

### Estrategia de Paginación

La paginación no se implementa actualmente porque:

- **Volumen de datos:** Se esperan máximo 4-6 sótanos en la UVG; listar todos simultáneamente es viable.
- **Experiencia:** Una única pantalla con scroll es más intuitiva que paginación por sótanos.

Cuando el catálogo escale, se introducirá paginación lazy con `LazyColumn` filtrando por estado (disponibles primero, luego completos).

## Manejo de Estado sin ViewModel Directo

### Arquitectura de Estado Reactivo

Aunque la aplicación usa ViewModels estándar de Android, sigue principios de programación reactiva que permiten descopplement:

**State Holders:** Cada pantalla define un `State` data class inmutable (LoginState, ReservationState, etc.) que centraliza la UI como función del estado.

**Flow/StateFlow:** Los ViewModels exponen `StateFlow` que observamos con `collectAsState()`, permitiendo que Compose reactive sea la fuente de verdad.

### Flujo de Cambios de Estado

1. Usuario dispara acción (click en botón)
2. ViewModel invoca caso de uso
3. Caso de uso actualiza `StateFlow`
4. Compose recompone solo widgets afectados

Ejemplo con Reservación:

```kotlin
viewModel.confirmArrival()
  → updateState { it.copy(isLoading = true) }
  → invocar ConfirmArrivalUseCase
  → setState { it.copy(isConfirmed = true, isLoading = false) }
  → LaunchedEffect ejecuta onNavigateBack tras 2s
```

### Independencia de ViewModel

El modelo permite testear lógica de estado sin ViewModels:

- Los casos de uso son funciones puras suspendibles
- State data classes son testables directamente
- Compose preview funciona con cualquier estado pasado como parámetro

## Consideraciones Offline

### Estrategia de Resiliencia

La aplicación enfrenta desconexión con estas tácticas:

**Simulación Local:** Al usar casos de uso que generan datos localmente (sin HTTP real), funciona íntegramente sin red. Esto es apropiado para una demostración, pero requeriría ajuste en producción.

**Estado Persistente en Memoria:** Las últimas reservas se mantienen en `ReservationState` incluso sin conectividad, permitiendo al usuario confirmar llegada o cancelar sin conexión.

**Pantalla de Error Anticipada:** Existe `Screen.NoConnection` para mostrar cuando la conectividad falla (en producción, con llamadas HTTP reales).

### Trade-offs de Offline

**Pro:** Simplicidad actual; los datos simulados nunca fallan.

**Contra:** No hay sincronización real con servidor. Al integrar un backend:

- Implementar `WorkManager` para sync de reservas en background
- Caché híbrido: datos críticos (historial) en SQLite, datos volátiles (disponibilidad) solo en red
- Estrategia de retry exponencial con backoff

**Recomendación:** Prioritizar sync de confirmaciones de llegada, que generan registros, sobre disponibilidad que puede reintentarse al reconectar.

## Trade-offs Arquitectónicos

### 1. Reactividad vs. Complejidad

**Decisión:** Flows continuos para disponibilidad de estacionamiento.

**Pro:** Usuarios ven cambios instantáneamente; experiencia fluida sin botones "actualizar".

**Contra:** El flujo consume recursos en background. `delay(5s)` es configurable pero siempre activo mientras la pantalla es visible.

**Mitigation:** `LaunchedEffect` y cancelación automática al salir de pantalla.

---

### 2. Datos Simulados vs. Servidor Real

**Decisión:** Lógica generativa en casos de uso sin HTTP.

**Pro:** Offline-first; testing sin mock servers; prototipado rápido.

**Contra:** No refleja latencia de red real; no hay errores auténticos de servidor.

**Camino a Producción:** Reemplazar `GetParkingSpotsUseCase` con llamada HTTP + retry logic.

---

### 3. Sin Persistencia Local

**Decisión:** Memoria volátil; no usar Room/SQLite.

**Pro:** Cero overhead; no sincronización de esquemas.

**Contra:** Perder datos al cerrar app; sin historial durabilidad completa.

**Futuro:** SQLite para historial; caché temporal para últimas búsquedas.

---

### 4. Kotlin Coroutines sobre Rx

**Decisión:** `suspend` y `Flow` vs. RxJava/RxKotlin.

**Pro:** Nativo en Kotlin; curva de aprendizaje menor; debugging más simple.

**Contra:** Menos operadores de transformación; comunidad RxJava más grande.

**Justificación:** El equipo de Android de Google favorece coroutines; ideal para nuevos proyectos.

---

### 5. Validación en UseCase vs. Presentation

**Decisión:** Validación dual: UI en ViewModel (feedback rápido) + UseCase (seguridad).

**Pro:** Feedback instantáneo al usuario; lógica de negocio protegida.

**Contra:** Duplicación de reglas de validación.

**Solución:** Extraer validadores a clase compartida `ValidationRules` reutilizable.

---

## Componentes Reutilizables

`CommonComponents.kt` centraliza patrones visuales:

- **CustomTextField:** Campo con validación, visibilidad de contraseña, error inline
- **CustomButton:** Estado de carga; soporte para disabled
- **CustomTopAppBar:** Barra con navegación e icono
- **LoadingScreen, ErrorMessage:** Estados visuales estándar

Esto asegura consistencia sin duplicar código Compose.

## Manejo de Errores

Cada UseCase devuelve `Result<T>` que encapsula éxito o excepción:

```kotlin
suspend fun invoke(...): Result<User>
  → ViewModel observa y actualiza state.errorMessage
  → UI muestra ErrorMessage con opción de reintento
```

No se usa `try-catch` explosivo; los errores fluyen naturalmente a través del estado.

## Consideraciones de Rendimiento

- **Recomposes Mínimas:** `StateFlow` + `collectAsState()` asegura que solo widgets que consumen cambios específicos se redibuja.
- **Lazy Loading:** `LazyColumn` en historial para volúmenes grandes (futuro).
- **Actualización Selectiva:** `occupancyPercentage` calculada bajo demanda, no guardada redundantemente.

## Próximas Mejoras

1. Integración HTTP real con Retrofit + OkHttp
2. Persistencia con Room para historial
3. Mapas con Google Maps API
4. Notificaciones push con Firebase Cloud Messaging
5. Autenticación OAuth 2.0
6. Análisis con Firebase Analytics
7. Testing unitario exhaustivo con Mockk
8. CI/CD con GitHub Actions

## Requisitos Técnicos

- **Android:** API 24+
- **Kotlin:** 2.0.21
- **Compose:** 2024.09.00
- **JDK:** 11+

## Dependencias Principales

- `androidx.compose.*`: UI moderna
- `androidx.navigation.compose`: Navegación declarativa
- `androidx.lifecycle.viewmodel.compose`: Estado reactivo
- `androidx.material.icons.extended`: Iconografía Material Design 3

## Video: 
https://youtube.com/shorts/BNY9Sn2WGro
