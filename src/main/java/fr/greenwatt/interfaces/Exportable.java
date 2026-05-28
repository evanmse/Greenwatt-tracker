package fr.greenwatt.interfaces;

/** Capacité d'exporter un objet dans plusieurs formats textuels. */
public interface Exportable {

    String versJson();

    String versCsv();

    /** Export Markdown pour rapports lisibles. */
    String versMarkdown();
}
