package com.hotelsa.backend.prediction.service;

import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.prediction.client.MLApiClient;
import com.hotelsa.backend.prediction.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {
    
    private final BookingRepository bookingRepository;
    private final FeatureExtractorService featureExtractorService;
    private final MLApiClient mlApiClient;
    
    /**
     * Predice cancelaciones para todas las reservas pendientes (CONFIRMED, PENDING)
     */
    @Transactional(readOnly = true)
    public BatchPredictionResponseDTO predictPendingBookings() {
        log.info("Obteniendo predicciones para reservas pendientes");
        
        List<Booking> pendingBookings = bookingRepository.findByStatusIn(
            List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING)
        );
        
        if (pendingBookings.isEmpty()) {
            log.info("No hay reservas pendientes para predecir");
            return buildEmptyResponse();
        }
        
        return predictBatch(pendingBookings);
    }
    
    /**
     * Predice cancelaciones para reservas en un rango de fechas
     */
    @Transactional(readOnly = true)
    public BatchPredictionResponseDTO predictByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Obteniendo predicciones para reservas entre {} y {}", startDate, endDate);
        
        List<Booking> bookings = bookingRepository.findByCheckInDateBetweenAndStatusIn(
            startDate,
            endDate,
            List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING)
        );
        
        if (bookings.isEmpty()) {
            log.info("No hay reservas en el rango de fechas especificado");
            return buildEmptyResponse();
        }
        
        return predictBatch(bookings);
    }
    
    /**
     * Predice cancelaciones para una lista específica de booking IDs
     */
    @Transactional(readOnly = true)
    public BatchPredictionResponseDTO predictByIds(List<Long> bookingIds) {
        log.info("Obteniendo predicciones para {} reservas específicas", bookingIds.size());
        
        List<Booking> bookings = bookingRepository.findAllById(bookingIds);
        
        if (bookings.isEmpty()) {
            log.info("No se encontraron reservas con los IDs especificados");
            return buildEmptyResponse();
        }
        
        return predictBatch(bookings);
    }
    
    /**
     * Extrae datos históricos para entrenamiento del modelo ML
     */
    @Transactional(readOnly = true)
    public List<TrainingDataDTO> extractTrainingData() {
        log.info("Extrayendo datos de entrenamiento");
        
        List<Booking> historicalBookings = bookingRepository.findByStatusIn(
            List.of(BookingStatus.CHECKED_OUT, BookingStatus.CANCELLED, BookingStatus.COMPLETED)
        );
        
        log.info("Se encontraron {} reservas históricas para entrenamiento", historicalBookings.size());
        
        return historicalBookings.stream()
            .map(this::toTrainingData)
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica el estado del servicio ML
     */
    public Map<String, String> checkMLServiceHealth() {
        return mlApiClient.healthCheck();
    }
    
    /**
     * Realiza predicción batch para una lista de bookings
     */
    private BatchPredictionResponseDTO predictBatch(List<Booking> bookings) {
        log.debug("Procesando batch de {} reservas", bookings.size());
        
        // Extraer features de cada booking
        List<PredictionFeatureDTO> features = bookings.stream()
            .map(featureExtractorService::extractFeatures)
            .collect(Collectors.toList());
        
        // Llamar a ML API
        BatchPredictionRequestDTO request = BatchPredictionRequestDTO.builder()
            .bookings(features)
            .build();
        
        List<PredictionResultDTO> predictions = mlApiClient.predictBatch(request);
        
        // Mapear predicciones a bookings
        Map<Long, PredictionResultDTO> predictionMap = new HashMap<>();
        for (int i = 0; i < features.size(); i++) {
            predictionMap.put(features.get(i).getBookingId(), predictions.get(i));
        }
        
        // Construir respuesta enriquecida
        List<BookingPredictionDetailDTO> results = bookings.stream()
            .map(booking -> enrichWithPrediction(booking, predictionMap.get(booking.getId())))
            .sorted(Comparator.comparing(BookingPredictionDetailDTO::getCancellationProbability).reversed())
            .collect(Collectors.toList());
        
        return buildResponse(results);
    }
    
    /**
     * Enriquece la predicción con datos del booking
     */
    private BookingPredictionDetailDTO enrichWithPrediction(Booking booking, PredictionResultDTO prediction) {
        Guest guest = booking.getGuest();
        
        return BookingPredictionDetailDTO.builder()
            .bookingId(booking.getId())
            .guestName(guest != null ? guest.getFullName() : "N/A")
            .guestEmail(guest != null ? guest.getEmail() : "N/A")
            .roomNumber(booking.getRoom() != null ? booking.getRoom().getNumber() : "N/A")
            .checkInDate(booking.getCheckInDate())
            .checkOutDate(booking.getCheckOutDate())
            .totalAmount(booking.getTotalAmount())
            .cancellationProbability(prediction != null ? prediction.getCancellationProbability() : 0.0)
            .willCancel(prediction != null ? prediction.getWillCancel() : false)
            .riskLevel(prediction != null ? prediction.getRiskLevel() : "UNKNOWN")
            .build();
    }
    
    /**
     * Convierte un booking a DTO de datos de entrenamiento
     */
    private TrainingDataDTO toTrainingData(Booking booking) {
        PredictionFeatureDTO features = featureExtractorService.extractFeatures(booking);
        
        return TrainingDataDTO.builder()
            .leadTime(features.getLeadTime())
            .avgPricePerRoom(features.getAvgPricePerRoom())
            .noOfSpecialRequests(features.getNoOfSpecialRequests())
            .arrivalDate(features.getArrivalDate())
            .arrivalMonth(features.getArrivalMonth())
            .noOfWeekNights(features.getNoOfWeekNights())
            .noOfWeekendNights(features.getNoOfWeekendNights())
            .previousCancellations(features.getPreviousCancellations())
            .previousBookingsNotCanceled(features.getPreviousBookingsNotCanceled())
            .isRepeatedGuest(features.getIsRepeatedGuest() ? 1 : 0)
            .isCanceled(booking.getStatus() == BookingStatus.CANCELLED ? 1 : 0)
            .build();
    }
    
    /**
     * Construye respuesta vacía
     */
    private BatchPredictionResponseDTO buildEmptyResponse() {
        return BatchPredictionResponseDTO.builder()
            .predictions(Collections.emptyList())
            .totalBookings(0)
            .highRiskCount(0)
            .mediumRiskCount(0)
            .lowRiskCount(0)
            .averageCancellationProbability(0.0)
            .build();
    }
    
    /**
     * Construye respuesta con estadísticas
     */
    private BatchPredictionResponseDTO buildResponse(List<BookingPredictionDetailDTO> results) {
        long highRisk = results.stream()
            .filter(r -> "HIGH".equals(r.getRiskLevel()))
            .count();
        long mediumRisk = results.stream()
            .filter(r -> "MEDIUM".equals(r.getRiskLevel()))
            .count();
        long lowRisk = results.stream()
            .filter(r -> "LOW".equals(r.getRiskLevel()))
            .count();
        
        double avgProbability = results.stream()
            .mapToDouble(BookingPredictionDetailDTO::getCancellationProbability)
            .average()
            .orElse(0.0);
        
        return BatchPredictionResponseDTO.builder()
            .predictions(results)
            .totalBookings(results.size())
            .highRiskCount((int) highRisk)
            .mediumRiskCount((int) mediumRisk)
            .lowRiskCount((int) lowRisk)
            .averageCancellationProbability(Math.round(avgProbability * 10000.0) / 10000.0)
            .build();
    }
}
