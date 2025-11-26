# 🔍 DIAGNÓSTICO Y SOLUCIÓN - Active Guests Endpoint Retorna 0

## 🐛 Problema Reportado
El endpoint `GET /bookings/active-guests-count` **siempre retorna 0**, incluso cuando hay reservas con estado `CHECKED_IN` en el rango de fechas actual.

---

## 🔎 Análisis del Problema

### Causa Raíz Probable

El problema tiene **3 posibles causas**:

#### 1. **Filtros de Hibernate Activos (MÁS PROBABLE)**
Los filtros automáticos de Hibernate (`tenantFilter` y `deletedFilter`) se aplican a **TODAS** las queries, incluyendo las de COUNT:

```java
@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "hotelId", type = Long.class))
@Filter(name = "deletedFilter", condition = "deleted = :isDeleted")
@Filter(name = "tenantFilter", condition = "hotel_id = :hotelId")
```

**Impacto:**
- Si el `hotelId` del contexto no coincide con las bookings → COUNT = 0
- Si las bookings tienen `deleted = true` → COUNT = 0

#### 2. **No Existen Datos CHECKED_IN en el Rango Actual**
En la base de datos **NO HAY** bookings que cumplan:
```sql
status = 'CHECKED_IN'
AND checkInDate <= HOY
AND checkOutDate >= HOY
AND deleted = false
AND hotel_id = {hotel_del_usuario_actual}
```

#### 3. **Problema con el Operador BETWEEN**
Algunos dialectos de Hibernate/JPA pueden tener problemas con `BETWEEN` cuando se usa con fechas y parámetros posicionales.

---

## ✅ Soluciones Implementadas

### 1. Query Mejorada (Más Explícita)

**Antes:**
```java
@Query("""
    SELECT COUNT(DISTINCT b.guest.id)
    FROM Booking b
    WHERE b.status = 'CHECKED_IN'
    AND :today BETWEEN b.checkInDate AND b.checkOutDate
    """)
```

**Después:**
```java
@Query("""
    SELECT COUNT(DISTINCT b.guest.id)
    FROM Booking b
    WHERE b.status = :status
    AND b.checkInDate <= :today
    AND b.checkOutDate >= :today
    AND b.deleted = false
    """)
int countActiveGuestsTodayExplicit(@Param("today") LocalDate today,
                                   @Param("status") BookingStatus status);
```

**Cambios:**
- ✅ Enum como parámetro en vez de string literal
- ✅ Comparación explícita `<= y >=` en vez de `BETWEEN`
- ✅ Filtro manual de `deleted = false`

### 2. Endpoint de Debug

Se creó `BookingDebugController` con el endpoint:

```
GET /api/debug/bookings/active-guests-debug
```

**Respuesta:**
```json
{
  "today": "2025-11-25",
  "totalBookings": 45,
  "pendingCount": 12,
  "confirmedCount": 18,
  "checkedInCount": 8,
  "cancelledCount": 7,
  "activeGuestsCount": 8,
  "checkedInBookingsList": 8,
  "manualCount": 8
}
```

**Usar este endpoint para:**
1. Verificar que existen bookings en la BD
2. Confirmar cuántas tienen estado `CHECKED_IN`
3. Comparar el conteo automático vs manual

---

## 🧪 Pasos de Diagnóstico

### Paso 1: Verificar Datos en Base de Datos

Ejecuta directamente en PostgreSQL:

```sql
-- Ver todas las bookings CHECKED_IN
SELECT 
    b.id,
    b.status,
    b.check_in_date as checkIn,
    b.check_out_date as checkOut,
    b.deleted,
    b.hotel_id,
    g.full_name as guest,
    CURRENT_DATE as today,
    CASE 
        WHEN CURRENT_DATE BETWEEN b.check_in_date AND b.check_out_date 
        THEN 'EN RANGO' 
        ELSE 'FUERA DE RANGO' 
    END as rango_status
FROM bookings b
JOIN guests g ON b.id_guest_fk = g.id
WHERE b.status = 'CHECKED_IN'
  AND b.deleted = false
ORDER BY b.check_in_date DESC;
```

**Esperado:**
- Debe mostrar al menos 1 booking con `rango_status = 'EN RANGO'`
- Verificar que `hotel_id` coincide con el del usuario logueado

### Paso 2: Verificar Contexto de Tenant

Ejecuta en la aplicación:

```java
// En cualquier Service
Long currentHotelId = authService.getCurrentHotelId();
log.info("Current Hotel ID: {}", currentHotelId);
```

Luego verifica en la BD:
```sql
SELECT hotel_id, COUNT(*) 
FROM bookings 
WHERE status = 'CHECKED_IN' 
  AND deleted = false
GROUP BY hotel_id;
```

**Si los `hotel_id` no coinciden → El filtro de tenant está bloqueando las queries.**

### Paso 3: Usar el Endpoint de Debug

```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/debug/bookings/active-guests-debug
```

**Analizar:**
- Si `totalBookings = 0` → No hay datos en el tenant actual
- Si `checkedInCount = 0` → No hay bookings con ese estado
- Si `checkedInCount > 0` pero `activeGuestsCount = 0` → Problema con las fechas

