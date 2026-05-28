package fr.greenwatt.service;

import fr.greenwatt.interfaces.BatimentRepository;
import fr.greenwatt.model.Batiment;
import fr.greenwatt.model.RapportAnomalie;
import fr.greenwatt.observer.EventBus;
import fr.greenwatt.observer.Evenement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Détecte et publie les anomalies. */
public class DiagnosticService {

    private final BatimentRepository repository;

    public DiagnosticService(BatimentRepository repository) { this.repository = repository; }

    public Map<Batiment, List<RapportAnomalie>> diagnostiquerTout() {
        Map<Batiment, List<RapportAnomalie>> resultat = new LinkedHashMap<>();
        for (Batiment b : repository.lister()) {
            List<RapportAnomalie> rapports = b.diagnostiquer();
            if (!rapports.isEmpty()) {
                resultat.put(b, rapports);
                for (RapportAnomalie r : rapports) {
                    EventBus.INSTANCE.publier(new Evenement(Evenement.Type.ANOMALIE_DETECTEE,
                            b.getDenomination() + " : " + r));
                }
            }
        }
        return resultat;
    }
}
