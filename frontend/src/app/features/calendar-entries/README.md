# Calendar Entries Feature

## Descripción

Vista de calendario para el registro diario de check-ins y check-outs del PMS Hotel SA.

## Estructura de Archivos

```
/features/calendar-entries/
├── calendar-entries-page.component.ts       # Componente principal
├── calendar-entries-page.component.html     # Template del calendario
├── calendar-entries-page.component.css      # Estilos personalizados
├── calendar-entries-page.component.spec.ts  # Tests unitarios
├── calendar-entries.service.ts              # Servicio HTTP
├── calendar-entries.service.spec.ts         # Tests del servicio
├── calendar-entries.routes.ts               # Rutas del módulo
└── models/
    └── calendar-entry.interface.ts          # Interface de datos
```

## Ruta de Acceso

```
/calendar/entries
```

## API Backend

### Endpoint

```
GET /api/calendar/entries?start=YYYY-MM-DD&end=YYYY-MM-DD
```

### Respuesta

```json
[
  {
    "bookingId": number,
    "guestName": string,
    "roomNumber": string,
    "checkInDate": "YYYY-MM-DD",
    "checkOutDate": "YYYY-MM-DD"
  }
]
```

## Funcionalidades

### Visualización
- 🟢 **Check-in**: Eventos de color verde con ícono ✓
- 🔴 **Check-out**: Eventos de color rojo con ícono ✗

### Navegación
- Navegar entre meses (anterior/siguiente)
- Botón "Hoy" para volver al mes actual
- Botón de refresco manual

### Interacción
- Click en evento → Redirige a `/bookings/:id` para gestión de la reserva
- Hover sobre evento → Muestra tooltip con información del huésped y habitación

## Componentes Utilizados

- **angular-calendar**: Librería oficial para calendarios en Angular
- **date-fns**: Utilidades de manipulación de fechas

## Formato de Eventos

### Check-in
```
✓ John Doe - 203
```

### Check-out
```
✗ John Doe - 203
```

## Roles con Acceso

- ADMIN
- EMPLOYEE
- USER

## Dependencias

```json
{
  "angular-calendar": "^0.32.0",
  "angular-draggable-droppable": "^9.0.0",
  "angular-resizable-element": "^9.0.0",
  "date-fns": "^4.0.0"
}
```

## Uso

El componente se carga automáticamente cuando el usuario navega a `/calendar/entries`. Al montarse:

1. Calcula el primer y último día del mes actual
2. Llama al backend con `start` y `end` del mes visible
3. Mapea las entradas recibidas a eventos del calendario:
   - Un evento verde para cada check-in
   - Un evento rojo para cada check-out
4. Al cambiar de mes, repite el proceso con las nuevas fechas
5. Al hacer click en un evento, navega a la vista de detalle de la reserva
