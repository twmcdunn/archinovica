

import java.util.ArrayList;
/**
 * Aggiungi qui una descrizione della classe PotentialPitch
 * 
 * @author (il tuo nome) 
 * @version (un numero di versione o una data)
 */
public class PotentialPitch extends PitchClass
{
    private ArrayList<RecursiveSearchPoint> sources;
    private int totalDistanceFromAllSources;
    public PotentialPitch(RecursiveSearchPoint rsp){
        super(new int[]{rsp.signified[0], rsp.signified[1]});
        sources = new ArrayList<RecursiveSearchPoint>();
        totalDistanceFromAllSources = 0;
    }
    
    public void addSource(RecursiveSearchPoint rsp){
        sources.add(rsp);
        totalDistanceFromAllSources += rsp.getDistanceSearched();
    }
    
    public int getDistanceSearched(){
        return totalDistanceFromAllSources;
    }
    
    public ArrayList<RecursiveSearchPoint> getSources(){
        return sources;
    }
    
    @Override
    public String toString(){
        return "PP: " + super.toString() + " | SOURCES: " + sources;
    }
}
