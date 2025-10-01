package pl.kalin.simpledms;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
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
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

    private Map<DecodeHintType, Object> buildHints() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        // We target QR codes specifically in this service
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        // If using ZXing 3.5+, you can also try inverted images:
        hints.put(DecodeHintType.ALSO_INVERTED, Boolean.TRUE);
        return hints;
    }

    private List<String> decodeFromImage(File file) throws Exception {
        List<String> results = new ArrayList<>();
        BufferedImage image = ImageIO.read(file);
        if (image == null) throw new Exception("Nieprawidłowy plik graficzny");
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        // First: try multiple
        List<String> multi = decodeMultiple(bitmap);
        if (!multi.isEmpty()) {
            results.addAll(multi);
            return results;
        }

        // Fallback: single
        MultiFormatReader singleReader = new MultiFormatReader();
        singleReader.setHints(buildHints());
        try {
            Result result = singleReader.decode(bitmap);
            results.add(result.getText());
        } catch (NotFoundException e) {
            // none found
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

                // Try multiple per page first
                List<String> multi = decodeMultiple(bitmap);
                if (!multi.isEmpty()) {
                    results.addAll(multi);
                    continue;
                }

                // Fallback: single per page
                MultiFormatReader singleReader = new MultiFormatReader();
                singleReader.setHints(buildHints());
                try {
                    Result result = singleReader.decode(bitmap);
                    results.add(result.getText());
                } catch (NotFoundException e) {
                    // no codes on this page
                }
            }
        }
        return results;
    }

    private List<String> decodeMultiple(BinaryBitmap bitmap) {
        List<String> results = new ArrayList<>();
        MultiFormatReader baseReader = new MultiFormatReader();
        baseReader.setHints(buildHints());
        MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(baseReader);
        try {
            // If your ZXing version supports hints here, you can pass them as a second argument.
            Result[] multiResults = multiReader.decodeMultiple(bitmap);
            if (multiResults != null) {
                for (Result r : multiResults) {
                    results.add(r.getText());
                }
            }
        } catch (NotFoundException e) {
            // no QR codes found
        }
        return results;
    }
}
