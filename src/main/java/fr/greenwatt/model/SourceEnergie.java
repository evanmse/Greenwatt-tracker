package fr.greenwatt.model;

/** Catégorie de la source d'énergie utilisée. */
public enum SourceEnergie {
    VERTE(0.04),       // kgCO2 / kWh
    NUCLEAIRE(0.06),
    MIXTE(0.30),
    FOSSILE(0.45);

    public final double kgCO2ParKwh;

    SourceEnergie(double kgCO2ParKwh) { this.kgCO2ParKwh = kgCO2ParKwh; }
}
