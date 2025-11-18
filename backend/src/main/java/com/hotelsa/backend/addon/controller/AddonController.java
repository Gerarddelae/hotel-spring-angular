package com.hotelsa.backend.addon.controller;

import com.hotelsa.backend.addon.dto.AddonRequest;
import com.hotelsa.backend.addon.dto.AddonResponse;
import com.hotelsa.backend.addon.service.AddonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;

    @PostMapping
    public ResponseEntity<AddonResponse> create(@Valid @RequestBody AddonRequest request) {
        AddonResponse response = addonService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddonResponse> getById(@PathVariable Long id) {
        AddonResponse response = addonService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AddonResponse>> getAll() {
        List<AddonResponse> list = addonService.findAll();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddonResponse> update(@PathVariable Long id, @Valid @RequestBody AddonRequest request) {
        AddonResponse response = addonService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<AddonResponse>> search(@RequestParam String name) {
        List<AddonResponse> list = addonService.searchByName(name);
        return ResponseEntity.ok(list);
    }
}

