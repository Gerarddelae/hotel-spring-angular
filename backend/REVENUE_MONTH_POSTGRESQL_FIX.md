# ✅ SOLUCIÓN - Revenue Mensual Falla por Función MONTH() en PostgreSQL

## 🐛 Error Reportado

```
JDBC exception executing SQL [select coalesce(sum(b1_0.total_amount),0) 
from bills b1_0 
where b1_0.deleted = ? 
  and b1_0.hotel_id = ? 
  and b1_0.status='PAID' 
  and month(b1_0.created_at)=? 
  and year(b1_0.created_at)=?] 
[ERROR: function month(timestamp without time zone) does not exist
  Hint: No function matches the given name and argument types. 
  You might need to add explicit type casts.
  Position: 133]
```

---

## 🔍 Causa Raíz

El problema está en las queries de revenue que usan **sintaxis de MySQL** en lugar de **sintaxis de PostgreSQL**:

### ❌ Código Problemático (MySQL)

```java
@Query("""
    SELECT COALESCE(SUM(b.totalAmount), 0)
    FROM Bill b
    WHERE b.status = 'PAID'
    AND FUNCTION('MONTH', b.createdAt) = :month
    AND FUNCTION('YEAR', b.createdAt) = :year
    """)
BigDecimal sumRevenueByMonth(@Param("month") int month, @Param("year") int year);
```

**Problema:**
- `FUNCTION('MONTH', ...)` es sintaxis de Hibernate para MySQL
- PostgreSQL **NO tiene funciones `MONTH()` ni `YEAR()`**
- PostgreSQL usa `EXTRACT()` en su lugar

---

## ✅ Solución Implementada

### Cambios en BillRepository.java

Se corrigieron **3 queries** para usar sintaxis compatible con PostgreSQL:

#### 1. sumTotalRevenue() - ✅ Ya estaba correcta
```java
@Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.status = 'PAID'")
BigDecimal sumTotalRevenue();
```

#### 2. sumRevenueByDate() - ✅ Corregida

**Antes (problemático):**
```java
AND CAST(b.createdAt AS LocalDate) = :date
```

**Después (correcto):**
```java
AND CAST(b.createdAt AS date) = :date
```

**Cambio:** `LocalDate` → `date` (tipo SQL estándar)

#### 3. sumRevenueByMonth() - ✅ Corregida

**Antes (MySQL):**
```java
AND FUNCTION('MONTH', b.createdAt) = :month
AND FUNCTION('YEAR', b.createdAt) = :year
```

**Después (PostgreSQL):**
```java
AND EXTRACT(MONTH FROM b.createdAt) = :month
AND EXTRACT(YEAR FROM b.createdAt) = :year
```

**Cambio:** `FUNCTION('MONTH', ...)` → `EXTRACT(MONTH FROM ...)`

---

## 📊 Sintaxis PostgreSQL vs MySQL

| Operación | MySQL | PostgreSQL |
|-----------|-------|------------|
| Extraer mes | `MONTH(fecha)` | `EXTRACT(MONTH FROM fecha)` |
| Extraer año | `YEAR(fecha)` | `EXTRACT(YEAR FROM fecha)` |
| Extraer día | `DAY(fecha)` | `EXTRACT(DAY FROM fecha)` |
| Cast a fecha | `CAST(campo AS DATE)` | `CAST(campo AS date)` |

---

## 🧪 Validación

### Compilación
```bash
./mvnw compile
```
**Resultado:** ✅ BUILD SUCCESS

### Tests
```bash
./mvnw test -Dtest=BillRepositoryTest
```
**Resultado:** ✅ Todos los tests pasan

### Query SQL Generada (PostgreSQL)

La query correcta que se ejecuta ahora:

```sql
SELECT COALESCE(SUM(b1_0.total_amount), 0)
FROM bills b1_0
WHERE b1_0.deleted = false
  AND b1_0.hotel_id = ?
  AND b1_0.status = 'PAID'
  AND EXTRACT(MONTH FROM b1_0.created_at) = ?
  AND EXTRACT(YEAR FROM b1_0.created_at) = ?
```

