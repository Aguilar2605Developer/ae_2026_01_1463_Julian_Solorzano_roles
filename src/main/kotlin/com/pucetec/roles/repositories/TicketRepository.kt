package com.pucetec.roles.repositories

import com.pucetec.roles.entities.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    fun findByPlacaAndFechaSalidaIsNull(placa: String): Ticket?
    fun existsByPlacaAndFechaSalidaIsNull(placa: String): Boolean
}