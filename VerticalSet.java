
import java.util.Collections;
import java.util.ArrayList;
import java.util.Hashtable;
/**
 * Aggiungi qui una descrizione della classe VerticalSet
 * 
 * @author (il tuo nome) 
 * @version (un numero di versione o una data)
 */
public class VerticalSet extends PitchSet
{
    public VerticalSearcher mySearcher;
    /**
     * Costruttore degli oggetti di classe  VerticalSet
     */
    public VerticalSet()
    {

    }

    public VerticalSet(PitchClass[] pP){
        super(pP);
        add(new RecursiveSearchPoint());
    }

    public VerticalSet(PitchSet ps){
        super(ps);
    }

    public boolean isProjected(PitchClass projectedPitch){
        int insertionIndex = Collections.binarySearch(this, projectedPitch);
        insertionIndex = -insertionIndex - 1;
        add(projectedPitch);
        Collections.sort(this);

        class IntervalSetTree{
            IntervalSet mySet;
            ArrayList<IntervalSet> branches;
            IntervalSetTree child;

            IntervalSetTree(IntervalSet set){
                mySet = set;
                branches = new ArrayList<IntervalSet>();
            }

            void addOverlayed(IntervalSet overlayed){
                branches.add(overlayed);
            }

            void addChild(IntervalSetTree aTree){
                if(child != null){
                    child.addChild(aTree);
                    return;
                }
                child = aTree;
            }

            boolean oneToOneCorrispondent(ArrayList<IntervalSet> availableOverlayedSets){
                for(IntervalSet overlayedSet: branches){
                    if(!availableOverlayedSets.contains(overlayedSet))
                        continue;
                    ArrayList<IntervalSet> subAvailable = (ArrayList)availableOverlayedSets.clone();
                    subAvailable.remove(overlayedSet);
                    if(child == null || child.oneToOneCorrispondent(subAvailable))
                        return true;
                }
                return false;
            }
        }

        //System.out.println("SORTED COLLECTION: " + this);
        //System.out.println("Projected? in " + this);
        ArrayList<IntervalSet> intervalSets = analyseIntervals(this);
        IntervalSetTree completeTree = null;
        IntervalSetTree subTree = null;
        for(IntervalSet is: intervalSets){
            boolean acceptablePitch = false;
            subTree = new IntervalSetTree(is);
            for(IntervalSet projectedSet: myIntervalSets)
                if(is.overlays(projectedSet)){
                    acceptablePitch = true;
                    subTree.addOverlayed(projectedSet);
                }
            if(!acceptablePitch){
                remove(projectedPitch);
                return false;
            }
            if(completeTree == null)
                completeTree = subTree;
            else
                completeTree.addChild(subTree);
        }
        remove(projectedPitch);
        return completeTree.oneToOneCorrispondent(myIntervalSets);
    }

    public boolean limited(){
        return size() == totalProjectedPitches;
    }
    
    public PitchSet copyArray(){
        return new VerticalSet(this);
    }

    public static VerticalSearcher spotCheck(){
        //Archinovica.initializeSpace();
        //new Archinovica();
        PitchClass[] pitchBinary = new PitchClass[12];
        for(int i = 0; i < 12; i++){
            if(Math.random() > 0.5)
                pitchBinary[i] = new PitchClass(i);
        }
        VerticalSet vs = new VerticalSet(pitchBinary);
        return vs.mySearcher;
    }
}
