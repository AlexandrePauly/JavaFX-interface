/**
*
* Interface Java pour bruiter et débruiter des images par analyse en composantes principales
*
* @file 	Interface.java
* @author  	PAULY Alexandre
* @version 	1.0 Premier jet
* @date   	18 juin 2023
* @brief	Projet Java
* 
*/

package Interface;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import Bruitage.Bruitage;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * @class 	Interface
 * @author  PAULY Alexandre
 * @version 1.0
 * @date 	18 juin 2023
 */

public class Interface extends Application {
	private BorderPane mainContent;
    private BorderPane root;
    private VBox sideMenu;
    private Label contentLabel;
    private VBox vbox;
    private HBox buttonBox;
    private Label indicationLabel;
    private IntegerProperty checkDownloadImage = new SimpleIntegerProperty(0); //Prend 0 si aucune image n'est téléchargée, sinon vaut 1
    private IntegerProperty checkNoising = new SimpleIntegerProperty(0); //Prend 0 si l'image n'est pas bruitée, sinon vaut 1
    private IntegerProperty checkStats = new SimpleIntegerProperty(0); //Prend 0 si les stats n'ont pas besoin d'être générées sinon vaut 1
	private Integer ecarttype;
    
    private Image image;
    private ImageView imageView;
    private ImageView imageViewBruit;
    
    //Tableaux de statistiques
	double[] tabMSE;
	double[] tabPSNR;
    
    //Collection des images à afficher en grand
    private Map<String, ImageView> imageViewMap = new LinkedHashMap<>();
    
    private TabPane tabPane;
    
    private Scene scene;
    
