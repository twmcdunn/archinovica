
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Comparator;
/**
 * Aggiungi qui una descrizione della classe HorizontalSearcher
 * 
 * @author (il tuo nome) 
 * @version (un numero di versione o una data)
 */
public class HorizontalSearcher extends GenerativeSearcher
{
    public ArrayList<HorizontalSet> potentialSets, solutions;
    public HorizontalSet untransposedSet;

    public HorizontalSearcher(PitchSet source, HorizontalSet projection){
        super(source, projection);
        untransposedSet = projection;
        potentialSets = new ArrayList<HorizontalSet>();
        solutions = new ArrayList<HorizontalSet>();
    }

    public PitchSet limitSet(int setting){

        while(solutions.size() < 4){
            ArrayList<RecursiveSearchPoint> nextPitches = findNextPitches(true);
            ArrayList<HorizontalSet> foundThisIteration = new ArrayList<HorizontalSet>();
            // System.out.println("HS: nextPitches: " + nextPitches);
            for(RecursiveSearchPoint rsp: nextPitches){
                boolean pitchRegistered = false;
                for(HorizontalSet potentialSet: potentialSets){
                    if(potentialSet.contains(rsp)){
                        //System.out.println(potentialSet + " contains " + rsp);
                        pitchRegistered = true;
                        if(potentialSet.registerPitch(rsp) && potentialSet.fullyRegistered()){
                            if(!solutions.contains(potentialSet) && !foundThisIteration.contains(potentialSet)){
                                int insertionIndex = Collections.binarySearch(solutions, potentialSet, new CentralityIndex());
                                insertionIndex = -insertionIndex - 1;
                                solutions.add(insertionIndex, potentialSet);
                                /*
                                int insertionIndex = Collections.binarySearch(foundThisIteration, potentialSet, new CentralityIndex());
                                insertionIndex = -insertionIndex  - 1;
                                foundThisIteration.add(potentialSet);
                                 */
                                //equals with respect to the distance implicetly considered in the search
                                //algorithm will be ordered with respect to centralityIndex
                            }
                            //System.out.println("FULLY REGISTERD: " + potentialSet);
                        }
                        break;
                    }
                }
                if(!pitchRegistered){
                    HorizontalSet transposedSet = (HorizontalSet)untransposedSet.clone();
                    int projectionIndex = untransposedSet.getProjectionIndex(rsp);
                    Interval transposition = untransposedSet.getArray()[projectionIndex].getInterval(rsp);
                    transposedSet.transpose(transposition);
                    transposedSet.registerPitch(rsp);
                    if(setting == 0)
                        return transposedSet;
                    transposedSet.setCentralityIndex(potentialSets.size());
                    if(transposedSet.fullyRegistered()
                    && !solutions.contains(transposedSet) && !foundThisIteration.contains(transposedSet)){
                        int insertionIndex = Collections.binarySearch(solutions, transposedSet, new CentralityIndex());
                        insertionIndex = -insertionIndex  - 1;
                        solutions.add(insertionIndex, transposedSet);

                        /*int insertionIndex = Collections.binarySearch(foundThisIteration, transposedSet, new CentralityIndex());
                        insertionIndex = -insertionIndex  - 1;
                        foundThisIteration.add(transposedSet);
                         */
                        //equals with respect to the distance implicetly considered in the search
                        //algorithm will be ordered with respect to centralityIndex
                    }
                    //System.out.println("FULLY REGISTERD: " + transposedSet);

                    potentialSets.add(transposedSet);
                }
            }
            //solutions.addAll(foundThisIteration);
        }
        //System.out.println("NUMBER OF HORIZONTAL SOLUTIONS: " + solutions.size());
        while(solutions.size() > 4)
            solutions.remove(solutions.size() - 1);
        /*System.out.println("SOLUTIONS ARRAY: ");
        for(PitchSet s: solutions)
        System.out.println("        " + s);
         */
        solutions.remove(0); // the defalut, case 0 (already returned)
        Collections.sort(solutions, new Intonation());
        switch(setting){
            case 1:
            return solutions.get(0);
            case 2:
            return solutions.get(2);
            case 3:
            return solutions.get(1);
        }
        // VerticalSet vs = new VerticalSet(solution);

        return null;
    }

