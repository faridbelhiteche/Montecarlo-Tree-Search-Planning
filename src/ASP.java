import fr.uga.pddl4j.heuristics.state.StateHeuristic;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.*;
import fr.uga.pddl4j.planners.statespace.HSP;
import fr.uga.pddl4j.planners.statespace.search.StateSpaceSearch;
import fr.uga.pddl4j.problem.*;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.*;
import java.util.*;

/**
 * The class is an example. It shows how to create a simple A* search planner able to
 * solve an ADL problem by choosing the heuristic to used and its weight.
 *
 * @author D. Pellier
 * @version 4.0 - 30.11.2021
 */
@CommandLine.Command(name = "ASP",
        version = "ASP 1.0",
        description = "Solves a specified planning problem using A* search strategy.",
        sortOptions = false,
        mixinStandardHelpOptions = true,
        headerHeading = "Usage:%n",
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
public class ASP extends AbstractPlanner {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ASP.class.getName());

    /**
     * The HEURISTIC property used for planner configuration.
     */
    public static final String HEURISTIC_SETTING = "HEURISTIC";

    /**
     * The default value of the HEURISTIC property used for planner configuration.
     */
    public static final StateHeuristic.Name DEFAULT_HEURISTIC = StateHeuristic.Name.FAST_FORWARD;

    /**
     * The WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final String WEIGHT_HEURISTIC_SETTING = "WEIGHT_HEURISTIC";

    /**
     * The default value of the WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final double DEFAULT_WEIGHT_HEURISTIC = 1.0;

    /**
     * The weight of the heuristic.
     */
    private double heuristicWeight;

    /**
     * The name of the heuristic used by the planner.
     */
    private StateHeuristic.Name heuristic;

    /**
     * Creates a new A* search planner with the default configuration.
     */
    public ASP() {
        this(ASP.getDefaultConfiguration());
    }
    /**
     * Creates a new A* search planner with a specified configuration.
     *
     * @param configuration the configuration of the planner.
     */
    public ASP(final PlannerConfiguration configuration) {
        super();
        this.setConfiguration(configuration);
    }

    /**
     * Sets the weight of the heuristic.
     *
     * @param weight the weight of the heuristic. The weight must be greater than 0.
     * @throws IllegalArgumentException if the weight is strictly less than 0.
     */
    @CommandLine.Option(names = {"-w", "--weight"}, defaultValue = "1.0",
            paramLabel = "<weight>", description = "Set the weight of the heuristic (preset 1.0).")
    public void setHeuristicWeight(final double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight <= 0");
        }
        this.heuristicWeight = weight;
    }

    /**
     * Set the name of heuristic used by the planner to solve a planning problem.
     *
     * @param heuristic the name of the heuristic.
     */
    @CommandLine.Option(names = {"-e", "--heuristic"}, defaultValue = "FAST_FORWARD",
            description = "Set the heuristic : AJUSTED_SUM, AJUSTED_SUM2, AJUSTED_SUM2M, COMBO, "
                    + "MAX, FAST_FORWARD SET_LEVEL, SUM, SUM_MUTEX (preset: FAST_FORWARD)")
    public void setHeuristic(StateHeuristic.Name heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * Returns the name of the heuristic used by the planner to solve a planning problem.
     *
     * @return the name of the heuristic used by the planner to solve a planning problem.
     */
    public final StateHeuristic.Name getHeuristic() {
        return this.heuristic;
    }

    /**
     * Returns the weight of the heuristic.
     *
     * @return the weight of the heuristic.
     */
    public final double getHeuristicWeight() {
        return this.heuristicWeight;
    }

    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Search a solution plan to a specified domain and problem using A*.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(final Problem problem) {
        LOGGER.info("* Starting Montecarlo search\n");
        // Search a solution
        final long begin = System.currentTimeMillis();
        final Plan plan = this.LocalSearchMCRW(problem);
        final long end = System.currentTimeMillis();
        LOGGER.info("* Starting Montecarlo search\n");
        // If a plan is found update the statistics of the planner
        // and log search information
        if (plan != null) {
            LOGGER.info("* Montecarlo search succeeded\n");
            this.getStatistics().setTimeToSearch(end - begin);
        } else {
            LOGGER.info("* Montecarlo search failed\n");
        }
        // Return the plan found or null if the search fails.
        return plan;
    }


    /**
     * Checks the planner configuration and returns if the configuration is valid.
     * A configuration is valid if (1) the domain and the problem files exist and
     * can be read, (2) the timeout is greater than 0, (3) the weight of the
     * heuristic is greater than 0 and (4) the heuristic is a not null.
     *
     * @return <code>true</code> if the configuration is valid <code>false</code> otherwise.
     */
    public boolean hasValidConfiguration() {
        return super.hasValidConfiguration()
                && this.getHeuristicWeight() > 0.0
                && this.getHeuristic() != null;
    }

    /**
     * This method return the default arguments of the planner.
     *
     * @return the default arguments of the planner.
     * @see PlannerConfiguration
     */
    public static PlannerConfiguration getDefaultConfiguration() {
        PlannerConfiguration config = Planner.getDefaultConfiguration();
        config.setProperty(ASP.HEURISTIC_SETTING, ASP.DEFAULT_HEURISTIC.toString());
        config.setProperty(ASP.WEIGHT_HEURISTIC_SETTING,
                Double.toString(ASP.DEFAULT_WEIGHT_HEURISTIC));
        return config;
    }

    /**
     * Returns the configuration of the planner.
     *
     * @return the configuration of the planner.
     */
    @Override
    public PlannerConfiguration getConfiguration() {
        final PlannerConfiguration config = super.getConfiguration();
        config.setProperty(ASP.HEURISTIC_SETTING, this.getHeuristic().toString());
        config.setProperty(ASP.WEIGHT_HEURISTIC_SETTING, Double.toString(this.getHeuristicWeight()));
        return config;
    }

    /**
     * Sets the configuration of the planner. If a planner setting is not defined in
     * the specified configuration, the setting is initialized with its default value.
     *
     * @param configuration the configuration to set.
     */
    @Override
    public void setConfiguration(final PlannerConfiguration configuration) {
        super.setConfiguration(configuration);
        if (configuration.getProperty(ASP.WEIGHT_HEURISTIC_SETTING) == null) {
            this.setHeuristicWeight(ASP.DEFAULT_WEIGHT_HEURISTIC);
        } else {
            this.setHeuristicWeight(Double.parseDouble(configuration.getProperty(
                    ASP.WEIGHT_HEURISTIC_SETTING)));
        }
        if (configuration.getProperty(ASP.HEURISTIC_SETTING) == null) {
            this.setHeuristic(ASP.DEFAULT_HEURISTIC);
        } else {
            this.setHeuristic(StateHeuristic.Name.valueOf(configuration.getProperty(
                    ASP.HEURISTIC_SETTING)));
        }
    }

    /**
     * The main method of the <code>ASP</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) throws IOException {
        try {
            final ASP mrwPlanner = new ASP();
            final HSP hspPlanner = new HSP();

            /**
             * 2 attributs pour l'??criture des r??sultats dans un fichier + ??criture de l'ent??te dans le fichier.
             */
            File resultFile = new File("pddl/results.csv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
            writer.write("domain,n_problem,MRW_time_spent,MRW_plan_length,HSP_time_spent,HSP_plan_length");
            writer.newLine();


            List<File> blocks_problems = List.of(new File("pddl/problemes_blocks").listFiles());
            List<File> depots_problems = List.of(new File("pddl/problemes_depots").listFiles());
            //List<File> gripper_problems = List.of(new File("pddl/problemes_gripper").listFiles());
            //List<File> logistics_problems = List.of(new File("pddl/problemes_logistics").listFiles());

            Map<File, List<File>> pddlFiles = new TreeMap<>();
            pddlFiles.put(new File("pddl/domain_blocks.pddl"), blocks_problems);
            pddlFiles.put(new File("pddl/domain_depots.pddl"), depots_problems);
            //pddlFiles.put(new File("pddl/domain_gripper.pddl"), gripper_problems);
            //pddlFiles.put(new File("pddl/domain_logistics.pddl"), logistics_problems);

            for(File domainFile : pddlFiles.keySet()) {
                for(File problemFile : pddlFiles.get(domainFile)) {
                    String domainPath = domainFile.getPath();
                    String problemPath = problemFile.getPath();
                    mrwPlanner.setDomain(domainPath);
                    hspPlanner.setDomain(domainPath);
                    mrwPlanner.setProblem(problemPath);
                    hspPlanner.setProblem(problemPath);

                    String domain = domainFile.getName();
                    String problem = problemFile.getName();
                    String mrwResults = run(mrwPlanner);
                    String hspResults = run(hspPlanner);
                    writer.write(domain + "," + problem + "," + mrwResults + "," + hspResults);
                    writer.newLine();
                }
            }

            writer.close();

        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    private static String run(AbstractPlanner planner) throws FileNotFoundException {
        Plan p = planner.solve();
        Statistics s = planner.getStatistics();
        double TimeSpent = s.getTimeToParse() + s.getTimeToEncode() + s.getTimeToSearch();
        int planLength = p == null ? 0 : p.size();
        return TimeSpent + "," + planLength;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Search a solution plan for a planning problem using an A* search strategy.
     *
     * @param problem the problem to solve.
     * @return a plan solution for the problem or null if there is no solution
     */
    public Plan astar(Problem problem) {

        // First we create an instance of the heuristic to use to guide the search
        final StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);

        // We get the initial state from the planning problem
        final State init = new State(problem.getInitialState());

        // We initialize the closed list of nodes (store the nodes explored)
        final Set<Node> close = new HashSet<>();

        // We initialize the opened list to store the pending node according to function f
        final double weight = this.getHeuristicWeight();
        final PriorityQueue<Node> open = new PriorityQueue<>(100, new Comparator<Node>() {
            public int compare(Node n1, Node n2) {
                double f1 = weight * n1.getHeuristic() + n1.getCost();
                double f2 = weight * n2.getHeuristic() + n2.getCost();
                return Double.compare(f1, f2);
            }
        });


        // We create the root node of the tree search
        final Node root = new Node(init, null, -1, 0, heuristic.estimate(init, problem.getGoal()));

        // We add the root to the list of pending nodes
        open.add(root);
        Plan plan = null;

        // We set the timeout in ms allocated to the search
        final int timeout = this.getTimeout() * 1000;
        long time = 0;

        // We start the search
        while (!open.isEmpty() && plan == null && time < timeout) {

            // We pop the first node in the pending list open
            final Node current = open.poll();
            close.add(current);

            // If the goal is satisfied in the current node then extract the search and return it
            if (current.satisfy(problem.getGoal())) {
                return this.extractPlan(current, problem);
            } else { // Else we try to apply the actions of the problem to the current node
                for (int i = 0; i < problem.getActions().size(); i++) {
                    // We get the actions of the problem
                    Action a = problem.getActions().get(i);
                    // If the action is applicable in the current node
                    System.out.println(a.toString());
                    if (a.isApplicable(current)) {
                        Node next = new Node(current);
                        // We apply the effect of the action
                        final List<ConditionalEffect> effects = a.getConditionalEffects();
                        for (ConditionalEffect ce : effects) {
                            if (current.satisfy(ce.getCondition())) {
                                next.apply(ce.getEffect());
                            }
                        }
                        // We set the new child node information
                        final double g = current.getCost() + 1;
                        if (!close.contains(next)) {
                            next.setCost(g);
                            next.setParent(current);
                            next.setAction(i);
                            next.setHeuristic(heuristic.estimate(next, problem.getGoal()));
                            open.add(next);
                        }
                    }
                }
            }
        }

        // Finally, we return the search computed or null if no search was found
        return plan;
    }

    public static int MAX_STEPS = 7;
    public Plan LocalSearchMCRW(Problem problem){

        // On instancie l'heuristique
        StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);

        // On r??cup??re l'??tat initial du probl??me
        State init = new State(problem.getInitialState());

        final Goal G = new Goal(problem.getGoal());

        //On instancie le n??ud associ?? ?? l'??tat initial
        Node current = new Node(init, null, -1, 0, 0, heuristic.estimate(init, problem.getGoal()));

        //On r??cup??re l'heuristique du n??ud en question
        double hMin = current.getHeuristic();

        //on initialise le compteur
        int counter = 0;

        //Node current = root;


        while(!current.satisfy(G)){
            //System.out.println(current.getHeuristic());
            //System.out.println("MAX_STEPS" + MAX_STEPS);
            //System.out.println("Counter" + counter);
            //System.out.println(getApplicableActions(problem, current).isEmpty());
            if(counter > MAX_STEPS || deadEnd(current, problem)) {
                current = new Node(init, null, -1, 0, 0, heuristic.estimate(init, problem.getGoal()));;
                counter = 0;
            }

            current = MonteCarloRandomWalk(current,problem, heuristic);
            if(current.getHeuristic() < hMin){
                hMin = current.getHeuristic();
                counter = 0;
                //System.out.println("avance");
            }else {
                counter ++;
                //System.out.println("n'avance pas");
            }
        }

        return extractPlan(current,problem);
    }

    public Node MonteCarloRandomWalk (Node n, Problem p, StateHeuristic h){


        int NUM_WALKS = 2000;
        int LENGTH_WALK = 10;

        Node nMin = null;
        double hMin = Double.MAX_VALUE-1;

        //nMin.setHeuristic(heuristic.estimate(nMin, p.getGoal()));

        for(int i = 0; i < NUM_WALKS; i++){
            Node nTest = n;
            //nTest.setHeuristic(heuristic.estimate(nTest, p.getGoal()));
            for (int j = 0; j < LENGTH_WALK; j++){
                //on r??cup??re les actions possibles dans la variable a
                List<Action> a = actionsApplicables(nTest, p);
                Collections.shuffle(a);
                Action randomAction = a.get(0);
                // On applique les effets de l'action
                final List<ConditionalEffect> effects = randomAction.getConditionalEffects();
                State s = new State(nTest);
                s.apply(effects);
                Node fils = new Node(s, nTest, p.getActions().indexOf(randomAction), n.getCost() + 1, n.getDepth() + 1, 0);
                fils.setHeuristic(h.estimate(fils, p.getGoal()));
                nTest = fils;


                if (nTest.satisfy(p.getGoal())){
                    return nTest;
                }
            }

            if (nTest.getHeuristic() < hMin){
                nMin = nTest;
                hMin = nTest.getHeuristic();
            }
        }

        if(nMin == null){
            return n;
        }

        return nMin;
    }


    public Boolean deadEnd(Node n,Problem p){
        return actionsApplicables(n, p).isEmpty();
    }

    private List<Action> actionsApplicables(Node n, Problem p) {
        List<Action> a = p.getActions();
        List<Action> actionsApplicables = new ArrayList<>();
        for (Action action : a)
            if (action.isApplicable(n))
                actionsApplicables.add(action);
        return actionsApplicables;
    }

    /**
     * Extracts a search from a specified node.
     *
     * @param node    the node.
     * @param problem the problem.
     * @return the search extracted from the specified node.
     */
    private Plan extractPlan(final Node node, final Problem problem) {
        Node n = node;
        final Plan plan = new SequentialPlan();
        int compteur = 0;
        while (n.getAction() != -1) {
            final Action a = problem.getActions().get(n.getAction());
            plan.add(0, a);
            n = n.getParent();
            compteur ++;
        }
        System.out.println("Longueur du plan " + compteur);
        return plan;
    }



}


