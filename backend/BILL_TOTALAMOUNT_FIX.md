# 🔧 CORRECCIÓN CRÍTICA - Cálculo de totalAmount en Bill

## 📋 Resumen
Se corrigió un error crítico en el cálculo del `totalAmount` de las facturas (Bill) que causaba **duplicación del monto de addons**.

## 🐛 Problema Identificado

### Síntoma
Las facturas mostraban un `totalAmount` incorrecto (mayor al esperado), especialmente cuando la reserva tenía addons.

### Causa Raíz
En `BillService.createBill()`, el código estaba sumando:
```java
// ❌ INCORRECTO
BigDecimal bookingTotal = booking.getTotalAmount();  // Ya incluye addons
BigDecimal addonsTotal = persistedAddons.stream()...  // Suma addons de nuevo
BigDecimal total = bookingTotal.add(addonsTotal);    // DUPLICACIÓN!
```

El problema es que `booking.getTotalAmount()` **ya incluye los addons** de la reserva (calculado en `BookingService.calculateAndSetBookingTotal()`), pero luego se estaban sumando nuevamente los addons de la factura, causando una duplicación.

### Ejemplo del Error
```
Reserva:
- 3 noches × $100/noche = $300
- Addon1: $50 × 2 = $100
- booking.totalAmount = $400 (correcto)

Factura (antes del fix):
- bookingTotal = $400 (que ya incluye $100 de addons)
- addonsTotal = $100 (suma addons de nuevo)
- total = $500 ❌ INCORRECTO (debería ser $400)
```

## ✅ Solución Implementada

### Cambio en BillService.java
Se modificó el cálculo para que calcule el total desde cero:

```java
// ✅ CORRECTO
// Calcular estadía directamente desde Room y fechas
Room room = booking.getRoom();
BigDecimal roomPricePerNight = BigDecimal.valueOf(room.getPricePerNight());

long nights = ChronoUnit.DAYS.between(
    booking.getCheckInDate(), 
    booking.getCheckOutDate()
);

BigDecimal accommodationSubtotal = roomPricePerNight.multiply(BigDecimal.valueOf(nights));

BigDecimal addonsTotal = persistedAddons.stream()
    .map(ba -> ba.getTotalPrice())
    .reduce(BigDecimal.ZERO, BigDecimal::add);

BigDecimal total = accommodationSubtotal.add(addonsTotal);
```

### Fórmula Correcta
```
Bill.totalAmount = (noches × precio_por_noche) + suma(addons_factura)
```

**NO** usar `booking.getTotalAmount()` porque ya incluye addons del booking.

## 🎯 Resultado

### Ejemplo Correcto (después del fix)
```
Reserva:
- 3 noches × $100/noche = $300
- Addon1: $50 × 2 = $100
- booking.totalAmount = $400

Factura (después del fix):
- accommodationSubtotal = 3 × $100 = $300
- addonsTotal = $100
- total = $400 ✅ CORRECTO
```

## 📝 Archivos Modificados

### `BillService.java`
- **Línea ~90-104**: Refactorizado cálculo de `totalAmount`
- **Línea ~107**: Actualizado mensaje de log para reflejar el nuevo cálculo
- **Importación añadida**: `com.hotelsa.backend.room.model.Room`

## 🧪 Testing Recomendado

1. **Crear una factura para una reserva sin addons**
   - Verificar: `bill.totalAmount == noches × precio_noche`

2. **Crear una factura para una reserva con addons**
   - Verificar: `bill.totalAmount == (noches × precio_noche) + suma(addons)`
   - Verificar: NO duplicación de addons

3. **Verificar consistencia con frontend**
   - El subtotal de alojamiento debe coincidir
   - El subtotal de addons debe coincidir
   - El total debe coincidir

## ⚠️ Notas Importantes

- El `totalAmount` en **Booking** SÍ incluye addons (por diseño de `BookingService`)
- El `totalAmount` en **Bill** se calcula independientemente desde cero
- Los `BillAddon` son una **copia snapshot** de los `BookingAddon` al momento de crear la factura
- Esta diferencia de cálculo es intencional para mantener la integridad histórica de las facturas

## 🔄 Impacto en Revenue

Este fix es crítico para el cálculo correcto de ingresos (Revenue), ya que:
- `billRepository.sumTotalRevenue()` usa `bill.totalAmount`
- Si `bill.totalAmount` está inflado, los reportes de ingresos son incorrectos

## ✅ Estado
- [x] Problema identificado
- [x] Solución implementada
- [x] Código compilado exitosamente
- [ ] Tests actualizados (pendiente)
- [ ] Verificación en producción (pendiente)

---
**Fecha**: 2025-11-26  
**Módulo**: Bill Service  
**Severidad**: CRÍTICA  
**Estado**: RESUELTO

