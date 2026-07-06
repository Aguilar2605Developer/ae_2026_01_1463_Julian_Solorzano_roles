package com.pucetec.roles.services

import com.pucetec.roles.dto.EspacioDTO
import com.pucetec.roles.dto.SalidaRequestDTO
import com.pucetec.roles.dto.TicketRequestDTO
import com.pucetec.roles.dto.TicketResponseDTO
import com.pucetec.roles.entities.Espacio
import com.pucetec.roles.entities.Ticket
import com.pucetec.roles.exceptions.BusinessValidationException
import com.pucetec.roles.exceptions.EstacionamientoLlenoException
import com.pucetec.roles.exceptions.ResourceNotFoundException
import com.pucetec.roles.mappers.EspacioMapper
import com.pucetec.roles.mappers.TicketMapper
import com.pucetec.roles.repositories.EspacioRepository
import com.pucetec.roles.repositories.TicketRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class EstacionamientoServiceTest {

    @Mock
    lateinit var espacioRepository: EspacioRepository

    @Mock
    lateinit var ticketRepository: TicketRepository

    @Mock
    lateinit var ticketMapper: TicketMapper

    @Mock
    lateinit var espacioMapper: EspacioMapper

    private lateinit var service: EstacionamientoService

    @BeforeEach
    fun setUp() {
        service = EstacionamientoService(
            espacioRepository,
            ticketRepository,
            ticketMapper,
            espacioMapper
        )
    }

    // ---------- consultarDisponibles ----------

    @Test
    fun `consultarDisponibles devuelve solo los espacios disponibles mapeados a DTO`() {
        val disponible = Espacio(codigo = "A1", disponible = true)
        val ocupado = Espacio(codigo = "A2", disponible = false)
        val dto = EspacioDTO(codigo = "A1", disponible = true)

        whenever(espacioRepository.findAll()).thenReturn(listOf(disponible, ocupado))
        whenever(espacioMapper.toDTO(disponible)).thenReturn(dto)

        val resultado = service.consultarDisponibles()

        assertEquals(1, resultado.size)
        assertEquals("A1", resultado[0].codigo)
        verify(espacioMapper, never()).toDTO(ocupado)
    }

    // ---------- crearEspacio ----------

    @Test
    fun `crearEspacio guarda el espacio cuando el codigo no existe`() {
        val request = EspacioDTO(codigo = "B2", disponible = true)
        val entidad = Espacio(codigo = "B2", disponible = true)
        val guardado = Espacio(id = 1, codigo = "B2", disponible = true)
        val respuesta = EspacioDTO(codigo = "B2", disponible = true)

        whenever(espacioRepository.findByCodigo("B2")).thenReturn(null)
        whenever(espacioMapper.toEntity(request)).thenReturn(entidad)
        whenever(espacioRepository.save(entidad)).thenReturn(guardado)
        whenever(espacioMapper.toDTO(guardado)).thenReturn(respuesta)

        val resultado = service.crearEspacio(request)

        assertEquals("B2", resultado.codigo)
        verify(espacioRepository).save(entidad)
    }

    @Test
    fun `crearEspacio lanza excepcion cuando el codigo ya existe`() {
        val request = EspacioDTO(codigo = "B2", disponible = true)
        val existente = Espacio(codigo = "B2", disponible = true)

        whenever(espacioRepository.findByCodigo("B2")).thenReturn(existente)

        assertThrows(BusinessValidationException::class.java) {
            service.crearEspacio(request)
        }
        verify(espacioRepository, never()).save(any())
    }

    // ---------- registrarEntrada ----------

    @Test
    fun `registrarEntrada crea el ticket cuando todo es valido`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "B2")
        val espacio = Espacio(codigo = "B2", disponible = true)
        val ticketGuardado = Ticket(id = 1, placa = "ABC123", espacio = espacio)
        val respuesta = TicketResponseDTO(
            id = 1,
            placa = "ABC123",
            fechaEntrada = LocalDateTime.now(),
            fechaSalida = null,
            codigoEspacio = "B2"
        )

        whenever(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        whenever(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(false)
        whenever(espacioRepository.findByCodigo("B2")).thenReturn(espacio)
        whenever(ticketRepository.save(any())).thenReturn(ticketGuardado)
        whenever(ticketMapper.toResponseDTO(ticketGuardado)).thenReturn(respuesta)

        val resultado = service.registrarEntrada(request)

        assertEquals("ABC123", resultado.placa)
        assertEquals(false, espacio.disponible)
        verify(ticketRepository).save(any())
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando el estacionamiento esta lleno`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "B2")
        whenever(espacioRepository.countByDisponibleFalse()).thenReturn(20)

        assertThrows(EstacionamientoLlenoException::class.java) {
            service.registrarEntrada(request)
        }
        verify(ticketRepository, never()).save(any())
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando la placa ya esta dentro`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "B2")

        whenever(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        whenever(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(true)

        assertThrows(BusinessValidationException::class.java) {
            service.registrarEntrada(request)
        }
        verify(espacioRepository, never()).findByCodigo(any())
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando el espacio no existe`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "ZZ")

        whenever(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        whenever(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(false)
        whenever(espacioRepository.findByCodigo("ZZ")).thenReturn(null)

        assertThrows(ResourceNotFoundException::class.java) {
            service.registrarEntrada(request)
        }
        verify(ticketRepository, never()).save(any())
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando el espacio ya esta ocupado`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "B2")
        val espacioOcupado = Espacio(codigo = "B2", disponible = false)

        whenever(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        whenever(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(false)
        whenever(espacioRepository.findByCodigo("B2")).thenReturn(espacioOcupado)

        assertThrows(BusinessValidationException::class.java) {
            service.registrarEntrada(request)
        }
        verify(ticketRepository, never()).save(any())
    }

    // ---------- registrarSalida ----------

    @Test
    fun `registrarSalida cierra el ticket cuando existe uno activo`() {
        val request = SalidaRequestDTO(placa = "ABC123")
        val espacio = Espacio(codigo = "B2", disponible = false)
        val ticket = Ticket(id = 1, placa = "ABC123", espacio = espacio)
        val respuesta = TicketResponseDTO(
            id = 1,
            placa = "ABC123",
            fechaEntrada = ticket.fechaEntrada,
            fechaSalida = LocalDateTime.now(),
            codigoEspacio = "B2"
        )

        whenever(ticketRepository.findByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(ticket)
        whenever(ticketRepository.save(any())).thenReturn(ticket)
        whenever(ticketMapper.toResponseDTO(ticket)).thenReturn(respuesta)

        val resultado = service.registrarSalida(request)

        assertEquals("ABC123", resultado.placa)
        assertEquals(true, espacio.disponible)
        verify(ticketRepository).save(ticket)
    }

    @Test
    fun `registrarSalida lanza excepcion cuando no hay ticket activo`() {
        val request = SalidaRequestDTO(placa = "ABC123")

        whenever(ticketRepository.findByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(null)

        assertThrows(ResourceNotFoundException::class.java) {
            service.registrarSalida(request)
        }
        verify(ticketRepository, never()).save(any())
    }
}