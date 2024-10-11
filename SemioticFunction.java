
import java.util.Arrays;
import java.util.ArrayList;
/**
 * Classe astratta SemioticFunction - descrivi qui la classe
 * 
 * @author: 
 * Date: 
 */
public abstract class SemioticFunction implements Comparable<SemioticFunction>, Cloneable
{
    // variabili d'istanza - sostituisci l'esempio che segue con il tuo
    public int signifier;
    public int[] signified;
    public boolean limited;
    /**
     * Un esempio di metodo - aggiungi i tuoi commenti
     * 
     * @param  y    un parametro d'esempio per un metodo
     * @return    la somma di x e y
     */

    public SemioticFunction(){
        signified = new int[]{0, 0};
        signifier = getSignifier(signified);
        limited = true;
    }

    public SemioticFunction(int[] np, int emp)
    {
        //super(np);
        signified = np;
        if(emp < 0)
            signifier = getSignifier(signified);
        else
            signifier = emp;
        limited = (signified != null);
        //myClass = this;
    }

    public SemioticFunction(int[] transformation, SemioticFunction pc)
    {
        signified = new int[2];
        for(int i = 0; i < 2; i++)
            signified[i] = pc.signified[i] + transformation[i];
        signifier = getSignifier(signified);
        limited = true;
        //myClass = this;
    }

    public int getSignifier(int[] np){
        int epc = np[0] * 7 + np[1] * 4;
        while(epc < 0)
            epc += 12;
        while(epc > 11)
            epc -= 12;
        return epc;
    }

    public double getMidiPb(){
        //return (64 + 0.6272 * signified[0] - 4.3776 * signified[1]);
        double pb = (64 +  (2 * 32 / 100.0) * signified[0] + (-14 * 32 / 100.0) * signified[1]);
        //System.out.println("PB: " + pb);
        return pb;       
    }

    @Override
    public int compareTo(SemioticFunction s){
        return  signifier - s.signifier;
    }

    /*public int[] getSignified(int ep){
    IntervalSet is = new IntervalSet(new Interval(ep));

    return null;
    }
     */
    @Override
    public boolean equals(Object o){
        if(o==null) return false;
        if(limited && ((SemioticFunction)o).limited)
            return Arrays.equals(signified, ((SemioticFunction)o).signified);
        return signifier == ((SemioticFunction)o).signifier;
    }

    @Override
    public String toString(){
        if(!limited)
            return signifier +"";
        String a = "[";
        for(int i: signified)
            a += i + " ";
        a = a.substring(0, a.length() - 1) + "]";
        return "[" + signified[0] + " " + signified[1] + "]";
    }

    public int[] getTransformation(SemioticFunction pc){
        int[] trans = new int[2];
        for(int i = 0; i < 2; i++)
            trans[i] = pc.signified[i] - signified[i];
        return trans;
    }

    public void delimit(){
        RecursiveSearchPoint rsp = new RecursiveSearchPoint();
        HorizontalSet destination = new HorizontalSet(signifier);
        ArrayList<RecursiveSearchPoint> solutions = null;
        do
        solutions = rsp.generateNeighbors(destination, false);
        while(solutions.size() == 0);
        signified = solutions.get(0).signified;
    }

    public boolean isHigher(SemioticFunction p){
        return signifier > p.signifier;
    }
}
