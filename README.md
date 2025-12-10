# 🏨 Maguestic - Sistema de Gestión Hotelera

<p align="center">
  <img src="https://img.shields.io/badge/Angular-20-red?style=for-the-badge&logo=angular" alt="Angular 20">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-green?style=for-the-badge&logo=springboot" alt="Spring Boot 3.5">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL 16">
  <img src="https://img.shields.io/badge/Python-3.x-yellow?style=for-the-badge&logo=python" alt="Python">
</p>

## 📋 Descripción

**Maguestic** es un sistema integral de gestión hotelera que permite administrar todas las operaciones de un hotel de manera eficiente. El proyecto está compuesto por tres módulos principales: un backend robusto desarrollado con Spring Boot, un frontend moderno con Angular, y una API de Machine Learning para predicción de cancelaciones de reservas.

## 🏗️ Arquitectura del Proyecto

```
Hotel_Spring_angular/
├── backend/          # API REST con Spring Boot 3.5 + Java 21
├── frontend/         # Aplicación SPA con Angular 20
└── ml-api/           # API de predicción con Flask + scikit-learn
```

## ✨ Características Principales




https://github.com/user-attachments/assets/e7b7a5c6-c180-41a2-aab9-35a89ebc17b9




### 🔐 Autenticación y Autorización
- Sistema de login y registro de usuarios
- Autenticación basada en JWT
- Control de acceso por roles (ADMIN, EMPLOYEE, USER)
- Guards de rutas para protección de recursos

### 📊 Dashboard Administrativo
- Visualización de métricas clave del hotel
- Estadísticas de ocupación y revenue
- Gráficos interactivos con PrimeNG

### 🛏️ Gestión de Habitaciones
- CRUD completo de habitaciones
- Tipos de habitación y precios
- Estado de disponibilidad

### 👥 Gestión de Huéspedes
- Registro y administración de huéspedes
- Historial de estancias
- Información de contacto

### 📅 Sistema de Reservas (Bookings)
- Creación y gestión de reservas
- Calendario de check-ins y check-outs
- Asignación de habitaciones
- Seguimiento del estado de reservas

### 💰 Facturación
- Generación de facturas
- Gestión de servicios adicionales (addons)
- Cálculo automático de totales
- Historial de pagos

### 🤖 Predicción de Cancelaciones (ML)
- Modelo de Machine Learning para predecir cancelaciones
- Análisis de riesgo (LOW, MEDIUM, HIGH)
- Predicciones individuales y en lote
- Basado en factores como:
  - Lead time
  - Precio promedio por habitación
  - Número de noches
  - Historial del huésped
  - Peticiones especiales

### 📆 Calendario de Eventos
- Vista de calendario para check-ins/check-outs
- Gestión visual de ocupación
- Integración con sistema de reservas

### 👤 Gestión de Usuarios
- Administración de empleados
- Asignación de roles y permisos
- Configuración de cuentas

## 🏢 Arquitectura Multi-Tenant

El sistema implementa una arquitectura **multi-tenant** que permite que múltiples hoteles (tenants) utilicen la misma instancia de la aplicación manteniendo sus datos completamente aislados y seguros.

### Características del Multi-Tenancy

#### 🔒 Aislamiento de Datos
Cada hotel opera como un tenant independiente con:
- **Aislamiento automático a nivel de base de datos**: Los datos de cada hotel están completamente separados
- **Filtrado transparente**: Las consultas SQL se filtran automáticamente por `hotel_id`
- **Seguridad por diseño**: Imposible acceder a datos de otros hoteles

#### 🔑 Identificación del Tenant
- El `hotelId` se extrae automáticamente del **token JWT** en cada petición
- El contexto del tenant se mantiene en `ThreadLocal` durante toda la petición
- No requiere pasar manualmente el `hotelId` en cada operación

#### ⚙️ Implementación Técnica

El multi-tenancy se implementa mediante tres componentes principales:

1. **TenantContext** (`ThreadLocal`)
   - Almacena el `hotelId` actual en el hilo de ejecución
   - Proporciona acceso thread-safe al tenant activo

2. **TenantFilter** (Servlet Filter)
   - Intercepta cada petición HTTP
   - Extrae el `hotelId` del token JWT
   - Inicializa el contexto del tenant antes de procesar la petición

3. **HibernateFilterAspect** (AOP)
   - Se aplica automáticamente en métodos `@Transactional`
   - Habilita filtros de Hibernate:
     - `tenantFilter`: Filtra por `hotel_id = :hotelId`
     - `deletedFilter`: Excluye registros marcados como eliminados
   - Garantiza que todas las consultas respeten el tenant activo

#### 📊 BaseEntity
Todas las entidades del sistema heredan de `BaseEntity`, que incluye:
```java
@Column(name = "hotel_id", nullable = false)
private Long hotelId;
```

Esta columna asegura que cada registro pertenezca a un hotel específico y permite el filtrado automático.

