package fr.greenwatt.service;

import fr.greenwatt.factory.BatimentFactory;
import fr.greenwatt.factory.BatimentFactory.SpecBatiment;
import fr.greenwatt.interfaces.TarificationStrategy;
import fr.greenwatt.model.*;
import fr.greenwatt.strategy.TarifFixeStrategy;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Génération automatique d'un jeu de test (PDF §5.2).
 */
public class GenerateurDonneesService {

    private final GestionBatimentsService gestion;
    private final TarificationStrategy strategy = new TarifFixeStrategy();
    private final Random rng = new Random();

    public GenerateurDonneesService(GestionBatimentsService gestion) {
        this.gestion = gestion;
    }

    public void genererJeuComplet(int nbBatiments, int mesuresParBatiment) {
        String[] types = BatimentFactory.typesDisponibles().toArray(new String[0]);
        String[] villes = {"Paris", "Lyon", "Marseille", "Toulouse", "Nantes", "Strasbourg"};
        Localisation.ZoneClimatique[] zones = Localisation.ZoneClimatique.values();
        SourceEnergie[] sources = SourceEnergie.values();

        for (int i = 0; i < nbBatiments; i++) {
            String type = types[rng.nextInt(types.length)];
            Localisation l = new Localisation(villes[rng.nextInt(villes.length)], "",
                    String.format("%05d", rng.nextInt(95000) + 1000),
                    zones[rng.nextInt(zones.length)]);
            double surface = 60 + rng.nextDouble() * 900;
            int occ = 1 + rng.nextInt(40);
            Batiment b = BatimentFactory.creer(type,
                    new SpecBatiment("Test_" + type + "_" + (i + 1), l, surface, occ));
            b.setSource(sources[rng.nextInt(sources.length)]);
            Batiment enregistre = gestion.creer(b);

            for (int m = 0; m < mesuresParBatiment; m++) {
                CategorieEnergie c = CategorieEnergie.values()[rng.nextInt(CategorieEnergie.values().length)];
                double q = 10 + rng.nextDouble() * 500;
                LocalDateTime d = LocalDateTime.now().minusDays(rng.nextInt(365));
                Mesure mesure = new Mesure(c, q, d);
                mesure.setCoutEstime(strategy.calculerCout(q, enregistre.getSource()));
                gestion.ajouterMesure(enregistre.getIdentifiant(), mesure);
            }
        }
    }
}
