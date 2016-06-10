package aima.core.search.basic.example;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import aima.core.search.api.Node;
import aima.core.search.api.NodeFactory;
import aima.core.search.api.Problem;
import aima.core.search.api.SearchController;
import aima.core.search.api.SearchForActionsFunction;
import aima.core.search.basic.support.BasicNodeFactory;
import aima.core.search.basic.support.BasicSearchController;

/**
 * <pre>
 * function UNIFORM-COST-SEARCH(problem) returns a solution, or failure
 *   node &larr; a node with STATE = problem.INITIAL-STATE, PATH-COST=0
 *   frontier &larr; a priority queue ordered by PATH-COST, with node as the only element
 *   explored &larr; an empty set
 *   loop do
 *      if EMPTY?(frontier) then return failure
 *      node &lt;- POP(frontier) // chooses the lowest-cost node in frontier
 *      if problem.GOAL-TEST(node.STATE) then return SOLUTION(node)
 *      add node.STATE to explored
 *      for each action in problem.ACTIONS(node.STATE) do
 *          child &larr; CHILD-NODE(problem, node, action)
 *          if child.STATE is not in explored or frontier then
 *             frontier &larr; INSERT(child, frontier)
 *          else if child.STATE is in frontier with higher PATH-COST then
 *             replace that frontier node with child
 * </pre>
 *
 * Figure ?? Uniform-cost search on a graph. The algorithm is identical to the
 * general graph search algorithm in Figure ??, except for the use of a priority
 * queue and the addition of an extra check in case a shorted path to a frontier
 * state is discovered. The data structure for frontier needs to support
 * efficient membership testing, so it should combine the capabilities of a
 * priority queue and a hash table.
 *
 * @author Ciaran O'Reilly
 * @author Ruediger Lunde
 */
public class ExampleUniformCostSearch<A, S> implements SearchForActionsFunction<A, S> {
	// function UNIFORM-COST-SEARCH((problem) returns a solution, or failure
	@Override
	public List<A> apply(Problem<A, S> problem) {
		// node <- a node with STATE = problem.INITIAL-STATE, PATH-COST=0
		Node<A, S> node = nodeFactory.newRootNode(problem.initialState(), 0);
		// frontier <- a priority queue ordered by PATH-COST, with node as the only element
		Queue<Node<A, S>> frontier = newPriorityQueueOrderedByPathCost(node);
		// explored <- an empty set
		Set<S> explored = newExploredSet();
		// loop do
		while (true) {
			// if EMPTY?(frontier) then return failure
			if (frontier.isEmpty()) { return failure(); }
			// node <- POP(frontier) // chooses the lowest-cost node in frontier
			node = frontier.remove();
			// if problem.GOAL-TEST(node.STATE) then return SOLUTION(node)
			if (problem.isGoalState(node.state())) { return solution(node); }
			// add node.STATE to explored
			explored.add(node.state());
			// for each action in problem.ACTIONS(node.STATE) do
			for (A action : problem.actions(node.state())) {
				// child <- CHILD-NODE(problem, node, action)
				Node<A, S> child = nodeFactory.newChildNode(problem, node, action);
				// if child.STATE is not in explored or frontier then
				if (!(explored.contains(child.state()) || containsState(frontier, child.state()))) {
					// frontier <- INSERT(child, frontier)
					frontier.add(child);
				} // else if child.STATE is in frontier with higher PATH-COST then
				else if (removedNodeFromFrontierWithSameStateAndHigherPathCost(child, frontier)) {
					// replace that frontier node with child
					frontier.add(child);
				}
			}
		}
	}

	//
	// Supporting Code
	protected NodeFactory<A, S> nodeFactory = new BasicNodeFactory<>();
	protected SearchController<A, S> searchController = new BasicSearchController<A, S>();

	public ExampleUniformCostSearch() {
	}

	public Queue<Node<A, S>> newPriorityQueueOrderedByPathCost(Node<A, S> initialNode) {
		Queue<Node<A, S>> frontier = new PriorityQueue<>((n1, n2) -> Double.compare(n1.pathCost(), n2.pathCost()));
		frontier.add(initialNode);
		return frontier;
	}

	public Set<S> newExploredSet() {
		return new HashSet<>();
	}

	public List<A> failure() {
		return searchController.failure();
	}

	public List<A> solution(Node<A, S> node) {
		return searchController.solution(node);
	}

	public boolean containsState(Queue<Node<A, S>> frontier, S state) {
		// NOTE: Not very efficient (i.e. linear in the size of the frontier)
		return frontier.stream().anyMatch(frontierNode -> frontierNode.state().equals(state));
	}
	
	public boolean removedNodeFromFrontierWithSameStateAndHigherPathCost(Node<A, S> child, Queue<Node<A, S>> frontier) {
		// NOTE: Not very efficient (i.e. linear in the size of the frontier)
		return frontier.removeIf(n -> n.state().equals(child.state()) && n.pathCost() > child.pathCost());
	}
}