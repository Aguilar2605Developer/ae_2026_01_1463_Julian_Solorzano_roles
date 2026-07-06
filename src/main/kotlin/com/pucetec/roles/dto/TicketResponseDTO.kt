package com.pucetec.roles.dto

import java.time.LocalDateTime


data class TicketResponseDTO(
    val id: Long,
    val placa: String,
    val fechaEntrada: LocalDateTime,
    val fechaSalida: LocalDateTime?,
    val codigoEspacio: String
)