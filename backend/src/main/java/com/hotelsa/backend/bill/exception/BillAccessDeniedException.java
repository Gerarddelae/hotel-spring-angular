package com.hotelsa.backend.bill.exception;

import com.hotelsa.backend.common.exception.ResourceAccessDeniedException;

/**
 * Excepción lanzada cuando un usuario intenta acceder a una factura
 * que no pertenece a su hotel.
 */
public class BillAccessDeniedException extends ResourceAccessDeniedException {

    public BillAccessDeniedException(Long billId) {
        super("Factura", billId);
    }

    public BillAccessDeniedException(String message) {
        super(message);
    }
}
