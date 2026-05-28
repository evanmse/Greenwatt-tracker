package fr.greenwatt.model;

public class Maison extends Batiment {

    private int nbPieces;
    private boolean isolationRenforcee;
    private boolean pompeAChaleur;

    public Maison() {}

    public Maison(String denomination, Localisation l, double surface, int occupants,
                  int nbPieces, boolean isolationRenforcee, boolean pompeAChaleur) {
        super(denomination, l, surface, occupants);
        this.nbPieces = nbPieces;
        this.isolationRenforcee = isolationRenforcee;
        this.pompeAChaleur = pompeAChaleur;
    }

    /** Formule maison : 75 kWh/m²/saison, modulée par isolation + PAC + zone climatique. */
    @Override
    public double estimerConsommation(Saison saison) {
        double base = surfaceM2 * 75;
        if (isolationRenforcee) base *= 0.75;
        if (pompeAChaleur) base *= 0.65;
        base += occupants * 220;
        return base * saison.facteur * facteurZone();
    }

    @Override
    public String getDescription() {
        return String.format("🏡 Maison « %s » — %.0fm² — %d pièces — %s — %s",
            denomination, surfaceM2, nbPieces,
            isolationRenforcee ? "isolée RT2012+" : "isolation standard",
            pompeAChaleur ? "PAC" : "chaudière classique");
    }

    public int getNbPieces() { return nbPieces; }
    public void setNbPieces(int n) { this.nbPieces = n; }
    public boolean isIsolationRenforcee() { return isolationRenforcee; }
    public void setIsolationRenforcee(boolean v) { this.isolationRenforcee = v; }
    public boolean isPompeAChaleur() { return pompeAChaleur; }
    public void setPompeAChaleur(boolean v) { this.pompeAChaleur = v; }
}
