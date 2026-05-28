package fr.greenwatt.model;

public class Appartement extends Batiment {

    private int numeroEtage;
    private boolean chauffageCollectif;
    private boolean balcon;

    public Appartement() {}

    public Appartement(String denomination, Localisation l, double surface, int occupants,
                       int numeroEtage, boolean chauffageCollectif, boolean balcon) {
        super(denomination, l, surface, occupants);
        this.numeroEtage = numeroEtage;
        this.chauffageCollectif = chauffageCollectif;
        this.balcon = balcon;
    }

    /** Formule appartement : 55 kWh/m²/saison, malus étage élevé, bonus chauffage collectif. */
    @Override
    public double estimerConsommation(Saison saison) {
        double base = surfaceM2 * 55;
        base += numeroEtage * 25;                // étages élevés = ascenseur partagé
        if (chauffageCollectif) base *= 0.85;    // mutualisation
        base += occupants * 160;
        return base * saison.facteur * facteurZone();
    }

    @Override
    public String getDescription() {
        return String.format("🏢 Appartement « %s » — étage %d — %.0fm² — %s%s",
            denomination, numeroEtage, surfaceM2,
            chauffageCollectif ? "chauffage collectif" : "chauffage individuel",
            balcon ? " — balcon" : "");
    }

    public int getNumeroEtage() { return numeroEtage; }
    public void setNumeroEtage(int n) { this.numeroEtage = n; }
    public boolean isChauffageCollectif() { return chauffageCollectif; }
    public void setChauffageCollectif(boolean v) { this.chauffageCollectif = v; }
    public boolean isBalcon() { return balcon; }
    public void setBalcon(boolean v) { this.balcon = v; }
}
