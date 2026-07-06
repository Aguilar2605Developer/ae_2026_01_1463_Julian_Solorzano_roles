package com.pucetec.roles.controllers

import com.pucetec.roles.dto.EspacioDTO
import com.pucetec.roles.services.EstacionamientoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/parking-spaces")
class ParkingSpacesController(private val estacionamientoService: EstacionamientoService) {

    @GetMapping("/available")
    fun consultarDisponibles(): ResponseEntity<List<EspacioDTO>> {
        return ResponseEntity.ok(estacionamientoService.consultarDisponibles())
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun crearEspacio(@RequestBody request: EspacioDTO): ResponseEntity<EspacioDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(estacionamientoService.crearEspacio(request))
    }
}