### Paso 4: Probar con Datos de Prueba

Insertar una booking de prueba:

```sql
-- 1. Obtener IDs necesarios
SELECT id, name FROM hotels LIMIT 1;
SELECT id, full_name FROM guests LIMIT 1;
SELECT id, number FROM rooms LIMIT 1;

-- 2. Insertar booking CHECKED_IN para HOY
INSERT INTO bookings (
    id_guest_fk, 
    id_room_fk, 
    hotel_id,
    check_in_date, 
    check_out_date, 
    status, 
    created_by, 
    booking_lead_time,
    deleted,
    created_at
) VALUES (
    {guest_id},
    {room_id},
    {hotel_id},
    CURRENT_DATE - INTERVAL '1 day',  -- Empezó ayer
    CURRENT_DATE + INTERVAL '2 days', -- Termina en 2 días
    'CHECKED_IN',
    'system',
    CURRENT_DATE,
    false,
    NOW()
);
```

Luego volver a probar el endpoint.

---

## 🔧 Configuración de Filtros Hibernate

Verificar que los filtros están habilitados correctamente:

```java
// TenantFilter.java
@Component
@Order(1)
public class TenantFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, 
                        ServletResponse response, 
                        FilterChain chain) 
            throws IOException, ServletException {
        
        // El hotelId debe estar en el contexto
        Long hotelId = // extraer del JWT
        TenantContext.setCurrentTenant(hotelId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
```

**Verificar:**
```java
// Antes de la query
log.debug("Current Tenant: {}", TenantContext.getCurrentTenant());
```

---

## 📊 Queries SQL Generadas por Hibernate

Para ver las queries reales ejecutadas, activar logs en `application.properties`:

```properties
# Ver las queries SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Ver los valores de los parámetros
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Ver los filtros aplicados
logging.level.org.hibernate.engine.spi.FilterHelper=DEBUG
```

Luego buscar en logs algo como:
```sql
Hibernate: 
    select
        count(distinct booking0_.id_guest_fk) 
    from
        bookings booking0_ 
    where
        booking0_.hotel_id=?      -- FILTRO DE TENANT
        and booking0_.deleted=?   -- FILTRO DE SOFT DELETE
        and booking0_.status=? 
        and booking0_.check_in_date<=? 
        and booking0_.check_out_date>=?
```

---

## 🎯 Checklist de Validación

- [ ] Existen bookings con `status = 'CHECKED_IN'` en la BD
- [ ] Las fechas `checkInDate <= HOY` y `checkOutDate >= HOY`
- [ ] `deleted = false` en esas bookings
- [ ] El `hotel_id` de las bookings coincide con el del usuario logueado
- [ ] El `TenantContext` está correctamente configurado
- [ ] Los filtros de Hibernate están activos
- [ ] La query explícita `countActiveGuestsTodayExplicit` retorna el valor correcto en el endpoint de debug

---

## 🚀 Recomendaciones

### Solución Temporal: Deshabilitar Filtro en la Query

Si el problema persiste, agregar hint para deshabilitar filtros:

```java
@Query(value = """
    SELECT COUNT(DISTINCT b.id_guest_fk)
    FROM bookings b
    WHERE b.status = :status
    AND b.check_in_date <= :today
    AND b.check_out_date >= :today
    AND b.deleted = false
    AND b.hotel_id = :hotelId
    """, nativeQuery = true)
int countActiveGuestsNative(@Param("today") LocalDate today,
                            @Param("status") String status,
                            @Param("hotelId") Long hotelId);
```

Y usarlo en el Service:
```java
Long hotelId = getCurrentHotelId();
int count = bookingRepository.countActiveGuestsNative(
    today, 
    "CHECKED_IN", 
    hotelId
);
```

### Solución Definitiva: Asegurar Datos Consistentes

1. **Crear datos de prueba** con un seeder/script
2. **Validar multi-tenancy** en todas las capas
3. **Monitorear logs** de Hibernate para detectar filtros problemáticos
4. **Tests de integración** que validen el conteo con datos reales

---

## 📝 Archivos Modificados

1. **BookingRepository.java**
   - Agregada query `countActiveGuestsTodayExplicit`
   - Query explícita sin BETWEEN

2. **BookingService.java**
   - Usa la query explícita
   - Agregado log de debug

3. **BookingDebugController.java** (NUEVO)
   - Endpoint de diagnóstico completo
   - Múltiples contadores para comparar

---

## 🧪 Ejecutar Tests

```bash
# Test del repository
./mvnw test -Dtest=BookingRepositoryTest

# Test del service
./mvnw test -Dtest=BookingServiceDashboardTest

# Ver logs detallados
./mvnw test -Dtest=BookingRepositoryTest -X
```

---

## 📞 Contacto de Soporte

Si el problema persiste después de seguir todos los pasos:

1. Exportar el resultado del endpoint de debug
2. Exportar el resultado de la query SQL directa
3. Verificar logs de Hibernate con los parámetros
4. Compartir la configuración de `application.properties`

---

**Fecha:** 2025-11-25  
**Versión:** backend-0.0.1-SNAPSHOT  
**Estado:** Solución implementada, pendiente validación con datos reales

