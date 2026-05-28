package fr.greenwatt;

import fr.greenwatt.observer.EventBus;
import fr.greenwatt.observer.Evenement;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

    @Test
    void eventBus_notifieLesAbonnes() {
        AtomicInteger compteur = new AtomicInteger(0);
        EventBus.INSTANCE.abonner(Evenement.Type.BATIMENT_AJOUTE, e -> compteur.incrementAndGet());

        EventBus.INSTANCE.publier(new Evenement(Evenement.Type.BATIMENT_AJOUTE, "test"));
        EventBus.INSTANCE.publier(new Evenement(Evenement.Type.BATIMENT_AJOUTE, "test2"));

        assertEquals(2, compteur.get());
    }

    @Test
    void singletonEnum_estUnique() {
        assertSame(EventBus.INSTANCE, EventBus.INSTANCE);
    }
}