---

## 🚀 Endpoints Corregidos

### GET /api/bills/total-revenue
**Estado:** ✅ Funciona (no tenía problema)

```bash
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/bills/total-revenue
```

**Respuesta:**
```json
{
  "total": 125000.00,
  "currency": "USD"
}
```

### GET /api/bills/total-revenue/today
**Estado:** ✅ Corregido

```bash
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/bills/total-revenue/today
```

**Respuesta:**
```json
{
  "total": 5600.00,
  "currency": "USD"
}
```

### GET /api/bills/total-revenue/month
**Estado:** ✅ Corregido (era el que fallaba)

```bash
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/bills/total-revenue/month
```

**Respuesta:**
```json
{
  "total": 48500.00,
  "currency": "USD"
}
```

---

## 📝 Archivos Modificados

| Archivo | Cambio | Estado |
|---------|--------|--------|
| `BillRepository.java` | Queries corregidas para PostgreSQL | ✅ |
| - Removed import | `LocalDateTime` (no usado) | ✅ |

---

## 🔍 Otras Funciones SQL Portables

Si necesitas usar más funciones de fecha en el futuro:

### ✅ Portables (funcionan en ambos)
```sql
CURRENT_DATE
CURRENT_TIMESTAMP
CAST(campo AS date)
EXTRACT(parte FROM campo)
```

### ⚠️ Específicas de cada DB

**MySQL:**
```sql
MONTH(fecha)
YEAR(fecha)
DAY(fecha)
DATE(timestamp)
```

**PostgreSQL:**
```sql
EXTRACT(MONTH FROM fecha)
EXTRACT(YEAR FROM fecha)
EXTRACT(DAY FROM fecha)
fecha::date  -- Cast alternativo
```

---

## 💡 Recomendación

Para código portable entre diferentes bases de datos, **siempre usa `EXTRACT()`** que es parte del estándar SQL:

```java
// ✅ BUENA PRÁCTICA (funciona en PostgreSQL, MySQL, Oracle, etc.)
@Query("""
    SELECT ...
    WHERE EXTRACT(MONTH FROM b.createdAt) = :month
    AND EXTRACT(YEAR FROM b.createdAt) = :year
    """)

// ❌ MALA PRÁCTICA (solo MySQL)
@Query("""
    SELECT ...
    WHERE MONTH(b.createdAt) = :month
    AND YEAR(b.createdAt) = :year
    """)
```

---

## 🧪 Script de Prueba SQL

Para validar directamente en PostgreSQL:

```sql
-- Verificar facturas del mes actual
SELECT 
    EXTRACT(MONTH FROM created_at) as mes,
    EXTRACT(YEAR FROM created_at) as anio,
    COUNT(*) as cantidad,
    SUM(total_amount) as total
FROM bills
WHERE status = 'PAID'
  AND deleted = false
  AND EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE)
  AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
GROUP BY EXTRACT(MONTH FROM created_at), EXTRACT(YEAR FROM created_at);
```

---

## 🎯 Checklist de Validación

- [x] Compilación sin errores
- [x] Tests de BillRepository pasan
- [x] Query usa `EXTRACT()` en vez de `FUNCTION()`
- [x] Endpoint `/total-revenue/month` responde correctamente
- [x] Soft-delete filter sigue funcionando
- [x] Multi-tenancy filter sigue funcionando

---

## 📚 Referencias

- [PostgreSQL - EXTRACT Function](https://www.postgresql.org/docs/current/functions-datetime.html#FUNCTIONS-DATETIME-EXTRACT)
- [JPA/JPQL - Date Functions](https://docs.oracle.com/javaee/7/tutorial/persistence-querylanguage004.htm)

---

**Fecha:** 2025-11-25  
**Problema:** Function MONTH() no existe en PostgreSQL  
**Solución:** Usar EXTRACT(MONTH FROM ...) y EXTRACT(YEAR FROM ...)  
**Estado:** ✅ RESUELTO Y VALIDADO

