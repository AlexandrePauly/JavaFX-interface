/**
*
* Programme Java pour débruiter des images par analyse en composantes principales
*
* @file 	Result.java
* @author  	GOUTH Thomas, GUIZAOUI Kenza, KRYL Noe, PARENT Matthieu et PAULY Alexandre
* @version 	1.0 Premier jet
* @date   	1er juin 2023
* @brief	Projet Java
* 
*/

package Interface;

import java.awt.image.BufferedImage;

/**
 * @class 	Result
 * @author  GOUTH Thomas, GUIZAOUI Kenza, KRYL Noe, PARENT Matthieu et PAULY Alexandre
 * @version 1.0
 * @date 	1er juin 2023
 */

public class Result {
    private double[] array1;
    private double[] array2;
    private BufferedImage[] image;
    
	/**
	 * @brief	Constructeur de la classe result
	 * @param 	array1 = Tableau de double
	 * @param 	array2 = Tableau de double
	 * @param 	image = Tableau de BufferedImage
	 */

    public Result(double[] array1, double[] array2, BufferedImage[] images) {
        this.array1 = array1;
        this.array2 = array2;
        this.image = images;
    }
    
	/**
	 * @brief	Accesseur de l'attribut array1
	 * @return	un tableau de double
	 */

    public double[] getArray1() {
        return array1;
    }
    
	/**
	 * @brief	Accesseur de l'attribut array2
	 * @return	un tableau de double
	 */

    public double[] getArray2() {
        return array2;
    }
    
	/**
	 * @brief	Accesseur de l'attribut image
	 * @return	un tableau de BufferedImage
	 */

    public BufferedImage[] getImage() {
        return image;
    }
}