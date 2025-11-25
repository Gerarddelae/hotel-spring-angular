# 📚 Módulo de Bookings - Hotel Management System

## 📋 Descripción

Módulo completo de gestión de reservas (bookings) para el sistema de administración hotelera. Incluye funcionalidades de CRUD, validación de disponibilidad, gestión de servicios adicionales (addons), y cálculo automático de totales.

---

## 🏗️ Estructura del Módulo

```
features/bookings/
├── models/
│   ├── booking.interface.ts          # Interfaces principales de Booking
│   └── booking-filters.interface.ts  # Interfaces de filtros y disponibilidad
├── services/
│   ├── booking.service.ts            # Servicio HTTP con lógica de negocio
│   └── booking.service.spec.ts       # Tests unitarios del servicio
├── pages/
│   ├── booking-list/                 # Página de listado con filtros
│   │   ├── booking-list.component.ts
│   │   ├── booking-list.component.html
│   │   ├── booking-list.component.scss
│   │   └── booking-list.component.spec.ts
│   └── booking-detail/               # Página de detalle
│       ├── booking-detail.component.ts
│       ├── booking-detail.component.html
│       ├── booking-detail.component.scss
│       └── booking-detail.component.spec.ts
├── bookings.component.ts             # Componente raíz (router-outlet)
├── bookings.component.spec.ts
└── bookings.routes.ts                # Configuración de rutas
```

### Componentes Compartidos

```
shared/components/
├── booking-modal-form/               # Modal CRUD de reservas
│   ├── booking-modal-form.component.ts
│   ├── booking-modal-form.component.html
│   ├── booking-modal-form.component.scss
│   └── booking-modal-form.component.spec.ts
└── addon-selector/                   # Selector de servicios adicionales
    ├── addon-selector.component.ts
    ├── addon-selector.component.html
    ├── addon-selector.component.scss
    └── addon-selector.component.spec.ts
```

### Validadores Personalizados

```
shared/validators/
└── date.validators.ts                # Validadores de fechas
    ├── dateRangeValidator()
    ├── futureDateValidator()
    ├── dateRangeLimitValidator()
    ├── maxDateRangeValidator()
    └── minDateRangeValidator()
```

---

## ✨ Funcionalidades

### 1. Gestión de Reservas (CRUD)
- ✅ Crear nueva reserva
- ✅ Editar reserva existente
- ✅ Ver detalle de reserva
- ✅ Cancelar reserva
- ✅ Eliminar reserva
- ✅ Listar todas las reservas

### 2. Validación de Disponibilidad
- ✅ Verificar disponibilidad de habitación en fechas seleccionadas
- ✅ Mostrar habitaciones disponibles según rango de fechas
- ✅ Prevenir solapamiento de reservas
- ✅ Validación en tiempo real durante creación/edición

### 3. Gestión de Servicios Adicionales (Addons)
- ✅ Seleccionar addons disponibles
- ✅ Ajustar cantidad de cada addon
- ✅ Calcular subtotales automáticamente
- ✅ Cálculo de total general (hospedaje + addons)
- ✅ Editar/eliminar addons de una reserva

### 4. Filtros y Búsqueda
- ✅ Filtrar por huésped
- ✅ Filtrar por habitación
- ✅ Filtrar por estado (PENDING, CONFIRMED, etc.)
- ✅ Filtrar por rango de fechas (check-in/check-out)
- ✅ Búsqueda por texto (ID, nombre huésped, número habitación)
- ✅ Limpiar filtros

### 5. Características UX/UI
- ✅ Modal responsivo con Material Design
- ✅ Autocomplete de huéspedes con debounce (300ms)
- ✅ Datepickers con validación de fechas mínimas/máximas
- ✅ Indicadores visuales de disponibilidad
- ✅ Confirmación antes de guardar con resumen
- ✅ Chips de estado con colores semánticos
- ✅ Loading spinners durante operaciones HTTP
- ✅ Mensajes de error/éxito con Snackbar
- ✅ Diseño responsive (mobile-first)

---

## 🔧 Tecnologías Utilizadas

