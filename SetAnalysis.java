import java.util.ArrayList;
import javax.swing.JOptionPane;
/**
 * Write a description of class SetAnalysis here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class SetAnalysis
{
    public ArrayList<Integer> set;
    public SetAnalysis()
    {

        /*
        ArrayList<ArrayList<Basket>> sequences = allSequences();
        for(ArrayList<Basket> sequence: sequences){
        for(Basket b: sequence)
        System.out.print(b);
        System.out.println();
        }
         */
    }

    public void searchForNegativeSets(){
        ArrayList<boolean[]> allSets = allSets();
        for(boolean[] aSet: allSets){
            set = new ArrayList<Integer>();
            for(int i = 0; i < 12; i++){
                if(aSet[i])
                    set.add(new Integer(i));
            }
            if(set.size() != 7)
                continue;
            double[] vector = average5LimVector1();
            if(vector[0] < 0 || vector[1] < 0){
                System.out.print("[");
                for(int i = 0; i < set.size(); i++){
                    System.out.print(set.get(i));
                    if(i < set.size() - 1)
                        System.out.print(",");
                }
                System.out.print("] ");
                System.out.println("<" + vector[0] + "," + vector[1] + ">");
            }
        }
    }

    public void enterSetToAnalyze(){
        String input = JOptionPane.showInputDialog(null, "Enter PC's separated by commas, no spaces.");
        input += ",";

        set = new ArrayList<Integer>();
        int lastInd = 0;
        do{
            int index = input.indexOf(",", lastInd);
            if(index < 0)
                break;
            String a = input.substring(lastInd, index);
            //System.out.println(a);
            set.add(Integer.parseInt(a));
            lastInd = index + 1;
        }while(lastInd > 0);

        double[] vector = average5LimVector1();
        System.out.println("<" + vector[0] + "," + vector[1] + ">");
    }

    public ArrayList<boolean[]> allSets(){
        ArrayList<boolean[]> seed = new ArrayList<boolean[]>();
        seed.add(new boolean[12]);
        return allSets(seed, 0);
    }

    public ArrayList<boolean[]> allSets(ArrayList<boolean[]> sets, int noteIndex){
        ArrayList<boolean[]> allSets = new ArrayList<boolean[]>();
        for(boolean[] set: sets){
            boolean[] copy = new boolean[12];
            for(int i = 0; i < noteIndex; i++){
                copy[i] = set[i];
            }
            copy[noteIndex] = false;
            allSets.add(copy);
        }
        for(boolean[] set: sets){
            boolean[] copy = new boolean[12];
            for(int i = 0; i < noteIndex; i++){
                copy[i] = set[i];
            }
            copy[noteIndex] = true;
            allSets.add(copy);
        }
        if(noteIndex < 11){
            return allSets(allSets, noteIndex + 1);
        }
        return allSets;
    }

    public double[] average5LimVector(){
        Archinovica anArch = new Archinovica(null);
        anArch.setPedaling(0);
        ArrayList<ArrayList<Integer>> allPerms = allPermutations(set);
        double[] vector = new double[2];
        for(ArrayList<Integer> perm: allPerms){
            PitchClass firstPitch = null;
            PitchClass lastPitch = null;
            System.out.print("     [");
            for(Integer note: perm)
                System.out.print(note + ",");
            System.out.print("]");
            for(int i = 0; i < perm.size(); i++){
                boolean[]  pitchBinary =  new boolean[12];
                int pc = perm.get(i);
                pitchBinary[pc] = true;
                anArch.updateIntonation(pitchBinary);
                PitchClass[] archiArray = anArch.soundingPitchClasses;
                if(i == 0){
                    firstPitch = archiArray[pc];
                }
                if(i == perm.size() - 1){
                    lastPitch = archiArray[pc];
                }
            }

            double[] delta = new double[]{(lastPitch.signified[0] - firstPitch.signified[0]),
                    (lastPitch.signified[1] - firstPitch.signified[1])};
            vector[0] += (lastPitch.signified[0] - firstPitch.signified[0]);
            vector[1] += (lastPitch.signified[1] - firstPitch.signified[1]);
            System.out.println("<" + delta[0] + "," + delta[1] + ">");
        }
        vector[0] /= (double)allPerms.size();
        vector[1] /= (double)allPerms.size();
        return vector;
    }

    public double[] average5LimVector1(){
        Archinovica anArch = new Archinovica(null);
        anArch.setPedaling(0);
        ArrayList<ArrayList<Basket>> sequences = allSequences();
        double[] vector = new double[2];
        for(ArrayList<Basket> sequence: sequences){
            double[] firstPitch = new double[2];
            double[] lastPitch = new double[2];
            //System.out.print("]");
            for(int i = 0; i < sequence.size(); i++){
                boolean[]  pitchBinary =  new boolean[12];
                Basket chord = sequence.get(i);

                for(Integer pc: chord)
                    pitchBinary[pc] = true;
                anArch.updateIntonation(pitchBinary);
                PitchClass[] archiArray = anArch.soundingPitchClasses;
                if(i == 0){
                    for(Integer pc: chord){
                        firstPitch[0] += archiArray[pc].signified[0];
                        firstPitch[1] += archiArray[pc].signified[1];
                    }
                    firstPitch[0] /= (double)chord.size();
                    firstPitch[1] /= (double)chord.size();
                }
                if(i == sequence.size() - 1){
                    for(Integer pc: chord){
                        lastPitch[0] += archiArray[pc].signified[0];
                        lastPitch[1] += archiArray[pc].signified[1];
                    }
                    lastPitch[0] /= (double)chord.size();
                    lastPitch[1] /= (double)chord.size();
                }
            }

            double[] delta = new double[]{(lastPitch[0] - firstPitch[0]),
                    (lastPitch[1] - firstPitch[1])};
            vector[0] += delta[0];
            vector[1] += delta[1];
            //System.out.println("<" + delta[0] + "," + delta[1] + ">");
        }
        vector[0] /= (double)sequences.size();
        vector[1] /= (double)sequences.size();
        return vector;
    }

    public ArrayList<ArrayList<Integer>> allPermutations(ArrayList<Integer> aset){
        ArrayList<ArrayList<Integer>> allPerms = new ArrayList<ArrayList<Integer>>();
        if(aset.size() == 1){
            ArrayList<Integer> perm = new ArrayList<Integer>();
            perm.add(aset.get(0));
            allPerms.add(perm);
            return allPerms;
        }
        for(int i = 0; i < aset.size(); i++){
            Integer firstNote = aset.get(i);
            ArrayList<Integer> aSubSet = new ArrayList<Integer>();
            aSubSet.addAll(aset);
            ((ArrayList)aSubSet).remove(i);
            ArrayList<ArrayList<Integer>> subPerms = allPermutations(aSubSet);
            for(ArrayList perm: subPerms){
                ArrayList<Integer> perm1 = new ArrayList<Integer>();
                perm1.addAll(perm);
                perm1.add(0, firstNote);
                allPerms.add(perm1);
            }
        }
        return allPerms;
    }

    public ArrayList<ArrayList<Basket>> allSequences(){
        ArrayList<BasketGroup> seed = new ArrayList<BasketGroup>();
        seed.add(new BasketGroup());
        ArrayList<BasketGroup> bgs = getAllBasketGroups(seed,0);
        return allPermsOfAllBasketGroups(bgs);
    }

    public ArrayList<ArrayList<Basket>> allPermsOfAllBasketGroups(ArrayList<BasketGroup> bgs){
        ArrayList<ArrayList<Basket>> allPerms = new ArrayList<ArrayList<Basket>>();
        for(BasketGroup bg: bgs){
            allPerms.addAll(allBasketPermutations(bg));
        }
        return allPerms;
    }

    public ArrayList<ArrayList<Basket>> allBasketPermutations(ArrayList<Basket> basketSet){
        ArrayList<ArrayList<Basket>> allPerms = new ArrayList<ArrayList<Basket>>();
        if(basketSet.size() == 1){
            ArrayList<Basket> perm = new ArrayList<Basket>();
            perm.add(basketSet.get(0));
            allPerms.add(perm);
            return allPerms;
        }
        for(int i = 0; i < basketSet.size(); i++){
            Basket firstNote = basketSet.get(i);
            ArrayList<Basket> aSubSet = new ArrayList<Basket>();
            aSubSet.addAll(basketSet);
            ((ArrayList)aSubSet).remove(i);
            ArrayList<ArrayList<Basket>> subPerms = allBasketPermutations(aSubSet);
            for(ArrayList perm: subPerms){
                ArrayList<Basket> perm1 = new ArrayList<Basket>();
                perm1.addAll(perm);
                perm1.add(0, firstNote);
                allPerms.add(perm1);
            }
        }
        return allPerms;
    }

    public ArrayList<BasketGroup> getAllBasketGroups(ArrayList<BasketGroup> bgs, int noteIndex){
        ArrayList<BasketGroup> allBasketGroups = new ArrayList<BasketGroup>();
        for(BasketGroup bg: bgs){
            for(int i = 0; i < bg.size(); i++){
                Basket b = bg.get(i);
                if(b.getSpace() > 0){
                    BasketGroup deriv = new BasketGroup(bg);
                    deriv.get(i).add(set.get(noteIndex));
                    allBasketGroups.add(deriv);
                }
            }
            for(int i = 1; i <= bg.getSpace(); i++){
                Basket originalBasket = new Basket(i);
                originalBasket.add(set.get(noteIndex));
                BasketGroup deriv = new BasketGroup(bg);
                deriv.add(originalBasket);
                allBasketGroups.add(deriv);
            }
        }
        if(noteIndex < set.size() - 1){
            return getAllBasketGroups(allBasketGroups, noteIndex + 1);
        }
        return allBasketGroups;
    }

    class Basket extends ArrayList<Integer>{
        int capacity;
        public Basket(int cap){
            capacity = cap;
        }

        public int getSpace(){
            return capacity - size();
        }

        @Override
        public String toString(){
            String a = "";
            a += "[";
            for(int i = 0; i < size(); i++){
                a += get(i);
                if(i < size() - 1)
                    a += ",";
            }
            a += "] ";

            return a;
        }
    }

    class BasketGroup extends ArrayList<Basket>{
        public int spotsFilled;
        public BasketGroup(){
            spotsFilled = 0;
        }

        @Override
        public boolean add(Basket b){
            boolean result = super.add(b);
            spotsFilled += b.capacity;
            return result;
        }

        public BasketGroup(BasketGroup bg){
            for(Basket b: bg){
                Basket copy = new Basket(b.capacity);
                copy.addAll(b);
                add(copy);
            }
            spotsFilled = bg.spotsFilled;
        }

        public int getSpace(){
            return set.size() - spotsFilled;
        }

        @Override
        public String toString(){
            String a = "";
            for(Basket b: this){
                a += "[";
                for(int i = 0; i < b.size(); i++){
                    a += b.get(i);
                    if(i < b.size() - 1)
                        a += ",";
                }
                a += "] ";
            }
            return a;
        }
    }
}
