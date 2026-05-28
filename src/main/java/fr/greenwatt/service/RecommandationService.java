package fr.greenwatt.service;

import fr.greenwatt.model.Batiment;
import fr.greenwatt.model.CategorieEnergie;
import fr.greenwatt.model.Mesure;
import fr.greenwatt.model.SourceEnergie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de <b>recommandations d'économies d'énergie</b> (bonus §6.1).
 *
 * Approche à base de règles métier — pas d'IA — pour rester explicable et
 * facilement justifiable en soutenance. Chaque règle produit une recommandation
 * textuelle accompagnée d'un niveau de priorité.
 */
public class RecommandationService {

    public enum Priorite { ELEVEE, MOYENNE, INFO }

    public record Recommandation(Priorite priorite, String batiment, String message) {
        @Override public String toString() {
            String icone = switch (priorite) {
                case ELEVEE  -> "🔴";
                case MOYENNE -> "🟠";
                case INFO    -> "🟡";
            };
            return icone + " [" + batiment + "] " + message;
        }
    }

    /** Évalue tous les bâtiments et retourne les conseils triés par priorité. */
    public List<Recommandation> analyser(List<Batiment> batiments) {
        List<Recommandation> conseils = new ArrayList<>();
        if (batiments.isEmpty()) return conseils;

        double moyenneConso = batiments.stream()
            .mapToDouble(Batiment::estimerConsommationAnnuelle).average().orElse(0);

        for (Batiment b : batiments) {
            evaluerSurconsommation(b, moyenneConso, conseils);
            evaluerSourceEnergie(b, conseils);
            evaluerRepartition(b, conseils);
            evaluerVolatilite(b, conseils);
        }

        conseils.sort((a, c) -> a.priorite().compareTo(c.priorite()));
        return conseils;
    }

    /** Bâtiments significativement au-dessus de la moyenne. */
    private void evaluerSurconsommation(Batiment b, double moyenne, List<Recommandation> out) {
        double conso = b.estimerConsommationAnnuelle();
        if (moyenne > 0 && conso > moyenne * 1.4) {
            int ecartPct = (int) Math.round((conso / moyenne - 1) * 100);
            out.add(new Recommandation(Priorite.ELEVEE, b.getDenomination(),
                "Consommation supérieure de " + ecartPct + " % à la moyenne du parc. "
              + "Audit énergétique recommandé (isolation, équipements, usages)."));
        }
    }

    /** Source d'énergie fossile → suggestion de bascule. */
    private void evaluerSourceEnergie(Batiment b, List<Recommandation> out) {
        if (b.getSource() == SourceEnergie.FOSSILE) {
            double gainCO2 = b.estimerConsommationAnnuelle()
                * (b.getSource().kgCO2ParKwh - SourceEnergie.MIXTE.kgCO2ParKwh);
            out.add(new Recommandation(Priorite.MOYENNE, b.getDenomination(),
                String.format("Source d'énergie fossile : basculer vers un contrat MIXTE/VERTE "
                + "éviterait ≈ %,.0f kg CO₂/an.", gainCO2)));
        }
    }

    /** Catégorie d'énergie nettement majoritaire → conseil ciblé. */
    private void evaluerRepartition(Batiment b, List<Recommandation> out) {
        if (b.getMesures().isEmpty()) return;
        Map<CategorieEnergie, Double> parCat = b.getMesures().stream()
            .collect(Collectors.groupingBy(Mesure::getCategorie,
                     Collectors.summingDouble(Mesure::getQuantite)));
        double total = parCat.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) return;
        parCat.forEach((cat, val) -> {
            if (val / total > 0.55) {
                int pct = (int) Math.round(val / total * 100);
                String conseil = switch (cat) {
                    case CHAUFFAGE     -> "Programmer une régulation horaire et renforcer l'isolation.";
                    case CLIMATISATION -> "Installer des stores extérieurs et viser 26 °C en consigne.";
                    case ELECTRICITE   -> "Auditer les veilles et passer à l'éclairage LED.";
                    case EAU           -> "Mettre des mousseurs et vérifier l'absence de fuite.";
                    case GAZ           -> "Vérifier la combustion de la chaudière, envisager une PAC.";
                    case PRODUCTION_SOLAIRE -> "Suivi mensuel de production pour détecter une panne d'onduleur.";
                };
                out.add(new Recommandation(Priorite.MOYENNE, b.getDenomination(),
                    cat + " représente " + pct + " % des relevés. " + conseil));
            }
        });
    }

    /** Forte variabilité = pic ponctuel ou usage irrégulier. */
    private void evaluerVolatilite(Batiment b, List<Recommandation> out) {
        if (b.getMesures().size() < 3) return;
        double moyenne = b.getMesures().stream().mapToDouble(Mesure::getQuantite).average().orElse(0);
        if (moyenne == 0) return;
        double variance = b.getMesures().stream()
            .mapToDouble(m -> Math.pow(m.getQuantite() - moyenne, 2)).average().orElse(0);
        double cv = Math.sqrt(variance) / moyenne; // coefficient de variation
        if (cv > 0.6) {
            out.add(new Recommandation(Priorite.INFO, b.getDenomination(),
                String.format("Forte variabilité des relevés (CV = %.0f %%). Lisser les usages "
                + "par programmation horaire ou délestage automatique.", cv * 100)));
        }
    }
}
