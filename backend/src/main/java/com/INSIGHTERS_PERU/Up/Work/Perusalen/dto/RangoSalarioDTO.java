package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangoSalarioDTO {
    private BigDecimal min;
    private BigDecimal max;
    private String label;
}