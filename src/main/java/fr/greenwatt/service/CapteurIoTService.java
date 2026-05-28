package fr.greenwatt.service;

import fr.greenwatt.model.Batiment;
import fr.greenwatt.model.CategorieEnergie;
import fr.greenwatt.model.Mesure;
import fr.greenwatt.observer.EventBus;
import fr.greenwatt.observer.Evenement;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulateur de capteurs IoT (bonus §6.2 du cahier des charges).
 *
 * À intervalle régulier, choisit un bâtiment au hasard parmi ceux enregistrés
 * et y injecte un relevé énergétique plausible, comme le ferait un capteur
 * connecté (Zigbee, LoRa, MQTT…). Les nouvelles mesures sont publiées via
 * l'{@link EventBus}, ce qui alimente automatiquement le journal d'événements
 * et le tableau de bord.
 *
 * Utilise un {@link Timeline} JavaFX pour rester sur le fil applicatif et
 * éviter tout problème de concurrence sur l'UI.
 */
public class CapteurIoTService {

    private final GestionBatimentsService gestion;
    private final Timeline horloge;
    private Runnable callbackRafraichissement;

    public CapteurIoTService(GestionBatimentsService gestion) {
        this(gestion, Duration.seconds(4));
    }

    public CapteurIoTService(GestionBatimentsService gestion, Duration cadence) {
        this.gestion = gestion;
        this.horloge = new Timeline(new KeyFrame(cadence, e -> tick()));
        this.horloge.setCycleCount(Animation.INDEFINITE);
    }

    /** Définit le rafraîchissement UI à exécuter après chaque relevé simulé. */
    public void setCallbackRafraichissement(Runnable callback) {
        this.callbackRafraichissement = callback;
    }

    public void demarrer() {
        if (horloge.getStatus() != Animation.Status.RUNNING) {
            horloge.play();
            EventBus.INSTANCE.publier(new Evenement(Evenement.Type.CAPTEUR_IOT, "🛰 capteurs démarrés"));
        }
    }

    public void arreter() {
        if (horloge.getStatus() == Animation.Status.RUNNING) {
            horloge.stop();
            EventBus.INSTANCE.publier(new Evenement(Evenement.Type.CAPTEUR_IOT, "⏹ capteurs arrêtés"));
        }
    }

    public boolean enMarche() {
        return horloge.getStatus() == Animation.Status.RUNNING;
    }

    /** Un cycle : choisit un bâtiment et y injecte une mesure aléatoire. */
    private void tick() {
        List<Batiment> batiments = gestion.tousLesBatiments();
        if (batiments.isEmpty()) return;
        Batiment cible = batiments.get(ThreadLocalRandom.current().nextInt(batiments.size()));
        CategorieEnergie cat = CategorieEnergie.values()[
            ThreadLocalRandom.current().nextInt(CategorieEnergie.values().length)];
        double quantite = quantitePlausible(cat);
        Mesure m = new Mesure(cat, quantite, LocalDateTime.now());
        try {
            gestion.ajouterMesure(cible.getIdentifiant(), m);
            if (callbackRafraichissement != null) callbackRafraichissement.run();
        } catch (Exception ignored) {
            // Un capteur peut envoyer une trame invalide : on ignore, comme un vrai filtre IoT.
        }
    }

    /** Plage réaliste par catégorie pour que la mesure ne soit pas absurde. */
    private double quantitePlausible(CategorieEnergie c) {
        return switch (c) {
            case ELECTRICITE        -> ThreadLocalRandom.current().nextDouble(5, 80);
            case EAU                -> ThreadLocalRandom.current().nextDouble(0.1, 5);
            case GAZ                -> ThreadLocalRandom.current().nextDouble(10, 60);
            case CHAUFFAGE          -> ThreadLocalRandom.current().nextDouble(20, 150);
            case CLIMATISATION      -> ThreadLocalRandom.current().nextDouble(10, 90);
            case PRODUCTION_SOLAIRE -> ThreadLocalRandom.current().nextDouble(2, 25);
        };
    }
}
