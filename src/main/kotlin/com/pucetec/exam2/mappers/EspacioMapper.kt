package com.pucetec.exam2.mappers

import com.pucetec.exam2.dto.EspacioDTO
import com.pucetec.exam2.entities.Espacio
import org.springframework.stereotype.Component

@Component
class EspacioMapper {
    // Convierte una entidad Espacio a EspacioDTO para exponer su estado
    fun toDTO(espacio: Espacio): EspacioDTO {
        return EspacioDTO(
            codigo = espacio.codigo,
            disponible = espacio.disponible
        )
    }
}