package fr.greenwatt.controller;

import fr.greenwatt.factory.BatimentFactory;
import fr.greenwatt.model.*;
import fr.greenwatt.observer.EventBus;
import fr.greenwatt.observer.Evenement;
import fr.greenwatt.service.*;
import fr.greenwatt.strategy.TarifEcologiqueStrategy;
import fr.greenwatt.strategy.TarifFixeStrategy;
import fr.greenwatt.strategy.TarifVariableStrategy;
import fr.greenwatt.utils.FormatUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Contrôleur unique pour l'application à 4 onglets. */
public class AppController {

    private final GestionBatimentsService    gestion;
    private final TableauDeBordService       tdb;
    private final DiagnosticService          diagnostic;
    private final ComparateurTarifsService   comparateur;
    private final PrevisionMeteoService      meteo;
    private final RapportService             rapport;
    private final ImportCsvService           importCsv;
    private final GenerateurDonneesService   generateur;
    private final PredictionService          prediction;
    private final RecommandationService      reco;
    private final CapteurIoTService          iot;

    // --- Onglet Tableau de bord ---
    @FXML private Label lblNbBatiments;
    @FXML private Label lblConsoTotale;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCO2Total;
    @FXML private Label lblMeteo;
    @FXML private PieChart pieParType;
    @FXML private BarChart<String, Number> barTop5;
    @FXML private LineChart<String, Number> lineTemporel;
    @FXML private ComboBox<String> cmbStrategie;
    @FXML private Label lblConsoJour;
    @FXML private Label lblConsoMois;
    @FXML private Label lblConsoAnnee;

    // --- Onglet Bâtiments ---
    @FXML private TableView<Batiment> tableBatiments;
    @FXML private TableColumn<Batiment, Number> colId;
    @FXML private TableColumn<Batiment, String> colType;
    @FXML private TableColumn<Batiment, String> colDenom;
    @FXML private TableColumn<Batiment, Number> colSurface;
    @FXML private TableColumn<Batiment, Number> colConso;
    @FXML private TableColumn<Batiment, Number> colCO2;

    @FXML private ComboBox<String> cmbTypeBatiment;
    @FXML private TextField txtDenomination;
    @FXML private TextField txtVille;
    @FXML private ComboBox<Localisation.ZoneClimatique> cmbZone;
    @FXML private TextField txtSurface;
    @FXML private TextField txtOccupants;
    @FXML private ComboBox<SourceEnergie> cmbSource;

    // --- Onglet Mesures ---
    @FXML private ComboBox<Batiment> cmbBatimentMesure;
    @FXML private ComboBox<CategorieEnergie> cmbCategorie;
    @FXML private TextField txtQuantite;
    @FXML private ListView<Mesure> listMesures;

    // --- Onglet Rapports ---
    @FXML private TextArea txtRapportMarkdown;
    @FXML private VBox panelAnomalies;
    @FXML private VBox panelEvenements;
    @FXML private VBox panelRecommandations;
    @FXML private Button btnIoT;

    public AppController(GestionBatimentsService gestion, TableauDeBordService tdb,
                         DiagnosticService diagnostic, ComparateurTarifsService comparateur,
                         PrevisionMeteoService meteo, RapportService rapport,
                         ImportCsvService importCsv, GenerateurDonneesService generateur,
                         PredictionService prediction, RecommandationService reco,
                         CapteurIoTService iot) {
        this.gestion = gestion;
        this.tdb = tdb;
        this.diagnostic = diagnostic;
        this.comparateur = comparateur;
        this.meteo = meteo;
        this.rapport = rapport;
        this.importCsv = importCsv;
        this.generateur = generateur;
        this.prediction = prediction;
        this.reco = reco;
        this.iot = iot;
    }

