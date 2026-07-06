package com.pucetec.roles.controllers

import tools.jackson.databind.ObjectMapper
import com.pucetec.roles.dto.EspacioDTO
import com.pucetec.roles.dto.TicketRequestDTO
import com.pucetec.roles.dto.TicketResponseDTO
import com.pucetec.roles.config.SecurityConfig
import com.pucetec.roles.services.EstacionamientoService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(ParkingSpacesController::class, TicketsController::class)
@Import(SecurityConfig::class)
class EstacionamientoControllerSecurityTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var estacionamientoService: EstacionamientoService

    // ---------- GET /parking-spaces/available: público ----------

    @Test
    fun `GET available responde 200 sin autenticacion`() {
        whenever(estacionamientoService.consultarDisponibles()).thenReturn(emptyList())

        mockMvc.perform(get("/parking-spaces/available"))
            .andExpect(status().isOk)
    }

    // ---------- POST /parking-spaces: solo ADMIN ----------

    @Test
    fun `POST parking-spaces sin token responde 401`() {
        val body = objectMapper.writeValueAsString(EspacioDTO(codigo = "A1", disponible = true))

        mockMvc.perform(
            post("/parking-spaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `un USER no puede crear espacios y recibe 403`() {
        val body = objectMapper.writeValueAsString(EspacioDTO(codigo = "A1", disponible = true))

        mockMvc.perform(
            post("/parking-spaces")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `un ADMIN si puede crear espacios y recibe 201`() {
        val request = EspacioDTO(codigo = "A1", disponible = true)
        val body = objectMapper.writeValueAsString(request)

        whenever(estacionamientoService.crearEspacio(any())).thenReturn(request)

        mockMvc.perform(
            post("/parking-spaces")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated)
    }

    // ---------- POST /tickets/entry: solo USER ----------

    @Test
    fun `POST tickets entry sin token responde 401`() {
        val body = objectMapper.writeValueAsString(TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1"))

        mockMvc.perform(
            post("/tickets/entry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `un ADMIN no puede registrar entrada y recibe 403`() {
        val body = objectMapper.writeValueAsString(TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1"))

        mockMvc.perform(
            post("/tickets/entry")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `un USER si puede registrar entrada y recibe 201`() {
        val request = TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1")
        val body = objectMapper.writeValueAsString(request)
        val respuesta = TicketResponseDTO(
            id = 1,
            placa = "ABC123",
            fechaEntrada = LocalDateTime.now(),
            fechaSalida = null,
            codigoEspacio = "A1"
        )

        whenever(estacionamientoService.registrarEntrada(any())).thenReturn(respuesta)

        mockMvc.perform(
            post("/tickets/entry")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated)
    }
}