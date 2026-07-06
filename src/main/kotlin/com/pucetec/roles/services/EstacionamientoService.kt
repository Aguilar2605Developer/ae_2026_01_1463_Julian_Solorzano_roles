package com.pucetec.roles.services

import com.pucetec.roles.dto.EspacioDTO
import com.pucetec.roles.dto.SalidaRequestDTO
import com.pucetec.roles.dto.TicketRequestDTO
import com.pucetec.roles.dto.TicketResponseDTO
import com.pucetec.roles.entities.Ticket
import com.pucetec.roles.exceptions.BusinessValidationException
import com.pucetec.roles.exceptions.EstacionamientoLlenoException
import com.pucetec.roles.exceptions.ResourceNotFoundException
import com.pucetec.roles.mappers.EspacioMapper
import com.pucetec.roles.mappers.TicketMapper
import com.pucetec.roles.repositories.EspacioRepository
import com.pucetec.roles.repositories.TicketRepository
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
    // Logger para dejar trazabilidad de cada operación importante del negocio
    private val logger = LoggerFactory.getLogger(EstacionamientoService::class.java)

    // Capacidad máxima del estacionamiento, definida como variable del service
    // (no viene de la base de datos, es una regla de negocio fija)
    private val capacidadMaxima = 20

    // Devuelve solo los espacios que están libres, ya convertidos a DTO
    fun consultarDisponibles(): List<EspacioDTO> {
        logger.info("Consultando espacios disponibles")
        return espacioRepository.findAll()
            .filter { it.disponible }
            .map { espacioMapper.toDTO(it) }
    }

    // Crea un nuevo espacio de estacionamiento (solo lo puede hacer un ADMIN, según Spring Security)
    @Transactional
    fun crearEspacio(request: EspacioDTO): EspacioDTO {
        logger.info("Creando espacio con código ${request.codigo}")

        // Regla de negocio: no puede haber dos espacios con el mismo código
        val existente = espacioRepository.findByCodigo(request.codigo)
        if (existente != null) {
            logger.warn("Código duplicado: ${request.codigo} ya existe")
            throw BusinessValidationException("Ya existe un espacio con el código ${request.codigo}.")
        }


        val espacio = espacioMapper.toEntity(request)
        val espacioGuardado = espacioRepository.save(espacio)
        logger.info("Espacio creado: ${espacioGuardado.codigo}")

        return espacioMapper.toDTO(espacioGuardado)
    }

    // Registra el ingreso de un vehículo (solo lo puede hacer un USER, según Spring Security)
    @Transactional
    fun registrarEntrada(request: TicketRequestDTO): TicketResponseDTO {
        logger.info("Registrando entrada para placa ${request.placa} en espacio ${request.codigoEspacio}")

        // Validación 1: el estacionamiento no puede superar su capacidad máxima
        val ocupados = espacioRepository.countByDisponibleFalse()
        if (ocupados >= capacidadMaxima) {
            logger.warn("Estacionamiento lleno: $ocupados/$capacidadMaxima")
            throw EstacionamientoLlenoException("El estacionamiento ha alcanzado su capacidad máxima de $capacidadMaxima vehículos.")
        }

        // Validación 2: la misma placa no puede tener dos entradas activas a la vez
        if (ticketRepository.existsByPlacaAndFechaSalidaIsNull(request.placa)) {
            logger.warn("Placa duplicada: ${request.placa} ya está dentro del estacionamiento")
            throw BusinessValidationException("El vehículo con placa ${request.placa} ya se encuentra dentro del estacionamiento.")
        }

        // Validación 3: el espacio indicado debe existir
        val espacio = espacioRepository.findByCodigo(request.codigoEspacio)
            ?: throw ResourceNotFoundException("Espacio no encontrado")

        // Validación 4: el espacio debe estar libre (no ocupado por otro vehículo)
        if (!espacio.disponible) {
            logger.warn("Espacio ${request.codigoEspacio} ya está ocupado")
            throw BusinessValidationException("El espacio ${request.codigoEspacio} ya está ocupado.")
        }

        // Si pasó todas las validaciones: se ocupa el espacio y se crea el ticket
        espacio.disponible = false
        val ticket = Ticket(placa = request.placa, espacio = espacio)

        val ticketGuardado = ticketRepository.save(ticket)
        logger.info("Entrada registrada: ticket ${ticketGuardado.id} para placa ${request.placa}")

        return ticketMapper.toResponseDTO(ticketGuardado)
    }

    // Registra la salida de un vehículo (solo lo puede hacer un USER, según Spring Security)
    @Transactional
    fun registrarSalida(request: SalidaRequestDTO): TicketResponseDTO {
        logger.info("Registrando salida para placa ${request.placa}")

        val ticket = ticketRepository.findByPlacaAndFechaSalidaIsNull(request.placa)
            ?: throw ResourceNotFoundException("No hay un ticket activo para la placa ${request.placa}")

        // Cierra el ticket (marca la hora de salida) y libera el espacio ocupado
        ticket.fechaSalida = LocalDateTime.now()
        ticket.espacio.disponible = true

        val ticketGuardado = ticketRepository.save(ticket)
        logger.info("Salida registrada: ticket ${ticketGuardado.id} para placa ${request.placa}")

        return ticketMapper.toResponseDTO(ticketGuardado)
    }
}