package fr.greenwatt.strategy;

import fr.greenwatt.interfaces.TarificationStrategy;
import fr.greenwatt.model.SourceEnergie;

/** Tarif unique par kWh, indépendant de l'heure. */
public class TarifFixeStrategy implements TarificationStrategy {

    private final double prixKwh;

    public TarifFixeStrategy() { this(0.22); }
    public TarifFixeStrategy(double prixKwh) { this.prixKwh = prixKwh; }

    @Override
    public double calculerCout(double kwh, SourceEnergie source) {
        return kwh * prixKwh;
    }

    @Override
    public String nom() { return "Tarif fixe"; }
}
