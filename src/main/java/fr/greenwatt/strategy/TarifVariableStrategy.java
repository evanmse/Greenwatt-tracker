package fr.greenwatt.strategy;

import fr.greenwatt.interfaces.TarificationStrategy;
import fr.greenwatt.model.SourceEnergie;

/** Tarif variable selon créneau, simplifié sur une moyenne pondérée. */
public class TarifVariableStrategy implements TarificationStrategy {

    private final double prixPointe;
    private final double prixCreuse;

    public TarifVariableStrategy() { this(0.28, 0.14); }
    public TarifVariableStrategy(double prixPointe, double prixCreuse) {
        this.prixPointe = prixPointe;
        this.prixCreuse = prixCreuse;
    }

    @Override
    public double calculerCout(double kwh, SourceEnergie source) {
        return kwh * 0.65 * prixPointe + kwh * 0.35 * prixCreuse;
    }

    @Override
    public String nom() { return "Tarif variable"; }
}
