package fr.greenwatt.model;

import java.time.LocalDate;

/** Prévision météo simplifiée (peut être branchée à OpenWeatherMap). */
public class PrevisionMeteo {

    private final String ville;
    private final double temperatureMoyenne;
    private final double tauxEnsoleillement;
    private final LocalDate date;

    public PrevisionMeteo(String ville, double temperatureMoyenne, double tauxEnsoleillement, LocalDate date) {
        this.ville = ville;
        this.temperatureMoyenne = temperatureMoyenne;
        this.tauxEnsoleillement = tauxEnsoleillement;
        this.date = date;
    }

    public String getVille() { return ville; }
    public double getTemperatureMoyenne() { return temperatureMoyenne; }
    public double getTauxEnsoleillement() { return tauxEnsoleillement; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return String.format("%s : %.1f°C, ensoleillement %.0f%%", ville, temperatureMoyenne, tauxEnsoleillement * 100);
    }
}