    public PitchSet getSearchableSources(){
        PitchSet searchableSources = mySourceSet.clone();
        PitchClass center = mySourceSet.getCenter();
        Collections.sort(searchableSources, new Centricity(center));
        /*System.out.println("SEARCHABLE SOURCES: ");
        for(PitchClass pc: searchableSources)
            System.out.println("    " + pc + " (aka " + pc.signifier + ")");
            */

        //System.out.println("CENTRALIZED SET: " + searchableSources);
        return searchableSources;
    }

    class Intonation implements Comparator<HorizontalSet>{
        public int compare(HorizontalSet a, HorizontalSet b){
            int difference = (int)(1000 * (getPbTotal(a) - getPbTotal(b)));
            if(difference == 0){
                System.out.println("EQUAL INTONATION DIFFERENCES!!!  IT'S A ROUNDING ERROR MIRACLE!");
            }
            return difference;
        }

        public double getPbTotal(HorizontalSet hs){
            int total = 0;
            for(PitchClass pc: hs)
                total += pc.getMidiPb();
            return total;
        }
    }

    class CentralityIndex implements Comparator<HorizontalSet>{
        public int compare(HorizontalSet a, HorizontalSet b){
            int difference = a.getCentralityIndex() - b.getCentralityIndex();
            return difference;
        }
    }

    public static PitchSet spotCheck(){
        PitchClass[] pitchBinary = new PitchClass[12];
        boolean hasAtleastOnePitch = false;
        boolean hasAtLeastOneNull = false;
        for(int i = 0; i < 12; i++){
            if(Math.random() > 0.5){
                pitchBinary[i] = new PitchClass(i);
                hasAtleastOnePitch = true;
            }
            else
                hasAtLeastOneNull = true;
        }
        if(!hasAtleastOnePitch){
            int randomIndex = (int)(12 * Math.random());
            pitchBinary[randomIndex] = new PitchClass(randomIndex);
        }
        if(!hasAtLeastOneNull){
            int randomIndex = (int)(12 * Math.random());
            pitchBinary[randomIndex] = null;
        }

        VerticalSet vs = new VerticalSet(pitchBinary);
        VerticalSearcher vSearch = new VerticalSearcher(vs);
        PitchSet lastSet = vSearch.limitSet(0);

        PitchClass[] pitchBinary1 = new PitchClass[12];
        hasAtleastOnePitch = false;
        hasAtLeastOneNull = false;
        for(int i = 0; i < 12; i++){
            if(Math.random() > 0.25 && pitchBinary[i] == null){
                pitchBinary1[i] = new PitchClass(i);
                hasAtleastOnePitch = true;
            }
            else
                hasAtLeastOneNull = true;
        }
        while(!hasAtleastOnePitch){
            int randomIndex = (int)(12 * Math.random());
            if(pitchBinary[randomIndex] == null){
                pitchBinary1[randomIndex] = new PitchClass(randomIndex);
                hasAtleastOnePitch = true;
            }
        }
        if(!hasAtLeastOneNull){
            int randomIndex = (int)(12 * Math.random());
            pitchBinary1[randomIndex] = null;
        }
        HorizontalSet projectedSet = new HorizontalSet(pitchBinary1);
        HorizontalSearcher hs = new HorizontalSearcher(lastSet, projectedSet);
        PitchSet set = hs.limitSet(0);
        System.out.println("Set: " + set);
        return set;
    }

