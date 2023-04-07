
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Aggiungi qui una descrizione della classe Interval
 * 
 * @author (il tuo nome) 
 * @version (un numero di versione o una data)
 */
public class Interval extends SemioticFunction
{
    public int normativeType;
    public ArrayList<SemioticFunction> solutions;

    /**
     * Costruttore degli oggetti di classe  Interval
     */
    public Interval(int empInt)
    {
        super(null, empInt);
    }

    public Interval(int[] normInt)
    {
        super(normInt, -1);
    }

    public Interval(SemioticFunction sf){
        super(sf.signified, sf.signifier);
    }

    /*public Interval(SemioticFunction anInt, int nt)
    {
    signifier = anInt.signifier % 12;
    if(anInt.limited){
    signified = anInt.signified;
    }
    else{
    solutions = new ArrayList<SemioticFunction>();
    normativeType = nt;
    //generativeSearch();

    switch(normativeType){
    case 1:
    signified = Collections.max(solutions).signified;
    break;
    case 2:
    signified = Collections.min(solutions).signified;
    break;
    case 3:
    solutions.remove(Collections.max(solutions));
    solutions.remove(Collections.min(solutions));
    signified = solutions.get(0).signified;
    break;
    }
    }
    limited = true;
    }
     */

    public void addInterval(Interval interval){
        for(int i = 0; i < 2; i++)
            signified[i] = signified[i] + interval.signified[i];

    }

    /*public boolean pitchFound(SubSpace s){
    if(s.signifier == signifier){
    solutions.add(s);
    if(normativeType == 0 || solutions.size() == 4){
    signified = solutions.get(0).signified; // default
    solutions.remove(0);
    return true;
    }
    }
    return false;
    }

    public boolean generativeSearch(){
    SubSpace s = new SubSpace(this);
    while(!s.generateNeighbors()){

    }
    Archinovica.pitchSpace.resetSpaceCode();
    return true;
    }
     */
    public int getFoundPitches(){
        return 1;
    }

    public int expressDistance(){
        if(!limited)
            return -1;
        int d = 0;
        for(int i: signified)
            d += Math.abs(i);
        return d;
    }

    /*public double expressTheta(){
    return Math.atan2(signified[0], signified[1]);
    }
     */

    @Override
    public Interval clone(){
        if(limited)
            return new Interval(new int[]{signified[0], signified[1]});
        return new Interval(signifier);
    }

    @Override
    public String toString(){

        String a = "[";
        if(limited){
            for(int i: signified)
                a += i + " ";
            a = a.substring(0, a.length() - 1) + "]";
        }
        else
            a += signifier + "]";
        return a;
    }

}
