package fr.greenwatt.model;

/** Décrit une anomalie détectée sur un bâtiment. */
public class RapportAnomalie {

    public enum Severite { INFO, ALERTE, CRITIQUE }

    private final Severite severite;
    private final String description;
    private final CategorieEnergie categorie;

    public RapportAnomalie(Severite severite, CategorieEnergie cat, String description) {
        this.severite = severite;
        this.categorie = cat;
        this.description = description;
    }

    public Severite getSeverite() { return severite; }
    public CategorieEnergie getCategorie() { return categorie; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("[%s] %s : %s", severite, categorie, description);
    }
}
