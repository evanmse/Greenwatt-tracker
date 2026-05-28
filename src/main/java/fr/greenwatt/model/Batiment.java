package fr.greenwatt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.greenwatt.exception.MesureInvalideException;
import fr.greenwatt.interfaces.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe abstraite de la hiérarchie.
 *
 * Caractéristiques :
 *  - Méthode polymorphique principale : estimerConsommation(Saison) — la formule
 *    est appliquée à une saison donnée, ce qui permet d'analyser les variations annuelles.
 *  - empreinteCarbone() s'appuie sur la SourceEnergie de chaque bâtiment.
 *  - getDescription() + versMarkdown() pour les rapports.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Maison.class,                name = "Maison"),
    @JsonSubTypes.Type(value = Appartement.class,           name = "Appartement"),
    @JsonSubTypes.Type(value = Bureau.class,                name = "Bureau"),
    @JsonSubTypes.Type(value = LocalCommercial.class,       name = "LocalCommercial"),
    @JsonSubTypes.Type(value = BatimentUniversitaire.class, name = "BatimentUniversitaire"),
    @JsonSubTypes.Type(value = AutreBatiment.class,         name = "AutreBatiment")
})
public abstract class Batiment
        implements ConsommationCalculable, Exportable, Analysable, Persistable, Cloneable {

    protected Long identifiant;
    protected String denomination;
    protected Localisation localisation;
    protected double surfaceM2;
    protected int occupants;
    protected SourceEnergie source = SourceEnergie.MIXTE;
    protected List<Mesure> mesures = new ArrayList<>();
    protected LocalDateTime creeLe = LocalDateTime.now();

    protected Batiment() {}

    protected Batiment(String denomination, Localisation localisation, double surface, int occupants) {
        if (denomination == null || denomination.isBlank())
            throw new IllegalArgumentException("La dénomination est requise");
        if (surface <= 0) throw new IllegalArgumentException("Surface doit être > 0");
        if (occupants < 0) throw new IllegalArgumentException("Occupants ne peut pas être négatif");
        this.denomination = denomination;
        this.localisation = localisation;
        this.surfaceM2 = surface;
        this.occupants = occupants;
    }

    // ===== Méthodes POLYMORPHIQUES =====
    @Override
    public abstract double estimerConsommation(Saison saison);

    @Override
    public double estimerCout(TarificationStrategy strategy) {
        return strategy.calculerCout(estimerConsommationAnnuelle(), source);
    }

    public abstract String getDescription();

    /** Type lisible pour les rapports. */
    public String typeLibelle() { return getClass().getSimpleName(); }

    // ===== Domaine =====
    public void ajouterMesure(Mesure m) {
        if (m == null) throw new MesureInvalideException("Mesure nulle");
        if (m.getQuantite() < 0) throw new MesureInvalideException("Quantité négative");
        if (m.getCategorie() == null) throw new MesureInvalideException("Catégorie manquante");
        mesures.add(m);
    }

    public boolean retirerMesure(Mesure m) {
        return mesures.remove(m);
    }

    /**
     * Clonage profond. La dénomination est préfixée par "Copie de ".
     */
    @Override
    public Batiment clone() {
        try {
            Batiment copie = (Batiment) super.clone();
            copie.identifiant = null;
            copie.denomination = "Copie de " + this.denomination;
            copie.mesures = new ArrayList<>();
            for (Mesure m : this.mesures) copie.mesures.add(m.dupliquer());
            return copie;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    // ===== Exportable =====
    @Override
    public String versJson() {
        return String.format(
            "{\"id\":%s,\"type\":\"%s\",\"denomination\":\"%s\",\"surface\":%.2f,\"occupants\":%d,\"conso\":%.2f,\"co2\":%.2f}",
            identifiant, typeLibelle(), echapper(denomination),
            surfaceM2, occupants, estimerConsommationAnnuelle(), empreinteCarbone());
    }

    @Override
    public String versCsv() {
        return String.join(";",
            String.valueOf(identifiant), typeLibelle(), echapper(denomination),
            String.valueOf(surfaceM2), String.valueOf(occupants),
            String.format("%.2f", estimerConsommationAnnuelle()),
            String.format("%.2f", empreinteCarbone()));
    }

    @Override
    public String versMarkdown() {
        return String.format("### %s (%s)%n- Surface : %.0f m²%n- Occupants : %d%n- Conso annuelle : %.0f kWh%n- CO₂ : %.0f kg%n",
            denomination, typeLibelle(), surfaceM2, occupants, estimerConsommationAnnuelle(), empreinteCarbone());
    }

    private static String echapper(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    // ===== Analysable =====
    @Override
    public List<RapportAnomalie> diagnostiquer() {
        List<RapportAnomalie> resultats = new ArrayList<>();
        if (mesures.isEmpty()) return resultats;
        double moyenne = mesures.stream().mapToDouble(Mesure::getQuantite).average().orElse(0);
        for (Mesure m : mesures) {
            if (moyenne > 0 && m.getQuantite() > moyenne * 2.5) {
                resultats.add(new RapportAnomalie(
                    RapportAnomalie.Severite.CRITIQUE, m.getCategorie(),
                    String.format("Pic %.2f vs moyenne %.2f", m.getQuantite(), moyenne)));
            } else if (moyenne > 0 && m.getQuantite() > moyenne * 1.5) {
                resultats.add(new RapportAnomalie(
                    RapportAnomalie.Severite.ALERTE, m.getCategorie(),
                    String.format("Consommation élevée : %.2f", m.getQuantite())));
            }
            if (m.getQuantite() == 0) {
                resultats.add(new RapportAnomalie(
                    RapportAnomalie.Severite.INFO, m.getCategorie(),
                    "Valeur nulle, capteur en panne ?"));
            }
        }
        return resultats;
    }

    @Override
    public Map<String, Double> calculerTendances() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (CategorieEnergie c : CategorieEnergie.values()) map.put(c.name(), 0.0);
        for (Mesure m : mesures) map.merge(m.getCategorie().name(), m.getQuantite(), Double::sum);
        return map.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public double empreinteCarbone() {
        return estimerConsommationAnnuelle() * source.kgCO2ParKwh;
    }

    // ===== Persistable =====
    @Override public Long getIdentifiant() { return identifiant; }
    @Override public void setIdentifiant(Long id) { this.identifiant = id; }

    // ===== Getters / Setters =====
    public String getDenomination() { return denomination; }
    public void setDenomination(String denomination) { this.denomination = denomination; }
    public Localisation getLocalisation() { return localisation; }
    public void setLocalisation(Localisation l) { this.localisation = l; }
    public double getSurfaceM2() { return surfaceM2; }
    public void setSurfaceM2(double s) { this.surfaceM2 = s; }
    public int getOccupants() { return occupants; }
    public void setOccupants(int o) { this.occupants = o; }
    public SourceEnergie getSource() { return source; }
    public void setSource(SourceEnergie s) { this.source = s; }
    public List<Mesure> getMesures() { return Collections.unmodifiableList(mesures); }
    public void setMesures(List<Mesure> m) { this.mesures = m != null ? new ArrayList<>(m) : new ArrayList<>(); }
    public LocalDateTime getCreeLe() { return creeLe; }
    public void setCreeLe(LocalDateTime d) { this.creeLe = d; }

    @JsonIgnore
    protected double facteurZone() {
        return localisation != null && localisation.getZone() != null
                ? localisation.getZone().facteurThermique : 1.0;
    }

    @Override public String toString() { return getDescription(); }
}