    //Couleurs par défaut de l'interface
    private static SimpleObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.WHITE);
    private static SimpleObjectProperty<Color> tabColor = new SimpleObjectProperty<>(Color.WHITE);
    
	/**
	* @brief Interface graphique de l'application
	* @param primaryStage = Fenêtre principale
	*/

    @Override
    public void start(Stage primaryStage) {    	
        // Création d'une BorderPane pour disposer le menu en haut, le menu latéral et le contenu de la page
        root = new BorderPane();
        root.setTop(createMenu(primaryStage));
        root.setLeft(createSideMenu());
        root.setStyle("-fx-background-color: " + toHex(backgroundColor.get()));
       
        // Création du TabPane pour gérer les onglets du contenu de la page
        tabPane = new TabPane();
        
        // Création d'un nouvel onglet avec un contenu généré
        Tab newTab = new Tab("Nouvel onglet");
        BorderPane tabContent = new BorderPane();
        ScrollPane content = createTabContent();
        tabContent.setCenter(content);
        newTab.setContent(tabContent);
        newTab.setClosable(false);
        
        // Modification du style de l'onglet
        newTab.setStyle("-fx-background-color: " + toHex(tabColor.get()));
        
        tabColor.addListener((observable, oldValue, newValue) -> {
            newTab.setStyle("-fx-background-color: " + toHex(newValue));
        });

        // Ajout de l'onglet à la TabPane à l'index 0
        tabPane.getTabs().add(0, newTab);

        // Sélection de l'onglet nouvellement ajouté
        tabPane.getSelectionModel().select(newTab);
        
        // Ajout des bordures et marges pour délimiter les zones
        sideMenu.setStyle("-fx-border-color: grey; -fx-border-width: 0 1 0 0;");
        sideMenu.setPadding(new Insets(10));
        contentLabel = new Label("");
        root.setCenter(contentLabel);

        // Initialisation de mainContent (vérifiez si vous avez cette ligne dans votre code)
        mainContent = new BorderPane();

        // Ajout des marges à mainContent
        BorderPane.setMargin(mainContent, new Insets(0));

        // Ajouter le TabPane au centre du BorderPane
        root.setCenter(tabPane);

        // Modification du style du fond d'écran
        backgroundColor.addListener((observable, oldValue, newValue) -> {
            root.setStyle("-fx-background-color: " + toHex(newValue));
        });
        
        tabColor.addListener((observable, oldValue, newValue) -> {
            newTab.setStyle("-fx-background-color: " + toHex(newValue));
        });

        // Création de la scène
        scene = new Scene(root, 680, 480);
        primaryStage.setTitle("Débruitage d'images par ACP");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Appel de la fonction pour créer les raccourcis clavier
        shortcuts(primaryStage);
        
        // Enregistrer la référence de l'instance de MainApp dans les propriétés du Stage
        primaryStage.getProperties().put("Interface", this);
    }
    
	/**
	* @brief Méthode pour créer la barre de menu de la fenêtre
	* @param primaryStage = Fenêtre principale
	* @return menuBar = Barre de menu de type MenuBar
	*/

    private MenuBar createMenu(Stage primaryStage) {
    	//Création d'une barre de menu au top de la fenêtre
        MenuBar menuBar = new MenuBar();

        //Création des menus
        Menu fileMenu = new Menu("Fichier");
        Menu editMenu = new Menu("Édition");
        Menu viewMenu = new Menu("Affichage");
        Menu helpMenu = new Menu("Aide");

        //Création des éléments de menu avec les raccourcis pour Fichier
        MenuItem newItem = new MenuItem("Nouveau");
        newItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        MenuItem openItem = new MenuItem("Ouvrir");
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        MenuItem saveItem = new MenuItem("Enregistrer");
        MenuItem saveAsItem = new MenuItem("Enregistrer sous");
        saveAsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        MenuItem exitItem = new MenuItem("Quitter");
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+W"));

        //Création des éléments de menu avec les raccourcis pour Edition
        MenuItem cutItem = new MenuItem("Couper");
        MenuItem copyItem = new MenuItem("Copier");
        MenuItem pasteItem = new MenuItem("Coller");
        MenuItem deleteItem = new MenuItem("Supprimer");
        MenuItem selectAllItem = new MenuItem("Sélectionner tout");

        //Création des éléments de menu avec les raccourcis pour Affichage
        MenuItem zoomInItem = new MenuItem("Zoom avant");
        zoomInItem.setAccelerator(KeyCombination.keyCombination("Ctrl+ADD"));
        MenuItem zoomOutItem = new MenuItem("Zoom arrière");
        zoomOutItem.setAccelerator(KeyCombination.keyCombination("Ctrl+SUBTRACT"));
        MenuItem styleItem = new MenuItem("Apparence");
        //styleItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));

        //Création des éléments de menu avec les raccourcis pour Aide
        MenuItem aboutItem = new MenuItem("À propos");
        
        //Ajout d'un onglet lors du click
        newItem.setOnAction(event -> openNewWindow());
        
        //Fermeture de la fenêtre lors du click
	    exitItem.setOnAction(event -> {
	        primaryStage.close();
	    });
	    
	    //Agrandissement de la fenêtre lors du click
	    zoomInItem.setOnAction(event -> {
	        primaryStage.setWidth(primaryStage.getWidth() * 1.1);
	        primaryStage.setHeight(primaryStage.getHeight() * 1.1);
	    });
	    
	    //Rétrecissement de la fenêtre lors du ckick
	    zoomOutItem.setOnAction(event -> {
	        primaryStage.setWidth(primaryStage.getWidth() * 0.9);
	        primaryStage.setHeight(primaryStage.getHeight() * 0.9);
	    });
	    
	    openItem.setOnAction(event -> {
        	FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                String imageURL = selectedFile.toURI().toString();
                image = new Image(imageURL);
                imageView.setImage(image);
                indicationLabel.setVisible(false);
                checkDownloadImage.set(1);
            }
            
            //Ajout de l'image à la collection
            imageViewMap.put("Image originale", imageView);
	    });
	    
	    saveAsItem.setOnAction(event -> {
	    	saveImages();
	    });
	   	    
	    //Modification de l'apparence
	    styleItem.setOnAction(event -> {
	    	//Si la couleur de l'interface est light, elle devient dark
	    	if (backgroundColor.get() == Color.WHITE) {
	    	    backgroundColor.set(Color.DARKGRAY);
	    	    tabColor.set(Color.DARKGRAY);
	    	}
	    	//Snon si la couleur de l'interface est dark, elle devient light
	    	else if (backgroundColor.get() == Color.DARKGRAY) {
	    	    backgroundColor.set(Color.WHITE);
	    	    tabColor.set(Color.WHITE);
	    	}	    	
	    });
	    	 
        //Ajout des éléments de menu aux menus correspondants
        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, new SeparatorMenuItem(), exitItem);
        editMenu.getItems().addAll(cutItem, copyItem, pasteItem, deleteItem, new SeparatorMenuItem(), selectAllItem);
        viewMenu.getItems().addAll(zoomInItem, zoomOutItem, styleItem);
        helpMenu.getItems().addAll(aboutItem);

        //Ajout des menus à la barre de menu
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

        return menuBar;
    }
    
	/**
	* @brief Méthode pour créer le menu latéral de la fenêtre
	* @return sideMenu = Menu latéral de type VBox
	*/

    private VBox createSideMenu() {
    	//Création de la VBox pour le menu latéral
        sideMenu = new VBox();
        sideMenu.setPadding(new Insets(10));
        sideMenu.setSpacing(20);

        //Création du bloc "Bruitage"
        VBox bruitageSection = new VBox();
        bruitageSection.setStyle("-fx-background-color: lightgrey; -fx-padding: 10;");
        Label labelBruitage = new Label("Bruitage");
        labelBruitage.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

        //Création de la Slider pour les valeurs du bruit
        Slider sliderBruit = new Slider(10, 30, 10);
        sliderBruit.setShowTickMarks(true);
        sliderBruit.setShowTickLabels(true);
        sliderBruit.setMajorTickUnit(10);
        sliderBruit.setMinorTickCount(0);
        sliderBruit.setSnapToTicks(true);
        sliderBruit.setStyle("-fx-pref-width: 100px; -fx-min-width: 100px; -fx-max-width: 100px;");

        //Création du bouton pour bruiter l'image
        Button buttonBruitage = new Button("Bruitage");
        buttonBruitage.setStyle("-fx-pref-width: 100px; -fx-min-width: 100px; -fx-max-width: 100px;");
                       
        //Actions du bouton de bruitage
        buttonBruitage.setOnAction(event -> {
        	//Initialisation d'une matrice à partir de l'image
            int[][] tabImage = Bruitage.convertirImageNB(image);
            
            //Préparation du bruit à appliquer pour le bruitage
            Integer ecartType = (int) sliderBruit.getValue();
            ecarttype = ecartType;
            int[][] imagebruite = Bruitage.noisingNB(tabImage, ecartType);
            
            //On récupère l'image bruitée
            BufferedImage[] images = new BufferedImage[3];
            images[1] = Bruitage.convertirImageBuffered(imagebruite);

            //Conversion de l'image d'origine et de l'image bruitée en JavaFX Image
            BufferedImage bufferedImageBruit = images[1];
            Image fxImageBruit = SwingFXUtils.toFXImage(bufferedImageBruit, null);  
           
            //Création d'une nouvelle instance de ImageView pour l'image bruitée
            imageViewBruit = new ImageView(fxImageBruit);
            imageViewBruit.setPreserveRatio(true);
            imageViewBruit.setFitWidth(200);
            imageViewBruit.setFitHeight(Region.USE_COMPUTED_SIZE);
            imageViewBruit.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

            //Création d'un conteneur HBox pour afficher les images côte à côte
            HBox imageBox = new HBox(10);
            imageBox.setAlignment(Pos.CENTER);
            imageBox.getChildren().addAll(imageView, imageViewBruit);
            
            //Ajout du nouveau conteneur HBox avec les images
            vbox.getChildren().add(0, imageBox);
            
            //Ajustement de la largeur de la fenêtre en fonction de la largeur de l'image bruitée
            Stage stage = (Stage) buttonBruitage.getScene().getWindow();
            stage.setWidth(imageViewBruit.getFitWidth() + 590);
            
            //Ajout des images à une liste pour les afficher en grand par la suite
            imageViewMap.put("Image bruitée", imageViewBruit);
            
            //Modification de la valeur de checkNoising pour débloquer la partie de dévérouillage
            checkNoising.set(1);
        });
        
        //Ajout des éléments à la section "Bruitage"
        VBox.setMargin(labelBruitage, new Insets(0, 0, 10, 0));
        VBox.setMargin(sliderBruit, new Insets(0, 0, 10, 0));
        VBox.setMargin(buttonBruitage, new Insets(0, 0, 10, 0));
        bruitageSection.getChildren().addAll(labelBruitage, sliderBruit, buttonBruitage);
       
        //Création du bloc "Débruitage"
        VBox debruitageSection = new VBox();
        debruitageSection.setStyle("-fx-background-color: lightgrey; -fx-padding: 10;");
        Label labelDebruitage = new Label("Débruitage");
        labelDebruitage.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");
        
        //Création de la liste déroulante pour l'approche du débruitage
        VBox approachBox = new VBox();
        Label labelApproach = new Label("Approche :");
        ComboBox<String> approachComboBox = new ComboBox<>();
        approachComboBox.setStyle("-fx-pref-width: 100px; -fx-min-width: 100px; -fx-max-width: 100px;");
        approachComboBox.getItems().addAll("Globale", "Locale");
        approachBox.getChildren().addAll(labelApproach, approachComboBox);

        //Création de la liste déroulante pour le seuillage
        VBox thresholdingBox = new VBox();
        Label labelThresholding = new Label("Seuillage :");
        ComboBox<String> thresholdingComboBox = new ComboBox<>();
        thresholdingComboBox.setStyle("-fx-pref-width: 100px; -fx-min-width: 100px; -fx-max-width: 100px;");
        thresholdingComboBox.getItems().addAll("Doux", "Dur");
        thresholdingBox.getChildren().addAll(labelThresholding, thresholdingComboBox);

        //Création de la liste déroulante pour le choix du seuil
        VBox vBThresholdingBox = new VBox();
        Label labelVBThresholding = new Label("Seuil :");
        ComboBox<String> vBThresholdingComboBox = new ComboBox<>();
        vBThresholdingComboBox.setStyle("-fx-pref-width: 100px; -fx-min-width: 100px; -fx-max-width: 100px;");
        vBThresholdingComboBox.getItems().addAll("Seuil V", "Seuil B");
        vBThresholdingBox.getChildren().addAll(labelVBThresholding, vBThresholdingComboBox);
        
        CheckBox fullGlobal = new CheckBox("Approche Globale");
        CheckBox fullLocal = new CheckBox("Approche Locale");

        Button buttonDebruitage = new Button("Débruiter");
        buttonDebruitage.setStyle("-fx-pref-width: 100px; -fx-min-width: 100px; -fx-max-width: 100px;");

        sliderBruit.disableProperty().bind(Bindings.equal(checkDownloadImage, 0));
        buttonBruitage.disableProperty().bind(Bindings.equal(checkDownloadImage, 0));
        
        approachComboBox.disableProperty().bind(Bindings.equal(checkNoising, 0));
        thresholdingComboBox.disableProperty().bind(Bindings.equal(checkNoising, 0));
        vBThresholdingComboBox.disableProperty().bind(Bindings.equal(checkNoising, 0));
        fullGlobal.disableProperty().bind(Bindings.equal(checkNoising, 0));
        fullLocal.disableProperty().bind(Bindings.equal(checkNoising, 0));

        buttonDebruitage.disableProperty().bind(
            Bindings.createBooleanBinding(() -> {
                boolean fullOptionsSelected = fullGlobal.isSelected() || fullLocal.isSelected();
                boolean approachSelected = approachComboBox.getValue() != null;
                boolean thresholdingSelected = thresholdingComboBox.getValue() != null;
                boolean vBThresholdingSelected = vBThresholdingComboBox.getValue() != null;

                return !fullOptionsSelected && (!approachSelected || !thresholdingSelected || !vBThresholdingSelected);
            }, fullGlobal.selectedProperty(),
            fullLocal.selectedProperty(),
            approachComboBox.valueProperty(),
            thresholdingComboBox.valueProperty(),
            vBThresholdingComboBox.valueProperty())
        );

        // Désactiver l'approche locale si l'approche globale est sélectionnée et vice versa
        fullGlobal.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                fullLocal.setSelected(false);
            }
        });

        fullLocal.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                fullGlobal.setSelected(false);
            }
        });
              
        //Actions du bouton de débruitage
        buttonDebruitage.setOnAction(event -> {
        	showAlert("Ce projet étant un projet de groupe, vous ne trouverez que ma partie (interface et bruitage d'une image).");
        });
                
        //Ajout des éléments à la section "Débruitage"
        VBox.setMargin(labelDebruitage, new Insets(0, 0, 10, 0));
        VBox.setMargin(approachBox, new Insets(0, 0, 10, 0));
        VBox.setMargin(thresholdingBox, new Insets(0, 0, 10, 0));
        VBox.setMargin(vBThresholdingBox, new Insets(0, 0, 10, 0));
        VBox.setMargin(fullGlobal, new Insets(0, 0, 10, 0));
        VBox.setMargin(fullLocal, new Insets(0, 0, 10, 0));
        VBox.setMargin(buttonDebruitage, new Insets(0, 0, 10, 0));
        debruitageSection.getChildren().addAll(labelDebruitage, approachBox, thresholdingBox, vBThresholdingBox, fullGlobal, fullLocal, buttonDebruitage);

        sideMenu.getChildren().addAll(bruitageSection, debruitageSection);
        
        return sideMenu;
    }
    
	/**
	* @brief Raccourcis clavier de l'application
	* @param primaryStage = Fenêtre principale
	*/
    
    private void shortcuts(Stage primaryStage) {
        //Création des raccourcis clavier pour les actions de zoom
    	KeyCombination zoomInShortcut = new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN);
    	KeyCombination zoomOutShortcut = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN);
    	KeyCombination closeShortcut = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
    	KeyCombination apparence = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
    	KeyCombination newWindow = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    	KeyCombination saveShortcut = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    	KeyCombination imgShortcut = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);

        //Associer les raccourcis clavier aux actions
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
        	//Si l'action est un zoom
            if (zoomInShortcut.match(event)) {
                primaryStage.setWidth(primaryStage.getWidth() * 1.1);
                primaryStage.setHeight(primaryStage.getHeight() * 1.1);
                
                //Empêcher d'autres traitements du raccourci clavier
                event.consume();
            }
            //Sinon si c'est un dézoom
            else if (zoomOutShortcut.match(event)) {
                primaryStage.setWidth(primaryStage.getWidth() * 0.9);
                primaryStage.setHeight(primaryStage.getHeight() * 0.9);
                
                //Empêcher d'autres traitements du raccourci clavier
                event.consume();
            }
            //Sinon si la fenêtre est fermée
            else if (closeShortcut.match(event)) {
            	// Fermer la fenêtre
    	        primaryStage.close();
                event.consume(); // Empêcher d'autres traitements du raccourci clavier
            }
            //Sinon si un nouvel onglet est créé
            else if (newWindow.match(event)) {
            	openNewWindow();
            	
            	//Empêcher d'autres traitements du raccourci clavier
                event.consume();
            }
            //Sinon si une sauvegarde est demandée
            else if (saveShortcut.match(event)) {
            	saveImages();
            	
            	//Empêcher d'autres traitements du raccourci clavier
                event.consume();
            }
            //Sinon si une image est ouverte
            else if (imgShortcut.match(event)) {
            	FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Sélectionner une image");
                File selectedFile = fileChooser.showOpenDialog(null);
                if (selectedFile != null) {
                    String imageURL = selectedFile.toURI().toString();
                    image = new Image(imageURL);
                    imageView.setImage(image);
                    indicationLabel.setVisible(false);
                    checkDownloadImage.set(1);
                }
                
                //Ajout de l'image à la collection
                imageViewMap.put("Image originale", imageView);
            }
            //Sinon si l'apparence est modifiée
            else if (apparence.match(event)) {            	
    	    	//Si la couleur de l'interface est light, elle devient dark
    	    	if (backgroundColor.get() == Color.WHITE) {
    	    	    backgroundColor.set(Color.DARKGRAY);
    	    	    tabColor.set(Color.DARKGRAY);
    	    	}
    	    	//Snon si la couleur de l'interface est dark, elle devient light
    	    	else if (backgroundColor.get() == Color.DARKGRAY) {
    	    	    backgroundColor.set(Color.WHITE);
    	    	    tabColor.set(Color.WHITE);
    	    	}	
            }
        });
    }
    
	/**
	* @brief Méthode pour créer le bouton d'ajout d'un onglet
	* @return addButton = Boutton pour créer un onglet
	*/
   /*
    private Label createAddButton() {
    	//Création d'un label
        Label addButton = new Label("+");
        addButton.getStyleClass().add("add-button");
        addButton.setPadding(new Insets(5));
        addButton.setAlignment(Pos.CENTER);
        HBox.setHgrow(addButton, Priority.NEVER);
        
        return addButton;
    }
    */
	/**
	* @brief Méthode pour créer un nouvel onglet
	* @param primaryStage = Fenêtre principale
	*/
