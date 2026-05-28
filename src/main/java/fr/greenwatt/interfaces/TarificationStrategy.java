package fr.greenwatt.interfaces;

import fr.greenwatt.model.SourceEnergie;

/**
 * Pattern Strategy : encapsule un algorithme de tarification.
 * Prend en compte la source d'énergie (verte / fossile / mixte).
 */
public interface TarificationStrategy {

    double calculerCout(double kwh, SourceEnergie source);

    /** Nom lisible affiché dans l'UI. */
    String nom();
}
