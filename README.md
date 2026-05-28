# 🌱 GreenWatt Tracker

Application JavaFX 21 / Java 17 / Maven pour le suivi **écologique** des bâtiments et de leur empreinte énergétique.

## 🚀 Lancement

```bash
cd greenwatt-tracker
mvn javafx:run
```

## 🧪 Tests

```bash
mvn test
```

## 📁 Architecture

```
fr.greenwatt
├── model/         Batiment (abstract), Maison, Appartement, Bureau, LocalCommercial,
│                  BatimentUniversitaire, AutreBatiment, Mesure, Localisation,
│                  PrevisionMeteo, Saison, SourceEnergie, CategorieEnergie, RapportAnomalie
├── interfaces/    ConsommationCalculable, Exportable, Analysable, Persistable,
│                  BatimentRepository, MesureRepository, TarificationStrategy,
│                  EvenementListener
├── factory/       BatimentFactory (mode registry)
├── strategy/      TarifFixe / TarifVariable / TarifEcologique
├── observer/      EventBus (Singleton enum), Evenement
├── repository/    BatimentJsonRepository (Jackson, par défaut),
│                  GestionnaireSQLite (Singleton enum), BatimentSqliteRepository
├── service/       GestionBatimentsService, TableauDeBordService,
│                  DiagnosticService, ComparateurTarifsService,
│                  PrevisionMeteoService, RapportService
├── controller/    AppController (JavaFX, TabPane)
├── utils/         JsonMapperFactory, Validateur, FormatUtils
└── exception/     GreenWattException + sous-types
```

## 🎯 Concepts POO illustrés

| Concept | Où |
|---|---|
| **Héritage** | `Batiment` abstrait → Maison, Appartement, Bureau, LocalCommercial, BatimentUniversitaire, AutreBatiment |
| **Polymorphisme** | `estimerConsommation(Saison)`, `estimerCout()`, `getDescription()` |
| **Abstraction** | `Batiment` abstrait, interfaces multiples |
| **Interfaces (ISP)** | `ConsommationCalculable`, `Exportable`, `Analysable`, `Persistable` |
| **DIP** | Services dépendent de `BatimentRepository`, pas de l'implémentation |
| **Factory (registry)** | `BatimentFactory` enregistre les types dans une `Map` (extensible sans modifier le code) |
| **Singleton (enum)** | `EventBus`, `GestionnaireSQLite` |
| **Strategy** | 3 tarifications interchangeables (incluant tarif écologique avec taxe CO₂) |
| **Observer** | `EventBus` notifie le contrôleur, journal d'événements en direct |
| **Prototype** | `Batiment.clone()` (clonage profond) |

## ✨ Fonctionnalités

- CRUD de 6 types de bâtiments (dont un type « Autre » paramétrable)
- Calcul saisonnier (hiver / printemps / été / automne) + facteur de zone climatique (H1/H2/H3)
- Source d'énergie par bâtiment (verte / nucléaire / mixte / fossile) → empreinte CO₂
- Mesures multi-catégories (électricité, eau, gaz, chauffage, clim, solaire)
- 3 stratégies tarifaires interchangeables
- Tableau de bord (4 KPIs + PieChart + BarChart)
- Détection d'anomalies à 3 niveaux de sévérité
- Export JSON / CSV / Markdown
- Journal d'événements en temps réel (Observer)
- Persistance JSON (Jackson) par défaut, SQLite en alternative
- Météo réelle via l'API publique **Open-Meteo** (géocodage + relevé température/couverture nuageuse), avec repli automatique en mode hors-ligne

### 🚀 Fonctionnalités avancées (bonus §6 du cahier des charges)

