package Beans;

import Control.MergeAlg;
import Control.TestEngine;
import Util.ReportUtil;
import Util.StringUtil;
import java.io.*;
import java.util.Stack;

/**
 * Debug code = 200
 * represent an Stochastic Deterministic Finite Automata object.
 * @modified Prashanth Sandela
 */
public class SDFA implements Serializable {

// ........................ D A T A   F I E L D S ............................//
// ............. G L O B A L   P R I V A T E   C O N S T A N T S .............//
// .............. G L O B A L   P U B L I C   C O N S T A N T S ..............//
// ................. G L O B A L   P R I V A T E   V A R S ...................//

    /** hold the SDFA pta. */
    PTA pta;

    /** hold the alpha factor. */
    private double alpha;
    
    /** Holds the merge algorithm object.*/
    private MergeAlg mergeAlg;

    /** Holds the status of forcing the automaton return back to the entry
     * point when the testing unit reaches the leaf and still has characters. */
    private boolean forceAutoReturn;

    /** Holds the initial number of nodes before applying the merge algorithm. */
    private int initialTotNodes;
    
    /** Holds the number of merged nodes after applying the merge algorithm. */
    private int mergedNodesNum;
    
    

// ........................ C O N S T R U C T O R S ..........................//

    /**
     * The main Constructor for the whole application
     * @modified Prashanth Sandela
     * @param learnDoc - the learning document
     * @param alpha - the alpha
     * @param tCond - the test condition object
     */
    public SDFA(Document learnDoc, double alpha, TestCond tCond, PTA pta1) {

        this.alpha = alpha;
        
        /**
         * Commented by Prashanth Sandela
         * Coz PTA tree is being generated for every value of ALPHA
         * and it is same tree coz ALPHA value is independent of PTA.
         * This has been implemented in TestMatrix
         */
//        pta = new PTA(
//            learnDoc.getSymbolUnitArr(), learnDoc.getEmptyStringSymbol());
        
        pta = pta1;
        
        if (tCond.isDebugMode(200)) 
            ReportUtil.showDebugInfo(TestEngine.class, pta, 200);
        
        initialTotNodes = pta.getTotNodes();
        mergeAlg = tCond.getMergeAlg();
        forceAutoReturn = tCond.isForceAutoReturn();
        
        System.out.println("Applying Merge Alergia Algorithm..");
        pta = mergeAlg.applyAlgorithm(pta, alpha);
        mergedNodesNum = mergeAlg.getMergedNodesNum();
        
        if (tCond.isDebugMode(201)) 
            ReportUtil.showDebugInfo(SDFA.class, pta, 201);
        

    } //constructor


    /**
     * instantiate with a stored automaton
     * @param inFN - the name of the file that contains the automaton
     */
    public SDFA(String inFN) {

        ObjectInputStream in;

        try {
            in = new ObjectInputStream(
                 new BufferedInputStream(
                 new FileInputStream(inFN)));

            SDFA auto = (SDFA) in.readObject();

            alpha = auto.getAlpha();
            pta  = auto.getPta();
            forceAutoReturn = auto.isForceAutoReturn();
            initialTotNodes = auto.getInitialTotNodes();
            mergedNodesNum = auto.getMergedNodesNum();

            in.close();

        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        }

    } //method


//.............................. G E T T E R S ...............................//

    public PTA getPta() {
        return pta;
    }


    public boolean isForceAutoReturn() {
        return forceAutoReturn;
    }


    public double getAlpha() {
        return alpha;
    }


    public int getInitialTotNodes() {
        return initialTotNodes;
    }


    public int getMergedNodesNum() {
        return mergedNodesNum;
    }


//.............................. S E T T E R S ...............................//
// ...................... P R I V A T E   M E T H O D S ......................//
// ...................... P U B L I C   M E T H O D S ........................//

    /**
     * get an automata from a stored file.
     * @param inFN - the automata file name
     * @return the automata
     */
    public static SDFA getSDFAFromFile(String inFN) {

        ObjectInputStream in;

        try {
            in = new ObjectInputStream(new
                    BufferedInputStream(new FileInputStream(inFN)));

            SDFA auto = (SDFA) in.readObject();
            in.close();

            return auto;

        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
            return null;
        }

    } //method


    /**
     * write this automata to the given file.
     * @param outFN - the automata file name
     */
    public void writeToFile(String outFN) {

        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(new
                    BufferedOutputStream(new FileOutputStream(outFN)));

            out.writeObject(this);
            out.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    } //method


