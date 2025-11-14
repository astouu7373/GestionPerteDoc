package com.Smtd.GestionPerteDoc.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeDocStatsDTO {
    private String typeDocument;
    private Long total;
}
