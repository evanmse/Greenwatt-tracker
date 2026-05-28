package fr.greenwatt.observer;

import fr.greenwatt.interfaces.EvenementListener;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Pattern Observer + Singleton (implémentation via ENUM).
 *
 * Pourquoi enum ?
 *  - Thread-safe par construction (chargée par le ClassLoader une seule fois).
 *  - Immunisé à la sérialisation/réflection (impossible d'instancier 2 fois).
 *  - Plus concis que le double-checked locking.
 */
public enum EventBus {

    INSTANCE;

    private final Map<Evenement.Type, List<EvenementListener>> abonnes = new EnumMap<>(Evenement.Type.class);

    EventBus() {
        for (Evenement.Type t : Evenement.Type.values()) abonnes.put(t, new CopyOnWriteArrayList<>());
    }

    public void abonner(Evenement.Type type, EvenementListener listener) {
        abonnes.get(type).add(listener);
    }

    public void desabonner(Evenement.Type type, EvenementListener listener) {
        abonnes.get(type).remove(listener);
    }

    public void publier(Evenement e) {
        for (EvenementListener l : abonnes.get(e.getType())) {
            try {
                l.onEvenement(e);
            } catch (Exception ex) {
                System.err.println("Listener en erreur : " + ex.getMessage());
            }
        }
    }
}
