package com.pucetec.exam2.mappers

import com.pucetec.exam2.dto.TicketResponseDTO
import com.pucetec.exam2.entities.Ticket
import org.springframework.stereotype.Component

@Component
class TicketMapper {
    // Convierte una entidad Ticket a un TicketResponseDTO para el cliente
    fun toResponseDTO(ticket: Ticket): TicketResponseDTO {
        return TicketResponseDTO(
            id = ticket.id,
            placa = ticket.placa,
            fechaEntrada = ticket.fechaEntrada,
            fechaSalida = ticket.fechaSalida,
            codigoEspacio = ticket.espacio.codigo
        )
    }
}