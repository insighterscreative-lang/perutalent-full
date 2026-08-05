package com.INSIGHTERS_PERU.Up.Work.Perusalen.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheckEsPublicoYNoExponeDetalles() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void endpointDeCuentaRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/usuarios/cuenta"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void listadoDeOfertasRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/ofertas-laborales/paginadas"))
                .andExpect(status().is4xxClientError());
    }
}
