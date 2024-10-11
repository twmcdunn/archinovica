
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
/**
 * Aggiungi qui una descrizione dell'interfaccia Searcher
 * 
 * @author (il tuo nome) 
 * @version (un numero di versione o una data)
 */

public abstract class GenerativeSearcher
{
    public Grid myGrid;
    public PitchSet mySourceSet, myProjectedSet, mySearchableSources;
    /**
     * Un esempio di header di metodo - aggiungi i tuoi commenti
     * 
     * @param  y    un parametro d'esempio per il metodo
     * @return    il risultato prodotto dal metodo
     */

    public GenerativeSearcher(PitchSet source, PitchSet projected){
        mySourceSet = source;
        myProjectedSet = projected;
        resetSearch();
    }

    public ArrayList<RecursiveSearchPoint> findNextPitches(boolean animate){
        ArrayList<RecursiveSearchPoint> solutions = new ArrayList<RecursiveSearchPoint>();
        // System.out.println("MYGRID: " + myGrid);
        while(solutions.size() == 0){
            //System.out.println("SEARCHING SEARCHABLE SOURCES...");
            for(PitchClass source: mySearchableSources)
                solutions.addAll(((RecursiveSearchPoint)source).generateNeighbors(myProjectedSet, animate));
        }
        //System.out.println("Solutions: " + solutions);
        myGrid.addPitches(solutions);
        return solutions;
    }

    /*public void search(){
    ArrayList<RecursiveSearchPoint> solutions = new ArrayList<RecursiveSearchPoint>();
    for(PitchClass source: mySearchableSources){
    solutions.addAll(((RecursiveSearchPoint)source).generateNeighbors(myProjectedSet));
    System.out.println("PITCHESFOUND: " + solutions);
    }
    myGrid.addPitches(solutions);
    }
     */

    public abstract PitchSet limitSet(int setting);

    public abstract PitchSet getSearchableSources();

    /*private PitchSet getCentricityOrderedSet(PitchSet ps){
    PitchSet orderedSet = new PitchSet();
    PitchClass center = ps.getCenter();
    orderedSet.add(center);
    int upperBound = orderedSet.size();
    int lowerBound = 0;
    while(orderedSet.size() < ps.size()){
    for(PitchClass pc: ps){
    while(upperBound - lowerBound > 1){
    int index = (upperBound + lowerBound) / 2;
    if(orderedSet.get(index).getInterval(center).expressDistance() <
    pc.getInterval(center).expressDistance())
    lowerBound = index;
    else
    upperBound = index;
    }
    orderedSet.add((upperBound + lowerBound) / 2, pc);
    }
    }
    return orderedSet;
    }*/

    public void resetSearch(){
        mySourceSet = mySourceSet.clone();
        Collections.sort(mySourceSet);
        myGrid = new Grid();
        myGrid.setCenter(mySourceSet.get(0));
        mySearchableSources = getSearchableSources();
        //System.out.println("SEARCH RESET");
    }

    public class Grid extends ArrayList<PotentialPitch>
    {
        private PotentialPitch[][] myGrid;
        private int[] offSets;

        public Grid(){
            myGrid = new PotentialPitch[50][50];
            offSets = new int[]{0, 0};
        }

        public void setCenter(SemioticFunction sf){
            for(int i = 0; i < 2; i++)
                offSets[i] =  25 - sf.signified[i];
        }

        public PotentialPitch generatePP(RecursiveSearchPoint rsp){
            PotentialPitch pp = new PotentialPitch(rsp);
            myGrid[rsp.signified[0] + offSets[0]][rsp.signified[1] + offSets[1]] = pp;
            add(pp);
            return pp;
        }

        public PotentialPitch getPP(RecursiveSearchPoint rsp){
            //System.out.println("getPP: " + rsp + " offSets: " + offSets[0] + "," + offSets[1]);
            PotentialPitch pp = myGrid[rsp.signified[0] + offSets[0]][rsp.signified[1] + offSets[1]];
            if(pp == null)
                pp = generatePP(rsp);
            return pp;
        }

        public void addRSP(RecursiveSearchPoint rsp){
            PotentialPitch pp = getPP(rsp);
            pp.addSource(rsp.parent);
        }

        public void addPitches(ArrayList<RecursiveSearchPoint> rsp){
            for(RecursiveSearchPoint anRSP: rsp)
                addRSP(anRSP);
        }
    }

    class Centricity implements Comparator<PitchClass>{
        PitchClass center;
        Centricity(PitchClass c){
            center = c;
        }

        //must avoid zero to prevent eratic behavior!
        public int compare(PitchClass a, PitchClass b){
            int difference = center.distanceFrom(a) - center.distanceFrom(b);
            if(difference == 0 && !Archinovica.legacyBehavior){
                //use an arbitrary def of centrality:
                //a pitch is nearer the center when it is
                //closer to the center PCN when accending a
                //chromatic scale.
                difference = ((12 + a.signifier - center.signifier) % 12)
                - ((12 + b.signifier - center.signifier) % 12); //we add twelve to make sure we're dealing w/ positive numbers
                //System.out.println("WARNING: RESULTS MAY VARY FROM VERSION 0.9");
            }
            return difference;
        }

    }

    class Excentricity implements Comparator<PitchClass>{
        PitchClass center;
        Excentricity(PitchClass c){
            center = c;
        }

        public int compare(PitchClass a, PitchClass b){
            return  center.distanceFrom(b) - center.distanceFrom(a);
        }

    }

}

