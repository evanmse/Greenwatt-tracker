package fr.greenwatt;

import fr.greenwatt.interfaces.TarificationStrategy;
import fr.greenwatt.model.SourceEnergie;
import fr.greenwatt.strategy.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategyTest {

    @Test
    void troisStrategies_donnentDesResultatsPositifs() {
        double kwh = 5000;
        for (TarificationStrategy s : new TarificationStrategy[]{
                new TarifFixeStrategy(), new TarifVariableStrategy(), new TarifEcologiqueStrategy()}) {
            assertTrue(s.calculerCout(kwh, SourceEnergie.MIXTE) > 0, s.nom() + " doit être > 0");
        }
    }

    @Test
    void tarifEcologique_avantageEnergieVerte() {
        TarifEcologiqueStrategy s = new TarifEcologiqueStrategy();
        double verte    = s.calculerCout(1000, SourceEnergie.VERTE);
        double fossile  = s.calculerCout(1000, SourceEnergie.FOSSILE);
        assertTrue(verte < fossile);
    }
}
