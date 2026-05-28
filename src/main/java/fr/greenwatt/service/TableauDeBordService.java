package fr.greenwatt.service;

import fr.greenwatt.interfaces.BatimentRepository;
import fr.greenwatt.interfaces.TarificationStrategy;
import fr.greenwatt.model.Batiment;
import fr.greenwatt.model.CategorieEnergie;
import fr.greenwatt.model.Mesure;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Agrège les indicateurs pour le tableau de bord. */
public class TableauDeBordService {

    private final BatimentRepository repository;
    private TarificationStrategy strategy;

    public TableauDeBordService(BatimentRepository repository, TarificationStrategy strategy) {
        this.repository = repository;
        this.strategy = strategy;
    }

    public double consommationGlobale() {
        return repository.lister().stream()
                .mapToDouble(Batiment::estimerConsommationAnnuelle).sum();
    }

    public double coutGlobal() {
        return repository.lister().stream()
                .mapToDouble(b -> b.estimerCout(strategy)).sum();
    }

    public double empreinteGlobaleCO2() {
        return repository.lister().stream()
                .mapToDouble(Batiment::empreinteCarbone).sum();
    }

    public Map<CategorieEnergie, Double> repartitionParCategorie() {
        Map<CategorieEnergie, Double> map = new EnumMap<>(CategorieEnergie.class);
        for (Batiment b : repository.lister()) {
            for (Mesure m : b.getMesures()) {
                map.merge(m.getCategorie(), m.getQuantite(), Double::sum);
            }
        }
        return map;
    }

    public Map<String, Double> repartitionParType() {
        return repository.lister().stream().collect(Collectors.groupingBy(
                Batiment::typeLibelle,
                LinkedHashMap::new,
                Collectors.summingDouble(Batiment::estimerConsommationAnnuelle)));
    }

    public List<Batiment> classement(int n) {
        return repository.lister().stream()
                .sorted(Comparator.comparingDouble(Batiment::estimerConsommationAnnuelle).reversed())
                .limit(n).collect(Collectors.toList());
    }

    public TarificationStrategy getStrategy() { return strategy; }
    public void setStrategy(TarificationStrategy s) { this.strategy = s; }
}
