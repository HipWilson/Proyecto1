# FindMySpot UVG

Una app Android para encontrar parqueo en la UVG, desarrollada con Jetpack Compose y Firebase.

## ¿Qué es esto?

FindMySpot es una aplicación que creé para resolver un problema real: encontrar parqueo en la universidad. La app te deja ver en tiempo real cuántos espacios hay disponibles en cada sótano, hacer "apartados simbólicos" de 5 minutos para que no pierdas tu lugar mientras llegas, y llevar un historial de tus parqueos.

## Demo

Pueden ver la app funcionando aquí: [https://youtube.com/shorts/BNY9Sn2WGro](https://youtu.be/dtcLUE2l994) 

## Características principales

- **Ver disponibilidad en tiempo real**: La app se actualiza automáticamente mostrando cuántos espacios hay libres en cada sótano
- **Sistema de apartados**: Puedes apartar un espacio por 5 minutos (cuenta regresiva incluida)
- **Confirmación de llegada**: Cuando llegas, confirmas tu llegada y el espacio queda marcado como ocupado
- **Historial**: Guarda todos tus parqueos anteriores con fecha, duración y si confirmaste o no
- **Tema oscuro/claro**: Puedes cambiar entre tema claro y oscuro
- **Multiidioma**: Español e inglés 

## Tecnologías que usé

- **Kotlin**: Todo el código está en Kotlin
- **Jetpack Compose**: Para la UI 
- **Firebase Auth**: Para login y registro de usuarios
- **Firebase Firestore**: Base de datos en la nube para parqueos y reservaciones
- **MVVM**: Arquitectura con ViewModels y StateFlow
- **Navigation Compose**: Para navegar entre pantallas
- **Coroutines & Flow**: Para operaciones asíncronas

## Estructura del proyecto

Traté de mantener todo organizado en capas:

```
app/src/main/java/com/example/proyecto1/
├── domain/                    # Lógica de negocio
│   ├── model/                # Modelos (User, ParkingSpot, Reservation, etc.)
│   ├── repository/           # Repositorios de Firebase
│   └── usecase/              # Casos de uso
├── presentation/             # UI
│   ├── login/               # Pantalla de login
│   ├── register/            # Registro de usuarios
│   ├── forgotpassword/      # Recuperar contraseña
│   ├── parkinglist/         # Lista de sótanos (pantalla principal)
│   ├── reservation/         # Pantalla de apartado con timer
│   ├── profile/             # Perfil e historial
│   ├── settings/            # Configuraciones
│   ├── navigation/          # NavGraph y rutas
│   └── common/              # Componentes reutilizables
└── ui/theme/                # Colores, tema, idiomas
```

## Cómo funciona

### 1. Autenticación
Usé Firebase Authentication para el login. Los usuarios se registran con email y contraseña. También implementé recuperación de contraseña por correo.

### 2. Ver sótanos disponibles
La pantalla principal muestra todos los sótanos con:
- Número de sótano
- Espacios disponibles (ej: "5/20 espacios")
- Estado con color:
  - 🟢 Verde = Disponible (más del 20% libre)
  - 🟡 Amarillo = Pocos espacios (20% o menos)
  - 🔴 Rojo = Lleno

Los datos se actualizan en tiempo real usando Firestore listeners.

### 3. Sistema de apartados
Cuando apartas un espacio:
1. Se crea una reservación en Firestore
2. El contador de espacios ocupados del sótano aumenta en 1
3. Se muestra un timer de 5 minutos
4. Solo puedes tener 1 apartado activo a la vez
5. Si no confirmas tu llegada antes de que expire, pierdes el apartado

### 4. Confirmar llegada
Al llegar al sótano, confirmas tu llegada:
1. El apartado se marca como confirmado
2. La pantalla cambia a "Estacionado"
3. Cuando te vayas, marcas el espacio como desocupado
4. Se crea un registro en tu historial

### 5. Historial
Todo queda guardado en Firestore:
- Fecha y hora
- Sótano usado
- Si confirmaste o no
- Duración total

## Decisiones de diseño

### ¿Por qué Firebase?
Originalmente iba a simular todo local, pero decidí usar Firebase para que fuera más realista. Así varios usuarios pueden usar la app al mismo tiempo y ver los cambios en tiempo real.

### El problema del apartado único
Decidí que solo puedas tener 1 apartado activo porque:
- Evita que alguien aparte múltiples espacios
- Es más justo para todos
- En la vida real tampoco puedes estar en dos lugares a la vez 

### Flows en tiempo real
Usé Firestore listeners para que los cambios se reflejen automáticamente. Si alguien aparta un espacio, todos lo ven de inmediato sin tener que hacer refresh.

### Validaciones
Implementé validaciones tanto en el frontend como en el backend:
- Emails válidos
- Contraseñas de al menos 6 caracteres
- No puedes apartar si ya tienes un apartado activo
- Los timers se validan en el cliente

## Problemas que tuve y cómo los resolví

### 1. Sincronización de datos
**Problema**: Al principio los contadores de espacios se desincronizaban.

**Solución**: Usé transacciones de Firestore y FieldValue.increment() para operaciones atómicas.

### 2. Timer que seguía corriendo
**Problema**: El timer de la reservación seguía corriendo incluso después de salir de la pantalla.

**Solución**: Usé LaunchedEffect con las condiciones correctas para cancelar el timer cuando cambias de estado.

### 3. Estado de reservación no se actualizaba
**Problema**: Cuando volvías a la lista de sótanos, no detectaba que tenías una reservación activa.

**Solución**: Agregué `checkActiveReservation()` en el init del ViewModel de la lista.

### 4. Timeouts de Firebase
**Problema**: A veces Firebase tardaba mucho y la app se quedaba cargando.

**Solución**: Implementé timeouts de 30 segundos con mensajes de error apropiados.

## Cosas que me hubiera gustado agregar

- [ ] Mapas con Google Maps API para mostrar ubicación exacta de cada sótano
- [ ] Notificaciones push cuando tu apartado está por expirar
- [ ] Sistema de favoritos para sótanos
- [ ] Estadísticas (cuál sótano usas más, a qué horas, etc.)
- [ ] Modo offline más robusto

## Requisitos para correr el proyecto

- Android Studio Hedgehog o superior
- JDK 11+
- Cuenta de Firebase (ya está configurada en el proyecto)
- Emulador o dispositivo con API 24+

## Cómo correr el proyecto

1. Clonar el repo:
```bash
git clone [tu-repo]
cd Proyecto1
```

2. Abrir en Android Studio

3. El proyecto ya tiene el archivo `google-services.json` configurado, así que debería funcionar de inmediato

4. Sync Gradle y correr en un emulador o dispositivo

5. Para probar con múltiples usuarios, puedes:
   - Crear varias cuentas
   - O usar dos emuladores a la vez

## Estructura de Firestore

### Colección `parkingSpots`
```
{
  basementNumber: 1,
  totalSpaces: 20,
  occupiedSpaces: 5,
  latitude: 14.6041,
  longitude: -90.4891
}
```

### Colección `reservations`
```
{
  userId: "user123",
  parkingSpotId: "spot1",
  basementNumber: 1,
  startTime: 1234567890,
  expirationTime: 1234568190,
  isActive: true,
  isConfirmed: false,
  isCompleted: false
}
```

### Colección `reservationHistory`
```
{
  userId: "user123",
  basementNumber: 1,
  date: 1234567890,
  wasConfirmed: true,
  duration: 45
}
```

## Aprendizajes

Este proyecto me ayudó a aprender:
- Cómo estructurar una app Android moderna
- Integración real con Firebase 
- Manejo de estados complejos con Compose
- Flujos de tiempo real con Firestore
- Arquitectura MVVM en la práctica
- Navegación con argumentos en Compose
- Manejo de errores y casos edge
- Y sobre todo: **la importancia de probar con usuarios reales** 

## Créditos

Proyecto desarrollado por Wilson Peña y Dally Ramirez para el curso de Plataformas Móviles - UVG

