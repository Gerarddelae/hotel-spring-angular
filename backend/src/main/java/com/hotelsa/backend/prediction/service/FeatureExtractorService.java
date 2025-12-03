package com.hotelsa.backend.prediction.service;

import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.prediction.dto.PredictionFeatureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class FeatureExtractorService {
    
    /**
     * Extrae las features de predicción desde una entidad Booking
     */
    public PredictionFeatureDTO extractFeatures(Booking booking) {
        LocalDate checkIn = booking.getCheckInDate();
        LocalDate checkOut = booking.getCheckOutDate();
        LocalDate bookingDate = getBookingDate(booking);
        
        // Calcular noches
        int[] nights = calculateNights(checkIn, checkOut);
        int totalNights = nights[0] + nights[1];
        
        // Calcular ADR (Average Daily Rate)
        BigDecimal adr = calculateADR(booking.getTotalAmount(), totalNights);
        
        // Obtener datos del huésped
        Guest guest = booking.getGuest();
        int previousCancellations = getGuestPreviousCancellations(guest);
        int totalBookings = getGuestTotalBookings(guest);
        int previousBookingsNotCanceled = Math.max(0, totalBookings - previousCancellations);
        boolean isRepeatedGuest = totalBookings > 1;
        
        // Contar addons/special requests
        int specialRequests = booking.getAddons() != null ? booking.getAddons().size() : 0;
        
        return PredictionFeatureDTO.builder()
            .bookingId(booking.getId())
            .leadTime(calculateLeadTime(bookingDate, checkIn))
            .avgPricePerRoom(adr)
            .noOfSpecialRequests(specialRequests)
            .arrivalDate(checkIn.getDayOfMonth())
            .arrivalMonth(checkIn.getMonthValue())
            .noOfWeekNights(nights[0])
            .noOfWeekendNights(nights[1])
            .previousCancellations(previousCancellations)
            .previousBookingsNotCanceled(previousBookingsNotCanceled)
            .isRepeatedGuest(isRepeatedGuest)
            .build();
    }
    
    /**
     * Calcula el número de noches de semana y fin de semana
     * @return array [weekNights, weekendNights]
     */
    public int[] calculateNights(LocalDate checkIn, LocalDate checkOut) {
        int weekNights = 0;
        int weekendNights = 0;
        LocalDate current = checkIn;
        
        while (current.isBefore(checkOut)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekendNights++;
            } else {
                weekNights++;
            }
            current = current.plusDays(1);
        }
        
        return new int[]{weekNights, weekendNights};
    }
    
    /**
     * Calcula el lead time (días entre reserva y check-in)
     */
    public int calculateLeadTime(LocalDate bookingDate, LocalDate checkInDate) {
        if (bookingDate == null || checkInDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(bookingDate, checkInDate);
        return Math.max(0, (int) days);
    }
    
    /**
     * Obtiene la fecha de booking, usando bookingLeadTime o createdAt
     */
    private LocalDate getBookingDate(Booking booking) {
        if (booking.getBookingLeadTime() != null) {
            return booking.getBookingLeadTime();
        }
        if (booking.getCreatedAt() != null) {
            return booking.getCreatedAt().toLocalDate();
        }
        return LocalDate.now();
    }
    
    /**
     * Calcula el Average Daily Rate (ADR)
     */
    private BigDecimal calculateADR(BigDecimal totalAmount, int totalNights) {
        if (totalAmount == null || totalNights <= 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.divide(new BigDecimal(totalNights), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Obtiene las cancelaciones previas del huésped (null-safe)
     */
    private int getGuestPreviousCancellations(Guest guest) {
        if (guest == null || guest.getPreviousCancellations() == null) {
            return 0;
        }
        return guest.getPreviousCancellations();
    }
    
    /**
     * Obtiene el total de bookings del huésped (null-safe)
     */
    private int getGuestTotalBookings(Guest guest) {
        if (guest == null || guest.getTotalBookingsClient() == null) {
            return 0;
        }
        return guest.getTotalBookingsClient();
    }
}
