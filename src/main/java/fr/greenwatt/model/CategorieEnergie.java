package fr.greenwatt.model;

/** Catégorie de l'énergie mesurée pour une mesure ponctuelle. */
public enum CategorieEnergie {
    ELECTRICITE("kWh"),
    EAU("m³"),
    GAZ("kWh"),
    CHAUFFAGE("kWh"),
    CLIMATISATION("kWh"),
    PRODUCTION_SOLAIRE("kWh");

    public final String unite;

    CategorieEnergie(String unite) { this.unite = unite; }
}
