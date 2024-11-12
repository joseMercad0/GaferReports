package com.example.gaferreports;

import android.content.Context;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFUtils {

    public static void createPDF(Context context, File pdfFile, String enterpriseName, String date, String startTime, String endTime, List<TrapEntry> traps) throws IOException {
        PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
        PdfReader reader = new PdfReader(context.getAssets().open("DETALLE DE ESTACIONES.pdf"));
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        Document document = new Document(pdfDoc);

        // Escribir datos en el PDF
        writePdfData(document, enterpriseName, date, startTime, endTime, traps);

        document.close();
    }



    private static void writePdfData(Document document, String enterpriseName, String date, String startTime, String endTime, List<TrapEntry> traps) throws IOException {
        PdfDocument pdfDoc = document.getPdfDocument();
        com.itextpdf.kernel.pdf.canvas.PdfCanvas pdfCanvas = new com.itextpdf.kernel.pdf.canvas.PdfCanvas(pdfDoc.getFirstPage());

        // Establecer el color del texto
        pdfCanvas.setColor(new DeviceRgb(0, 0, 0), true);

        // Escribir el nombre de la empresa
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 10);
        pdfCanvas.beginText();
        pdfCanvas.moveText(209, 690);
        pdfCanvas.showText(enterpriseName);
        pdfCanvas.endText();

        // Escribir la fecha
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 10);
        pdfCanvas.beginText();
        pdfCanvas.moveText(209, 665);
        pdfCanvas.showText(date);
        pdfCanvas.endText();

        // Escribir la hora de inicio
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 10);
        pdfCanvas.beginText();
        pdfCanvas.moveText(394, 665);
        pdfCanvas.showText(startTime);
        pdfCanvas.endText();

        // Coordenadas para las trampas
        pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 5);
        float startX = 103;
        float startY = 603;
        float offsetY = 21.5f;

        for (int i = 0; i < traps.size(); i++) {
            TrapEntry trap = traps.get(i);

            if (i == 25) { // Cambiar a una nueva columna después de 25 trampas
                startX = 350;
                startY = 620;
            }

            boolean isNoAccess = (trap.isNoAccess());  // Verifica si noAccess es true

            if (isNoAccess) {
                // Si la trampa no se pudo registrar por inaccesibilidad, mostrar el cuadro de texto blanco
                pdfCanvas.saveState();
                pdfCanvas.setFillColor(new DeviceRgb(255, 255, 255));  // Fondo blanco para el cuadro
                Rectangle rect = new Rectangle(startX, startY - 7, 250, 20);  // Ajusta el tamaño del cuadro
                pdfCanvas.rectangle(rect);
                pdfCanvas.fill();
                pdfCanvas.restoreState();

                // Escribir el texto "Esta trampa no se pudo registrar por inaccesibilidad" dentro del cuadro
                pdfCanvas.beginText();
                pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 12);  // Ajusta la fuente y el tamaño si es necesario
                pdfCanvas.moveText(startX + 5, startY);  // Mover el texto un poco a la derecha
                pdfCanvas.showText("Esta trampa no se pudo registrar por inaccesibilidad");
                pdfCanvas.endText();
            }else {
                // Datos normales de la trampa
                pdfCanvas.beginText();
                pdfCanvas.moveText(startX, startY);
                pdfCanvas.showText(trap.getTrapType());
                pdfCanvas.endText();

                pdfCanvas.beginText();
                pdfCanvas.moveText(startX + 50, startY);
                pdfCanvas.showText(trap.getPoisonType() + " (" + trap.getPoisonAmount() + ")");
                pdfCanvas.endText();

                pdfCanvas.beginText();
                pdfCanvas.moveText(startX + 100, startY);
                pdfCanvas.showText(trap.isConsumption() ? String.valueOf(trap.getConsumptionPercentage()) + "%" : "Ninguno");
                pdfCanvas.endText();

                pdfCanvas.beginText();
                pdfCanvas.moveText(startX + 150, startY);
                pdfCanvas.showText(trap.isReplace() ? trap.getReplacePoisonType() + " (" + trap.getReplaceAmount() + ")" : "Ninguna");
                pdfCanvas.endText();
            }

            startY -= offsetY;
        }
    }

}
