/**
*
* Programme Java pour bruiter des images
*
* @file 	Bruitage.java
* @author  	PAULY Alexandre
* @version 	1.0 Premier jet
* @date   	18 juin 2023
* @brief	Projet Java
* 
*/

package Bruitage;

import java.awt.image.BufferedImage;
import java.util.Random;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * @class 	Bruitage
 * @author  PAULY Alexandre
 * @version 1.0
 * @date 	18 juin 2023
 */

public class Bruitage {
	
	/**
	* @brief 	Méthode pour convertir une image en tableau de tableau avec la valeur rgb de chaque tableaux
	* @param 	image = Image à convertir en tableau
	* @return 	Le tableau de tableau correspondant à l'image donnée en paramètres
	*/
	
	public static int[][] convertirImageNB(Image image) {
		//Si la conversion de l'image en tableaux fonctionne
	    try {
	        //Création d'un BufferedImage à partir de l'objet Image
	        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

	        //Initialisation des dimensions de l'image
	        int width = bufferedImage.getWidth();
	        int height = bufferedImage.getHeight();

	        //Définition du nouveau tableau
	        int[][] tabImage = new int[height][width];
	        
	        //Itération de la conversion de l'image en tableau
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                //Récupération de la valeur RVB du pixel
	                int rgb = bufferedImage.getRGB(x, y);
	                int composant = (rgb >> 16) & 0xFF;
	                tabImage[y][x] = composant;
	            }
	        }
	        
	        //Retourne le tableau de tableau de l'image
	        return tabImage;
	    }
	    //Si elle ne fonctionne pas
	    catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	/**
	* @brief 	Méthode pour convertir un tableau de tableau avec la valeur rgb de chaque tableaux en BufferedImage
	* @param 	tabImage = Tableau à convertir en image
	* @return 	L'image correspondant au tableau de tableau donné en paramètres
	*/
	
	public static BufferedImage convertirImageBuffered(int[][] tabImage) {
		//Initialisation de la largeur et de la longueur de l'image à partir du tableau
		int width = tabImage[0].length;
		int height = tabImage.length;
		
		//Création d'un BufferedImage pour stocker les valeurs de chaque valeur du tableau
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		//Itération de la conversion du tableau de tableau en BufferedImage
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		int newRgb = (tabImage[y][x] << 16) | (tabImage[y][x]<< 8) | tabImage[y][x];
                image.setRGB(x, y, newRgb);
	        }
	    }
	    //Retourne l'image du tableau de tableau
	    return(image);
	}
	
	/**
	* @brief 	Méthode pour bruiter une image
	* @param 	image = Image à bruiter
	* @param 	ecartType = Ecart-type du bruit à ajouter à l'image
	* @return 	l'image bruité en noir et blanc
	*/
	
	public static int[][] noisingNB(int[][] image, Integer ecartType){
		//Initialisation de la largeur et de la longueur de l'image à partir du tableau
		int width = image[0].length;
		int height = image.length;
		
		//Création d'un tableau pour stocker les valeurs de chaque pixels de l'image bruitée
		int[][] imageBruitee = new int[height][width];
		
		//Bruitage de chaque pixel à partir du bruit donné
		for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
            	//Ajout d'un bruit gaussien à chaque pixel
            	Random rand = new Random();
            	int bruit = (int) (rand.nextGaussian() * ecartType);
            	imageBruitee[y][x] = Math.max(0, Math.min(255, image[y][x] + bruit));
            }
        }
		
		//Retourne l'image bruitée
		return imageBruitee;
	}
}

