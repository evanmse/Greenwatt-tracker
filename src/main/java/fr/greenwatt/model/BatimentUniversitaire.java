package fr.greenwatt.model;

public class BatimentUniversitaire extends Batiment {

    private int amphitheatres;
    private int laboratoires;
    private boolean bibliotheque;
    private boolean restaurationUniversitaire;

    public BatimentUniversitaire() {}

    public BatimentUniversitaire(String denomination, Localisation l, double surface, int occupants,
                                 int amphitheatres, int laboratoires,
                                 boolean bibliotheque, boolean restaurationUniversitaire) {
        super(denomination, l, surface, occupants);
        this.amphitheatres = amphitheatres;
        this.laboratoires = laboratoires;
        this.bibliotheque = bibliotheque;
        this.restaurationUniversitaire = restaurationUniversitaire;
    }

    /** Formule université : très énergivore (laboratoires, RU, biblio chauffée). */
    @Override
    public double estimerConsommation(Saison saison) {
        double base = surfaceM2 * 140;
        base += amphitheatres * 2500;
        base += laboratoires * 5000;
        if (bibliotheque) base += 3500;
        if (restaurationUniversitaire) base += 9000;
        return base * saison.facteur * facteurZone();
    }

    @Override
    public String getDescription() {
        return String.format("🎓 Bât. univ. « %s » — %.0fm² — %d amphis — %d labos%s%s",
            denomination, surfaceM2, amphitheatres, laboratoires,
            bibliotheque ? " — bibliothèque" : "",
            restaurationUniversitaire ? " — RU" : "");
    }

    public int getAmphitheatres() { return amphitheatres; }
    public void setAmphitheatres(int a) { this.amphitheatres = a; }
    public int getLaboratoires() { return laboratoires; }
    public void setLaboratoires(int l) { this.laboratoires = l; }
    public boolean isBibliotheque() { return bibliotheque; }
    public void setBibliotheque(boolean b) { this.bibliotheque = b; }
    public boolean isRestaurationUniversitaire() { return restaurationUniversitaire; }
    public void setRestaurationUniversitaire(boolean r) { this.restaurationUniversitaire = r; }
}
