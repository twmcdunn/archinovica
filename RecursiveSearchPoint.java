

import java.util.ArrayList;
/**
 * Aggiungi qui una descrizione della classe SubSpace
 * 
 * @author (il tuo nome) 
 * @version (un numero di versione o una data)
 */
public class RecursiveSearchPoint extends PitchClass
{
    public GenerativeSearcher mySearcher; //depreciated
    public RecursiveSearchPoint[] neighbors;
    public ArrayList<RecursiveSearchPoint> children;
    public static final int[][] TRANSFORMATION_GROUP = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    public RecursiveSearchPoint parent;
    private int generations;
    //initialize independent subspace
    public RecursiveSearchPoint(){
        super(new int[]{0, 0});
        children = new ArrayList<RecursiveSearchPoint>();
        neighbors = new RecursiveSearchPoint[4];
        parent = this;
        generations = 0;
    }

    public RecursiveSearchPoint(SemioticFunction sf){
        super(new int[]{sf.signified[0], sf.signified[1]});
        children = new ArrayList<RecursiveSearchPoint>();
        neighbors = new RecursiveSearchPoint[4];
        parent = this;
        generations = 0;
    }

    //initialize dependent subspace
    public RecursiveSearchPoint(int neighborIndex, RecursiveSearchPoint source, boolean animate){
        super(TRANSFORMATION_GROUP[neighborIndex], source);
        //mySearcher = source.mySearcher;
        neighbors = new RecursiveSearchPoint[4];
        parent = source.parent;
        children = new ArrayList<RecursiveSearchPoint>();
        addNeighbor(source, (neighborIndex + 2) % 4);
        RecursiveSearchPoint[] kittyCorners = new RecursiveSearchPoint[]{source.neighbors[(neighborIndex + 3) % 4],
                source.neighbors[(neighborIndex + 1) % 4]};
        for(RecursiveSearchPoint r: kittyCorners)
            if(r != null && r.neighbors[neighborIndex] != null)
                addNeighbor(r.neighbors[neighborIndex]);
        //mySearcher.addSubSpace(this);
        //System.out.println(this);
        if(animate){
            Archinovica.animateGeneration(this, source.signified);
        }
    }

    public ArrayList<RecursiveSearchPoint> generateNeighbors(PitchSet searchDestination, boolean animate){
        boolean found = false;
        ArrayList<RecursiveSearchPoint> solutions = new ArrayList<RecursiveSearchPoint>();
        if(parent == this)
            generations++;
        if(children.size() == 0){
            for(int i = 0; i < 4; i++)
                if(neighbors[i] == null){
                    RecursiveSearchPoint child = bearChild(i, animate);
                    children.add(child);
                    if(searchDestination.isProjected(child))
                        solutions.add(child);
                }
        }
        else
            for(RecursiveSearchPoint child: children)
                solutions.addAll(child.generateNeighbors(searchDestination, animate));
        return solutions;
        /*
        ArrayList<SubSpace> newborns = new ArrayList<SubSpace>();
        //if(furthestDescendents.size() == 0)
        furthestDescendents.get(0);
        while(furthestDescendents.size() > 0){
        SubSpace descendent = furthestDescendents.get(0);
        furthestDescendents.remove(0);
        for(int i = 0; i < 4; i++){
        if(descendent.neighbors[i] == null){
        SubSpace child = descendent.bearChild(i);
        boolean lastChild = descendent.isLastChild(child);
        newborns.add(child);
        if(lastChild){
        if(i == 3)
        furthestDescendents = newborns;
        else
        furthestDescendents.add(0, descendent);
        return true;
        }
        //System.out.println("NEWBORN: " +descendent.children.get(descendent.children.size() - 1));

        }
        }
        }
        //System.out.println("Number of newborns: " + newborns.size());
        furthestDescendents = newborns;
        return false;
         */
    }

    //allows THIS to function independently, reseting search generations, returns THIS
    /* public RecursiveSearchPoint declareIndependence(){
    children = new ArrayList<SubSpace>();
    neighbors = new SubSpace[4];
    return this;
    }
     */

    public RecursiveSearchPoint bearChild(int i, boolean animate){
        RecursiveSearchPoint child = new RecursiveSearchPoint(i, this, animate);
        //Archinovica.gui.displayStaticSign(child);
        //System.out.println("NEW CHILD: " + child);
        return child;
    }

    /*public boolean isLastChild(RecursiveSearchPoint child){
    Archinovica.gui.animateGeneration(child, signified);
    //System.out.println("CHILD: " + child);
    return (child.getSubSpaces() == mySearcher.size()) && mySearcher.pitchFound(child);
    }
     */
    public void addNeighbor(RecursiveSearchPoint rsp){
        addNeighbor(rsp, getTransType(rsp));
    }

    public void addNeighbor(RecursiveSearchPoint rss, int index){
        neighbors[index] = rss;
        rss.neighbors[(index + 2) % 4] = this;
    }

    public int getTransType(RecursiveSearchPoint rsp){
        int[] trans = getTransformation(rsp);
        return getTransType(trans);
    }

    public int getTransType(int[] trans){
        for(int i = 0; i < 4; i++){
            //System.out.println(TRANSFORMATION_GROUP[i][0] + ", " + TRANSFORMATION_GROUP[i][1]);
            if(TRANSFORMATION_GROUP[i][0] == trans[0] && TRANSFORMATION_GROUP[i][1] == trans[1])
                return i;
        }
        System.out.println("ERROR! INVALID NEIGHBOR TRANFORMATION: [" + trans[0] + ", " + trans[1] + "]");
        //System.exit(-1);
        return -1;
    }

    public int getDistanceSearched(){
        return generations;
    }

    @Override
    public RecursiveSearchPoint clone(){
        return new RecursiveSearchPoint(this);
    }
    /*public int getSubSpaces(){
    return mySearcher.getSubSpaces(this);
    }

    public void setSearcher(GenerativeSearcher gs){
    mySearcher = gs;
    gs.addSubSpace(this);
    }
     */

    /*@Override
    public String toString(){
    String a = super.toString();
    for(RecursivePitchSpace r: children)
    a += " " + r;
    return a;
    }
     */
}
