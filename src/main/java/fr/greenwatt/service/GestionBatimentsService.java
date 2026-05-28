package fr.greenwatt.service;

import fr.greenwatt.exception.BatimentIntrouvableException;
import fr.greenwatt.interfaces.BatimentRepository;
import fr.greenwatt.model.Batiment;
import fr.greenwatt.model.Mesure;
import fr.greenwatt.observer.EventBus;
import fr.greenwatt.observer.Evenement;
import fr.greenwatt.utils.Validateur;

import java.util.List;

/** Service métier pour le CRUD bâtiments + publication d'événements. */
public class GestionBatimentsService {

    private final BatimentRepository repository;

    public GestionBatimentsService(BatimentRepository repository) {
        this.repository = repository;
    }

    public Batiment creer(Batiment b) {
        Batiment enregistre = repository.enregistrer(b);
        EventBus.INSTANCE.publier(new Evenement(Evenement.Type.BATIMENT_AJOUTE, enregistre));
        return enregistre;
    }

    public Batiment modifier(Batiment b) {
        Batiment enregistre = repository.enregistrer(b);
        EventBus.INSTANCE.publier(new Evenement(Evenement.Type.BATIMENT_MODIFIE, enregistre));
        return enregistre;
    }

    public Batiment trouver(Long id) {
        return repository.chercher(id).orElseThrow(() -> new BatimentIntrouvableException(id));
    }

    public List<Batiment> tousLesBatiments() {
        return repository.lister();
    }

    public long nombre() { return repository.compter(); }

    public void supprimer(Long id) {
        Batiment b = trouver(id);
        repository.supprimer(id);
        EventBus.INSTANCE.publier(new Evenement(Evenement.Type.BATIMENT_SUPPRIME, b));
    }

    public Batiment dupliquer(Long id) {
        Batiment original = trouver(id);
        Batiment copie = original.clone();
        return creer(copie);
    }

    public void ajouterMesure(Long batimentId, Mesure mesure) {
        Validateur.verifierMesure(mesure);
        Batiment b = trouver(batimentId);
        b.ajouterMesure(mesure);
        repository.enregistrer(b);
        EventBus.INSTANCE.publier(new Evenement(Evenement.Type.MESURE_AJOUTEE, mesure));
    }
}
