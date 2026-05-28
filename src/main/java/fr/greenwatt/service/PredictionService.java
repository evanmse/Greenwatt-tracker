package fr.greenwatt.service;

import fr.greenwatt.model.Batiment;
import fr.greenwatt.model.Mesure;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prédiction de la consommation future par <b>régression linéaire</b>
 * (méthode des moindres carrés) appliquée à l'historique mensuel.
 *
 * Pédagogiquement intéressant car :
 *  - Pas de librairie externe ML : la formule est entièrement explicite.
 *  - Le résultat est une projection mois par mois sur N mois à venir.
 *  - Couvre le bonus §6.1 du cahier des charges (« Prédiction de consommation future »).
 */
public class PredictionService {

    /** Point de la courbe prédite : étiquette « YYYY-MM » + valeur kWh. */
    public record PointPrediction(String mois, double valeur) {}

    /**
     * Calcule la projection sur {@code horizonMois} mois à partir de l'historique
     * mensuel cumulé sur l'ensemble des bâtiments fournis.
     *
     * @return liste ordonnée des points de prévision (vide si trop peu de données).
     */
    public List<PointPrediction> projeter(List<Batiment> batiments, int horizonMois) {
        Map<YearMonth, Double> historique = agregerParMois(batiments);
        if (historique.size() < 2) return List.of(); // pas assez d'historique

        // Conversion en (x, y) où x = indice du mois, y = conso totale.
        List<YearMonth> mois = new ArrayList<>(historique.keySet());
        int n = mois.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double y = historique.get(mois.get(i));
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumX2 += (double) i * i;
        }
        double denom = n * sumX2 - sumX * sumX;
        double pente = denom == 0 ? 0 : (n * sumXY - sumX * sumY) / denom;
        double ordonnee = (sumY - pente * sumX) / n;

        // Projection à partir du mois suivant le dernier connu.
        YearMonth dernier = mois.get(n - 1);
        List<PointPrediction> projection = new ArrayList<>();
        for (int k = 1; k <= horizonMois; k++) {
            YearMonth m = dernier.plusMonths(k);
            double y = Math.max(0, ordonnee + pente * (n - 1 + k));
            projection.add(new PointPrediction(
                m.getYear() + "-" + String.format("%02d", m.getMonthValue()), y));
        }
        return projection;
    }

    /** Estimation grossière de la facture mensuelle moyenne sur l'horizon donné. */
    public double facturePrevisionnelle(List<PointPrediction> projection, double tarifKwh) {
        if (projection.isEmpty()) return 0;
        double moyenne = projection.stream().mapToDouble(PointPrediction::valeur).average().orElse(0);
        return moyenne * tarifKwh;
    }

    private Map<YearMonth, Double> agregerParMois(List<Batiment> batiments) {
        Map<YearMonth, Double> acc = new LinkedHashMap<>();
        for (Batiment b : batiments) {
            for (Mesure m : b.getMesures()) {
                LocalDate d = m.getHorodatage().toLocalDate();
                acc.merge(YearMonth.of(d.getYear(), d.getMonth()), m.getQuantite(), Double::sum);
            }
        }
        // Trier chronologiquement
        Map<YearMonth, Double> trie = new LinkedHashMap<>();
        acc.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> trie.put(e.getKey(), e.getValue()));
        return trie;
    }
}
