package fr.greenwatt.service;

import fr.greenwatt.exception.MesureInvalideException;
import fr.greenwatt.model.CategorieEnergie;
import fr.greenwatt.model.Mesure;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Import CSV de mesures (PDF §5.2).
 * Format : categorie;quantite;unite;horodatage;coutEstime
 * Ex.    : ELECTRICITE;120.5;kWh;2026-01-15T08:00:00;24.10
 * La 1ère ligne est ignorée (entête).
 */
public class ImportCsvService {

    public List<Mesure> lire(Path fichier) {
        List<Mesure> out = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(fichier)) {
            String ligne;
            boolean entete = true;
            int num = 0;
            while ((ligne = br.readLine()) != null) {
                num++;
                if (entete) { entete = false; continue; }
                if (ligne.isBlank()) continue;
                String[] p = ligne.split(";");
                if (p.length < 4)
                    throw new MesureInvalideException("CSV ligne " + num + " invalide : " + ligne);
                Mesure m = new Mesure(
                    CategorieEnergie.valueOf(p[0].trim().toUpperCase()),
                    Double.parseDouble(p[1].trim()),
                    p[2].trim(),
                    LocalDateTime.parse(p[3].trim()));
                if (p.length >= 5 && !p[4].isBlank())
                    m.setCoutEstime(Double.parseDouble(p[4].trim()));
                out.add(m);
            }
        } catch (Exception e) {
            throw new MesureInvalideException("Erreur lecture CSV : " + e.getMessage());
        }
        return out;
    }
}
