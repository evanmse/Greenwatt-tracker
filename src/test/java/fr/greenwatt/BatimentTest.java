package fr.greenwatt;

import fr.greenwatt.model.*;
import fr.greenwatt.strategy.TarifFixeStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatimentTest {

    @Test
    void polymorphisme_consommationVariePerType() {
        Batiment maison = new Maison("M", new Localisation(), 100, 4, 4, true, true);
        Batiment univ = new BatimentUniversitaire("U", new Localisation(), 1000, 200, 5, 8, true, true);

        assertTrue(univ.estimerConsommationAnnuelle() > maison.estimerConsommationAnnuelle());
    }

    @Test
    void consommation_dependDeLaSaison() {
        Maison m = new Maison("M", new Localisation(), 100, 4, 4, false, false);
        assertTrue(m.estimerConsommation(Saison.HIVER) > m.estimerConsommation(Saison.PRINTEMPS));
    }

    @Test
    void clonage_estProfondEtPrefixe() {
        Maison m = new Maison("MaMaison", new Localisation(), 80, 3, 3, false, false);
        m.ajouterMesure(new Mesure(CategorieEnergie.ELECTRICITE, 1500, java.time.LocalDateTime.now()));

        Batiment copie = m.clone();
        assertNull(copie.getIdentifiant());
        assertEquals("Copie de MaMaison", copie.getDenomination());
        assertEquals(1, copie.getMesures().size());
        assertNotSame(m.getMesures().get(0), copie.getMesures().get(0));
    }

    @Test
    void cout_dependDeLaStrategy() {
        Batiment m = new Maison("M", new Localisation(), 100, 4, 4, false, false);
        double cout = m.estimerCout(new TarifFixeStrategy(0.20));
        assertTrue(cout > 0);
    }

    @Test
    void surfaceNegativeRejetee() {
        assertThrows(IllegalArgumentException.class,
                () -> new Maison("M", new Localisation(), -10, 1, 1, false, false));
    }

    @Test
    void empreinteCarbone_depend_de_la_source() {
        Maison verte = new Maison("V", new Localisation(), 100, 4, 4, false, false);
        verte.setSource(SourceEnergie.VERTE);
        Maison foss = new Maison("F", new Localisation(), 100, 4, 4, false, false);
        foss.setSource(SourceEnergie.FOSSILE);

        assertTrue(foss.empreinteCarbone() > verte.empreinteCarbone() * 5);
    }
}
