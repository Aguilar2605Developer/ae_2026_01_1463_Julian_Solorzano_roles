package com.pucetec.exam2.repositories

import com.pucetec.exam2.entities.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    fun findByEspacioIdAndFechaSalidaIsNull(espacioId: Long): Ticket?
    fun existsByPlacaAndFechaSalidaIsNull(placa: String): Boolean
}