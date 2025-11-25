package com.hotelsa.backend.bill.mapper;

import com.hotelsa.backend.bill.dto.BillRequestDTO;
import com.hotelsa.backend.bill.dto.BillResponseDTO;
import com.hotelsa.backend.bill.model.Bill;
import com.hotelsa.backend.billaddon.mapper.BillAddonMapper;
import com.hotelsa.backend.billaddon.entity.BillAddon;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.room.model.Room;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillMapper {

    private final BillAddonMapper billAddonMapper;

    public BillMapper(BillAddonMapper billAddonMapper) {
        this.billAddonMapper = billAddonMapper;
    }

    public Bill fromRequestDto(BillRequestDTO dto) {
        Bill bill = Bill.builder()
                .notes(dto.getNotes())
                .status(dto.getStatus() == null ? com.hotelsa.backend.bill.enums.BillStatus.UNPAID : dto.getStatus())
                .paymentMethod(dto.getPaymentMethod())
                .build();
        return bill;
    }

    public BillResponseDTO fromEntity(Bill entity) {
        List<BillAddon> addons = entity.getAddons();
        Booking booking = entity.getBooking();

        // Inicializar valores por defecto
        Long guestId = null;
        String guestName = null;
        Long roomId = null;
        String roomNumber = null;
        java.time.LocalDate checkInDate = null;
        java.time.LocalDate checkOutDate = null;
        Integer nights = null;
        BigDecimal roomPricePerNight = null;
        BigDecimal accommodationSubtotal = null;

        // Extraer información del booking si existe
        if (booking != null) {
            checkInDate = booking.getCheckInDate();
            checkOutDate = booking.getCheckOutDate();

            // Calcular noches
            if (checkInDate != null && checkOutDate != null) {
                nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            }

            // Extraer información del huésped
            Guest guest = booking.getGuest();
            if (guest != null) {
                guestId = guest.getId();
                guestName = guest.getFullName();
            }

            // Extraer información de la habitación
            Room room = booking.getRoom();
            if (room != null) {
                roomId = room.getId();
                roomNumber = room.getNumber();

                // Calcular precio por noche y subtotal de alojamiento
                if (room.getPricePerNight() != null) {
                    roomPricePerNight = BigDecimal.valueOf(room.getPricePerNight());

                    if (nights != null && nights > 0) {
                        accommodationSubtotal = roomPricePerNight.multiply(BigDecimal.valueOf(nights));
                    }
                }
            }
        }

        // Calcular subtotal de addons
        BigDecimal addonsSubtotal = BigDecimal.ZERO;
        if (addons != null && !addons.isEmpty()) {
            addonsSubtotal = addons.stream()
                    .map(BillAddon::getTotalPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return BillResponseDTO.builder()
                .id(entity.getId())
                .bookingId(booking == null ? null : booking.getId())
                .guestId(guestId)
                .guestName(guestName)
                .roomId(roomId)
                .roomNumber(roomNumber)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .nights(nights)
                .roomPricePerNight(roomPricePerNight)
                .accommodationSubtotal(accommodationSubtotal)
                .addonsSubtotal(addonsSubtotal)
                .notes(entity.getNotes())
                .status(entity.getStatus() == null ? null : entity.getStatus().name())
                .paymentMethod(entity.getPaymentMethod() == null ? null : entity.getPaymentMethod().name())
                .createdAt(entity.getCreatedAt())
                .totalAmount(entity.getTotalAmount())
                .addons(billAddonMapper.fromEntityList(addons))
                .build();
    }

    public List<BillResponseDTO> fromEntityList(List<Bill> list) {
        return list == null ? java.util.Collections.emptyList() : list.stream().map(this::fromEntity).collect(Collectors.toList());
    }
}