    public static PitchSet spotCheck(PitchSet lastSet){
        PitchClass[] pitchBinary = lastSet.getArray();

        PitchClass[] pitchBinary1 = new PitchClass[12];
        boolean hasAtleastOnePitch = false;
        boolean hasAtLeastOneNull = false;
        for(int i = 0; i < 12; i++){
            if(Math.random() > 0.25 && pitchBinary[i] == null){
                pitchBinary1[i] = new PitchClass(i);
                hasAtleastOnePitch = true;
            }
            else
                hasAtLeastOneNull = true;
        }
        while(!hasAtleastOnePitch){
            int randomIndex = (int)(12 * Math.random());
            if(pitchBinary[randomIndex] == null){
                pitchBinary1[randomIndex] = new PitchClass(randomIndex);
                hasAtleastOnePitch = true;
            }
        }
        if(!hasAtLeastOneNull){
            int randomIndex = (int)(12 * Math.random());
            pitchBinary1[randomIndex] = null;
        }
        //System.out.println("CONSTRUCTING PROJECTEDSET");
        HorizontalSet projectedSet = new HorizontalSet(pitchBinary1);
        //System.out.println("CONSTRUCTING HORIZONTALSEARCHER");
        HorizontalSearcher hs = new HorizontalSearcher(lastSet, projectedSet);
        //System.out.println("LIMITING HORIZONTALSET");
        PitchSet set = hs.limitSet((int)(Math.random() * 4));
        //System.out.println("LIMITED SET: " + set);
        return set;
    }

    public static void inifinitTest(){
        PitchSet lastSet = spotCheck();
        long max = 0L;
        for(int i = 0; i < 100000; i++){
            long milis = System.currentTimeMillis();
            lastSet = spotCheck(lastSet);
            long runTime = System.currentTimeMillis() - milis;
            max = Math.max(max, runTime);
            System.out.println("Test Number: " + i +" run time: " + runTime + " max run time: " + max);
            if(i % 1000 == 0)
                max = 0;
        }
    }

    /*public HorizontalSearcher(PitchClass[] pP, PitchSet lastSet){
    super(lastSet);
    centerSpace(lastSet.mySearcher.get(0));
    addAll(lastSet.mySearcher);
    resetSpace();
    for(SubSpace s: this){
    s.declareIndependence();
    s.setSearcher(this);
    }
    projectedPitches = pP;

    untransposedSet = new PitchSet(pP);

    System.out.println(untransposedSet);
    System.out.println(untransposedSet.mySearcher);

    registeredPitches = new Hashtable<PitchSet, Integer>();
    potentialSets = new ArrayList<PitchSet>();
    solutions = new ArrayList<PitchSet>();
    System.out.println(this);

    // Archinovica.gui.displayPitches(myPitchSet.getArray());
    }
    public PitchSet search(int setting){
    int limit = 1;
    if(setting > 0)
    limit = 4;
    while(solutions.size() < limit){
    //System.out.println("SOTIONSLU " + solutions.size());
    //System.out.println("POTENTIA" + potentialSets.size());
    if(potentialSets.size() > 0)
    System.out.println("REG" + registeredPitches.get(potentialSets.get(0)));
    generativeSearch();

    }
    PitchSet solution = null;
    if(setting == 0){
    solution = solutions.get(0);
    return solution;
    }
    solutions.remove(0);
    switch(setting){
    case 1:
    solution = Collections.max(solutions);
    break;
    case 2:
    solution = Collections.min(solutions);
    break;
    case 3:
    solutions.remove(Collections.max(solutions));
    solutions.remove(Collections.min(solutions));
    solution = solutions.get(0);
    break;
    }
    return solution;
    }

    @Override
    public boolean pitchFound(RecursiveSearchPoint s){
    // System.out.println("SUBSPACEFOUND? " + s);
    int projectedIndex = Arrays.asList(projectedPitches).indexOf(s);
    if(getSubSpaces(s) < size() || projectedIndex < 0){
    //System.out.println("NOT CONTAINED");
    return false;
    }
    //System.out.println("CONTAINED");
    for(PitchSet ps: potentialSets){
    if(ps.contains(s)){
    if(ps.registerPitch(s)){
    if(ps.fullyRegistered())
    solutions.add(ps);
    return true;
    }
    else
    return false;
    }
    //System.out.println("PitchSet size: " + ps.size());
    }
    Interval transposition = untransposedSet.getArray()[projectedIndex].getInterval(s);
    PitchSet ps = untransposedSet.getTransposedSet(transposition);
    ps.registerPitch(s);
    Archinovica.gui.displayStaticSigns(ps);
    //registeredPitches.put(ps, 1);
    potentialSets.add(ps);
    return true;

    }

    @Override
    public String toString(){
    return "HORIZONTAL SEARCHER" + super.toString();
    }

    public void print(){
    System.out.println(this);
    }
     */
}
