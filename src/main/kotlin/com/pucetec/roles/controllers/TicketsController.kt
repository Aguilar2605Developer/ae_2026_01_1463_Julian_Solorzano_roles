package com.pucetec.roles.controllers

import com.pucetec.roles.dto.SalidaRequestDTO
import com.pucetec.roles.dto.TicketRequestDTO
import com.pucetec.roles.dto.TicketResponseDTO
import com.pucetec.roles.services.EstacionamientoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tickets")
class TicketsController(private val estacionamientoService: EstacionamientoService) {

    @PostMapping("/entry")
    @PreAuthorize("hasRole('USER')")
    fun registrarEntrada(@RequestBody request: TicketRequestDTO): ResponseEntity<TicketResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(estacionamientoService.registrarEntrada(request))
    }

    @PostMapping("/exit")
    @PreAuthorize("hasRole('USER')")
    fun registrarSalida(@RequestBody request: SalidaRequestDTO): ResponseEntity<TicketResponseDTO> {
        return ResponseEntity.ok(estacionamientoService.registrarSalida(request))
    }
}