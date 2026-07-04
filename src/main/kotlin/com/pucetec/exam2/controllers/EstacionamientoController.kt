package com.pucetec.exam2.controllers

import com.pucetec.exam2.dto.TicketRequestDTO
import com.pucetec.exam2.dto.TicketResponseDTO
import com.pucetec.exam2.services.EstacionamientoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/estacionamiento")
class EstacionamientoController(private val estacionamientoService: EstacionamientoService) {

    @GetMapping("/disponibles")
    fun consultarDisponibles(): ResponseEntity<String> {
        return ResponseEntity.ok("Servicio de consulta disponible")
    }

    @PostMapping("/entrada")
    fun registrarEntrada(@RequestBody request: TicketRequestDTO): ResponseEntity<TicketResponseDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(estacionamientoService.registrarEntrada(request))
    }

    @PutMapping("/salida/{codigoEspacio}")
    fun registrarSalida(@PathVariable codigoEspacio: String): ResponseEntity<TicketResponseDTO> {
        return ResponseEntity.ok(estacionamientoService.registrarSalida(codigoEspacio))
    }
}