package fr.greenwatt.interfaces;

import fr.greenwatt.model.RapportAnomalie;
import java.util.List;
import java.util.Map;

/** Capacités d'analyse statistique. */
public interface Analysable {

    /** Renvoie un rapport détaillé pour chaque anomalie détectée. */
    List<RapportAnomalie> diagnostiquer();

    /** Tendances agrégées (par type d'énergie). */
    Map<String, Double> calculerTendances();

    /** Empreinte carbone estimée en kgCO2. */
    double empreinteCarbone();
}
