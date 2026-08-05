package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltrosPostulantesResponseDTO {
    private List<SimpleDTO> distritos;
    private List<SimpleDTO> modalidades;
    private List<SimpleDTO> habilidades;
    private List<SimpleDTO> herramientas;
}
