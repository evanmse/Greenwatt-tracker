package fr.greenwatt.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Objects;

/** Mesure ponctuelle d'une catégorie d'énergie. */
public class Mesure {
    private Long id;
    private CategorieEnergie categorie;
    private double quantite;
    private String unite;
    private double coutEstime;   // PDF §5.2 : coût estimé par relevé

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime horodatage;

    public Mesure() {}

    public Mesure(CategorieEnergie categorie, double quantite, LocalDateTime horodatage) {
        this(categorie, quantite, categorie.unite, horodatage);
    }

    public Mesure(CategorieEnergie categorie, double quantite, String unite, LocalDateTime horodatage) {
        this.categorie = Objects.requireNonNull(categorie);
        this.quantite = quantite;
        this.unite = unite;
        this.horodatage = horodatage != null ? horodatage : LocalDateTime.now();
    }

    public Mesure dupliquer() {
        Mesure m = new Mesure(categorie, quantite, unite, horodatage);
        m.coutEstime = this.coutEstime;
        return m;
    }

    public double getCoutEstime() { return coutEstime; }
    public void setCoutEstime(double c) { this.coutEstime = c; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CategorieEnergie getCategorie() { return categorie; }
    public void setCategorie(CategorieEnergie c) { this.categorie = c; }
    public double getQuantite() { return quantite; }
    public void setQuantite(double q) { this.quantite = q; }
    public String getUnite() { return unite; }
    public void setUnite(String u) { this.unite = u; }
    public LocalDateTime getHorodatage() { return horodatage; }
    public void setHorodatage(LocalDateTime h) { this.horodatage = h; }

    @Override
    public String toString() {
        return String.format("[%s] %.2f %s @ %s", categorie, quantite, unite, horodatage);
    }
}
