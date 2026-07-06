package com.pucetec.roles.mappers

import com.pucetec.roles.dto.EspacioDTO
import com.pucetec.roles.entities.Espacio
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

    // Convierte un EspacioDTO recibido en la creación a una entidad Espacio
    fun toEntity(dto: EspacioDTO): Espacio {
        return Espacio(
            codigo = dto.codigo,
            disponible = dto.disponible
        )
    }
}