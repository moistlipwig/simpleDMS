package pl.kalin.simpledms;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HelloController {

    @FXML
    private Label resultLabel;

    @FXML
    protected void onFileChooseButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik z kodem QR");
        File file = fileChooser.showOpenDialog(resultLabel.getScene().getWindow());

        if (file != null) {
            List<String> codes = decodeQrCodes(file);
            if (codes.isEmpty()) {
                resultLabel.setText("Nie znaleziono kodów QR");
            } else {
                resultLabel.setText(String.join("\n", codes));
                codes.forEach(code -> System.out.println("QR code: " + code));
            }
        }
    }

    private List<String> decodeQrCodes(File file) {
        List<String> codes = new ArrayList<>();
        String name = file.getName().toLowerCase(Locale.ROOT);
        try {
            if (name.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(file)) {
                    PDFRenderer renderer = new PDFRenderer(document);
                    for (int page = 0; page < document.getNumberOfPages(); page++) {
                        BufferedImage image = renderer.renderImageWithDPI(page, 300);
                        codes.addAll(decodeImage(image));
                    }
                }
            } else {
                BufferedImage image = ImageIO.read(file);
                codes.addAll(decodeImage(image));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return codes;
    }

    private List<String> decodeImage(BufferedImage image) {
        List<String> codes = new ArrayList<>();
        if (image == null) {
            return codes;
        }
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        GenericMultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
        try {
            Result[] results = multiReader.decodeMultiple(bitmap);
            for (Result result : results) {
                codes.add(result.getText());
            }
        } catch (NotFoundException e) {
            // no codes found
        }
        return codes;
    }
}

