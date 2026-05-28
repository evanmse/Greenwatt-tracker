package fr.greenwatt.model;

public class LocalCommercial extends Batiment {

    public enum Activite { ALIMENTAIRE, MODE, RESTAURATION, SERVICES, AUTRE }

    private Activite activite;
    private int heuresOuvertureParJour;
    private boolean chambreFroide;

    public LocalCommercial() {}

    public LocalCommercial(String denomination, Localisation l, double surface, int occupants,
                           Activite activite, int heuresOuvertureParJour, boolean chambreFroide) {
        super(denomination, l, surface, occupants);
        this.activite = activite;
        this.heuresOuvertureParJour = heuresOuvertureParJour;
        this.chambreFroide = chambreFroide;
    }

    /** Formule commerce : consommation pondérée par activité, heures d'ouverture et froid. */
    @Override
    public double estimerConsommation(Saison saison) {
        double base = surfaceM2 * 180;
        double facteurActivite = switch (activite != null ? activite : Activite.AUTRE) {
            case ALIMENTAIRE  -> 1.5;
            case RESTAURATION -> 1.8;
            case MODE         -> 1.0;
            case SERVICES     -> 0.8;
            case AUTRE        -> 1.0;
        };
        base *= facteurActivite;
        base *= heuresOuvertureParJour / 8.0;
        if (chambreFroide) base += 6000;
        return base * saison.facteur * facteurZone();
    }

    @Override
    public String getDescription() {
        return String.format("🏪 Local « %s » — %s — %.0fm² — %dh/j%s",
            denomination, activite, surfaceM2, heuresOuvertureParJour,
            chambreFroide ? " — chambre froide" : "");
    }

    public Activite getActivite() { return activite; }
    public void setActivite(Activite a) { this.activite = a; }
    public int getHeuresOuvertureParJour() { return heuresOuvertureParJour; }
    public void setHeuresOuvertureParJour(int h) { this.heuresOuvertureParJour = h; }
    public boolean isChambreFroide() { return chambreFroide; }
    public void setChambreFroide(boolean c) { this.chambreFroide = c; }
}