#### ✅ Ventajas
- **Escalabilidad**: Agregar nuevos hoteles no requiere cambios en el código
- **Mantenimiento simplificado**: Una sola base de datos para todos los tenants
- **Costos reducidos**: Infraestructura compartida entre múltiples hoteles
- **Seguridad**: Filtrado automático previene fugas de datos entre tenants
- **Transparencia**: Los servicios no necesitan código específico para multi-tenancy

## 🛠️ Tecnologías Utilizadas

### Backend
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 3.5.4 | Framework principal |
| Spring Security | - | Seguridad y autenticación |
| Spring Data JPA | - | Persistencia de datos |
| PostgreSQL | 16 | Base de datos |
| Lombok | - | Reducción de boilerplate |
| Maven | - | Gestión de dependencias |

### Frontend
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Angular | 20 | Framework frontend |
| Angular Material | 20 | Componentes UI |
| TailwindCSS | - | Framework CSS |
| RxJS | 7.8 | Programación reactiva |
| Angular Calendar | 0.32 | Componente de calendario |

### ML API
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Python | 3.x | Lenguaje de programación |
| Flask | 3.1.2 | Framework web |
| scikit-learn | 1.7.2 | Machine Learning |
| pandas | 2.3.3 | Manipulación de datos |
| NumPy | 2.3.5 | Computación numérica |

## 🚀 Instalación y Configuración

### Prerrequisitos
- Java 21+
- Node.js 18+
- Python 3.10+
- Docker y Docker Compose (opcional)
- PostgreSQL 16 (si no usa Docker)

### 1️⃣ Base de Datos (Docker)

```bash
cd backend
docker-compose up -d
```

Esto iniciará PostgreSQL con la siguiente configuración:
- **Host:** localhost
- **Puerto:** 5432
- **Base de datos:** hotel_db
- **Usuario:** hotel_user
- **Contraseña:** hotel_pass

### 2️⃣ Backend (Spring Boot)

```bash
cd backend

# Instalar dependencias y ejecutar
./mvnw spring-boot:run

# O en Windows
mvnw.cmd spring-boot:run
```

El backend estará disponible en `http://localhost:8080`

### 3️⃣ Frontend (Angular)

```bash
cd frontend

# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm start
```

El frontend estará disponible en `http://localhost:4200`

### 4️⃣ ML API (Flask)

```bash
cd ml-api

# Crear entorno virtual (recomendado)
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows

# Instalar dependencias
pip install -r requirements.txt

# Iniciar servidor
python app.py
```

La API de ML estará disponible en `http://localhost:5000`

## 📁 Estructura del Proyecto

### Backend
```
backend/src/main/java/com/hotelsa/backend/
├── addon/           # Servicios adicionales
├── aop/             # Aspectos (logging, etc.)
├── auth/            # Autenticación y JWT
├── bill/            # Facturación
├── billaddon/       # Relación factura-addon
├── booking/         # Reservas
├── bookingaddon/    # Relación reserva-addon
├── common/          # Clases comunes
├── config/          # Configuración
├── guest/           # Huéspedes
├── hotel/           # Información del hotel
├── prediction/      # Integración con ML
├── room/            # Habitaciones
├── tenant/          # Multi-tenancy
└── user/            # Usuarios
```

### Frontend
```
frontend/src/app/
├── auth/            # Autenticación
│   ├── login/
│   ├── register/
│   └── guards/
├── features/        # Módulos principales
│   ├── addons/
│   ├── billing/
│   ├── bookings/
│   ├── calendar-entries/
│   ├── dashboard/
│   ├── guests/
│   ├── rooms/
│   ├── settings/
│   └── users/
├── layout/          # Layout principal
└── shared/          # Componentes compartidos
```

## 🔌 Endpoints API

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar usuario

### Recursos principales
- `/api/rooms` - Gestión de habitaciones
- `/api/guests` - Gestión de huéspedes
- `/api/bookings` - Gestión de reservas
- `/api/bills` - Facturación
- `/api/addons` - Servicios adicionales
- `/api/users` - Gestión de usuarios

### ML API
- `GET /health` - Estado del servicio
- `POST /predict` - Predicción individual
- `POST /predict/batch` - Predicción en lote



## 🧪 Testing

### Backend
```bash
cd backend
./mvnw test
```

### Frontend
```bash
cd frontend
npm test
```

## 📝 Variables de Entorno

### Backend (application.properties)
```properties
spring.application.name=backend
ml.api.url=http://localhost:5000
```

Se pueden configurar perfiles adicionales:
- `application-dev.properties` - Desarrollo
- `application-test.properties` - Testing
- `application-docker.properties` - Docker

## 🤝 Contribución

1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit de cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

## 👥 Autores

- **Gerard Delae** - [GitHub](https://github.com/Gerarddelae)

---

<p align="center">
  Desarrollado con ❤️ para la gestión hotelera moderna
</p>

