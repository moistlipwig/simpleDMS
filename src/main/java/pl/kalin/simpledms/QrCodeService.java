package pl.kalin.simpledms;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class QrCodeService {
    public List<String> decodeQrCodes(File file) {
        List<String> results = new ArrayList<>();
        String name = file.getName().toLowerCase();
        try {
            if (name.endsWith(".pdf")) {
                results.addAll(decodeFromPdf(file));
            } else {
                results.addAll(decodeFromImage(file));
            }
        } catch (Exception e) {
            results.add("Błąd odczytu: " + e.getMessage());
        }
        return results;
    }

    private List<String> decodeFromImage(File file) throws Exception {
        List<String> results = new ArrayList<>();
        BufferedImage image = ImageIO.read(file);
        if (image == null) throw new Exception("Nieprawidłowy plik graficzny");
        LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            results.add(result.getText());
        } catch (NotFoundException e) {
            // Spróbuj znaleźć wiele kodów QR
            results.addAll(decodeMultiple(bitmap));
        }
        return results;
    }

    private List<String> decodeFromPdf(File file) throws Exception {
        List<String> results = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file)) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                LuminanceSource source = new BufferedImageLuminanceSource(bim);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    Result result = new MultiFormatReader().decode(bitmap);
                    results.add(result.getText());
                } catch (NotFoundException e) {
                    results.addAll(decodeMultiple(bitmap));
                }
            }
        }
        return results;
    }

    private List<String> decodeMultiple(BinaryBitmap bitmap) throws Exception {
        List<String> results = new ArrayList<>();
        MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(new MultiFormatReader());
        try {
            Result[] multiResults = multiReader.decodeMultiple(bitmap);
            for (Result r : multiResults) {
                results.add(r.getText());
            }
        } catch (NotFoundException e) {
            // brak kodów QR
        }
        return results;
    }
}
