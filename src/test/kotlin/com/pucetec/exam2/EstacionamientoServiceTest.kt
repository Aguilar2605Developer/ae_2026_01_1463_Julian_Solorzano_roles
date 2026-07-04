package com.pucetec.exam2.services

import com.pucetec.exam2.dto.EspacioDTO
import com.pucetec.exam2.dto.SalidaRequestDTO
import com.pucetec.exam2.dto.TicketRequestDTO
import com.pucetec.exam2.dto.TicketResponseDTO
import com.pucetec.exam2.entities.Espacio
import com.pucetec.exam2.entities.Ticket
import com.pucetec.exam2.exceptions.BusinessValidationException
import com.pucetec.exam2.exceptions.EstacionamientoLlenoException
import com.pucetec.exam2.exceptions.ResourceNotFoundException
import com.pucetec.exam2.mappers.EspacioMapper
import com.pucetec.exam2.mappers.TicketMapper
import com.pucetec.exam2.repositories.EspacioRepository
import com.pucetec.exam2.repositories.TicketRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class EstacionamientoServiceTest {

    @Mock
    private lateinit var espacioRepository: EspacioRepository

    @Mock
    private lateinit var ticketRepository: TicketRepository

    @Mock
    private lateinit var ticketMapper: TicketMapper

    @Mock
    private lateinit var espacioMapper: EspacioMapper

    @InjectMocks
    private lateinit var estacionamientoService: EstacionamientoService

    private lateinit var espacio: Espacio
    private lateinit var ticket: Ticket

    private fun <T> anyObject(): T {
        any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    @BeforeEach
    fun setUp() {
        espacio = Espacio(id = 1L, codigo = "A1", disponible = true)
        ticket = Ticket(id = 1L, placa = "ABC123", fechaEntrada = LocalDateTime.now(), espacio = espacio)
    }

    // --- registrarEntrada ---

    @Test
    fun `registrarEntrada exitosa`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1")
        val responseDTO = TicketResponseDTO(1L, "ABC123", ticket.fechaEntrada, null, "A1")

        `when`(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        `when`(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(false)
        `when`(espacioRepository.findByCodigo("A1")).thenReturn(espacio)
        `when`(ticketRepository.save(anyObject())).thenReturn(ticket)
        `when`(ticketMapper.toResponseDTO(ticket)).thenReturn(responseDTO)

        val resultado = estacionamientoService.registrarEntrada(request)

        assertEquals("ABC123", resultado.placa)
        assertEquals("A1", resultado.codigoEspacio)
        assertFalse(espacio.disponible)
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando el estacionamiento esta lleno`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1")

        `when`(espacioRepository.countByDisponibleFalse()).thenReturn(20)

        val excepcion = assertThrows(EstacionamientoLlenoException::class.java) {
            estacionamientoService.registrarEntrada(request)
        }
        assertTrue(excepcion.message!!.contains("capacidad máxima"))
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando la placa ya esta dentro`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1")

        `when`(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        `when`(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(true)

        val excepcion = assertThrows(BusinessValidationException::class.java) {
            estacionamientoService.registrarEntrada(request)
        }
        assertTrue(excepcion.message!!.contains("ya se encuentra dentro"))
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando el espacio no existe`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "Z9")

        `when`(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        `when`(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(false)
        `when`(espacioRepository.findByCodigo("Z9")).thenReturn(null)

        assertThrows(ResourceNotFoundException::class.java) {
            estacionamientoService.registrarEntrada(request)
        }
    }

    @Test
    fun `registrarEntrada lanza excepcion cuando el espacio ya esta ocupado`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1")
        val espacioOcupado = Espacio(id = 1L, codigo = "A1", disponible = false)

        `when`(espacioRepository.countByDisponibleFalse()).thenReturn(5)
        `when`(ticketRepository.existsByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(false)
        `when`(espacioRepository.findByCodigo("A1")).thenReturn(espacioOcupado)

        val excepcion = assertThrows(BusinessValidationException::class.java) {
            estacionamientoService.registrarEntrada(request)
        }
        assertTrue(excepcion.message!!.contains("ya está ocupado"))
    }

    // --- registrarSalida ---

    @Test
    fun `registrarSalida exitosa`() {
        val request = SalidaRequestDTO(placa = "ABC123")
        val responseDTO = TicketResponseDTO(1L, "ABC123", ticket.fechaEntrada, LocalDateTime.now(), "A1")

        `when`(ticketRepository.findByPlacaAndFechaSalidaIsNull("ABC123")).thenReturn(ticket)
        `when`(ticketRepository.save(anyObject())).thenReturn(ticket)
        `when`(ticketMapper.toResponseDTO(ticket)).thenReturn(responseDTO)

        val resultado = estacionamientoService.registrarSalida(request)

        assertEquals("ABC123", resultado.placa)
        assertNotNull(ticket.fechaSalida)
        assertTrue(espacio.disponible)
    }

    @Test
    fun `registrarSalida lanza excepcion cuando el ticket no existe`() {
        val request = SalidaRequestDTO(placa = "XYZ999")

        `when`(ticketRepository.findByPlacaAndFechaSalidaIsNull("XYZ999")).thenReturn(null)

        assertThrows(ResourceNotFoundException::class.java) {
            estacionamientoService.registrarSalida(request)
        }
    }

    // --- consultarDisponibles ---

    @Test
    fun `consultarDisponibles retorna solo los espacios disponibles`() {
        val espacioDisponible = Espacio(id = 1L, codigo = "A1", disponible = true)
        val espacioOcupado = Espacio(id = 2L, codigo = "A2", disponible = false)
        val dto = EspacioDTO(codigo = "A1", disponible = true)

        `when`(espacioRepository.findAll()).thenReturn(listOf(espacioDisponible, espacioOcupado))
        `when`(espacioMapper.toDTO(espacioDisponible)).thenReturn(dto)

        val resultado = estacionamientoService.consultarDisponibles()

        assertEquals(1, resultado.size)
        assertEquals("A1", resultado[0].codigo)
    }
}