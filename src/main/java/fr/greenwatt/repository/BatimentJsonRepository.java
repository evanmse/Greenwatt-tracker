package fr.greenwatt.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.greenwatt.exception.StockageException;
import fr.greenwatt.interfaces.BatimentRepository;
import fr.greenwatt.model.Batiment;
import fr.greenwatt.utils.JsonMapperFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implémentation JSON (Jackson) — persistance par défaut de GreenWatt.
 *
 * Charge tout en mémoire, persiste à chaque modification.
 */
public class BatimentJsonRepository implements BatimentRepository {

    private final Path fichier;
    private final ObjectMapper mapper = JsonMapperFactory.creer();
    private final Map<Long, Batiment> stockage = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public BatimentJsonRepository(Path fichier) {
        this.fichier = fichier;
        charger();
    }

    private void charger() {
        File f = fichier.toFile();
        if (!f.exists() || f.length() == 0) return;
        try {
            List<Batiment> liste = mapper.readValue(f, new TypeReference<List<Batiment>>() {});
            for (Batiment b : liste) {
                stockage.put(b.getIdentifiant(), b);
                sequence.updateAndGet(prev -> Math.max(prev, b.getIdentifiant()));
            }
        } catch (IOException e) {
            throw new StockageException("Lecture JSON impossible : " + fichier, e);
        }
    }

    private void persister() {
        try {
            fichier.toFile().getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(fichier.toFile(), new ArrayList<>(stockage.values()));
        } catch (IOException e) {
            throw new StockageException("Écriture JSON impossible : " + fichier, e);
        }
    }

    @Override
    public Batiment enregistrer(Batiment b) {
        if (b.getIdentifiant() == null) b.setIdentifiant(sequence.incrementAndGet());
        stockage.put(b.getIdentifiant(), b);
        persister();
        return b;
    }

    @Override
    public Optional<Batiment> chercher(Long id) {
        return Optional.ofNullable(stockage.get(id));
    }

    @Override
    public List<Batiment> lister() {
        return new ArrayList<>(stockage.values());
    }

    @Override
    public void supprimer(Long id) {
        stockage.remove(id);
        persister();
    }

    @Override
    public long compter() { return stockage.size(); }
}
