package com.balbuena.Scout.controller;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.service.PresidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presidents")
@RequiredArgsConstructor
@Tag(name = "2. Presidents")
public class PresidentController {

    private final PresidentService presidentService;

    @PostMapping
    @Operation(summary = "Cadastrar president", description = "Disponivel apenas na fase REGISTRATION. Cada president inicia com R$ 100.")
    public ResponseEntity<Response.President> create(@Valid @RequestBody Request.PresidentCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presidentService.create(request));
    }

    @PostMapping("/defaults")
    @Operation(summary = "Create the ten default clubs and presidents")
    public ResponseEntity<List<Response.President>> createDefaults() {
        return ResponseEntity.status(HttpStatus.CREATED).body(presidentService.createDefaults());
    }

    @GetMapping
    @Operation(summary = "Listar todos os presidents")
    public ResponseEntity<List<Response.President>> findAll() {
        return ResponseEntity.ok(presidentService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar president por ID")
    public ResponseEntity<Response.President> findById(@PathVariable Long id) {
        return ResponseEntity.ok(presidentService.findById(id));
    }
}
