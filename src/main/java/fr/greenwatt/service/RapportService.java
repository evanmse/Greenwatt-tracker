package fr.greenwatt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.greenwatt.exception.RapportException;
import fr.greenwatt.model.Batiment;
import fr.greenwatt.observer.EventBus;
import fr.greenwatt.observer.Evenement;
import fr.greenwatt.utils.JsonMapperFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/** Génère les rapports / exports. */
public class RapportService {

    private final ObjectMapper mapper = JsonMapperFactory.creer();

    public void exporterJson(List<Batiment> batiments, Path cible) {
        try {
            cible.toFile().getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(cible.toFile(), batiments);
            EventBus.INSTANCE.publier(new Evenement(Evenement.Type.EXPORT_REALISE, cible.toString()));
        } catch (Exception e) {
            throw new RapportException("Export JSON impossible", e);
        }
    }

    public void exporterCsv(List<Batiment> batiments, Path cible) {
        try {
            String header = "id;type;denomination;surface;occupants;conso;co2\n";
            String corps = batiments.stream().map(Batiment::versCsv).collect(Collectors.joining("\n"));
            cible.toFile().getParentFile().mkdirs();
            Files.writeString(cible, header + corps);
            EventBus.INSTANCE.publier(new Evenement(Evenement.Type.EXPORT_REALISE, cible.toString()));
        } catch (Exception e) {
            throw new RapportException("Export CSV impossible", e);
        }
    }

    public String genererMarkdown(List<Batiment> batiments) {
        StringBuilder sb = new StringBuilder("# Rapport GreenWatt\n\n");
        for (Batiment b : batiments) sb.append(b.versMarkdown()).append("\n");
        return sb.toString();
    }
}
