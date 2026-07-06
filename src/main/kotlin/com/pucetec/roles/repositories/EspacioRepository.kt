package com.pucetec.roles.repositories

import com.pucetec.roles.entities.Espacio
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EspacioRepository : JpaRepository<Espacio, Long> {
    fun findByCodigo(codigo: String): Espacio?
    fun countByDisponibleFalse(): Int
}