- **Prédiction de consommation à H+3 mois** par régression linéaire (méthode des moindres carrés) — affichée comme seconde série sur la courbe temporelle.
- **Recommandations d'économies** à 3 niveaux de priorité (🔴 élevée, 🟠 moyenne, 🟡 info), basées sur des règles métier explicables : surconsommation > 140 % de la moyenne du parc, source d'énergie fossile, déséquilibre de répartition, volatilité (coefficient de variation) élevée.
- **Simulateur de capteurs IoT** (`CapteurIoTService` + `Timeline` JavaFX) : injecte des relevés aléatoires plausibles à intervalle régulier, publiés via l'`EventBus` — utile pour démontrer en live le rafraîchissement réactif de l'IHM.

## 📊 UML — Diagramme de classes (Mermaid)

```mermaid
classDiagram
    direction LR

    class ConsommationCalculable {
        <<interface>>
        +estimerConsommation(Saison) double
        +estimerConsommationAnnuelle() double
        +estimerCout(TarificationStrategy) double
    }
    class Exportable {
        <<interface>>
        +versJson() String
        +versCsv() String
        +versMarkdown() String
    }
    class Analysable {
        <<interface>>
        +diagnostiquer() List~RapportAnomalie~
        +calculerTendances() Map~String,Double~
        +empreinteCarbone() double
    }
    class Persistable {
        <<interface>>
        +getIdentifiant() Long
        +setIdentifiant(Long) void
    }
    class TarificationStrategy {
        <<interface>>
        +calculerCout(double, SourceEnergie) double
        +nom() String
    }
    class BatimentRepository {
        <<interface>>
        +enregistrer(Batiment) Batiment
        +chercher(Long) Optional
        +lister() List
        +supprimer(Long) void
    }
    class EvenementListener {
        <<interface>>
        +onEvenement(Evenement) void
    }

    class Batiment {
        <<abstract>>
        #Long identifiant
        #String denomination
        #Localisation localisation
        #double surfaceM2
        #int occupants
        #SourceEnergie source
        #List~Mesure~ mesures
        +abstract estimerConsommation(Saison)
        +estimerCout(TarificationStrategy)
        +abstract getDescription()
        +clone() Batiment
        +ajouterMesure(Mesure)
    }

    class Maison
    class Appartement
    class Bureau
    class LocalCommercial
    class BatimentUniversitaire

    class Mesure
    class Localisation
    class RapportAnomalie
    class PrevisionMeteo

    class TarifFixeStrategy
    class TarifVariableStrategy
    class TarifEcologiqueStrategy

    class BatimentFactory {
        +creer(String, SpecBatiment) Batiment
        +enregistrer(String, Function)
        +typesDisponibles() Set
    }

    class EventBus {
        <<Singleton enum>>
        INSTANCE
        +abonner(Type, Listener)
        +publier(Evenement)
    }
    class Evenement

    class GestionnaireSQLite {
        <<Singleton enum>>
        INSTANCE
        +getConnection() Connection
    }
    class BatimentJsonRepository
    class BatimentSqliteRepository

    class GestionBatimentsService
    class TableauDeBordService
    class DiagnosticService
    class ComparateurTarifsService
    class PrevisionMeteoService
    class RapportService

    class AppController

    class GreenWattException
    class BatimentIntrouvableException
    class MesureInvalideException
    class StockageException
    class RapportException

    %% Héritage
    Batiment <|-- Maison
    Batiment <|-- Appartement
    Batiment <|-- Bureau
    Batiment <|-- LocalCommercial
    Batiment <|-- BatimentUniversitaire

    GreenWattException <|-- BatimentIntrouvableException
    GreenWattException <|-- MesureInvalideException
    GreenWattException <|-- StockageException
    GreenWattException <|-- RapportException

    %% Implémentations
    Batiment ..|> ConsommationCalculable
    Batiment ..|> Exportable
    Batiment ..|> Analysable
    Batiment ..|> Persistable
    TarifFixeStrategy ..|> TarificationStrategy
    TarifVariableStrategy ..|> TarificationStrategy
    TarifEcologiqueStrategy ..|> TarificationStrategy
    BatimentJsonRepository ..|> BatimentRepository
    BatimentSqliteRepository ..|> BatimentRepository

    %% Composition / Agrégation
    Batiment "1" *-- "1" Localisation
    Batiment "1" *-- "*" Mesure
    Analysable ..> RapportAnomalie

    %% Associations
    BatimentFactory ..> Batiment : crée
    GestionBatimentsService --> BatimentRepository
    GestionBatimentsService --> EventBus
    TableauDeBordService --> BatimentRepository
    TableauDeBordService --> TarificationStrategy
    DiagnosticService --> BatimentRepository
    DiagnosticService --> EventBus
    RapportService --> EventBus
    BatimentSqliteRepository --> GestionnaireSQLite
    AppController --> GestionBatimentsService
    AppController --> TableauDeBordService
    AppController --> DiagnosticService
    AppController --> RapportService
    AppController --> PrevisionMeteoService
    AppController ..|> EvenementListener : via lambda
    EventBus o-- "*" EvenementListener
```

