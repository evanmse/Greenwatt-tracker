package fr.greenwatt.model;

/**
 * Type « Autre » — bâtiment générique pour les structures non prévues
 * dans le catalogue (entrepôt, gymnase, lieu de culte, atelier, etc.).
 *
 * La formule s'appuie sur un coefficient kWh/m² configurable par l'utilisateur,
 * ce qui rend le type extensible sans créer une nouvelle sous-classe.
 */
public class AutreBatiment extends Batiment {

    /** kWh/m²/saison, paramétrable selon la nature de la structure. */
    private double coefficientEnergetique;
    /** Étiquette libre décrivant la nature exacte (ex : « Entrepôt », « Gymnase »). */
    private String categorieLibre;

    public AutreBatiment() {}

    public AutreBatiment(String denomination, Localisation l, double surface, int occupants,
                         String categorieLibre, double coefficientEnergetique) {
        super(denomination, l, surface, occupants);
        this.categorieLibre = categorieLibre != null ? categorieLibre : "Structure générique";
        this.coefficientEnergetique = coefficientEnergetique > 0 ? coefficientEnergetique : 90;
    }

    /** Formule générique : coefficient × surface × saison × zone, + occupants. */
    @Override
    public double estimerConsommation(Saison saison) {
        double base = surfaceM2 * coefficientEnergetique;
        base += occupants * 150;
        return base * saison.facteur * facteurZone();
    }

    @Override
    public String getDescription() {
        return String.format("🏗 %s « %s » — %.0fm² — coeff %.0f kWh/m²",
            categorieLibre, denomination, surfaceM2, coefficientEnergetique);
    }

    public double getCoefficientEnergetique() { return coefficientEnergetique; }
    public void setCoefficientEnergetique(double c) { this.coefficientEnergetique = c; }
    public String getCategorieLibre() { return categorieLibre; }
    public void setCategorieLibre(String c) { this.categorieLibre = c; }
}
