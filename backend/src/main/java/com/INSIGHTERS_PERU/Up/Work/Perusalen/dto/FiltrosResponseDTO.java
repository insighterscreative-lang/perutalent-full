package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltrosResponseDTO {
    private List<SimpleDTO> categorias;
    private List<SimpleDTO> modalidades;
    private List<SimpleDTO> experiencias;
    private List<RangoSalarioDTO> rangosSalario;
}