/*
    private void addTab(Stage primaryStage) {
        //Création d'un nouvel onglet avec un contenu généré
        Tab newTab = new Tab("Nouvel onglet");
        BorderPane tabContent = new BorderPane();
        ScrollPane content = createTabContent();
        tabContent.setCenter(content);
        newTab.setContent(tabContent);
        
        //Modification du style de l'onglet
        newTab.setStyle("-fx-background-color: " + toHex(tabColor.get()));
        
        tabColor.addListener((observable, oldValue, newValue) -> {
        	newTab.setStyle("-fx-background-color: " + toHex(newValue));
        });

        //Ajout de l'onglet à la TabPane
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);

        //Sélection de l'onglet nouvellement ajouté
        tabPane.getSelectionModel().select(newTab);
    }
    */
	/**
	* @brief Méthode pour créer une barre de scroll et le contenu de l'onglet
	* @return scrollPane = Contenu de l'onglet et sa barre pour scroller
	*/

    private ScrollPane createTabContent() {
        //Création du contenu de l'onglet
        vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        
        //Modification du style
        vbox.setStyle("-fx-background-color: " + toHex(tabColor.get()));
        
        tabColor.addListener((observable, oldValue, newValue) -> {
        	vbox.setStyle("-fx-background-color: " + toHex(newValue));
        });
        
        //Création du ScrollPane pour le contenu de l'onglet
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        //Création de l'ImageView pour afficher une image avec une bordure
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);
        imageView.setFitHeight(Region.USE_COMPUTED_SIZE);
        imageView.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        
        //Création de l'indication pour sélectionner une image
        indicationLabel = new Label("Sélectionnez une image");
        indicationLabel.setStyle("-fx-text-fill: gray;");
        VBox.setMargin(indicationLabel, new Insets(10));

        //Ajout de l'imageView et de l'indication au vbox
        vbox.getChildren().addAll(imageView, indicationLabel);

        //Création des boutons en bas de la page
        buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button chooseButton = new Button("Choisir une image");
        Button saveButton = new Button("Sauvegarder");
        Button showButton = new Button("Afficher");
        Button statsButton = new Button("Statistiques");
        
        saveButton.disableProperty().bind(Bindings.equal(checkDownloadImage, 0));
        showButton.disableProperty().bind(Bindings.equal(checkDownloadImage, 0));
        statsButton.disableProperty().bind(Bindings.equal(checkStats, 0));
                              
        buttonBox.getChildren().addAll(chooseButton, saveButton, showButton, statsButton);
        VBox.setMargin(buttonBox, new Insets(10));

        vbox.getChildren().add(buttonBox);
        
        //Action du bouton pour sélectionner une image dans les fichiers
        chooseButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                String imageURL = selectedFile.toURI().toString();
                image = new Image(imageURL);
                imageView.setImage(image);
                indicationLabel.setVisible(false);
                checkDownloadImage.set(1);
            }
            
            //Ajout de l'image à la collection
            imageViewMap.put("Image originale", imageView);
        });
        
        //Action du bouton pour sauvegarder les images dans ses dossiers
        saveButton.setOnAction(event -> {
        	saveImages();
        });
        
        showButton.setOnAction(event -> {
            Stage stage = new Stage();
            stage.setTitle("Affichage");

            GridPane gridPane = new GridPane();
            gridPane.setAlignment(Pos.CENTER);
            gridPane.setHgap(10);
            gridPane.setVgap(10);

            Iterator<ImageView> imageViewIterator = imageViewMap.values().iterator();
            Iterator<String> imageNameIterator = imageViewMap.keySet().iterator();
            
            int rowIndex = 0;
            int colIndex = 0;

            while (imageViewIterator.hasNext() && imageNameIterator.hasNext()) {
                ImageView imageView = imageViewIterator.next();
                String imageName = imageNameIterator.next();

                VBox imageBox = new VBox();
                imageBox.setAlignment(Pos.CENTER);
                imageBox.setSpacing(5);

                Label titleLabel = new Label(imageName);
                titleLabel.setAlignment(Pos.CENTER);
                titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-opacity: 0.8;");

                ImageView displayImageView = new ImageView(imageView.getImage());

                imageBox.getChildren().addAll(titleLabel, displayImageView);
                //container.getChildren().add(imageBox);
                
                gridPane.add(imageBox, colIndex, rowIndex);

                colIndex++;
                if (colIndex >= 3) {
                    colIndex = 0;
                    rowIndex++;
                }
            }
            
            ScrollPane scrollpane = new ScrollPane(gridPane);
            scrollpane.setFitToWidth(true);
            scrollpane.setFitToHeight(true);

            Scene scene = new Scene(scrollpane);
            stage.setScene(scene);
            
            // Ajout d'un padding de 10 à la fenêtre
            gridPane.setPadding(new Insets(20));
            
            stage.show();
        });

        //Affichage des statistiques
        statsButton.setOnAction(event -> {
            //Vérifier si les tableaux ne sont pas vides
            if (tabMSE != null && tabMSE.length > 0 && tabPSNR != null && tabPSNR.length > 0) {
                //Création des séries de données pour les graphiques
                XYChart.Series<Number, Number> mseSeries = new XYChart.Series<>();
                XYChart.Series<Number, Number> psnrSeries = new XYChart.Series<>();

                //Ajout des données aux séries
                for (int i = 0; i < tabMSE.length; i++) {
                    mseSeries.getData().add(new XYChart.Data<>(i, tabMSE[i]));
                    psnrSeries.getData().add(new XYChart.Data<>(i, tabPSNR[i]));
                }

                //Création des graphiques
                NumberAxis mseXAxis = new NumberAxis();
                mseXAxis.setLabel("Numéro de l'image");
                NumberAxis mseYAxis = new NumberAxis();
                mseYAxis.setLabel("Valeur du MSE");
                LineChart<Number, Number> mseChart = new LineChart<>(mseXAxis, mseYAxis);
                mseChart.getData().add(mseSeries);
                
                NumberAxis psnrXAxis = new NumberAxis();
                psnrXAxis.setLabel("Numéro de l'image");
                NumberAxis psnrYAxis = new NumberAxis();
                psnrYAxis.setLabel("Valeur du PSNR");
                LineChart<Number, Number> psnrChart = new LineChart<>(psnrXAxis, psnrYAxis);
                psnrChart.getData().add(psnrSeries);
                
                if(checkStats.get() == 1) {
                	mseChart.setTitle("Évolution du MSE pour un débruitage par approche globale et un écart-type de " + ecarttype);
                	psnrChart.setTitle("Évolution du PSNR pour un débruitage par approche globale et un écart-type de " + ecarttype);
                }
                else if(checkStats.get() == 2){
                	mseChart.setTitle("Évolution du MSE pour un débruitage par approche locale et un écart-type de " + ecarttype);
                	psnrChart.setTitle("Évolution du PSNR pour un débruitage par approche locale et un écart-type de " + ecarttype);
                }
                else {
                	mseChart.setTitle("Évolution du MSE en fonction des différentes méthodes de débruitage pour un écart-type de " + ecarttype);
                	psnrChart.setTitle("Évolution du PSNR en fonction des différentes méthodes de débruitage pour un écart-type de " + ecarttype);
                }
                
                //Création d'une fenêtre pour afficher les graphiques
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(statsButton.getScene().getWindow());
                stage.setTitle("Statistiques de la qualité du débruitage");

                VBox chartBox = new VBox(10);
                chartBox.setAlignment(Pos.CENTER);
                chartBox.getChildren().addAll(mseChart, psnrChart);
                
                Scene scene = new Scene(chartBox);
                stage.setScene(scene);
                stage.show();
            }
        });

        return scrollPane;
    }
    
	/**
	* @brief Méthode pour sauvegarder les images dans un dossier
	* @return scrollPane = Contenu de l'onglet et sa barre pour scroller
	*/
    
    private void saveImages() {
    	//Si l'image originale a été importée
		if (imageView.getImage() != null) {
			if (imageViewBruit.getImage() != null) {
				//Création d'une instance de FileChooser
	            FileChooser fileChooser = new FileChooser();
	            fileChooser.setTitle("Enregistrer les images");
	
	            //Affichage de la boîte de dialogue de sauvegarde
	            Window window = imageView.getScene().getWindow();
	            File selectedDirectory = fileChooser.showSaveDialog(window);
	
	            if (selectedDirectory != null) {
	                //Création du dossier pour les images
	                File folder = new File(selectedDirectory.getAbsolutePath());
	                if (!folder.exists()) {
	                    folder.mkdirs();
	                }
	
	                //Enregistrement de chaque image
	                try {
	                    //Conversion de ImageView en BufferedImage
	                    BufferedImage image1 = SwingFXUtils.fromFXImage(imageView.getImage(), null);
	                    BufferedImage image2 = SwingFXUtils.fromFXImage(imageViewBruit.getImage(), null);
	
	                    //Obtenir l'extension du fichier d'origine
	                    String extension = getImageExtension(imageView.getImage());
	                    
	                    //Retirer le premier caractère de l'extension
	                    if (extension.length() > 1) {
	                        extension = extension.substring(1);
	                    }
	
	                    //Enregistrement des images dans le dossier avec l'extension d'origine
	                    File file1 = new File(folder, "image." + extension);
                    	File file2 = new File(folder, "image_bruitee." + extension);
                    	
                    	ImageIO.write(image1, extension, file1);
                    	ImageIO.write(image2, extension, file2);
	
	                    //Affichage d'une alerte
	                    showAlert("Images enregistrées avec succès !");
	                } catch (IOException e) {
	                	//Affichage d'une alerte
	                    showAlert("Erreur lors de l'enregistrement des images : " + e.getMessage());
	                }
		    	}
			}
		}
		//Snon si l'image originale n'a pas été importée, on affiche un message d'erreur
		else {
			//Affichage d'une alerte
			showAlert("Vous n'avez téléchargé aucune image.");
		}
    }
    
	/**
	* @brief Méthode pour récupérer l'extension d'une image
	* @param image = Image dont il faut récupérer l'extension
	* @return une chaîne de caractère de la valeur de l'extension
	*/

    private String getImageExtension(Image image) {
        String extension = ".png"; // Extension par défaut

        String url = image.getUrl();
        int lastDotIndex = url.lastIndexOf(".");
        if (lastDotIndex >= 0 && lastDotIndex < url.length() - 1) {
            extension = url.substring(lastDotIndex);
        }

        return extension;
    }
    
	/**
	* @brief Création et affiche d'une alerte sur l'interface
	* @param message = Message contenu dans l'alerte
	*/
    
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
	/**
	* @brief Méthode pour récupérer le code hexadécimal d'une image
	* @param color = Couleur dont on va récupérer le code hexadécimal
	* @return une chaîne de caractère du code hexadécimal
	*/
    
    private String toHex(Color color) {
        return "#" + color.toString().substring(2, 8);
    }
    
	/**
	* @brief Méthode pour créer une nouvelle fenêtre
	*/
    
    private void openNewWindow() {
        Interface newApp = new Interface();
        Stage newStage = new Stage();
        newApp.start(newStage);
    }
    
	/**
	* @brief Méthode principale
	*/

    public static void main(String[] args) {
    	//Appel de fonction pour lancer l'interface de l'application
        launch(args);
    }
}