    /**
     * Returns a copy of this object, or null if the object cannot
     * be serialized.
     */
    @Override
    public SDFA clone() {

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(this);
                
                out.flush();
            }
            SDFA auto;
            try (ObjectInputStream in = new ObjectInputStream(
                     new ByteArrayInputStream(bos.toByteArray()))) {
                auto = (SDFA) in.readObject();
            }
            return auto;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    } //method


    /**
     * Traverses the graph based on post order and builds a string of the nodes.
     * @param sb - the input string builder.
     */
    private static void postOrderGraphTraverse(StringBuilder sb, Node root) {

        Stack<Node> levelStack = new Stack();
        levelStack.push(root);

        while (!levelStack.empty()) {

            Node curNode = levelStack.pop();
            
            if (curNode.isVisited()) {
                sb.append(" --> ");
                sb.append(curNode.toStringForSDFA());
                sb.append("\n");
                continue;
            }
            
            sb.append(curNode.toStringForSDFA());
            curNode.setIsVisited(true);
            curNode.pushUnVisitedChildren(levelStack);

        } //while

    } //method


    /**
     * Traverses the pta based on pre-order and builds a string of the nodes.
     * @param node - the entry point of the pta
     * @param depth - the depth of the pta
     * @param sb  - the string builder which will contains the final string
     */
    private void preOrderTraverse(Node node, int depth, StringBuilder sb) {

        sb.append("\n").append(StringUtil.space4Creator(depth));

        if (node == null) {
            sb.append("null");
            
        } else if (node.isVisited()) {
            sb.append("<--").append(node.toStringForSDFA());
            
        } else {
            sb.append(node.toStringForSDFA());
            node.setIsVisited(true);

            for (Node child : node.getChildren().values()) {
                preOrderTraverse(child, depth + 1, sb);
            } //for
        }

    } //mehtod


    /** Creates a string from all nodes of this pta.
     * @return the string made of nodes key by pre order traverse.
     */
    @Override
    public final String toString() {
        
        int depth = 0;
        
        StringBuilder sb = new StringBuilder("\n*****   S D F A  ******\n");
        sb.append("Number of Initial Nodes = ").append(initialTotNodes).append("\n");
        sb.append("Number of Merged Nodes  = ").append(mergedNodesNum).append("\n");
        sb.append("Alpha = ").append(alpha).append("\n");
        
        //postOrderGraphTraverse(sb, pta.getRoot());
        preOrderTraverse(pta.getRoot(), depth, sb);

        return sb.append("\n").toString();
        
    } //method


    /**
     * calculate the acceptance probability of a set of strings by this automata.
     * @param strArr - the array list of the strings
     * @return the probability of acceptance
     */
    public double probablityAcceptor(Document testDoc, double alpha) {

        int numTotalStr = 0;
        int numAcceptedStr = 0;

        for (String unitStr : testDoc.getSymbolUnitArr()) {

            numTotalStr++;
            if (isAccepted(unitStr.trim(), testDoc.getEmptyStringSymbol()))
                numAcceptedStr++;

        } //for

        return ((double) numAcceptedStr * 100.0 / (double) numTotalStr);

    } //method


    /**
     * check the acceptance of a string of symbols by this automata.
     * @precondition: the input string is trimmed
     * @param Symbolstr - the symbol string
     * @return true if accepted and false otherwise.
     */
    private boolean isAccepted(String symbolStr, String emptyStringSymbol)
    {
        //empty string
        if (symbolStr.equalsIgnoreCase(emptyStringSymbol)) return true;


        Node curNode = pta.getRoot();
        String[] symbolArr = symbolStr.split("\\p{javaWhitespace}+");
        
        if (curNode == null) return false;
        
        for (String symbol : symbolArr) {
            
            //curNode advances to the child node.
            curNode = curNode.getChild(Integer.parseInt(symbol));
            
            /* Normally, if we want to make a deterministic automaton and 
             * the curNode == null, then we should return false.
             * Here we wanted to force the unfinished string return back to
             * the begining of the automaton.
             */
            if (curNode == null) {
                if (forceAutoReturn) {
                    curNode = pta.getRoot();
                    curNode = curNode.getChild(Integer.parseInt(symbol));
                    if (curNode == null) return false;
                } else {
                    return false;
                }
            }
            
        } //for
        
        //return true if the termination node is an acceptor
        if (curNode.getNumAccepted() > 0) return true;
        else                              return false;

    } //method

} //class

