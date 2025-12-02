# Módulo de Configuración del Hotel

Este módulo permite a los administradores del sistema gestionar la información básica del hotel.

## Características

- ✅ Visualización de información actual del hotel
- ✅ Edición de datos básicos del hotel
- ✅ Validación de campos (nombre obligatorio, formato de teléfono)
- ✅ Mensajes de éxito y error
- ✅ Actualización en tiempo real del nombre del hotel en el layout
- ✅ Diseño responsive y consistente con el resto del proyecto

## Estructura de Archivos

```
settings/
├── models/
│   ├── hotel-update-request.interface.ts  # Interface para actualizar hotel
│   ├── hotel-response.interface.ts        # Interface de respuesta del hotel
│   └── index.ts                           # Exports centralizados
├── services/
│   └── hotel.service.ts                   # Servicio para gestionar API del hotel
├── settings.component.ts                  # Componente principal
├── settings.component.html                # Template del formulario
├── settings.component.css                 # Estilos del componente
└── settings.routes.ts                     # Configuración de rutas
```

## Endpoints Utilizados

### GET /api/hotels/{id}
Obtiene la información del hotel por ID

**Headers requeridos:**
- `Authorization: Bearer {token}`

**Respuesta exitosa (200):**
```json
{
  "id": 1,
  "name": "Hotel Example",
  "address": "Calle Principal 123",
  "city": "Bogotá",
  "country": "Colombia",
  "phone": "+573001234567",
  "description": "Descripción del hotel"
}
```

### PUT /api/hotels/{id}
Actualiza completamente la información del hotel

**Headers requeridos:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Body de la petición:**
```json
{
  "name": "Hotel Actualizado",
  "address": "Nueva Dirección 456",
  "city": "Medellín",
  "country": "Colombia",
  "phone": "+573009876543",
  "description": "Nueva descripción del hotel"
}
```

**Respuesta exitosa (200):**
```json
{
  "id": 1,
  "name": "Hotel Actualizado",
  "address": "Nueva Dirección 456",
  "city": "Medellín",
  "country": "Colombia",
  "phone": "+573009876543",
  "description": "Nueva descripción del hotel"
}
```

## Validaciones

### Campos Obligatorios
- `name`: Nombre del hotel (requerido)

### Validaciones de Formato
- `phone`: Debe coincidir con el patrón `^\+?[0-9]{7,15}$`
  - Puede comenzar con `+` (opcional)
  - Debe contener entre 7 y 15 dígitos
  - Ejemplo válido: `+573001234567`

## Permisos

Este módulo solo es accesible para usuarios con rol `ADMIN`.

## Navegación

El módulo es accesible desde:
- **Sidebar**: Click en "Configuración" (icono de engranaje)
- **URL directa**: `/settings`

## Comportamiento del Formulario

1. **Carga inicial**: Al entrar, carga automáticamente los datos actuales del hotel
2. **Edición**: Los campos pueden ser modificados libremente
3. **Validación**: Los campos se validan al perder el foco y al intentar guardar
4. **Guardar**: Envía todos los datos al backend (actualización completa)
5. **Restablecer**: Restaura los valores originales cargados del servidor
6. **Actualización del Layout**: Al cambiar el nombre, se actualiza automáticamente en el header

## Mensajes de Usuario

### Éxito
- "¡Información del hotel actualizada exitosamente!"
- Se muestra durante 5 segundos

### Error
- "No se pudo cargar la información del hotel. Por favor, intenta de nuevo."
- "No se pudo actualizar la información. Por favor, verifica los datos."
- Los mensajes de error se muestran durante 5 segundos

## Estilo Visual

El componente sigue la guía de estilo del proyecto:
- Diseño con Tailwind CSS
- Soporte para modo claro/oscuro
- Responsive (mobile-first)
- Iconos de PrimeIcons
- Gradientes azul-púrpura en botones principales
- Mensajes con bordes de color y iconos
