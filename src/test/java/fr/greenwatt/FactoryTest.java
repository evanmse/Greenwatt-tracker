package fr.greenwatt;

import fr.greenwatt.factory.BatimentFactory;
import fr.greenwatt.factory.BatimentFactory.SpecBatiment;
import fr.greenwatt.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {

    @Test
    void registry_contientLesCinqTypes() {
        assertTrue(BatimentFactory.typesDisponibles().containsAll(
                java.util.Set.of("Maison","Appartement","Bureau","LocalCommercial","BatimentUniversitaire","AutreBatiment")));
    }

    @Test
    void creer_produitLaBonneClasse() {
        SpecBatiment spec = new SpecBatiment("X", new Localisation(), 100, 4);
        assertInstanceOf(Maison.class,                BatimentFactory.creer("Maison", spec));
        assertInstanceOf(BatimentUniversitaire.class, BatimentFactory.creer("BatimentUniversitaire", spec));
        assertInstanceOf(AutreBatiment.class,         BatimentFactory.creer("AutreBatiment", spec));
    }

    @Test
    void typeInconnu_leveException() {
        assertThrows(IllegalArgumentException.class, () ->
                BatimentFactory.creer("Château", new SpecBatiment("X", new Localisation(), 1, 1)));
    }

    @Test
    void registry_estExtensibleSansToucherAuCode() {
        BatimentFactory.enregistrer("CustomTest", s ->
                new Maison(s.denomination(), s.localisation(), s.surface(), s.occupants(), 1, false, false));
        assertTrue(BatimentFactory.typesDisponibles().contains("CustomTest"));
    }
}
