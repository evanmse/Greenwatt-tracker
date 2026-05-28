package fr.greenwatt.factory;

import fr.greenwatt.model.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Factory de bâtiments en mode REGISTRY.
 *
 * Implémentation sans switch : le registre est un Map nom→constructeur.
 * Ajouter un nouveau type = un appel à enregistrer(), aucune ligne à modifier.
 * Respect strict de l'OCP (Open/Closed).
 */
public final class BatimentFactory {

    /** Décrit les paramètres communs nécessaires à la création. */
    public record SpecBatiment(String denomination, Localisation localisation,
                               double surface, int occupants) {}

    private static final Map<String, Function<SpecBatiment, Batiment>> REGISTRE = new LinkedHashMap<>();

    static {
        enregistrer("Maison", s -> new Maison(s.denomination(), s.localisation(), s.surface(), s.occupants(),
                4, false, false));
        enregistrer("Appartement", s -> new Appartement(s.denomination(), s.localisation(), s.surface(), s.occupants(),
                2, false, false));
        enregistrer("Bureau", s -> new Bureau(s.denomination(), s.localisation(), s.surface(), s.occupants(),
                25, true, false));
        enregistrer("LocalCommercial", s -> new LocalCommercial(s.denomination(), s.localisation(), s.surface(), s.occupants(),
                LocalCommercial.Activite.SERVICES, 10, false));
        enregistrer("BatimentUniversitaire", s -> new BatimentUniversitaire(s.denomination(), s.localisation(), s.surface(), s.occupants(),
                3, 4, true, true));
        enregistrer("AutreBatiment", s -> new AutreBatiment(s.denomination(), s.localisation(), s.surface(), s.occupants(),
                "Structure générique", 90));
    }

    private BatimentFactory() {}

    public static void enregistrer(String cle, Function<SpecBatiment, Batiment> constructeur) {
        REGISTRE.put(cle, constructeur);
    }

    public static Batiment creer(String cle, SpecBatiment spec) {
        var f = REGISTRE.get(cle);
        if (f == null) throw new IllegalArgumentException("Type de bâtiment non enregistré : " + cle);
        return f.apply(spec);
    }

    public static Set<String> typesDisponibles() {
        return REGISTRE.keySet();
    }
}
