# Endpoints Backend Requeridos para Módulo de Bookings

## 📋 Resumen
Este documento especifica los endpoints REST API que deben implementarse en el backend Spring Boot para soportar completamente el módulo de Bookings del frontend Angular.

---

## 🏨 Endpoints de Bookings

### 1. Listar todas las reservas (con paginación y filtros)
```http
GET /api/bookings
```

**Query Parameters:**
- `guestId` (optional): ID del huésped
- `roomId` (optional): ID de la habitación
- `status` (optional): Estado de la reserva (PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED)
- `checkInFrom` (optional): Fecha mínima de check-in (YYYY-MM-DD)
- `checkInTo` (optional): Fecha máxima de check-in (YYYY-MM-DD)
- `checkOutFrom` (optional): Fecha mínima de check-out (YYYY-MM-DD)
- `checkOutTo` (optional): Fecha máxima de check-out (YYYY-MM-DD)
- `search` (optional): Búsqueda por texto (nombre huésped, número habitación, etc.)
- `page` (optional): Número de página (default: 0)
- `size` (optional): Tamaño de página (default: 20)

**Response 200 OK:**
```json
{
  "content": [
    {
      "id": 1,
      "guestId": 2,
      "guestName": "John Doe",
      "roomId": 3,
      "roomNumber": "301",
      "checkInDate": "2025-06-01",
      "checkOutDate": "2025-06-05",
      "status": "CONFIRMED",
      "createdBy": "user@example.com",
      "bookingLeadTime": "2025-05-01",
      "notes": "Preferencia piso alto",
      "totalAmount": 500.00,
      "hotelId": 1,
      "createdAt": "2025-05-01T10:30:00",
      "updatedAt": "2025-05-01T10:30:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

---

### 2. Obtener reserva por ID
```http
GET /api/bookings/{id}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "guestId": 2,
  "guestName": "John Doe",
  "roomId": 3,
  "roomNumber": "301",
  "checkInDate": "2025-06-01",
  "checkOutDate": "2025-06-05",
  "status": "CONFIRMED",
  "createdBy": "user@example.com",
  "bookingLeadTime": "2025-05-01",
  "notes": "Preferencia piso alto",
  "totalAmount": 500.00,
  "hotelId": 1
}
```

**Response 404 Not Found:**
```json
{
  "status": 404,
  "message": "Reserva no encontrada",
  "timestamp": "2025-11-23T10:30:00"
}
```

---

### 3. Crear nueva reserva
```http
POST /api/bookings
```

**Request Body:**
```json
{
  "guestId": 2,
  "roomId": 3,
  "checkInDate": "2025-06-01",
  "checkOutDate": "2025-06-05",
  "status": "PENDING",
  "createdBy": "user@example.com",
  "bookingLeadTime": "2025-05-01",
  "notes": "Preferencia piso alto"
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "guestId": 2,
  "guestName": "John Doe",
  "roomId": 3,
  "roomNumber": "301",
  "checkInDate": "2025-06-01",
  "checkOutDate": "2025-06-05",
  "status": "PENDING",
  "createdBy": "user@example.com",
  "bookingLeadTime": "2025-05-01",
  "notes": "Preferencia piso alto",
  "totalAmount": 500.00,
  "hotelId": 1
}
```

**Validaciones Backend:**
- `checkOutDate` debe ser posterior a `checkInDate`
- La habitación debe existir y estar disponible en las fechas solicitadas
- El huésped debe existir
- Si la reserva se confirma, marcar la habitación como OCCUPIED

**Response 400 Bad Request:**
```json
{
  "status": 400,
  "message": "La fecha de check-out debe ser posterior a la fecha de check-in",
  "timestamp": "2025-11-23T10:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": 422,
  "message": "La habitación no está disponible en las fechas seleccionadas",
  "timestamp": "2025-11-23T10:30:00"
}
```

---

### 4. Actualizar reserva
```http
PUT /api/bookings/{id}
```

**Request Body:** (igual que POST)

**Response 200 OK:** (igual que GET by ID)

**Reglas de negocio:**
- No se puede actualizar una reserva CANCELLED o CHECKED_OUT
- Si se cambia el roomId, validar disponibilidad
- Si se cambia de OCCUPIED a AVAILABLE, liberar la habitación anterior

---

### 5. Eliminar reserva
```http
DELETE /api/bookings/{id}
```

**Response 204 No Content**

**Reglas de negocio:**
- Soft delete preferible
- Liberar la habitación si estaba OCCUPIED
- Eliminar addons asociados (soft delete)

---

### 6. Cancelar reserva
```http
PATCH /api/bookings/{id}/cancel
```

**Response 200 OK:** (reserva actualizada con status CANCELLED)

**Reglas de negocio:**
- Cambiar status a CANCELLED
- Liberar habitación (AVAILABLE)
- Mantener addons para historial

---

### 7. Obtener reservas por huésped
```http
GET /api/bookings/guest/{guestId}
```

**Response 200 OK:** Array de reservas

---

### 8. Obtener reservas por habitación
```http
GET /api/bookings/room/{roomId}
```

**Response 200 OK:** Array de reservas

---

### 9. Verificar disponibilidad de habitación
```http
GET /api/bookings/room/{roomId}/availability
```

**Query Parameters:**
- `checkIn` (required): Fecha de check-in (YYYY-MM-DD)
- `checkOut` (required): Fecha de check-out (YYYY-MM-DD)

**Response 200 OK:**
```json
{
  "available": true,
  "conflictingBookings": [],
  "message": "Habitación disponible"
}
```

**Response 200 OK (no disponible):**
```json
{
  "available": false,
  "conflictingBookings": [1, 5, 7],
  "message": "Habitación ocupada en las fechas solicitadas"
}
```

**Lógica de validación:**
```sql
-- Verificar solapamiento de fechas
SELECT COUNT(*) FROM bookings 
WHERE room_id = :roomId 
AND status NOT IN ('CANCELLED')
AND (
  (check_in_date < :checkOut AND check_out_date > :checkIn)
)
```

---

### 10. Obtener reservas en rango de fechas
```http
GET /api/bookings/range
```

**Query Parameters:**
- `startDate` (required): Fecha inicial (YYYY-MM-DD)
- `endDate` (required): Fecha final (YYYY-MM-DD)

**Response 200 OK:** Array de reservas en el rango

**Uso:** Para mostrar calendario de ocupación

---

## 🛏️ Endpoints de Habitaciones (extensiones necesarias)

### 11. Obtener habitaciones disponibles
```http
GET /api/rooms/available
```

**Query Parameters:**
- `checkIn` (required): Fecha de check-in (YYYY-MM-DD)
- `checkOut` (required): Fecha de check-out (YYYY-MM-DD)

**Response 200 OK:**
```json
[
  {
    "id": 3,
    "number": "301",
    "type": "SUITE",
    "floor": 3,
    "capacity": 4,
    "pricePerNight": 150.00,
    "status": "AVAILABLE"
  }
]
```

**Lógica:**
```sql
-- Habitaciones sin reservas conflictivas
SELECT * FROM rooms r
WHERE r.id NOT IN (
  SELECT DISTINCT room_id FROM bookings
  WHERE status NOT IN ('CANCELLED')
  AND check_in_date < :checkOut 
  AND check_out_date > :checkIn
)
AND r.deleted = false
```

---

## 🎁 Endpoints de Addons

### 12. Obtener addons activos
```http
GET /api/addons/active
```

**Response 200 OK:**
```json
[
  {
    "id": 200,
    "name": "WiFi Premium",
    "description": "Internet de alta velocidad",
    "price": 10.00,
    "category": "TECHNOLOGY",
    "active": true
  }
]
```

---

## 🔗 Endpoints de Booking-Addons (relación muchos a muchos)

### 13. Obtener addons de una reserva
```http
GET /api/bookings/{bookingId}/addons
```

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "addonId": 200,
    "addonName": "WiFi Premium",
    "price": 10.00,
    "quantity": 2,
    "subtotal": 20.00
  }
]
```

