package com.hotelsa.backend.bill.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.bill.dto.BillRequestDTO;
import com.hotelsa.backend.bill.dto.BillResponseDTO;
import com.hotelsa.backend.bill.exception.BillAccessDeniedException;
import com.hotelsa.backend.bill.exception.BillNotFoundException;
import com.hotelsa.backend.bill.mapper.BillMapper;
import com.hotelsa.backend.bill.model.Bill;
import com.hotelsa.backend.bill.enums.BillStatus;
import com.hotelsa.backend.bill.enums.PaymentMethod;
import com.hotelsa.backend.billaddon.entity.BillAddon;
import com.hotelsa.backend.billaddon.entity.BillAddonId;
import com.hotelsa.backend.billaddon.mapper.BillAddonMapper;
import com.hotelsa.backend.billaddon.repository.BillAddonRepository;
import com.hotelsa.backend.bill.repository.BillRepository;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.bookingaddon.entity.BookingAddon;
import com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository;
import com.hotelsa.backend.room.model.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillMapper billMapper;
    private final BillAddonRepository billAddonRepository;

    private final BookingRepository bookingRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final BillAddonMapper billAddonMapper;
    private final AuthService authService;

    private Long getCurrentHotelId() {
        return authService.getCurrentHotelId();
    }

    @AdminOnly
    @Transactional
    public BillResponseDTO createBill(Long bookingId, BillRequestDTO dto) {
        Long currentHotelId = getCurrentHotelId();
        log.debug("Crear factura para booking {} en hotel {}", bookingId, currentHotelId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new com.hotelsa.backend.booking.exception.BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        if (currentHotelId != null && !currentHotelId.equals(booking.getHotelId())) {
            throw new com.hotelsa.backend.booking.exception.BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel");
        }

        Bill bill = billMapper.fromRequestDto(dto);
        bill.setBooking(booking);
        bill.setHotelId(currentHotelId);

        // Guardar primero la factura (sin addons)
        bill.setTotalAmount(BigDecimal.ZERO);
        Bill saved = billRepository.save(bill);

        // Obtener addons activos de la reserva y clonarlos ahora que tenemos el id de la factura
        List<BookingAddon> bookingAddons = bookingAddonRepository.findByIdBookingIdAndHotelId(bookingId, currentHotelId);

        List<BillAddon> persistedAddons = bookingAddons.stream().map(ba -> {
            BillAddonId id = new BillAddonId(saved.getId(), ba.getAddon().getId());
            BillAddon b = BillAddon.builder()
                    .id(id)
                    .bill(saved)
                    .addonId(ba.getAddon().getId())
                    .addonName(ba.getAddon().getName())
                    .addonDescription(ba.getAddon().getDescription())
                    .unitPrice(BigDecimal.valueOf(ba.getAddon().getPrice()))
                    .quantity(ba.getQuantity())
                    .hotelId(currentHotelId)
                    .build();
            return billAddonRepository.save(b);
        }).collect(Collectors.toList());

        // Calcular total = estadía (noches × precio_noche) + addons de la factura
        // NO usar booking.getTotalAmount() porque ya incluye addons del booking
        Room room = booking.getRoom();
        BigDecimal roomPricePerNight = room != null && room.getPricePerNight() != null
            ? BigDecimal.valueOf(room.getPricePerNight())
            : BigDecimal.ZERO;

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
            booking.getCheckInDate(),
            booking.getCheckOutDate()
        );

        BigDecimal accommodationSubtotal = roomPricePerNight.multiply(BigDecimal.valueOf(nights));

        BigDecimal addonsTotal = persistedAddons.stream()
                .map(ba -> ba.getTotalPrice() == null ? BigDecimal.ZERO : ba.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = accommodationSubtotal.add(addonsTotal);

        saved.setTotalAmount(total);
        saved.setAddons(persistedAddons);
        billRepository.save(saved);

        log.debug("💰 Bill total calculated: Accommodation={}×{}={}, Addons={}, Total={}",
            nights, roomPricePerNight, accommodationSubtotal, addonsTotal, total);

        // Recargar la factura con todas las relaciones
        Bill finalSaved = billRepository.findByIdWithRelations(saved.getId())
                .orElseThrow(() -> new BillNotFoundException("Error al recargar la factura creada"));

        log.debug("✅ Created bill {} for booking {} in hotel {}", finalSaved.getId(), bookingId, currentHotelId);
        return billMapper.fromEntity(finalSaved);
    }

    @Transactional(readOnly = true)
    public BillResponseDTO findById(Long id) {
        Bill bill = billRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BillNotFoundException("Factura no encontrada con ID: " + id));

        Long currentHotelId = getCurrentHotelId();
        if (currentHotelId != null && !currentHotelId.equals(bill.getHotelId())) {
            throw new BillAccessDeniedException(id);
        }

        return billMapper.fromEntity(bill);
    }

    @Transactional(readOnly = true)
    public List<BillResponseDTO> findAll() {
        List<Bill> bills = billRepository.findAllWithRelations();
        return bills.stream().map(billMapper::fromEntity).collect(Collectors.toList());
    }

    @AdminOnly
    @Transactional
    public BillResponseDTO updateStatus(Long billId, BillStatus status) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Factura no encontrada con ID: " + billId));

        Long currentHotelId = getCurrentHotelId();
        if (currentHotelId != null && !currentHotelId.equals(bill.getHotelId())) {
            throw new BillAccessDeniedException(billId);
        }

        bill.setStatus(status);
        Bill updated = billRepository.save(bill);
        log.debug("✅ Updated bill {} status to {}", updated.getId(), status);
        return billMapper.fromEntity(updated);
    }

    @AdminOnly
    @Transactional
    public BillResponseDTO updatePaymentMethod(Long billId, PaymentMethod paymentMethod) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Factura no encontrada con ID: " + billId));

        Long currentHotelId = getCurrentHotelId();
        if (currentHotelId != null && !currentHotelId.equals(bill.getHotelId())) {
            throw new BillAccessDeniedException(billId);
        }

        bill.setPaymentMethod(paymentMethod);
        Bill updated = billRepository.save(bill);
        log.debug("✅ Updated bill {} payment method to {}", updated.getId(), paymentMethod);
        return billMapper.fromEntity(updated);
    }

    @AdminOnly
    @Transactional
    public void delete(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundException("Factura no encontrada con ID: " + id));

        Long currentHotelId = getCurrentHotelId();
        if (currentHotelId != null && !currentHotelId.equals(bill.getHotelId())) {
            throw new BillAccessDeniedException(id);
        }

        bill.setDeleted(true);
        billRepository.save(bill);
        log.debug("🗑️ Soft deleted bill {} for hotel {}", bill.getId(), bill.getHotelId());
    }

    // Dashboard methods
    @Transactional(readOnly = true)
    public com.hotelsa.backend.bill.dto.RevenueDTO getTotalRevenue() {
        BigDecimal total = billRepository.sumTotalRevenue();
        return new com.hotelsa.backend.bill.dto.RevenueDTO(total, "USD");
    }

    @Transactional(readOnly = true)
    public com.hotelsa.backend.bill.dto.RevenueDTO getTotalRevenueToday() {
        java.time.LocalDate today = java.time.LocalDate.now();
        BigDecimal total = billRepository.sumRevenueByDate(today);
        return new com.hotelsa.backend.bill.dto.RevenueDTO(total, "USD");
    }

    @Transactional(readOnly = true)
    public com.hotelsa.backend.bill.dto.RevenueDTO getTotalRevenueMonth() {
        java.time.LocalDate today = java.time.LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        BigDecimal total = billRepository.sumRevenueByMonth(month, year);
        return new com.hotelsa.backend.bill.dto.RevenueDTO(total, "USD");
    }
}





