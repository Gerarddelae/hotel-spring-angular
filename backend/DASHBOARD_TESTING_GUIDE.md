# 🧪 GUÍA DE PRUEBA RÁPIDA - DASHBOARD ENDPOINTS

## Requisitos Previos
1. Backend ejecutándose en `http://localhost:8080`
2. Token JWT válido (obtener desde `/auth/login`)
3. Base de datos con algunos registros de prueba

## 🔑 Obtener Token JWT

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

Guarda el token que recibes en la respuesta.

## 📝 Variables de Entorno (opcional)

```bash
# Linux/Mac
export JWT_TOKEN="tu_token_aqui"

# Windows PowerShell
$JWT_TOKEN="tu_token_aqui"
```

---

## 🧪 Pruebas de Endpoints

### 1. BOOKING - Count by Status

```bash
curl -X GET "http://localhost:8080/bookings/count-by-status" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "total": 45,
  "pending": 12,
  "confirmed": 18,
  "checkedIn": 15
}
```

---

### 2. BOOKING - Active Guests Count

```bash
curl -X GET "http://localhost:8080/bookings/active-guests-count" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "count": 15
}
```

---

### 3. ROOM - Occupied Count

```bash
curl -X GET "http://localhost:8080/rooms/occupied-count" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "count": 28
}
```

---

### 4. ROOM - Dashboard Summary

```bash
curl -X GET "http://localhost:8080/rooms/dashboard-summary" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
[
  {
    "roomId": 1,
    "number": "101",
    "status": "OCCUPIED",
    "roomTypeName": "SINGLE",
    "currentBookingId": 45
  },
  {
    "roomId": 2,
    "number": "102",
    "status": "AVAILABLE",
    "roomTypeName": "DOUBLE",
    "currentBookingId": null
  },
  {
    "roomId": 3,
    "number": "103",
    "status": "MAINTENANCE",
    "roomTypeName": "SUITE",
    "currentBookingId": null
  }
]
```

---

### 5. ROOM - Status Options

```bash
curl -X GET "http://localhost:8080/rooms/status-options" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
["AVAILABLE", "OCCUPIED", "MAINTENANCE"]
```

---

### 6. BILL - Total Revenue

```bash
curl -X GET "http://localhost:8080/api/bills/total-revenue" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "total": 125000.00,
  "currency": "USD"
}
```

---

### 7. BILL - Total Revenue Today

```bash
curl -X GET "http://localhost:8080/api/bills/total-revenue/today" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "total": 5600.00,
  "currency": "USD"
}
```

---

### 8. BILL - Total Revenue Month

```bash
curl -X GET "http://localhost:8080/api/bills/total-revenue/month" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "total": 48500.00,
  "currency": "USD"
}
```

---

## 🔍 Validaciones

### Caso 1: Sin Token (401 Unauthorized)
```bash
curl -X GET "http://localhost:8080/bookings/count-by-status"
```
**Esperado:** Error 401

### Caso 2: Token Inválido (403 Forbidden)
```bash
curl -X GET "http://localhost:8080/bookings/count-by-status" \
  -H "Authorization: Bearer token_invalido"
```
**Esperado:** Error 403

### Caso 3: Usuario sin Permisos (403 Forbidden)
Si el endpoint requiere ADMIN y el usuario es EMPLOYEE.

---

## 🐛 Troubleshooting

### Error: "Cannot resolve symbol"
- El IDE necesita un refresh del proyecto
- Solución: Ejecuta `./mvnw clean compile`

### Error: Connection Refused
- El backend no está ejecutándose
- Solución: Inicia el backend con `./mvnw spring-boot:run`

### Error: 401 Unauthorized
- Token JWT expirado o inválido
- Solución: Obtén un nuevo token con `/auth/login`

### Error: Empty Response (0 records)
- Base de datos vacía
- Solución: Inserta datos de prueba

---

## 📊 Script de Prueba Completo (PowerShell)

```powershell
# Configurar variables
$BASE_URL = "http://localhost:8080"
$USERNAME = "admin"
$PASSWORD = "password"

# 1. Login y obtener token
$loginResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body (@{username=$USERNAME; password=$PASSWORD} | ConvertTo-Json)

$TOKEN = $loginResponse.token

Write-Host "✅ Token obtenido: $($TOKEN.Substring(0,20))..."

# 2. Probar endpoints
$headers = @{
  "Authorization" = "Bearer $TOKEN"
  "Content-Type" = "application/json"
}

Write-Host "`n📊 Booking Status Count:"
$bookingCount = Invoke-RestMethod -Uri "$BASE_URL/bookings/count-by-status" `
  -Method GET -Headers $headers
$bookingCount | ConvertTo-Json

Write-Host "`n👥 Active Guests:"
$activeGuests = Invoke-RestMethod -Uri "$BASE_URL/bookings/active-guests-count" `
  -Method GET -Headers $headers
$activeGuests | ConvertTo-Json

Write-Host "`n🏠 Occupied Rooms:"
$occupiedRooms = Invoke-RestMethod -Uri "$BASE_URL/rooms/occupied-count" `
  -Method GET -Headers $headers
$occupiedRooms | ConvertTo-Json

Write-Host "`n🗂️ Room Dashboard:"
$roomDashboard = Invoke-RestMethod -Uri "$BASE_URL/rooms/dashboard-summary" `
  -Method GET -Headers $headers
$roomDashboard | ConvertTo-Json

Write-Host "`n💰 Total Revenue:"
$totalRevenue = Invoke-RestMethod -Uri "$BASE_URL/api/bills/total-revenue" `
  -Method GET -Headers $headers
$totalRevenue | ConvertTo-Json

Write-Host "`n✅ Todas las pruebas completadas!"
```

---

## 📊 Script de Prueba Completo (Bash)

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
USERNAME="admin"
PASSWORD="password"

# 1. Login y obtener token
echo "🔐 Obteniendo token JWT..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "✅ Token obtenido"

# 2. Probar endpoints
echo -e "\n📊 Booking Status Count:"
curl -s -X GET "$BASE_URL/bookings/count-by-status" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n👥 Active Guests:"
curl -s -X GET "$BASE_URL/bookings/active-guests-count" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n🏠 Occupied Rooms:"
curl -s -X GET "$BASE_URL/rooms/occupied-count" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n🗂️ Room Dashboard:"
curl -s -X GET "$BASE_URL/rooms/dashboard-summary" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n💰 Total Revenue:"
curl -s -X GET "$BASE_URL/api/bills/total-revenue" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n✅ Todas las pruebas completadas!"
```

---

## 🎯 Checklist de Validación

- [ ] Todos los endpoints responden con status 200
- [ ] Los DTOs tienen la estructura correcta
- [ ] Los números tienen sentido (no negativos)
- [ ] El multi-tenancy funciona (cada hotel ve solo sus datos)
- [ ] La seguridad JWT está activa
- [ ] Los LEFT JOIN funcionan correctamente (currentBookingId puede ser null)
- [ ] Las sumas de revenue son correctas

---

## 📝 Notas

- Los endpoints de `bills` usan el prefijo `/api/bills` (diferente a bookings y rooms)
- `currentBookingId` puede ser `null` si la habitación no tiene booking activo
- Los conteos usan solo estados activos (PENDING, CONFIRMED, CHECKED_IN)
- Los revenue filtran solo facturas con status PAID

---

**Creado:** 2025-11-25  
**Proyecto:** Hotel Spring Angular Backend

