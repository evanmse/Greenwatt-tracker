package fr.greenwatt.repository;

import fr.greenwatt.exception.StockageException;
import fr.greenwatt.factory.BatimentFactory;
import fr.greenwatt.interfaces.BatimentRepository;
import fr.greenwatt.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implémentation SQLite (alternative à la persistance JSON). */
public class BatimentSqliteRepository implements BatimentRepository {

    private final Connection conn = GestionnaireSQLite.INSTANCE.getConnection();

    @Override
    public Batiment enregistrer(Batiment b) {
        try {
            return (b.getIdentifiant() == null) ? inserer(b) : modifier(b);
        } catch (SQLException e) {
            throw new StockageException("Erreur SQLite save", e);
        }
    }

    private Batiment inserer(Batiment b) throws SQLException {
        String sql = """
            INSERT INTO batiment(type, denomination, ville, region, code_postal, zone,
                                 surface, occupants, source)
            VALUES(?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.typeLibelle());
            ps.setString(2, b.getDenomination());
            Localisation l = b.getLocalisation() != null ? b.getLocalisation() : new Localisation();
            ps.setString(3, l.getVille());
            ps.setString(4, l.getRegion());
            ps.setString(5, l.getCodePostal());
            ps.setString(6, l.getZone() != null ? l.getZone().name() : "H2");
            ps.setDouble(7, b.getSurfaceM2());
            ps.setInt(8, b.getOccupants());
            ps.setString(9, b.getSource() != null ? b.getSource().name() : SourceEnergie.MIXTE.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) b.setIdentifiant(rs.getLong(1));
            }
        }
        return b;
    }

    private Batiment modifier(Batiment b) throws SQLException {
        String sql = "UPDATE batiment SET denomination=?, surface=?, occupants=?, source=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getDenomination());
            ps.setDouble(2, b.getSurfaceM2());
            ps.setInt(3, b.getOccupants());
            ps.setString(4, b.getSource().name());
            ps.setLong(5, b.getIdentifiant());
            ps.executeUpdate();
        }
        return b;
    }

    @Override
    public Optional<Batiment> chercher(Long id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM batiment WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapper(rs));
            }
        } catch (SQLException e) {
            throw new StockageException("Erreur SQLite find", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Batiment> lister() {
        List<Batiment> out = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM batiment ORDER BY id")) {
            while (rs.next()) out.add(mapper(rs));
        } catch (SQLException e) {
            throw new StockageException("Erreur SQLite list", e);
        }
        return out;
    }

    @Override
    public void supprimer(Long id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM batiment WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StockageException("Erreur SQLite delete", e);
        }
    }

    @Override
    public long compter() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM batiment")) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new StockageException("Erreur SQLite count", e);
        }
    }

    private Batiment mapper(ResultSet rs) throws SQLException {
        Localisation.ZoneClimatique zone;
        try { zone = Localisation.ZoneClimatique.valueOf(rs.getString("zone")); }
        catch (Exception e) { zone = Localisation.ZoneClimatique.H2; }
        Localisation l = new Localisation(rs.getString("ville"), rs.getString("region"),
                rs.getString("code_postal"), zone);
        Batiment b = BatimentFactory.creer(rs.getString("type"),
                new BatimentFactory.SpecBatiment(rs.getString("denomination"), l,
                        rs.getDouble("surface"), rs.getInt("occupants")));
        b.setIdentifiant(rs.getLong("id"));
        String src = rs.getString("source");
        if (src != null) b.setSource(SourceEnergie.valueOf(src));
        return b;
    }
}
