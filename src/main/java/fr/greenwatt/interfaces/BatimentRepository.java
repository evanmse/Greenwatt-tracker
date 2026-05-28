package fr.greenwatt.interfaces;

import fr.greenwatt.model.Batiment;
import java.util.List;
import java.util.Optional;

/** Abstraction du stockage des bâtiments (DIP). */
public interface BatimentRepository {
    Batiment enregistrer(Batiment b);
    Optional<Batiment> chercher(Long id);
    List<Batiment> lister();
    void supprimer(Long id);
    long compter();
}
