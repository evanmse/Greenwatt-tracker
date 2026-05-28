package fr.greenwatt;

import fr.greenwatt.controller.AppController;
import fr.greenwatt.interfaces.BatimentRepository;
import fr.greenwatt.repository.BatimentJsonRepository;
import fr.greenwatt.service.*;
import fr.greenwatt.strategy.TarifFixeStrategy;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

/** Point d'entrée JavaFX de GreenWatt Tracker. */
public class AppLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // --- Injection manuelle des dépendances ---
        BatimentRepository repo = new BatimentJsonRepository(Path.of("data/batiments.json"));

        GestionBatimentsService    gestion     = new GestionBatimentsService(repo);
        TableauDeBordService       tdb         = new TableauDeBordService(repo, new TarifFixeStrategy());
        DiagnosticService          diagnostic  = new DiagnosticService(repo);
        ComparateurTarifsService   comparateur = new ComparateurTarifsService();
        PrevisionMeteoService      meteo       = new PrevisionMeteoService();
        RapportService             rapport     = new RapportService();
        ImportCsvService           importCsv   = new ImportCsvService();
        GenerateurDonneesService   generateur  = new GenerateurDonneesService(gestion);
        PredictionService          prediction  = new PredictionService();
        RecommandationService      reco        = new RecommandationService();
        CapteurIoTService          iot         = new CapteurIoTService(gestion);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/app.fxml"));
        loader.setControllerFactory(type -> {
            if (type == AppController.class) {
                return new AppController(gestion, tdb, diagnostic, comparateur, meteo, rapport,
                        importCsv, generateur, prediction, reco, iot);
            }
            throw new IllegalStateException("Controller inconnu : " + type);
        });

        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 800);
        var css = getClass().getResource("/css/theme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle("🌱 GreenWatt Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