---

### 14. Añadir addons a una reserva
```http
POST /api/bookings/{bookingId}/addons
```

**Request Body:**
```json
[
  {
    "addonId": 200,
    "quantity": 2
  },
  {
    "addonId": 201,
    "quantity": 1
  }
]
```

**Response 201 Created**

**Validaciones:**
- Los addons deben existir y estar activos
- quantity >= 1
- Calcular subtotal = addon.price × quantity
- No duplicar addons (si existe, actualizar quantity)

---

### 15. Actualizar cantidad de addon
```http
PATCH /api/bookings/{bookingId}/addons/{addonId}
```

**Request Body:**
```json
{
  "quantity": 3
}
```

**Response 200 OK**

---

### 16. Eliminar addon de reserva
```http
DELETE /api/bookings/{bookingId}/addons/{addonId}
```

**Response 204 No Content**

**Nota:** Soft delete preferible para mantener historial

---

## 👥 Endpoints de Guests (extensiones necesarias)

### 17. Buscar huéspedes
```http
GET /api/guests/search
```

**Query Parameters:**
- `query` (required): Texto de búsqueda (nombre, apellido, email)

**Response 200 OK:**
```json
[
  {
    "id": 2,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890"
  }
]
```

**Lógica:**
```sql
SELECT * FROM guests
WHERE (
  LOWER(first_name) LIKE LOWER(:query) OR
  LOWER(last_name) LIKE LOWER(:query) OR
  LOWER(email) LIKE LOWER(:query)
)
AND deleted = false
LIMIT 10
```

---

### 18. Listar todos los huéspedes
```http
GET /api/guests
```

**Query Parameters:**
- `page` (optional): Número de página
- `size` (optional): Tamaño de página

**Response 200 OK:** Lista paginada de huéspedes

---

## 🔐 Consideraciones de Seguridad

