package fr.greenwatt.strategy;

import fr.greenwatt.interfaces.TarificationStrategy;
import fr.greenwatt.model.SourceEnergie;

/**
 * Tarif écologique : bonus pour les bâtiments alimentés en énergie verte,
 * malus pour les énergies fossiles (taxe carbone simulée).
 */
public class TarifEcologiqueStrategy implements TarificationStrategy {

    private final double prixBase;
    private final double taxeCO2ParKg;

    public TarifEcologiqueStrategy() { this(0.20, 0.05); }
    public TarifEcologiqueStrategy(double prixBase, double taxeCO2ParKg) {
        this.prixBase = prixBase;
        this.taxeCO2ParKg = taxeCO2ParKg;
    }

    @Override
    public double calculerCout(double kwh, SourceEnergie source) {
        double facteurSource = switch (source) {
            case VERTE     -> 0.80;
            case NUCLEAIRE -> 0.95;
            case MIXTE     -> 1.05;
            case FOSSILE   -> 1.30;
        };
        double coutEnergie = kwh * prixBase * facteurSource;
        double co2 = kwh * source.kgCO2ParKwh;
        return coutEnergie + co2 * taxeCO2ParKg;
    }

    @Override
    public String nom() { return "Tarif écologique"; }
}
