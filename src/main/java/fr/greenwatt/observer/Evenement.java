package fr.greenwatt.observer;

import java.time.LocalDateTime;

/** Événement diffusé via l'EventBus. */
public class Evenement {

    public enum Type { BATIMENT_AJOUTE, BATIMENT_MODIFIE, BATIMENT_SUPPRIME,
                       MESURE_AJOUTEE, ANOMALIE_DETECTEE, EXPORT_REALISE,
                       CAPTEUR_IOT }

    private final Type type;
    private final Object payload;
    private final LocalDateTime instant;

    public Evenement(Type type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.instant = LocalDateTime.now();
    }

    public Type getType() { return type; }
    public Object getPayload() { return payload; }
    public LocalDateTime getInstant() { return instant; }

    @Override
    public String toString() {
        return "[" + instant + "] " + type + " : " + payload;
    }
}
