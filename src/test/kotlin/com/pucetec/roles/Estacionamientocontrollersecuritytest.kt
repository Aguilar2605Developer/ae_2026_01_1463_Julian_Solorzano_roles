package com.pucetec.roles.controllers

import tools.jackson.databind.ObjectMapper
import com.pucetec.roles.dto.EspacioDTO
import com.pucetec.roles.dto.TicketRequestDTO
import com.pucetec.roles.dto.TicketResponseDTO
import com.pucetec.roles.security.SecurityConfig
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

// Test de seguridad/web: verifica que la autorización por rol funcione a nivel HTTP,
// simulando un JWT con las authorities ROLE_ADMIN / ROLE_USER
@WebMvcTest(EstacionamientoController::class)
@Import(SecurityConfig::class)
class EstacionamientoControllerSecurityTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    // El service se mockea: este test NO valida lógica de negocio, solo autorización HTTP
    @MockitoBean
    lateinit var estacionamientoService: EstacionamientoService

    // ---------- GET /disponibles: público, no requiere rol ----------

    @Test
    fun `GET disponibles responde 200 sin autenticacion`() {
        whenever(estacionamientoService.consultarDisponibles()).thenReturn(emptyList())

        mockMvc.perform(get("/api/estacionamiento/disponibles"))
            .andExpect(status().isOk)
    }

    // ---------- POST /api/estacionamiento (crear espacio): solo ADMIN ----------

    @Test
    fun `POST crear espacio sin token responde 401`() {
        val body = objectMapper.writeValueAsString(EspacioDTO(codigo = "A1", disponible = true))

        mockMvc.perform(
            post("/api/estacionamiento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `un USER no puede crear espacios y recibe 403`() {
        val body = objectMapper.writeValueAsString(EspacioDTO(codigo = "A1", disponible = true))

        mockMvc.perform(
            post("/api/estacionamiento")
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
            post("/api/estacionamiento")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated)
    }

    // ---------- POST /api/estacionamiento/entrada: solo USER ----------

    @Test
    fun `POST entrada sin token responde 401`() {
        val body = objectMapper.writeValueAsString(TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1"))

        mockMvc.perform(
            post("/api/estacionamiento/entrada")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `un ADMIN no puede registrar entrada y recibe 403`() {
        val body = objectMapper.writeValueAsString(TicketRequestDTO(placa = "ABC123", codigoEspacio = "A1"))

        mockMvc.perform(
            post("/api/estacionamiento/entrada")
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
            post("/api/estacionamiento/entrada")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated)
    }
}