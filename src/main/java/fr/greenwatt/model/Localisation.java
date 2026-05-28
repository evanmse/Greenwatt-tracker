package fr.greenwatt.model;

/** Coordonnées d'un bâtiment (ville + zone climatique). */
public class Localisation {
    private String ville;
    private String region;
    private String codePostal;
    private ZoneClimatique zone;

    public Localisation() { this.zone = ZoneClimatique.H1; }

    public Localisation(String ville, String region, String codePostal, ZoneClimatique zone) {
        this.ville = ville;
        this.region = region;
        this.codePostal = codePostal;
        this.zone = zone;
    }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    public ZoneClimatique getZone() { return zone; }
    public void setZone(ZoneClimatique zone) { this.zone = zone; }

    @Override
    public String toString() {
        return String.format("%s (%s, zone %s)", ville, codePostal, zone);
    }

    public enum ZoneClimatique {
        H1(1.15), H2(1.00), H3(0.85);
        public final double facteurThermique;
        ZoneClimatique(double f) { this.facteurThermique = f; }
    }
}