- **Angular 18+** (Standalone Components)
- **Angular Material** (UI Components)
- **RxJS** (Reactive Programming)
- **TypeScript** (Type Safety)
- **SCSS** (Styling)
- **Jasmine/Karma** (Testing)

---

## 📦 Dependencias

### Angular Material Modules
```typescript
- MatDialogModule
- MatFormFieldModule
- MatInputModule
- MatButtonModule
- MatSelectModule
- MatDatepickerModule
- MatNativeDateModule
- MatAutocompleteModule
- MatProgressSpinnerModule
- MatSnackBarModule
- MatTableModule
- MatChipsModule
- MatIconModule
- MatCardModule
- MatDividerModule
- MatListModule
- MatTooltipModule
```

---

## 🚀 Instalación y Uso

### 1. Importar rutas en `app.routes.ts`

```typescript
{
  path: 'bookings',
  loadChildren: () =>
    import('./features/bookings/bookings.routes').then(
      (m) => m.bookingsRoutes
    ),
  canActivate: [() => roleGuard(['ADMIN', 'EMPLOYEE', 'USER'])],
}
```

### 2. Configurar Navigation (opcional)

Agregar en el menú de navegación:
```typescript
{
  label: 'Reservas',
  icon: 'bookmark',
  route: '/bookings'
}
```

### 3. Iniciar servidor backend

Asegúrate de que el backend esté corriendo en `http://localhost:8080` con los endpoints requeridos (ver `BACKEND_ENDPOINTS_BOOKINGS.md`).

---

## 🎯 Uso del Módulo

### Crear una Reserva

1. Navegar a `/bookings`
2. Click en "Nueva Reserva"
3. Buscar y seleccionar huésped
4. Seleccionar fechas de check-in y check-out
5. Seleccionar habitación disponible (se filtra automáticamente)
6. (Opcional) Agregar servicios adicionales
7. (Opcional) Agregar notas
8. Click en "Crear Reserva"
9. Confirmar en el modal de resumen

### Editar una Reserva

1. Desde el listado, click en el botón "Editar" (icono de lápiz)
2. Modificar campos necesarios
3. El sistema revalidará disponibilidad si cambian fechas o habitación
4. Click en "Actualizar Reserva"

### Ver Detalle

1. Click en el botón "Ver detalle" (icono de ojo)
2. Se muestra información completa: huésped, habitación, fechas, addons, total
3. Acciones disponibles: Editar, Cancelar, Eliminar

---

## 🔐 Seguridad

### Autenticación
- Todos los componentes están protegidos por `AuthGuard`
- Requiere token JWT válido

### Autorización (Roles)
- **ADMIN**: Acceso completo a todas las funciones
- **EMPLOYEE**: Puede crear, editar, ver y cancelar reservas
- **USER**: Solo puede ver listado (frontend filtra, backend debe filtrar por userId)

### Validaciones
- **Frontend**: Validación de formularios con Reactive Forms
- **Backend**: Validación adicional (no confiar solo en frontend)
- Fechas: Check-out > Check-in
- Disponibilidad: No permitir solapamiento de reservas
- Addons: Cantidad mínima 1

---

## 🧪 Testing

### Ejecutar tests unitarios

```bash
ng test --include='**/*booking*.spec.ts'
```

### Coverage de tests

```bash
ng test --no-watch --code-coverage
```

### Tests implementados

- ✅ BookingService: HTTP calls, cálculo de totales, validación de fechas
- ✅ BookingModalFormComponent: Inicialización, validaciones, submit
- ✅ BookingListComponent: Carga de datos, filtros
- ✅ BookingDetailComponent: Cálculo de noches, totales
- ✅ AddonSelectorComponent: Agregar/eliminar addons, cálculo de subtotales

---

## 📊 Modelos de Datos

### Booking
```typescript
interface Booking {
  id: number;
  guestId: number;
  guestName?: string;
  roomId: number;
  roomNumber?: string;
  checkInDate: string;       // YYYY-MM-DD
  checkOutDate: string;      // YYYY-MM-DD
  status: BookingStatus;
  createdBy: string;
  bookingLeadTime: string;   // YYYY-MM-DD
  notes?: string;
  totalAmount?: number;
  hotelId: number;
  addons?: BookingAddon[];
}
```

