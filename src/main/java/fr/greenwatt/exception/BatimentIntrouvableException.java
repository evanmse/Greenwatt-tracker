package fr.greenwatt.exception;

public class BatimentIntrouvableException extends GreenWattException {
    public BatimentIntrouvableException(Long id) {
        super("Aucun bâtiment trouvé pour l'identifiant " + id);
    }
}
