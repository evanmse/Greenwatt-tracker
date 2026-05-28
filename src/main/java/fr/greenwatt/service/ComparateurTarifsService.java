package fr.greenwatt.service;

import fr.greenwatt.interfaces.TarificationStrategy;
import fr.greenwatt.model.Batiment;
import fr.greenwatt.strategy.TarifEcologiqueStrategy;
import fr.greenwatt.strategy.TarifFixeStrategy;
import fr.greenwatt.strategy.TarifVariableStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compare les stratégies de tarification disponibles. */
public class ComparateurTarifsService {

    public List<TarificationStrategy> strategies() {
        return List.of(new TarifFixeStrategy(), new TarifVariableStrategy(), new TarifEcologiqueStrategy());
    }

    public Map<String, Double> comparerPour(Batiment b) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (TarificationStrategy s : strategies()) {
            map.put(s.nom(), b.estimerCout(s));
        }
        return map;
    }
}
