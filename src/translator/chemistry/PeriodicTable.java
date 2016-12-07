/*
 * PeriodicTable.java
 *
 * Created on October 30, 2007, 4:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package translator.chemistry;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author aceccoli
 */
public class PeriodicTable {
    
    private static PeriodicTable instance;
    
    private Set<String> elements;
    
    /** Creates a new instance of PeriodicTable */
    private PeriodicTable() {
        elements = new HashSet();
        
        elements.add("H");
        elements.add("He");
        elements.add("Li");
        elements.add("Be");
        elements.add("B");
        elements.add("C");
        elements.add("N");
        elements.add("O");
        elements.add("F");
        elements.add("Ne");
        elements.add("Na");
        elements.add("Mg");
        elements.add("Al");
        elements.add("Si");
        elements.add("P");
        elements.add("S");
        elements.add("Cl");
        elements.add("Ar");
        elements.add("K");
        elements.add("Ca");
        elements.add("Sc");
        elements.add("Ti");
        elements.add("V");
        elements.add("Cr");
        elements.add("Mn");
        elements.add("Fe");
        elements.add("Co");
        elements.add("Ni");
        elements.add("Cu");
        elements.add("Zn");
        elements.add("Ga");
        elements.add("Ge");
        elements.add("As");
        elements.add("Se");
        elements.add("Br");
        elements.add("Kr");
        elements.add("Rb");
        elements.add("Sr");
        elements.add("Y");
        elements.add("Zr");
        elements.add("Nb");
        elements.add("Mo");
        elements.add("Tc");
        elements.add("Ru");
        elements.add("Rh");
        elements.add("Pd");
        elements.add("Ag");
        elements.add("Cd");
        elements.add("In");
        elements.add("Sn");
        elements.add("Sb");
        elements.add("Te");
        elements.add("I");
        elements.add("Xe");
        elements.add("Cs");
        elements.add("Ba");
        elements.add("La");
        elements.add("Ce");
        elements.add("Pr");
        elements.add("Nd");
        elements.add("Pm");
        elements.add("Sm");
        elements.add("Eu");
        elements.add("Gd");
        elements.add("Tb");
        elements.add("Dy");
        elements.add("Ho");
        elements.add("Er");
        elements.add("Tm");
        elements.add("Yb");
        elements.add("Lu");
        elements.add("Hf");
        elements.add("Ta");
        elements.add("W");
        elements.add("Re");
        elements.add("Os");
        elements.add("Ir");
        elements.add("Pt");
        elements.add("Au");
        elements.add("Hg");
        elements.add("Tl");
        elements.add("Pb");
        elements.add("Bi");
        elements.add("Po");
        elements.add("At");
        elements.add("Rn");
        elements.add("Fr");
        elements.add("Ra");
        elements.add("Ac");
        elements.add("Th");
        elements.add("Pa");
        elements.add("U");
        elements.add("Np");
        elements.add("Pu");
        elements.add("Am");
        elements.add("Cm");
        elements.add("Bk");
        elements.add("Cf");
        elements.add("Es");
        elements.add("Fm");
        elements.add("Md");
        elements.add("No");
        elements.add("Lr");
        elements.add("Rf");
        elements.add("Db");
        elements.add("Sg");
        elements.add("Bh");
        elements.add("Hs");
        elements.add("Mt");
        elements.add("Ds");
        elements.add("Rg");
    }
    
    public static PeriodicTable getTable() {
        if (instance == null) {
            instance = new PeriodicTable();
        }
        return instance;
    }
    
    public boolean isElement(String element) {
        return elements.contains(element);
    }
    
}
