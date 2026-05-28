package fr.greenwatt.interfaces;

/** Tout objet pouvant être persisté. */
public interface Persistable {
    Long getIdentifiant();
    void setIdentifiant(Long id);
}
