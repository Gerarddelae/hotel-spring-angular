package com.hotelsa.backend.prediction.client;

import com.hotelsa.backend.prediction.dto.BatchPredictionRequestDTO;
import com.hotelsa.backend.prediction.dto.PredictionFeatureDTO;
import com.hotelsa.backend.prediction.dto.PredictionResultDTO;
import com.hotelsa.backend.prediction.exception.MLServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MLApiClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${ml.api.url:http://localhost:5000}")
    private String mlApiUrl;
    
    /**
     * Realiza una predicción individual
     */
    public PredictionResultDTO predictSingle(PredictionFeatureDTO features) {
        log.debug("Llamando a ML API para predicción individual: bookingId={}", features.getBookingId());
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<PredictionFeatureDTO> entity = new HttpEntity<>(features, headers);
            
            ResponseEntity<PredictionResultDTO> response = restTemplate.exchange(
                mlApiUrl + "/predict",
                HttpMethod.POST,
                entity,
                PredictionResultDTO.class
            );
            
            log.debug("Predicción recibida exitosamente para bookingId={}", features.getBookingId());
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Error al llamar a ML API para predicción individual: {}", e.getMessage());
            throw new MLServiceUnavailableException("No se pudo conectar con el servicio de ML", e);
        }
    }
    
    /**
     * Realiza predicciones en batch (más eficiente para múltiples reservas)
     */
    public List<PredictionResultDTO> predictBatch(BatchPredictionRequestDTO request) {
        log.info("Llamando a ML API para predicción batch: {} reservas", request.getBookings().size());
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<BatchPredictionRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<List<PredictionResultDTO>> response = restTemplate.exchange(
                mlApiUrl + "/predict/batch",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<PredictionResultDTO>>() {}
            );
            
            log.info("Predicción batch completada exitosamente: {} resultados", 
                response.getBody() != null ? response.getBody().size() : 0);
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("Error al llamar a ML API para predicción batch: {}", e.getMessage());
            // Retornar fallback con predicciones por defecto
            return buildFallbackResponse(request);
        }
    }
    
    /**
     * Verifica el estado del servicio ML
     */
    public Map<String, Object> healthCheck() {
        log.debug("Verificando estado del servicio ML");
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                mlApiUrl + "/health",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            return response.getBody();
            
        } catch (RestClientException e) {
            log.warn("Servicio ML no disponible: {}", e.getMessage());
            return Map.of("status", "unavailable", "error", e.getMessage());
        }
    }
    
    /**
     * Construye respuestas por defecto cuando el servicio ML no está disponible
     */
    private List<PredictionResultDTO> buildFallbackResponse(BatchPredictionRequestDTO request) {
        log.warn("Usando respuesta fallback para {} reservas", request.getBookings().size());
        
        return request.getBookings().stream()
            .map(b -> PredictionResultDTO.builder()
                .bookingId(b.getBookingId())
                .cancellationProbability(0.0)
                .willCancel(false)
                .riskLevel("UNKNOWN")
                .build())
            .collect(Collectors.toList());
    }
}
