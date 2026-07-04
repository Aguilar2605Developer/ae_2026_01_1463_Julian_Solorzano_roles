package com.pucetec.exam2.services

import com.pucetec.exam2.dto.TicketRequestDTO
import com.pucetec.exam2.dto.TicketResponseDTO
import com.pucetec.exam2.entities.Ticket
import com.pucetec.exam2.exceptions.BusinessValidationException
import com.pucetec.exam2.exceptions.ResourceNotFoundException
import com.pucetec.exam2.mappers.TicketMapper
import com.pucetec.exam2.repositories.EspacioRepository
import com.pucetec.exam2.repositories.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EstacionamientoService(
    private val espacioRepository: EspacioRepository,
    private val ticketRepository: TicketRepository,
    private val ticketMapper: TicketMapper
) {
    private val CAPACIDAD_MAXIMA = 20

    @Transactional
    fun registrarEntrada(request: TicketRequestDTO): TicketResponseDTO {
        // Validación: Capacidad máxima
        val ocupados = espacioRepository.countByDisponibleFalse()
        if (ocupados >= CAPACIDAD_MAXIMA) {
            throw BusinessValidationException("El estacionamiento ha alcanzado su capacidad máxima de $CAPACIDAD_MAXIMA vehículos.")
        }

        // Validación: Vehículo duplicado
        if (ticketRepository.existsByPlacaAndFechaSalidaIsNull(request.placa)) {
            throw BusinessValidationException("El vehículo con placa ${request.placa} ya se encuentra dentro del estacionamiento.")
        }

        val espacio = espacioRepository.findByCodigo(request.codigoEspacio)
            ?: throw ResourceNotFoundException("Espacio no encontrado")

        if (!espacio.disponible) {
            throw BusinessValidationException("El espacio ${request.codigoEspacio} ya está ocupado.")
        }

        espacio.disponible = false
        val ticket = Ticket(placa = request.placa, espacio = espacio)

        return ticketMapper.toResponseDTO(ticketRepository.save(ticket))
    }

    @Transactional
    fun registrarSalida(codigoEspacio: String): TicketResponseDTO {
        val espacio = espacioRepository.findByCodigo(codigoEspacio)
            ?: throw ResourceNotFoundException("Espacio no encontrado")

        val ticket = ticketRepository.findByEspacioIdAndFechaSalidaIsNull(espacio.id)
            ?: throw BusinessValidationException("No hay un vehículo activo en este espacio.")

        ticket.fechaSalida = LocalDateTime.now()
        espacio.disponible = true

        return ticketMapper.toResponseDTO(ticketRepository.save(ticket))
    }
}