package fr.greenwatt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.greenwatt.model.PrevisionMeteo;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service de prévision météo branché sur l'API publique <b>Open-Meteo</b>
 * (https://open-meteo.com) — gratuite, sans clé API.
 *
 * Pipeline en deux étapes :
 *  1. Géocodage de la ville (geocoding-api.open-meteo.com)
 *  2. Récupération de la météo courante (api.open-meteo.com/v1/forecast)
 *
 * En cas d'absence de réseau ou d'erreur HTTP, le service retombe sur des
 * valeurs aléatoires plausibles pour ne pas bloquer l'application.
 */
public class PrevisionMeteoService {

    private static final String GEOCODE_URL =
        "https://geocoding-api.open-meteo.com/v1/search?count=1&language=fr&format=json&name=";
    private static final String FORECAST_URL =
        "https://api.open-meteo.com/v1/forecast?current=temperature_2m,cloud_cover&latitude=%s&longitude=%s";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public PrevisionMeteoService() {
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.mapper = new ObjectMapper();
    }

    public PrevisionMeteo previsionDuJour(String ville) {
        String villeEffective = (ville == null || ville.isBlank()) ? "Paris" : ville;
        try {
            double[] coords = geocoder(villeEffective);
            if (coords != null) {
                JsonNode meteo = appelerForecast(coords[0], coords[1]);
                JsonNode current = meteo.path("current");
                double temp = current.path("temperature_2m").asDouble();
                double cloud = current.path("cloud_cover").asDouble(50);
                double ensoleillement = Math.max(0, Math.min(1, 1.0 - cloud / 100.0));
                return new PrevisionMeteo(villeEffective, temp, ensoleillement, LocalDate.now());
            }
        } catch (Exception ignored) {
            // Réseau indisponible ou réponse inattendue : on bascule en simulation.
        }
        return previsionSimulee(villeEffective);
    }

    private double[] geocoder(String ville) throws Exception {
        String url = GEOCODE_URL + URLEncoder.encode(ville, StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(
            HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) return null;
        JsonNode results = mapper.readTree(resp.body()).path("results");
        if (!results.isArray() || results.isEmpty()) return null;
        JsonNode first = results.get(0);
        return new double[] { first.path("latitude").asDouble(), first.path("longitude").asDouble() };
    }

    private JsonNode appelerForecast(double lat, double lon) throws Exception {
        String url = String.format(java.util.Locale.US, FORECAST_URL, lat, lon);
        HttpResponse<String> resp = client.send(
            HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(resp.body());
    }

    private PrevisionMeteo previsionSimulee(String ville) {
        double temp = ThreadLocalRandom.current().nextDouble(-2, 32);
        double ens  = ThreadLocalRandom.current().nextDouble(0.1, 1.0);
        return new PrevisionMeteo(ville, temp, ens, LocalDate.now());
    }
}
