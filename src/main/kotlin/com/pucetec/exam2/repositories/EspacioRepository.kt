package com.pucetec.exam2.repositories

import com.pucetec.exam2.entities.Espacio
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EspacioRepository : JpaRepository<Espacio, Long> {
    fun findByCodigo(codigo: String): Espacio?
    fun countByDisponibleFalse(): Int
}