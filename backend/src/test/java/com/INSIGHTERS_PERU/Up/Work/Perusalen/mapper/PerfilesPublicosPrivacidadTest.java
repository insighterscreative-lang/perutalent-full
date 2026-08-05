package com.INSIGHTERS_PERU.Up.Work.Perusalen.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoPublicoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorPublicoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.UsuarioEmpleadorResponseDTO;

class PerfilesPublicosPrivacidadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void perfilPublicoEmpleadoNoExponeDatosPrivados() throws Exception {
        UsuarioEmpleadoResponseDTO completo = new UsuarioEmpleadoResponseDTO();
        completo.setIdEmpleado(1L);
        completo.setNombre("Ana");
        completo.setApellido("Pérez");
        completo.setTipoDoc("DNI");
        completo.setNumDoc("12345678");
        completo.setFechaNacimiento(LocalDate.of(1995, 1, 2));
        completo.setGenero("Femenino");
        completo.setNacionalidad("Peruana");
        completo.setTelefono("999999999");
        completo.setCorreo("ana@correo.com");
        completo.setCurriculum("cvs/perfiles/privado.pdf");
        completo.setDistrito("Miraflores");
        completo.setDescripcion("Desarrolladora web");
        completo.setHabilidades(List.of("Java"));
        completo.setCategorias(List.of("Tecnología"));
        completo.setHerramientas(List.of("Git"));
        completo.setModalidades(List.of("Remoto"));
        completo.setIdiomas(List.of("Español"));

        UsuarioEmpleadoPublicoResponseDTO publico =
                new UsuarioEmpleadoMapper().toPublicResponseDTO(completo);
        String json = objectMapper.writeValueAsString(publico);

        assertEquals("Ana", publico.getNombre());
        assertEquals("Miraflores", publico.getDistrito());
        assertFalse(json.contains("tipoDoc"));
        assertFalse(json.contains("numDoc"));
        assertFalse(json.contains("fechaNacimiento"));
        assertFalse(json.contains("genero"));
        assertFalse(json.contains("nacionalidad"));
        assertFalse(json.contains("telefono"));
        assertFalse(json.contains("correo"));
        assertFalse(json.contains("curriculum"));
        assertFalse(json.contains("idDistrito"));
    }

    @Test
    void perfilPublicoEmpleadorNoExponeDatosLegalesNiContacto() throws Exception {
        UsuarioEmpleadorResponseDTO completo = new UsuarioEmpleadorResponseDTO();
        completo.setIdEmpleador(2L);
        completo.setTipoEmpleador("Empresa");
        completo.setNombreComercial("Empresa Demo");
        completo.setRazonSocial("Empresa Demo S.A.C.");
        completo.setTipoDoc("RUC");
        completo.setNumDoc("20123456789");
        completo.setCorreo("cuenta@empresa.com");
        completo.setCorreoContacto("contacto@empresa.com");
        completo.setTelefonoContacto("987654321");
        completo.setSitioWeb("https://empresa.example");
        completo.setCategorias(List.of("Servicios"));
        completo.setModalidadesContratacion(List.of("Presencial"));

        UsuarioEmpleadorPublicoResponseDTO publico =
                new UsuarioEmpleadorMapper().toPublicResponseDTO(completo);
        String json = objectMapper.writeValueAsString(publico);

        assertEquals("Empresa Demo", publico.getNombreComercial());
        assertEquals("https://empresa.example", publico.getSitioWeb());
        assertFalse(json.contains("razonSocial"));
        assertFalse(json.contains("tipoDoc"));
        assertFalse(json.contains("numDoc"));
        assertFalse(json.contains("correo"));
        assertFalse(json.contains("correoContacto"));
        assertFalse(json.contains("telefonoContacto"));
    }
}