## 🧭 Cas d'utilisation (Mermaid)

```mermaid
graph TB
    User((👤 Gestionnaire))
    Admin((👤 Administrateur))
    Meteo((☁ API Météo))

    subgraph "GreenWatt Tracker"
        U1[Créer un bâtiment]
        U2[Modifier un bâtiment]
        U3[Supprimer un bâtiment]
        U4[Dupliquer un bâtiment]
        U5[Saisir une mesure]
        U6[Consulter le tableau de bord]
        U7[Visualiser les graphiques]
        U8[Diagnostiquer les anomalies]
        U9[Comparer les tarifications]
        U10[Exporter un rapport JSON/CSV/MD]
        U11[Configurer la source d'énergie]
        U12[Consulter la météo]
        U13[Choisir la persistance JSON/SQLite]
    end

    User --> U1
    User --> U2
    User --> U3
    User --> U4
    User --> U5
    User --> U6
    User --> U7
    User --> U8
    User --> U9
    User --> U10
    User --> U11
    Admin --> U13
    U6 --> U7
    U6 -.->|extend| U12
    U12 --> Meteo
    U8 -.->|include| U6
```

## 🔄 Diagramme de séquence — ajout d'une mesure

```mermaid
sequenceDiagram
    actor User as 👤 Utilisateur
    participant UI as Onglet Mesures (FXML)
    participant Ctrl as AppController
    participant Svc as GestionBatimentsService
    participant Bat as Batiment
    participant Repo as BatimentJsonRepository
    participant Bus as EventBus
    participant Diag as DiagnosticService

    User->>UI: saisir (catégorie, quantité)
    UI->>Ctrl: onAjouterMesure()
    Ctrl->>Ctrl: Validateur.verifierMesure(m)
    alt mesure invalide
        Ctrl-->>UI: MesureInvalideException
        UI-->>User: ⚠ Erreur
    else mesure valide
        Ctrl->>Svc: ajouterMesure(id, m)
        Svc->>Repo: chercher(id)
        Repo-->>Svc: Optional<Batiment>
        Svc->>Bat: ajouterMesure(m)
        Svc->>Repo: enregistrer(b)
        Repo->>Repo: persister sur disque (Jackson)
        Svc->>Bus: publier(MESURE_AJOUTEE)
        Bus->>Ctrl: onEvenement → journal mis à jour
        Ctrl->>Diag: diagnostiquerTout()
        Diag-->>Ctrl: anomalies
        Ctrl->>UI: rafraichir tableau de bord
        UI-->>User: ✓ mesure enregistrée
    end
```

## 🔮 Pistes d'évolution

- IA prédictive (Smile, DL4J) pour prévoir la consommation à H+30j
- Capteurs IoT via MQTT (Eclipse Paho)
- Authentification + chiffrement SQLCipher
- Mode multi-utilisateurs avec PostgreSQL
- Packaging natif via `jpackage`
