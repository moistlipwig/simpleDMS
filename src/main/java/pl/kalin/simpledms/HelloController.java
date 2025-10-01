package pl.kalin.simpledms;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private Label qrResultLabel;

    @Autowired
    private QrCodeService qrCodeService;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onSelectFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik z QR");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Obrazy i PDF", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif", "*.pdf"),
                new FileChooser.ExtensionFilter("Wszystkie pliki", "*.*")
        );
        Window window = welcomeText.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            List<String> results = qrCodeService.decodeQrCodes(file);
            StringBuilder sb = new StringBuilder();
            for (String res : results) {
                System.out.println("Odczytano QR: " + res);
                sb.append(res).append("\n");
            }
            qrResultLabel.setText(sb.length() > 0 ? sb.toString() : "Nie znaleziono kodów QR");
        }
    }
}
