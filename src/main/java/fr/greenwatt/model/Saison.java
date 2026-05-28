package fr.greenwatt.model;

/** Saison utilisée pour pondérer les consommations. */
public enum Saison {
    HIVER(1.30),
    PRINTEMPS(0.95),
    ETE(1.10),
    AUTOMNE(1.00);

    public final double facteur;

    Saison(double facteur) { this.facteur = facteur; }
}
