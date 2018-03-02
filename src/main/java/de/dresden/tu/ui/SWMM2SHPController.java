package de.dresden.tu.ui;

import de.dresden.tu.SWMM2SHP;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.util.Optional;

public class SWMM2SHPController {

    @FXML
    private ComboBox crsComboBox;
    @FXML
    private Button selectFileButton;
    @FXML
    private Button selectFolderButton;
    @FXML
    private Button startTransformationButton;
    @FXML
    private TextField selectFolderTextField;
    @FXML
    private TextField selectFileTextField;

    public void initialize() throws FactoryException {
        crsComboBox.getItems().addAll(
                "EPSG:31469",
                "EPSG:4326"
        );
        crs = CRS.decode(crsComboBox.getItems().get(0).toString());
    }

    private File inpFile = null;
    private String outputFolder = null;
    private CoordinateReferenceSystem crs = null;

    public void selectFile(ActionEvent actionEvent) {
        File file = new File(this.getClass().getClassLoader().getResource("eschdorf_v6_20141208.inp").getFile());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select SWMM input file (.inp)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SWMM inp Files", "*.inp")
        );
        fileChooser.setInitialDirectory(file.getParentFile());
        File selectedFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (selectedFile != null) {
            inpFile = selectedFile;
            selectFileTextField.setText(selectedFile.getAbsolutePath());
        }
        updateTransformationButtonVisibility();
    }

    public void selectFolder(ActionEvent actionEvent) {
        File file = new File(this.getClass().getClassLoader().getResource("eschdorf_v6_20141208.inp").getFile());
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select folder for shapefiles");
        directoryChooser.setInitialDirectory(file.getParentFile());
        File selectedFolder = directoryChooser.showDialog(selectFolderButton.getScene().getWindow());
        if (selectedFolder != null) {
            outputFolder = selectedFolder.getAbsolutePath();
            selectFolderTextField.setText(selectedFolder.getAbsolutePath());
        }
        updateTransformationButtonVisibility();
    }

    public void comboBoxSelect(ActionEvent actionEvent) {
        try {
            String crsCode = crsComboBox.getSelectionModel().getSelectedItem().toString();
            crs = CRS.decode(crsCode);
            updateTransformationButtonVisibility();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    public void startTransformation(ActionEvent actionEvent) {
        try {
            SWMM2SHP swwm2shp = new SWMM2SHP();
            boolean successful;
            if (crs != null || inpFile != null || outputFolder != null) {
                successful = swwm2shp.run(inpFile, outputFolder, crs);
                if (successful){
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Transformation Information");
                    alert.setHeaderText("Transformation successful");
                    alert.initModality(Modality.WINDOW_MODAL);
                    alert.initStyle(StageStyle.UTILITY);

                    ButtonType buttonTypeOkAndClose = new ButtonType("OK & Close");

                    alert.getButtonTypes().setAll(buttonTypeOkAndClose);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == buttonTypeOkAndClose){
                        Platform.exit();
                        System.exit(0);
                    }
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Transformation Information");
                    alert.setHeaderText("Transformation NOT successful");
                    alert.initModality(Modality.WINDOW_MODAL);
                    alert.initStyle(StageStyle.UTILITY);
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTransformationButtonVisibility(){
        if (crs != null  && inpFile != null && outputFolder != null){
            startTransformationButton.setDisable(false);
        } else
            startTransformationButton.setDisable(true);
    }
}

