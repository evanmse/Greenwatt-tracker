package fr.greenwatt.model;

public class Bureau extends Batiment {

    private int postesTravail;
    private boolean openSpace;
    private boolean climReversible;

    public Bureau() {}

    public Bureau(String denomination, Localisation l, double surface, int occupants,
                  int postesTravail, boolean openSpace, boolean climReversible) {
        super(denomination, l, surface, occupants);
        this.postesTravail = postesTravail;
        this.openSpace = openSpace;
        this.climReversible = climReversible;
    }

    /** Formule bureau : 110 kWh/m²/saison + postes informatiques + clim été surcoût. */
    @Override
    public double estimerConsommation(Saison saison) {
        double base = surfaceM2 * 110;
        base += postesTravail * 350;
        if (openSpace) base *= 0.95;             // mutualisation éclairage
        if (climReversible && saison == Saison.ETE) base += surfaceM2 * 40;
        return base * saison.facteur * facteurZone();
    }

    @Override
    public String getDescription() {
        return String.format("🏛 Bureau « %s » — %.0fm² — %d postes — %s",
            denomination, surfaceM2, postesTravail,
            openSpace ? "open space" : "bureaux fermés");
    }

    public int getPostesTravail() { return postesTravail; }
    public void setPostesTravail(int p) { this.postesTravail = p; }
    public boolean isOpenSpace() { return openSpace; }
    public void setOpenSpace(boolean o) { this.openSpace = o; }
    public boolean isClimReversible() { return climReversible; }
    public void setClimReversible(boolean c) { this.climReversible = c; }
}
