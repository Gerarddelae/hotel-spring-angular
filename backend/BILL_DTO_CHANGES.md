# Cambios en BillResponseDTO

## Resumen
Se ha actualizado el DTO de respuesta de Bill para incluir información adicional del booking asociado, guest y room.

## Cambios realizados

### 1. BillResponseDTO.java
Se agregaron los siguientes campos al DTO:

#### Información del huésped
- `guestId` (Long) - ID del huésped
- `guestName` (String) - Nombre completo del huésped

#### Información de la habitación
- `roomId` (Long) - ID de la habitación
- `roomNumber` (String) - Número de la habitación

#### Información de fechas y estadía
- `checkInDate` (LocalDate) - Fecha de check-in
- `checkOutDate` (LocalDate) - Fecha de check-out
- `nights` (Integer) - Número de noches (calculado automáticamente)

#### Información de precios
- `roomPricePerNight` (BigDecimal) - Precio por noche de la habitación
- `accommodationSubtotal` (BigDecimal) - Subtotal del hospedaje (nights × roomPricePerNight)
- `addonsSubtotal` (BigDecimal) - Subtotal de servicios adicionales

### 2. BillMapper.java
Se actualizó el método `fromEntity()` para:
- Extraer información del booking asociado
- Navegar a través de las relaciones guest y room
- Calcular el número de noches usando `ChronoUnit.DAYS.between()`
- Calcular el subtotal de alojamiento
- Calcular el subtotal de addons
- Manejar correctamente casos donde las relaciones pueden ser null

### 3. BillRepository.java
Se agregaron dos nuevos métodos con queries personalizadas:

- `findByIdWithRelations(Long id)`: Carga una factura con todas sus relaciones (booking, guest, room, addons) usando LEFT JOIN FETCH
- `findAllWithRelations()`: Carga todas las facturas con sus relaciones

Estos métodos evitan el problema de LazyInitializationException al cargar todas las relaciones necesarias en una sola consulta.

### 4. BillService.java
Se actualizaron los siguientes métodos para usar las nuevas queries del repositorio:

- `findById()`: Ahora usa `findByIdWithRelations()`
- `findAll()`: Ahora usa `findAllWithRelations()`
- `createBill()`: Recarga la factura creada usando `findByIdWithRelations()` para asegurar que todas las relaciones estén disponibles

## Ejemplo de respuesta

### Antes:
```json
{
    "id": 6,
    "bookingId": 4,
    "notes": null,
    "status": "UNPAID",
    "paymentMethod": null,
    "createdAt": "2025-11-24T23:30:22.031109",
    "totalAmount": 24.00,
    "addons": [...]
}
```

### Después:
```json
{
    "id": 6,
    "bookingId": 4,
    "guestId": 1,
    "guestName": "Juan Pérez",
    "roomId": 3,
    "roomNumber": "101",
    "checkInDate": "2025-11-20",
    "checkOutDate": "2025-11-22",
    "nights": 2,
    "roomPricePerNight": 100.00,
    "accommodationSubtotal": 200.00,
    "addonsSubtotal": 24.00,
    "notes": null,
    "status": "UNPAID",
    "paymentMethod": null,
    "createdAt": "2025-11-24T23:30:22.031109",
    "totalAmount": 224.00,
    "addons": [
        {
            "addonId": 1,
            "addonName": "Desayuno mediterraneo",
            "description": "Desayuno continental incluido",
            "unitPrice": 12.00,
            "quantity": 2,
            "totalPrice": 24.00
        }
    ]
}
```

## Endpoints afectados
- `GET /api/bills` - Lista todas las facturas con la información enriquecida
- `GET /api/bills/{id}` - Obtiene una factura específica con la información enriquecida
- `POST /api/bills` - Crea una nueva factura y devuelve la respuesta enriquecida

## Consideraciones técnicas
1. **Performance**: Las queries usan JOIN FETCH para cargar todas las relaciones en una sola consulta, evitando el problema N+1.
2. **Nullabilidad**: El mapper maneja correctamente casos donde booking, guest o room puedan ser null.
3. **Cálculos**: Los campos calculados (nights, accommodationSubtotal, addonsSubtotal) se generan dinámicamente en el mapper.
4. **Compatibilidad**: Los campos existentes se mantienen en su lugar, por lo que es compatible con clientes que esperen la estructura anterior.

## Pruebas
Se recomienda ejecutar las siguientes pruebas:
```bash
.\mvnw.cmd test -Dtest=BillServiceTest
.\mvnw.cmd test -Dtest=BillControllerTest
.\mvnw.cmd test -Dtest=BillRepositoryTest
```

