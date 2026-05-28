package fr.greenwatt.interfaces;

import fr.greenwatt.model.Saison;

/**
 * Capacité de calcul énergétique pour un bâtiment.
 *
 * La consommation prend une saison en paramètre pour intégrer
 * la variation hiver/été (ISP : seuls les types qui en ont besoin l'implémentent).
 */
public interface ConsommationCalculable {

    /** Consommation annuelle en kWh, modulée par la saison. */
    double estimerConsommation(Saison saison);

    /** Consommation annuelle moyenne (toutes saisons confondues). */
    default double estimerConsommationAnnuelle() {
        double total = 0;
        for (Saison s : Saison.values()) total += estimerConsommation(s);
        return total / Saison.values().length;
    }

    /** Coût total via la stratégie de tarification courante. */
    double estimerCout(TarificationStrategy strategy);
}
