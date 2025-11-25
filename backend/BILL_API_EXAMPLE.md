# Ejemplos de API - Bill Response DTO Actualizado

## Resumen de cambios
El endpoint de facturas ahora devuelve información completa del booking, huésped y habitación asociados, facilitando la visualización de toda la información relevante en una sola respuesta.

---

## GET /api/bills/{id}

### Request
```http
GET /api/bills/6
Authorization: Bearer {token}
```

### Response (ANTES)
```json
{
    "id": 6,
    "bookingId": 4,
    "notes": null,
    "status": "UNPAID",
    "paymentMethod": null,
    "createdAt": "2025-11-24T23:30:22.031109",
    "totalAmount": 24.00,
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

### Response (DESPUÉS - Enriquecida)
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

---

## GET /api/bills

### Request
```http
GET /api/bills
Authorization: Bearer {token}
```

### Response
```json
[
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
    },
    {
        "id": 7,
        "bookingId": 5,
        "guestId": 2,
        "guestName": "María González",
        "roomId": 4,
        "roomNumber": "202",
        "checkInDate": "2025-11-25",
        "checkOutDate": "2025-11-27",
        "nights": 2,
        "roomPricePerNight": 150.00,
        "accommodationSubtotal": 300.00,
        "addonsSubtotal": 50.00,
        "notes": "Cliente VIP",
        "status": "PAID",
        "paymentMethod": "CREDIT_CARD",
        "createdAt": "2025-11-24T23:35:15.123456",
        "totalAmount": 350.00,
        "addons": [
            {
                "addonId": 1,
                "addonName": "Desayuno mediterraneo",
                "description": "Desayuno continental incluido",
                "unitPrice": 12.00,
                "quantity": 2,
                "totalPrice": 24.00
            },
            {
                "addonId": 2,
                "addonName": "Spa",
                "description": "Acceso al spa por 1 hora",
                "unitPrice": 26.00,
                "quantity": 1,
                "totalPrice": 26.00
            }
        ]
    }
]
```

---

## POST /api/bills

### Request
```http
POST /api/bills?bookingId=4
Authorization: Bearer {token}
Content-Type: application/json

{
    "notes": "Factura para reserva VIP",
    "status": "UNPAID"
}
```

### Response
```json
{
    "id": 8,
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
    "notes": "Factura para reserva VIP",
    "status": "UNPAID",
    "paymentMethod": null,
    "createdAt": "2025-11-24T23:40:00.000000",
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

---

## Campos adicionales explicados

| Campo | Tipo | Descripción | Fuente |
|-------|------|-------------|--------|
| `guestId` | Long | ID del huésped | Booking → Guest |
| `guestName` | String | Nombre completo del huésped | Booking → Guest → fullName |
| `roomId` | Long | ID de la habitación | Booking → Room |
| `roomNumber` | String | Número de la habitación | Booking → Room → number |
| `checkInDate` | LocalDate | Fecha de check-in | Booking → checkInDate |
| `checkOutDate` | LocalDate | Fecha de check-out | Booking → checkOutDate |
| `nights` | Integer | Número de noches | Calculado: checkOutDate - checkInDate |
| `roomPricePerNight` | BigDecimal | Precio por noche | Booking → Room → pricePerNight |
| `accommodationSubtotal` | BigDecimal | Subtotal alojamiento | Calculado: nights × roomPricePerNight |
| `addonsSubtotal` | BigDecimal | Subtotal de addons | Calculado: suma de todos los totalPrice de addons |

---

## Notas importantes

### Manejo de valores null
Todos los campos nuevos pueden ser `null` si:
- No existe un booking asociado
- El booking no tiene guest o room asociados
- Las fechas no están definidas

### Cálculos automáticos
- **nights**: Se calcula automáticamente usando `ChronoUnit.DAYS.between(checkInDate, checkOutDate)`
- **accommodationSubtotal**: Se calcula multiplicando `nights × roomPricePerNight`
- **addonsSubtotal**: Se calcula sumando todos los `totalPrice` de los addons

### Performance
Las consultas usan `LEFT JOIN FETCH` para cargar todas las relaciones en una sola query, evitando el problema N+1 y mejorando el rendimiento.

### Compatibilidad
Los campos existentes se mantienen sin cambios, por lo que la API es compatible con clientes que esperen la estructura anterior.

