package com.numerum.sdui.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmpruntRequest {
    private Long utilisateurId;
    private Long livreId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
