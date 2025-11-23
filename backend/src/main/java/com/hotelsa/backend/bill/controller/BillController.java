package com.hotelsa.backend.bill.controller;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.bill.dto.BillRequestDTO;
import com.hotelsa.backend.bill.dto.BillResponseDTO;
import com.hotelsa.backend.bill.enums.BillStatus;
import com.hotelsa.backend.bill.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/{bookingId}")
    @AdminOnly
    public ResponseEntity<BillResponseDTO> createBill(@PathVariable Long bookingId, @Valid @RequestBody BillRequestDTO dto) {
        BillResponseDTO response = billService.createBill(bookingId, dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<BillResponseDTO>> getAll() {
        return ResponseEntity.ok(billService.findAll());
    }

    @PatchMapping("/{id}/status")
    @AdminOnly
    public ResponseEntity<BillResponseDTO> updateStatus(@PathVariable Long id, @RequestBody BillStatus status) {
        return ResponseEntity.ok(billService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
