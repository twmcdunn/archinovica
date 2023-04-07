

import java.util.ArrayList;
/**
 * Classe astratta SemioticGroup - descrivi qui la classe
 * 
 * @author: 
 * Date: 
 */
public abstract class SemioticGroup<E extends SemioticFunction> extends ArrayList<E> implements Comparable<SemioticGroup<E>>
{

    public int compareTo(SemioticGroup<E> gs){
        int difference = 0;
        for(E finalElement: gs)
            for(E initialElement: this)
                difference += initialElement.compareTo(finalElement);
        return difference;
    }
}