### BookingStatus
```typescript
type BookingStatus = 
  | 'PENDING'       // Pendiente de confirmación
  | 'CONFIRMED'     // Confirmada
  | 'CHECKED_IN'    // Check-in realizado
  | 'CHECKED_OUT'   // Check-out realizado
  | 'CANCELLED';    // Cancelada
```

### BookingAddon
```typescript
interface BookingAddon {
  id?: number;
  addonId: number;
  addonName?: string;
  price: number;
  quantity: number;
  subtotal?: number;   // price * quantity
}
```

---

## 🔄 Flujo de Datos

### Crear Reserva con Addons

```
1. Usuario completa formulario
2. Frontend valida fechas (dateRangeValidator)
3. Frontend verifica disponibilidad (BookingService.checkRoomAvailability)
4. Si disponible, usuario agrega addons (AddonSelectorComponent)
5. Usuario confirma en modal de resumen
6. POST /api/bookings (crear reserva base)
7. POST /api/bookings/{id}/addons (agregar addons)
8. Backend marca habitación como OCCUPIED
9. Frontend refresca listado
10. Muestra mensaje de éxito
```

### Cancelar Reserva

```
1. Usuario click en "Cancelar"
2. Confirmar acción
3. PATCH /api/bookings/{id}/cancel
4. Backend cambia status a CANCELLED
5. Backend libera habitación (AVAILABLE)
6. Frontend refresca listado
7. Muestra mensaje de éxito
```

---

## 🐛 Manejo de Errores

### Errores Comunes

| Código | Error | Acción |
|--------|-------|--------|
| 400 | Bad Request | Mostrar mensaje de validación específico |
| 404 | Not Found | Redirigir al listado |
| 422 | Unprocessable Entity | Mostrar error de lógica de negocio (ej: habitación no disponible) |
| 500 | Internal Server Error | Mostrar error genérico |

### Formato de Error Backend

```typescript
interface ApiError {
  status: number;
  message: string;
  timestamp: string;
}
```

---

## 🎨 Personalización

### Cambiar colores de estados

Editar `booking-list.component.ts`:
```typescript
getStatusColor(status: string): string {
  const colors: { [key: string]: string } = {
    'PENDING': 'accent',      // Amarillo
    'CONFIRMED': 'primary',   // Azul
    'CHECKED_IN': 'primary',  // Azul
    'CHECKED_OUT': '',        // Gris (default)
    'CANCELLED': 'warn'       // Rojo
  };
  return colors[status] || '';
}
```

### Ajustar debounce de autocomplete

Editar `booking-modal-form.component.ts`:
```typescript
debounceTime(300)  // Cambiar a 500, 1000, etc.
```

---

## 📝 Notas Importantes

1. **Fechas**: Usar siempre formato ISO (YYYY-MM-DD) para comunicación con backend
2. **Timezone**: El sistema asume fechas sin zona horaria (local del hotel)
3. **Multitenancy**: Todos los endpoints backend deben filtrar por `hotelId`
4. **Soft Delete**: Se recomienda soft delete para mantener historial
5. **Addons**: Los addons se guardan en tabla intermedia `booking_addons`

---

## 🔮 Mejoras Futuras

- [ ] Paginación real (actualmente carga todas las reservas)
- [ ] Exportar reservas a PDF/Excel
- [ ] Vista de calendario (calendar view)
- [ ] Notificaciones por email
- [ ] Check-in/Check-out desde la app
- [ ] Historial de cambios (audit log)
- [ ] Integración con sistema de pagos
- [ ] Reportes y estadísticas
- [ ] Reservas recurrentes
- [ ] Lista de espera (waitlist)

---

## 📞 Soporte

Para dudas o problemas, consultar:
- Documentación de endpoints: `BACKEND_ENDPOINTS_BOOKINGS.md`
- Especificación original en el prompt inicial
- Tests unitarios para ejemplos de uso

---

## 📄 Licencia

Este módulo es parte del proyecto Hotel Management System.

---

**Última actualización:** 2025-11-23  
**Versión:** 1.0.0  
**Autor:** GitHub Copilot
