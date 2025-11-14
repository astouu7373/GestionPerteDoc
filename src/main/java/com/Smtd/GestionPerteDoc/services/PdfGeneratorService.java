package com.Smtd.GestionPerteDoc.services;

import com.Smtd.GestionPerteDoc.entities.Declaration;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.element.List;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public class PdfGeneratorService {

    private static String formatDate(java.util.Date date) {
        if (date == null) return "Non spécifiée";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public static byte[] generateDeclarationPdf(Declaration declaration) throws Exception {
        InputStream is = PdfGeneratorService.class.getResourceAsStream("/templates/template_police_dynamic.docx");
        if (is == null) throw new RuntimeException("Template introuvable");

        XWPFDocument doc = new XWPFDocument(is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Parcourir tous les paragraphes du Word
        for (IBodyElement element : doc.getBodyElements()) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph para = (XWPFParagraph) element;

                // Vérifier si c’est une liste
                if (para.getNumID() != null) {
                    List pdfList = new List()
                            .setSymbolIndent(12)
                            .setListSymbol("\u2022"); // puce par défaut

                    int numID = para.getNumID().intValue();
                    XWPFNumbering numbering = doc.getNumbering();
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        if (p.getNumID() != null && p.getNumID().intValue() == numID) {
                            String text = getRunText(p, declaration);
                            pdfList.add(new ListItem(text));
                        }
                    }
                    document.add(pdfList);
                    continue;
                }

                Paragraph pdfPara = new Paragraph();
                // Alignement
                switch (para.getAlignment()) {
                    case CENTER -> pdfPara.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
                    case RIGHT -> pdfPara.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT);
                    case BOTH -> pdfPara.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.JUSTIFIED);
                    default -> pdfPara.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT);
                }

                // Espacements
                pdfPara.setMarginTop(para.getSpacingBefore())
                        .setMarginBottom(para.getSpacingAfter());

                // Ajouter tous les runs (texte avec mise en forme)
                for (XWPFRun run : para.getRuns()) {
                    String text = run.text();
                    if (text == null || text.isEmpty()) continue;

                    // Remplacer placeholders
                    text = replacePlaceholders(text, declaration);

                    Text pdfText = new Text(text)
                            .setFont(PdfFontFactory.createFont("Times-Roman"))
                            .setFontSize(12);

                    if (run.isBold()) pdfText.setBold();
                    if (run.isItalic()) pdfText.setItalic();
                    if (run.isStrike()) pdfText.setStrokeWidth(0.5f);
                    if (run.getColor() != null) pdfText.setFontColor(ColorConstants.BLACK); // simplifié

                    pdfPara.add(pdfText);
                }
                document.add(pdfPara);

            }
        }

        document.close();
        return baos.toByteArray();
    }

    private static String replacePlaceholders(String text, Declaration declaration) {
        return text
                .replace("{{numero_reference}}", declaration.getNumeroReference())
                .replace("{{nom}}", declaration.getDeclarant().getNom())
                .replace("{{prenom}}", declaration.getDeclarant().getPrenom())
                .replace("{{adresse}}", declaration.getDeclarant().getAdresse())
                .replace("{{telephone}}", declaration.getDeclarant().getTelephone())
                .replace("{{email}}", declaration.getDeclarant().getEmail())
                .replace("{{type_document}}", declaration.getTypeDocument().getLibelleTypeDocument())
                .replace("{{numero_document}}", declaration.getNumeroDocument())
                .replace("{{date_perte}}", formatDate(declaration.getDatePerte()))
                .replace("{{date_jour}}", java.time.LocalDate.now().toString());
    }

    private static String getRunText(XWPFParagraph para, Declaration declaration) {
        StringBuilder sb = new StringBuilder();
        for (XWPFRun run : para.getRuns()) {
            String text = run.text();
            if (text != null) {
                sb.append(replacePlaceholders(text, declaration));
            }
        }
        return sb.toString();
    }
}
