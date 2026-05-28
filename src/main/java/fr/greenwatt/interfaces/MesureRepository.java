package fr.greenwatt.interfaces;

import fr.greenwatt.model.Mesure;
import java.util.List;

public interface MesureRepository {
    Mesure enregistrer(Long batimentId, Mesure m);
    List<Mesure> chercherPar(Long batimentId);
    void supprimer(Long id);
}
