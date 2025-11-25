# Módulo de Facturación (Billing)

## Estructura

```
billing/
├── models/
│   ├── bill.model.ts             # Interfaz principal de factura
│   ├── bill-request.model.ts     # DTOs para crear/actualizar facturas
│   ├── bill-status.enum.ts       # Estados de factura (PAID, UNPAID, CANCELED)
│   ├── payment-method.enum.ts    # Métodos de pago
│   ├── bill-addon.model.ts       # Interfaz de addons en factura
│   └── index.ts                  # Barrel export
├── services/
│   ├── bill.service.ts           # Servicio HTTP para facturas
│   └── bill.service.spec.ts      # Tests del servicio
├── pages/
│   ├── bill-list/                # Lista de todas las facturas
│   │   ├── bill-list.component.ts
│   │   ├── bill-list.component.html
│   │   ├── bill-list.component.scss
│   │   └── bill-list.component.spec.ts
│   └── bill-detail/              # Detalle de una factura
│       ├── bill-detail.component.ts
│       ├── bill-detail.component.html
│       ├── bill-detail.component.scss
│       └── bill-detail.component.spec.ts
├── billing.component.ts          # Componente contenedor
└── billing.routes.ts             # Rutas del módulo
```

## Rutas

- `/bills` - Lista de facturas
- `/bills/:id` - Detalle de factura

## Endpoints API

- `GET /api/bills` - Obtener todas las facturas
- `GET /api/bills/:id` - Obtener factura por ID
- `POST /api/bills/:bookingId` - Crear factura para un booking
- `PATCH /api/bills/:id/status` - Actualizar estado de factura
- `PUT /api/bills/:id` - Actualizar factura
- `DELETE /api/bills/:id` - Eliminar factura
- `GET /api/bills/booking/:bookingId` - Obtener factura por booking ID

## Flujo de Creación

1. El usuario navega a Booking Details (`/bookings/:id`)
2. Si no existe factura, aparece botón "Generar Factura"
3. Al presionar, se llama `POST /api/bills/:bookingId`
4. Se redirige automáticamente a `/bills/:newBillId`

## Estados de Factura

- `PAID` - Pagada
- `UNPAID` - Pendiente
- `CANCELED` - Cancelada

## Métodos de Pago

- `CASH` - Efectivo
- `CREDIT_CARD` - Tarjeta de Crédito
- `DEBIT_CARD` - Tarjeta de Débito
- `TRANSFER` - Transferencia
- `CHECK` - Cheque
- `OTHER` - Otro