1. **Autenticación**: Todos los endpoints requieren token JWT válido
2. **Autorización**: 
   - ADMIN: Acceso completo
   - EMPLOYEE: Puede crear/editar/ver reservas
   - USER: Solo puede ver sus propias reservas (filtrar por userId)
3. **Multitenancy**: Filtrar todas las queries por `hotelId` del usuario autenticado
4. **Validación**: 
   - Validar fechas en el backend (no confiar solo en frontend)
   - Prevenir SQL injection usando PreparedStatements/JPA
   - Validar que el usuario tiene permisos sobre el hotelId

---

## 📊 Modelo de Datos Requerido

### Tabla: bookings
```sql
CREATE TABLE bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  guest_id BIGINT NOT NULL,
  room_id BIGINT NOT NULL,
  check_in_date DATE NOT NULL,
  check_out_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_by VARCHAR(255) NOT NULL,
  booking_lead_time DATE NOT NULL,
  notes TEXT,
  total_amount DECIMAL(10,2),
  hotel_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (guest_id) REFERENCES guests(id),
  FOREIGN KEY (room_id) REFERENCES rooms(id),
  FOREIGN KEY (hotel_id) REFERENCES hotels(id),
  CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
);

CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_hotel ON bookings(hotel_id);
```

### Tabla: booking_addons
```sql
CREATE TABLE booking_addons (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  booking_id BIGINT NOT NULL,
  addon_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  FOREIGN KEY (addon_id) REFERENCES addons(id),
  UNIQUE KEY uk_booking_addon (booking_id, addon_id, deleted)
);
```

---

## 🧪 Casos de Prueba Recomendados

1. **Crear reserva con fechas válidas** → 201 Created
2. **Crear reserva con checkOut <= checkIn** → 400 Bad Request
3. **Crear reserva en habitación ocupada** → 422 Unprocessable Entity
4. **Actualizar reserva cancelada** → 400 Bad Request
5. **Verificar disponibilidad con solapamiento** → available: false
6. **Añadir addons duplicados** → Actualizar cantidad
7. **Eliminar addon inexistente** → 404 Not Found
8. **Buscar huéspedes con query vacío** → 400 Bad Request
9. **Filtrar reservas por multitenancy** → Solo reservas del hotel del usuario

---

## 📝 Notas de Implementación

1. **DTO Mapping**: Usar MapStruct o ModelMapper para convertir entidades a DTOs
2. **Validaciones**: Usar `@Valid` y Jakarta Bean Validation
3. **Transacciones**: Marcar métodos con `@Transactional` donde corresponda
4. **Excepciones**: Usar `@ControllerAdvice` para manejo global (GlobalExceptionHandler)
5. **Logging**: Registrar operaciones críticas (creación, cancelación, eliminación)
6. **Auditoría**: Considerar Spring Data JPA Auditing para `createdAt`, `updatedAt`

---

## 🚀 Prioridad de Implementación

### Fase 1 (Core - Alta Prioridad)
1. GET /api/bookings (listar)
2. GET /api/bookings/{id}
3. POST /api/bookings (crear)
4. PUT /api/bookings/{id} (actualizar)
5. DELETE /api/bookings/{id}

### Fase 2 (Disponibilidad - Alta Prioridad)
6. GET /api/bookings/room/{roomId}/availability
7. GET /api/rooms/available

### Fase 3 (Addons - Media Prioridad)
8. GET /api/addons/active
9. POST /api/bookings/{bookingId}/addons
10. GET /api/bookings/{bookingId}/addons

### Fase 4 (Búsqueda y Filtros - Media Prioridad)
11. GET /api/guests/search
12. GET /api/bookings/guest/{guestId}
13. GET /api/bookings/room/{roomId}

### Fase 5 (Adicionales - Baja Prioridad)
14. PATCH /api/bookings/{id}/cancel
15. GET /api/bookings/range
16. PATCH /api/bookings/{bookingId}/addons/{addonId}
17. DELETE /api/bookings/{bookingId}/addons/{addonId}

---

## ✅ Checklist de Implementación

- [ ] Crear entidades JPA (Booking, BookingAddon)
- [ ] Crear repositorios (BookingRepository, BookingAddonRepository)
- [ ] Implementar DTOs (BookingRequestDTO, BookingResponseDTO)
- [ ] Crear servicios (BookingService, BookingAddonService)
- [ ] Implementar controladores REST
- [ ] Añadir validaciones (@Valid, custom validators)
- [ ] Implementar GlobalExceptionHandler
- [ ] Configurar CORS si es necesario
- [ ] Escribir tests unitarios
- [ ] Escribir tests de integración
- [ ] Documentar con Swagger/OpenAPI
- [ ] Configurar logs
- [ ] Validar multitenancy

---

**Fecha de creación:** 2025-11-23  
**Versión:** 1.0  
**Autor:** GitHub Copilot  
**Proyecto:** Hotel Management System - Spring Boot + Angular
