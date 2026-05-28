package fr.greenwatt.utils;

import fr.greenwatt.exception.MesureInvalideException;
import fr.greenwatt.model.Mesure;

public final class Validateur {

    private Validateur() {}

    public static void verifierMesure(Mesure m) {
        if (m == null) throw new MesureInvalideException("Mesure absente");
        if (m.getCategorie() == null) throw new MesureInvalideException("Catégorie manquante");
        if (m.getQuantite() < 0) throw new MesureInvalideException("Quantité négative");
        if (m.getHorodatage() == null) throw new MesureInvalideException("Horodatage manquant");
    }
}
