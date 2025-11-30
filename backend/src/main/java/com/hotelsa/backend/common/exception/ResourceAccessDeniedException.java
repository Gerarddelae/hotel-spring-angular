package com.hotelsa.backend.common.exception;

/**
 * Excepción base lanzada cuando un usuario intenta acceder a un recurso
 * que no pertenece a su hotel (violación de multi-tenancy).
 */
public class ResourceAccessDeniedException extends RuntimeException {

    private final String resourceType;
    private final Long resourceId;

    public ResourceAccessDeniedException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public ResourceAccessDeniedException(String resourceType, Long resourceId) {
        super(String.format("%s con ID %d no encontrado o no pertenece a tu hotel", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceAccessDeniedException(String resourceType, Long resourceId, String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }
}
