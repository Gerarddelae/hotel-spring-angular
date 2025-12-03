package com.hotelsa.backend.prediction.controller;

import com.hotelsa.backend.prediction.dto.BatchPredictionResponseDTO;
import com.hotelsa.backend.prediction.dto.TrainingDataDTO;
import com.hotelsa.backend.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {
    
    private final PredictionService predictionService;
    
    /**
     * Predice cancelaciones para todas las reservas pendientes (CONFIRMED, PENDING)
     * GET /api/predictions/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<BatchPredictionResponseDTO> predictPendingBookings() {
        log.info("Solicitud de predicción para reservas pendientes");
        BatchPredictionResponseDTO response = predictionService.predictPendingBookings();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Predice cancelaciones para reservas en un rango de fechas de check-in
     * GET /api/predictions/by-date-range?startDate=2025-12-01&endDate=2025-12-31
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<BatchPredictionResponseDTO> predictByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Solicitud de predicción para rango de fechas: {} a {}", startDate, endDate);
        BatchPredictionResponseDTO response = predictionService.predictByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Predice cancelaciones para una lista específica de booking IDs
     * POST /api/predictions/by-ids
     * Body: [1, 2, 3, 4, 5]
     */
    @PostMapping("/by-ids")
    public ResponseEntity<BatchPredictionResponseDTO> predictByIds(
            @RequestBody List<Long> bookingIds) {
        log.info("Solicitud de predicción para {} booking IDs", bookingIds.size());
        BatchPredictionResponseDTO response = predictionService.predictByIds(bookingIds);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Exporta datos históricos para entrenamiento del modelo ML
     * GET /api/predictions/training-data
     */
    @GetMapping("/training-data")
    public ResponseEntity<List<TrainingDataDTO>> getTrainingData() {
        log.info("Solicitud de datos de entrenamiento");
        List<TrainingDataDTO> trainingData = predictionService.extractTrainingData();
        return ResponseEntity.ok(trainingData);
    }
    
    /**
     * Verifica el estado del servicio ML (Flask API)
     * GET /api/predictions/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkMLServiceHealth() {
        log.info("Verificando estado del servicio ML");
        Map<String, String> health = predictionService.checkMLServiceHealth();
        return ResponseEntity.ok(health);
    }
}
