package com.hotelsa.backend.common.exception;

import com.hotelsa.backend.addon.exception.AddonAccessDeniedException;
import com.hotelsa.backend.addon.exception.AddonNotFoundException;
import com.hotelsa.backend.bill.exception.BillAccessDeniedException;
import com.hotelsa.backend.bill.exception.BillNotFoundException;
import com.hotelsa.backend.billaddon.exception.BillAddonNotFoundException;
import com.hotelsa.backend.booking.exception.BookingAccessDeniedException;
import com.hotelsa.backend.booking.exception.BookingAddonNotFoundException;
import com.hotelsa.backend.booking.exception.BookingNotFoundException;
import com.hotelsa.backend.guest.exception.GuestAccessDeniedException;
import com.hotelsa.backend.guest.exception.GuestNotFoundException;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.room.exception.RoomAccessDeniedException;
import com.hotelsa.backend.room.exception.RoomNotFoundException;
import com.hotelsa.backend.user.exception.UserAccessDeniedException;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== EXCEPCIONES DE ACCESO DENEGADO (MULTI-TENANCY) ====================

    /**
     * Manejador genérico para todas las excepciones de acceso denegado a recursos de otro tenant.
     * Retorna 403 FORBIDDEN para indicar que el recurso existe pero el usuario no tiene acceso.
     */
    @ExceptionHandler(ResourceAccessDeniedException.class)
    public ResponseEntity<ApiError> handleResourceAccessDenied(ResourceAccessDeniedException ex, HttpServletRequest request) {
        log.warn("🚫 Intento de acceso a recurso de otro tenant: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(BookingAccessDeniedException.class)
    public ResponseEntity<ApiError> handleBookingAccessDenied(BookingAccessDeniedException ex, HttpServletRequest request) {
        log.warn("🚫 Intento de acceso a reserva de otro hotel: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(GuestAccessDeniedException.class)
    public ResponseEntity<ApiError> handleGuestAccessDenied(GuestAccessDeniedException ex, HttpServletRequest request) {
        log.warn("🚫 Intento de acceso a huésped de otro hotel: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(RoomAccessDeniedException.class)
    public ResponseEntity<ApiError> handleRoomAccessDenied(RoomAccessDeniedException ex, HttpServletRequest request) {
        log.warn("🚫 Intento de acceso a habitación de otro hotel: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(AddonAccessDeniedException.class)
    public ResponseEntity<ApiError> handleAddonAccessDenied(AddonAccessDeniedException ex, HttpServletRequest request) {
        log.warn("🚫 Intento de acceso a addon de otro hotel: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(BillAccessDeniedException.class)
    public ResponseEntity<ApiError> handleBillAccessDenied(BillAccessDeniedException ex, HttpServletRequest request) {
        log.warn("🚫 Intento de acceso a factura de otro hotel: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(UserAccessDeniedException.class)
    public ResponseEntity<ApiError> handleUserAccessDenied(UserAccessDeniedException ex, HttpServletRequest request) {
        log.warn("🚫 Intento de acceso a usuario de otro hotel: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // ==================== EXCEPCIONES DE AUTENTICACIÓN ====================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getFieldError() != null ? ex.getFieldError().getDefaultMessage() : "Error de validación";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(com.hotelsa.backend.common.exception.BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(com.hotelsa.backend.common.exception.BadRequestException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiError> handleRoomNotFound(RoomNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(GuestNotFoundException.class)
    public ResponseEntity<ApiError> handleGuestNotFound(GuestNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AddonNotFoundException.class)
    public ResponseEntity<ApiError> handleAddonNotFound(AddonNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiError> handleBookingNotFound(BookingNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BookingAddonNotFoundException.class)
    public ResponseEntity<ApiError> handleBookingAddonNotFound(BookingAddonNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<ApiError> handleBillNotFound(BillNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BillAddonNotFoundException.class)
    public ResponseEntity<ApiError> handleBillAddonNotFound(BillAddonNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ApiError> handleHotelNotFound(HotelNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
