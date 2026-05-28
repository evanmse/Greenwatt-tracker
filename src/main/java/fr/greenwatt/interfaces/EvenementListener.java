package fr.greenwatt.interfaces;

import fr.greenwatt.observer.Evenement;

/** Pattern Observer : abonné notifié quand un événement est publié. */
@FunctionalInterface
public interface EvenementListener {
    void onEvenement(Evenement evenement);
}
