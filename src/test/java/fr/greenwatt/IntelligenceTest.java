package fr.greenwatt;

import fr.greenwatt.model.*;
import fr.greenwatt.service.PredictionService;
import fr.greenwatt.service.RecommandationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntelligenceTest {

    private Batiment batimentAvecHistorique() {
        Batiment b = new Maison("Test", new Localisation("Lyon", "", "", Localisation.ZoneClimatique.H2),
                100, 4, 4, false, false);
        b.setIdentifiant(1L);
        // 6 mois consécutifs avec tendance haussière
        for (int i = 0; i < 6; i++) {
            b.ajouterMesure(new Mesure(CategorieEnergie.ELECTRICITE,
                100 + i * 20, LocalDateTime.now().minusMonths(6 - i)));
        }
        return b;
    }

    @Test
    void prediction_produitProjectionCroissante() {
        var p = new PredictionService().projeter(List.of(batimentAvecHistorique()), 3);
        assertEquals(3, p.size(), "Doit projeter 3 mois");
        assertTrue(p.get(2).valeur() >= p.get(0).valeur(),
                "La tendance haussière doit être préservée par la régression linéaire");
    }

    @Test
    void prediction_videSiHistoriqueInsuffisant() {
        Batiment vide = new Maison("Vide", new Localisation(), 80, 2, 3, false, false);
        var p = new PredictionService().projeter(List.of(vide), 3);
        assertTrue(p.isEmpty());
    }

    @Test
    void recommandations_detectentSourceFossile() {
        Batiment b = batimentAvecHistorique();
        b.setSource(SourceEnergie.FOSSILE);
        var conseils = new RecommandationService().analyser(List.of(b));
        assertTrue(conseils.stream().anyMatch(c -> c.message().toLowerCase().contains("fossile")),
                "Doit recommander la bascule vers une source moins carbonée");
    }
}
