package com.pucetec.exam2.services

import com.pucetec.exam2.dto.EspacioDTO
import com.pucetec.exam2.dto.SalidaRequestDTO
import com.pucetec.exam2.dto.TicketRequestDTO
import com.pucetec.exam2.dto.TicketResponseDTO
import com.pucetec.exam2.entities.Ticket
import com.pucetec.exam2.exceptions.BusinessValidationException
import com.pucetec.exam2.exceptions.EstacionamientoLlenoException
import com.pucetec.exam2.exceptions.ResourceNotFoundException
import com.pucetec.exam2.mappers.EspacioMapper
import com.pucetec.exam2.mappers.TicketMapper
import com.pucetec.exam2.repositories.EspacioRepository
import com.pucetec.exam2.repositories.TicketRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EstacionamientoService(
    private val espacioRepository: EspacioRepository,
    private val ticketRepository: TicketRepository,
    private val ticketMapper: TicketMapper,
    private val espacioMapper: EspacioMapper
) {
    private val logger = LoggerFactory.getLogger(EstacionamientoService::class.java)

    private val capacidadMaxima = 20

    fun consultarDisponibles(): List<EspacioDTO> {
        logger.info("Consultando espacios disponibles")
        return espacioRepository.findAll()
            .filter { it.disponible }
            .map { espacioMapper.toDTO(it) }
    }

    @Transactional
    fun registrarEntrada(request: TicketRequestDTO): TicketResponseDTO {
        logger.info("Registrando entrada para placa ${request.placa} en espacio ${request.codigoEspacio}")

        val ocupados = espacioRepository.countByDisponibleFalse()
        if (ocupados >= capacidadMaxima) {
            logger.warn("Estacionamiento lleno: $ocupados/$capacidadMaxima")
            throw EstacionamientoLlenoException("El estacionamiento ha alcanzado su capacidad máxima de $capacidadMaxima vehículos.")
        }

        if (ticketRepository.existsByPlacaAndFechaSalidaIsNull(request.placa)) {
            logger.warn("Placa duplicada: ${request.placa} ya está dentro del estacionamiento")
            throw BusinessValidationException("El vehículo con placa ${request.placa} ya se encuentra dentro del estacionamiento.")
        }

        val espacio = espacioRepository.findByCodigo(request.codigoEspacio)
            ?: throw ResourceNotFoundException("Espacio no encontrado")

        if (!espacio.disponible) {
            logger.warn("Espacio ${request.codigoEspacio} ya está ocupado")
            throw BusinessValidationException("El espacio ${request.codigoEspacio} ya está ocupado.")
        }

        espacio.disponible = false
        val ticket = Ticket(placa = request.placa, espacio = espacio)

        val ticketGuardado = ticketRepository.save(ticket)
        logger.info("Entrada registrada: ticket ${ticketGuardado.id} para placa ${request.placa}")

        return ticketMapper.toResponseDTO(ticketGuardado)
    }

    @Transactional
    fun registrarSalida(request: SalidaRequestDTO): TicketResponseDTO {
        logger.info("Registrando salida para placa ${request.placa}")

        val ticket = ticketRepository.findByPlacaAndFechaSalidaIsNull(request.placa)
            ?: throw ResourceNotFoundException("No hay un ticket activo para la placa ${request.placa}")

        ticket.fechaSalida = LocalDateTime.now()
        ticket.espacio.disponible = true

        val ticketGuardado = ticketRepository.save(ticket)
        logger.info("Salida registrada: ticket ${ticketGuardado.id} para placa ${request.placa}")

        return ticketMapper.toResponseDTO(ticketGuardado)
    }
}