    @FXML
    public void initialize() {
        cmbTypeBatiment.setItems(FXCollections.observableArrayList(BatimentFactory.typesDisponibles()));
        cmbTypeBatiment.getSelectionModel().selectFirst();

        cmbZone.setItems(FXCollections.observableArrayList(Localisation.ZoneClimatique.values()));
        cmbZone.getSelectionModel().select(Localisation.ZoneClimatique.H2);

        cmbSource.setItems(FXCollections.observableArrayList(SourceEnergie.values()));
        cmbSource.getSelectionModel().select(SourceEnergie.MIXTE);

        cmbCategorie.setItems(FXCollections.observableArrayList(CategorieEnergie.values()));
        cmbCategorie.getSelectionModel().selectFirst();

        cmbStrategie.setItems(FXCollections.observableArrayList("Tarif fixe", "Tarif variable", "Tarif écologique"));
        cmbStrategie.getSelectionModel().selectFirst();

        colId.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getIdentifiant() == null ? 0 : d.getValue().getIdentifiant()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().typeLibelle()));
        colDenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDenomination()));
        colSurface.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getSurfaceM2()));
        colConso.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().estimerConsommationAnnuelle()));
        colCO2.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().empreinteCarbone()));

        // --- Observer : on s'abonne pour afficher un journal d'événements ---
        EventBus.INSTANCE.abonner(Evenement.Type.BATIMENT_AJOUTE, e -> ajouterEvenement("➕ " + ((Batiment) e.getPayload()).getDenomination()));
        EventBus.INSTANCE.abonner(Evenement.Type.BATIMENT_SUPPRIME, e -> ajouterEvenement("🗑 " + ((Batiment) e.getPayload()).getDenomination()));
        EventBus.INSTANCE.abonner(Evenement.Type.MESURE_AJOUTEE, e -> ajouterEvenement("📊 mesure " + e.getPayload()));
        EventBus.INSTANCE.abonner(Evenement.Type.ANOMALIE_DETECTEE, e -> ajouterEvenement("⚠ " + e.getPayload()));
        EventBus.INSTANCE.abonner(Evenement.Type.EXPORT_REALISE, e -> ajouterEvenement("📤 export → " + e.getPayload()));
        EventBus.INSTANCE.abonner(Evenement.Type.CAPTEUR_IOT, e -> ajouterEvenement("🛰 " + e.getPayload()));

        // Le simulateur IoT déclenche un rafraîchissement du dashboard à chaque relevé.
        iot.setCallbackRafraichissement(this::rafraichirTout);

        rafraichirTout();
    }

    @FXML
    public void onToggleIoT() {
        if (iot.enMarche()) {
            iot.arreter();
            if (btnIoT != null) btnIoT.setText("🛰 Démarrer capteurs IoT");
        } else {
            iot.demarrer();
            if (btnIoT != null) btnIoT.setText("⏹ Arrêter capteurs IoT");
        }
    }

    @FXML
    public void onCreerBatiment() {
        try {
            double s = Double.parseDouble(txtSurface.getText());
            int o = Integer.parseInt(txtOccupants.getText());
            Localisation l = new Localisation(txtVille.getText(), "", "", cmbZone.getValue());
            Batiment b = BatimentFactory.creer(cmbTypeBatiment.getValue(),
                    new BatimentFactory.SpecBatiment(txtDenomination.getText(), l, s, o));
            b.setSource(cmbSource.getValue());
            gestion.creer(b);
            viderFormulaire();
            rafraichirTout();
        } catch (Exception ex) { erreur(ex.getMessage()); }
    }

    @FXML
    public void onSupprimerBatiment() {
        Batiment sel = tableBatiments.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("Sélectionner un bâtiment"); return; }
        gestion.supprimer(sel.getIdentifiant());
        rafraichirTout();
    }

    @FXML
    public void onDupliquerBatiment() {
        Batiment sel = tableBatiments.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("Sélectionner un bâtiment"); return; }
        gestion.dupliquer(sel.getIdentifiant());
        rafraichirTout();
    }

    @FXML
    public void onAjouterMesure() {
        Batiment cible = cmbBatimentMesure.getValue();
        if (cible == null) { erreur("Sélectionner un bâtiment"); return; }
        try {
            double q = Double.parseDouble(txtQuantite.getText());
            Mesure m = new Mesure(cmbCategorie.getValue(), q, LocalDateTime.now());
            gestion.ajouterMesure(cible.getIdentifiant(), m);
            rafraichirTout();
        } catch (Exception ex) { erreur(ex.getMessage()); }
    }

    @FXML
    public void onChangerStrategie() {
        switch (cmbStrategie.getValue()) {
            case "Tarif variable"    -> tdb.setStrategy(new TarifVariableStrategy());
            case "Tarif écologique"  -> tdb.setStrategy(new TarifEcologiqueStrategy());
            default                  -> tdb.setStrategy(new TarifFixeStrategy());
        }
        rafraichirTout();
    }

    @FXML
    public void onExporterJson() {
        rapport.exporterJson(gestion.tousLesBatiments(), Path.of("data/rapport.json"));
        info("Export JSON → data/rapport.json");
    }

    @FXML
    public void onExporterCsv() {
        rapport.exporterCsv(gestion.tousLesBatiments(), Path.of("data/rapport.csv"));
        info("Export CSV → data/rapport.csv");
    }

    @FXML
    public void onGenererMarkdown() {
        txtRapportMarkdown.setText(rapport.genererMarkdown(gestion.tousLesBatiments()));
    }

    @FXML
    public void onImporterCsv() {
        Batiment sel = cmbBatimentMesure.getValue();
        if (sel == null) { erreur("Sélectionner un bâtiment cible (onglet Mesures)"); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer un CSV de mesures");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(tableBatiments.getScene().getWindow());
        if (f == null) return;
        try {
            List<Mesure> mesures = importCsv.lire(Path.of(f.getAbsolutePath()));
            for (Mesure m : mesures) gestion.ajouterMesure(sel.getIdentifiant(), m);
            info("Import OK : " + mesures.size() + " mesures");
            rafraichirTout();
        } catch (Exception e) { erreur(e.getMessage()); }
    }

    @FXML
    public void onGenererDonneesTest() {
        try {
            generateur.genererJeuComplet(5, 10);
            info("Jeu de test : 5 bâtiments × 10 mesures");
            rafraichirTout();
        } catch (Exception e) { erreur(e.getMessage()); }
    }

    @FXML
    public void onMeteo() {
        Batiment sel = tableBatiments.getSelectionModel().getSelectedItem();
        String ville = sel != null && sel.getLocalisation() != null
                ? sel.getLocalisation().getVille() : "Paris";
        PrevisionMeteo p = meteo.previsionDuJour(ville);
        lblMeteo.setText("☁ " + p);
    }

    @FXML
    public void rafraichirTout() {
        List<Batiment> liste = gestion.tousLesBatiments();
        tableBatiments.setItems(FXCollections.observableArrayList(liste));
        cmbBatimentMesure.setItems(FXCollections.observableArrayList(liste));
        if (!liste.isEmpty()) cmbBatimentMesure.getSelectionModel().selectFirst();

        lblNbBatiments.setText(String.valueOf(liste.size()));
        lblConsoTotale.setText(FormatUtils.formaterKwh(tdb.consommationGlobale()));
        lblCoutTotal.setText(FormatUtils.formaterEuros(tdb.coutGlobal()));
        lblCO2Total.setText(String.format("%,.0f kg CO₂", tdb.empreinteGlobaleCO2()));

        pieParType.getData().clear();
        tdb.repartitionParType().forEach((k, v) -> pieParType.getData().add(new PieChart.Data(k, v)));

        barTop5.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Conso annuelle (kWh)");
        tdb.classement(5).forEach(b -> s.getData().add(new XYChart.Data<>(b.getDenomination(), b.estimerConsommationAnnuelle())));
        barTop5.getData().add(s);

        // Courbe temporelle (PDF §5.4) + projection linéaire (§6.1 bonus)
        if (lineTemporel != null) {
            lineTemporel.getData().clear();
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName("Mesures cumulées");
            java.util.Map<String, Double> parMois = new java.util.TreeMap<>();
            for (Batiment b : liste) {
                for (Mesure m : b.getMesures()) {
                    String key = m.getHorodatage().getYear() + "-" + String.format("%02d", m.getHorodatage().getMonthValue());
                    parMois.merge(key, m.getQuantite(), Double::sum);
                }
            }
            parMois.forEach((k, v) -> serie.getData().add(new XYChart.Data<>(k, v)));
            lineTemporel.getData().add(serie);

            // Série de prévision (3 mois à venir)
            List<PredictionService.PointPrediction> projection = prediction.projeter(liste, 3);
            if (!projection.isEmpty()) {
                XYChart.Series<String, Number> prevSerie = new XYChart.Series<>();
                prevSerie.setName("Prévision (3 mois)");
                projection.forEach(p -> prevSerie.getData().add(new XYChart.Data<>(p.mois(), p.valeur())));
                lineTemporel.getData().add(prevSerie);
            }
        }

        // KPI jour / mois / année (PDF §5.3)
        java.time.LocalDate auj = java.time.LocalDate.now();
        double jour = 0, mois = 0, annee = 0;
        for (Batiment b : liste) {
            for (Mesure m : b.getMesures()) {
                java.time.LocalDate d = m.getHorodatage().toLocalDate();
                if (d.equals(auj)) jour += m.getQuantite();
                if (d.getYear() == auj.getYear() && d.getMonth() == auj.getMonth()) mois += m.getQuantite();
                if (d.getYear() == auj.getYear()) annee += m.getQuantite();
            }
        }
        if (lblConsoJour  != null) lblConsoJour.setText(FormatUtils.formaterKwh(jour));
        if (lblConsoMois  != null) lblConsoMois.setText(FormatUtils.formaterKwh(mois));
        if (lblConsoAnnee != null) lblConsoAnnee.setText(FormatUtils.formaterKwh(annee));

        // Mesures de la sélection
        Batiment selM = cmbBatimentMesure.getValue();
        listMesures.getItems().clear();
        if (selM != null) listMesures.getItems().addAll(selM.getMesures());

        // Anomalies
        panelAnomalies.getChildren().clear();
        Map<Batiment, List<RapportAnomalie>> anomalies = diagnostic.diagnostiquerTout();
        if (anomalies.isEmpty()) {
            panelAnomalies.getChildren().add(new Label("✓ Aucune anomalie"));
        } else {
            anomalies.forEach((b, ra) -> ra.forEach(r -> {
                Label l = new Label(b.getDenomination() + " → " + r);
                l.getStyleClass().add("anomalie-" + r.getSeverite().name().toLowerCase());
                panelAnomalies.getChildren().add(l);
            }));
        }

        // Recommandations d'économies (§6.1 bonus)
        if (panelRecommandations != null) {
            panelRecommandations.getChildren().clear();
            List<RecommandationService.Recommandation> conseils = reco.analyser(liste);
            if (conseils.isEmpty()) {
                panelRecommandations.getChildren().add(new Label("✓ Parc énergétique équilibré, aucune action prioritaire."));
            } else {
                conseils.forEach(c -> {
                    Label l = new Label(c.toString());
                    l.setWrapText(true);
                    l.getStyleClass().add("reco-" + c.priorite().name().toLowerCase());
                    panelRecommandations.getChildren().add(l);
                });
            }
        }
    }

    private void ajouterEvenement(String txt) {
        if (panelEvenements != null) {
            Label l = new Label("• " + txt);
            panelEvenements.getChildren().add(0, l);
            if (panelEvenements.getChildren().size() > 30)
                panelEvenements.getChildren().remove(panelEvenements.getChildren().size() - 1);
        }
    }

    private void viderFormulaire() {
        txtDenomination.clear(); txtVille.clear(); txtSurface.clear(); txtOccupants.clear();
    }

    private void info(String m)   { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void erreur(String m) { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
}
