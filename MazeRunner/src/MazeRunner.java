import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

public class MazeRunner {

	private static char[][] maze;
	private static boolean[][] visited;
	private static int height;
	private static int width;
	private static int arrayHeight;
	private static int arrayWidth;
	private static State initState;
	private static State goalState;
	private static ArrayList<State> optimalPath = new ArrayList<State>();
	private static int visitedNodes = 0;
	private static int pathCost = 0;

	public static void main(String args[]) {
		
		// In case we run the code from CMD
		if(args.length > 0) {
			
			String filePath = args[1];
			readFile(filePath);

			visited = new boolean[arrayHeight][arrayWidth]; // keep a track of which nodes we visited

			initState = findState('*'); // find initial state
			goalState = findState('X'); // find goal state
			
			if(args[0].equalsIgnoreCase("bfs"))
			{
				BFS();

			}else if(args[0].equalsIgnoreCase("best"))
			{
				BestFirstSearch();
			}else if(args[0].equalsIgnoreCase("A*")) 
			{
				aStar();
			}else {
				System.out.println("Unknown Algorithm!");
				System.out.println("Available Algorithms: 1-BFS, 2-Best, 3-A*");
			}
			
			System.out.println("Alg Name: " + args[0]);
			System.out.println("Input: " + args[1]);
			checkDirections();
			System.out.print("Path: ");
			for(int i = optimalPath.size() - 1; i >= 0; i--)
			{
				System.out.print(optimalPath.get(i) + (i == 0 ? "\n":" --> "));
			}
			System.out.println("Cost: " + pathCost);			
			System.out.println("Visit Count: " + visitedNodes);
		}
		
		else { // In case we wanna run the program from the console
			Scanner input = new Scanner(System.in);
			
			System.out.println("Enter algorithm name, a new line and then input file name: ");
			String algoName = input.nextLine();
			String fileName = input.nextLine();
			
			String filePath = "src/" + fileName;
			readFile(filePath);

			visited = new boolean[arrayHeight][arrayWidth];

			initState = findState('*');
			goalState = findState('X');

			if(algoName.equalsIgnoreCase("bfs"))
			{
				BFS();

			}else if(algoName.equalsIgnoreCase("best"))
			{
				BestFirstSearch();
			}else if(algoName.equalsIgnoreCase("A*")) 
			{
				aStar();
			}else {
				System.out.println("Unknown Algorithm!");
				System.out.println("Available Algorithms: 1-BFS, 2-Best, 3-A*");
			}
			
			
			System.out.println("Alg Name: " + algoName);
			System.out.println("Input: " + fileName);
			checkDirections();
			System.out.print("Path: ");
			for(int i = optimalPath.size() - 1; i >= 0; i--)
			{
				System.out.print(optimalPath.get(i) + (i == 0 ? "\n":" --> "));
			}
			System.out.println("Cost: " + pathCost);			
			System.out.println("Visit Count: " + visitedNodes);
					
			
			// printing the path we found to output file
			try {
				FileWriter myWriter = new FileWriter("pathOutput.txt");
				for (int row = 0; row < arrayHeight; row++) {
					for (int col = 0; col < arrayWidth; col++) {
						myWriter.write(maze[row][col]);
					}

					myWriter.write("\n");
				}
				myWriter.close();
				System.out.println("Successfully wrote to the file.");
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
			
			
		}
		
	}

	// This method handles the file reading 
	public static void readFile(String path) {
		try {
			File fileObj = new File(path);
			Scanner in = new Scanner(fileObj);
			int row = 0;
			int col = 0;
			boolean firstLine = true;

			while (in.hasNextLine()) {

				String line = in.nextLine();

				if (firstLine) {
					String[] mazeDimensions = line.split(" ");
					height = Integer.parseInt(mazeDimensions[1]);
					width = Integer.parseInt(mazeDimensions[0]);
					arrayHeight = height * 2 + 1;
					arrayWidth = width * 2 + 1;

					maze = new char[arrayHeight][arrayWidth];

				}

				if (!firstLine && row < arrayHeight) {
					for (col = 0; col < line.length(); col++) {
						maze[row][col] = line.charAt(col);
					}

					row++;
				}

				firstLine = false;

			}

			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error! File not found");
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	// This method is used specifically to find initial and goal state
	public static State findState(char symbol) {
		State state = null;

		for (int row = 0; row < arrayHeight; row++) {
			for (int col = 0; col < arrayWidth; col++) {
				if (maze[row][col] == symbol) {
					state = new State(row, col);
				}
			}
		}

		return state;

	}

	// Successor method, returns all possible states for a given state
	public static Set<State> Successor(State curState) {
		Set<State> states = new HashSet<>();

		if (maze[curState.getX()][curState.getY() - 1] == ' ' || maze[curState.getX()][curState.getY() - 1] == 'X') // left
		{
			states.add(new State(curState.getX(), curState.getY() - 1));
		}

		if (maze[curState.getX()][curState.getY() + 1] == ' ' || maze[curState.getX()][curState.getY() + 1] == 'X') // right
		{
			states.add(new State(curState.getX(), curState.getY() + 1));
		}

		if (maze[curState.getX() - 1][curState.getY()] == ' ' || maze[curState.getX() - 1][curState.getY()] == 'X') // up
		{
			states.add(new State(curState.getX() - 1, curState.getY()));
		}

		if (maze[curState.getX() + 1][curState.getY()] == ' ' || maze[curState.getX() + 1][curState.getY()] == 'X') // down
		{
			states.add(new State(curState.getX() + 1, curState.getY()));
		}

		return states;

	}

	public static void BFS() {

		visitedNodes = 0;
		pathCost = 0;
		List<State> path = new LinkedList<>();

		PriorityQueue<State> queue = new PriorityQueue<>(new Comparator<State>() {

			@Override
			public int compare(State o1, State o2) {
				if (o1.getCost() > o2.getCost()) {
					return 1;
				}
				else if (o1.getCost() < o2.getCost()) {
					return -1;
				}
				else // if cost is equals then sort like this: up, right, down, left
				{
					if(o1.getX() > o2.getX())
					{
						return 1;
					}	
					else if(o1.getX() < o2.getX())
					{
						return -1;
					}
					else
					{
						if(o1.getY() < o2.getY())
						{
							return 1;
						}
						else if(o1.getY() > o2.getY())
						{
							return -1;
						}
						else
						{
							return 0;
						}
					}
				}
				
			}

		});

		queue.offer(initState);

		while (!queue.isEmpty()) {
			State state = queue.poll();

			visitedNodes++; // number of overall visited nodes till we reach the goal
			path.add(state); // all visited nodes
			visited[state.getX()][state.getY()] = true;

			if (goalReached(state)) {
				if(state.equals(goalState)) {
					state.setDirection("Goal"); // so we don't have null 
				}
				
				pathCost = state.getCost();

				State s = state;
				while (s != null) { // path
					maze[s.getX()][s.getY()] = '*'; // for debugging only
					optimalPath.add(s);
					//System.out.println(s);
					s = s.getFatherNode();
				}


				return;
			}

			Set<State> states = Successor(state);

			for (State s : states) {
				if (!visited[s.getX()][s.getY()]) {
					visited[s.getX()][s.getY()] = true;

					s.setCost(state.getCost() + 1);

					s.setFatherNode(state);

					//checkDirections(state, s);
					queue.offer(s);
				}
			}

			// System.out.println(queue);
		}

		System.out.println("Failed to find Goal!");

		// System.out.println(visited[5][5]);

	}

	public static void BestFirstSearch() {
		//System.out.println("Goal state is at: " + goalState);

		visitedNodes = 0;
		pathCost = 0;
		List<State> path = new LinkedList<>();

		PriorityQueue<State> queue = new PriorityQueue<>(new Comparator<State>() {

			@Override
			public int compare(State o1, State o2) {
				if(o1.getH() > o2.getH()) {
					return 1;
				}
				else if(o1.getH() < o2.getH()) {
					return -1;
				}
				else // if costs are equal then sort like this: up, right, down, left
				{
					if(o1.getX() > o2.getX())
					{
						return 1;
					}	
					else if(o1.getX() < o2.getX())
					{
						return -1;
					}
					else
					{
						if(o1.getY() < o2.getY())
						{
							return 1;
						}
						else if(o1.getY() > o2.getY())
						{
							return -1;
						}
						else
						{
							return 0;
						}
					}
				}

			}

		});

		initState.setH(calcManhattanDist(initState));
		queue.offer(initState);

		while (!queue.isEmpty()) {
			State state = queue.poll();

			// System.out.println("pop--> " + state);

			visitedNodes++; // number of overall visited nodes till we reach the goal
			path.add(state); // all visited nodes
			visited[state.getX()][state.getY()] = true;

			if (goalReached(state)) {
				
				if(state.equals(goalState)) {
					state.setDirection("Goal"); // so we don't have null 
				}
				
				pathCost = state.getCost();

				//System.out.println("Path:");

				State s = state;
				while (s != null) { // path
					maze[s.getX()][s.getY()] = '*'; // for debugging only
					optimalPath.add(s);
					//System.out.println(s);
					s = s.getFatherNode();
				}

				return;
			}

			Set<State> states = Successor(state);

			for (State s : states) {
				if (!visited[s.getX()][s.getY()]) {
					visited[s.getX()][s.getY()] = true;

					s.setCost(state.getCost() + 1);

					s.setH(calcManhattanDist(s));
					s.setFatherNode(state);

					//checkDirections(state, s);
					queue.offer(s);
				}
			}

			// System.out.println(queue);
		}

		System.out.println("Failed to find Goal!");

		// System.out.println(visited[5][5]);
	}

	public static void aStar() {
		//System.out.println("Goal state is at: " + goalState);

		visitedNodes = 0;
		pathCost = 0;
		List<State> path = new LinkedList<>();

		PriorityQueue<State> queue = new PriorityQueue<>(new Comparator<State>() {

			@Override
			public int compare(State o1, State o2) {
				if((o1.getH() + o1.getCost()) > (o2.getH() + o2.getCost())) {
					return 1;
				}
				else if((o1.getH() + o1.getCost()) < (o2.getH() + o2.getCost())) {
					return -1;
				}
				else // if costs are equal then sort like this: up, right, down, left
				{
					if(o1.getX() > o2.getX())
					{
						return 1;
					}	
					else if(o1.getX() < o2.getX())
					{
						return -1;
					}
					else
					{
						if(o1.getY() < o2.getY())
						{
							return 1;
						}
						else if(o1.getY() > o2.getY())
						{
							return -1;
						}
						else
						{
							return 0;
						}
					}
				}

				
			}

		});

		initState.setH(calcManhattanDist(initState));
		queue.offer(initState);

		while (!queue.isEmpty()) {
			State state = queue.poll();

			visitedNodes++; // number of overall visited nodes till we reach the goal
			path.add(state); // all visited nodes
			visited[state.getX()][state.getY()] = true;

			if (goalReached(state)) {
				
				if(state.equals(goalState)) {
					state.setDirection("Goal"); // so we don't have null 
				}
				
				pathCost = state.getCost();

				State s = state;
				while (s != null) { // path
					maze[s.getX()][s.getY()] = '*'; // for debugging only
					optimalPath.add(s);
					s = s.getFatherNode();
				}

				return;
			}

			Set<State> states = Successor(state);

			for (State s : states) {
				if (!visited[s.getX()][s.getY()]) {
					visited[s.getX()][s.getY()] = true;

					s.setCost(state.getCost() + 1);

					s.setH(calcManhattanDist(s));
					s.setFatherNode(state);

					queue.offer(s);
				}
			}

		}

		System.out.println("Failed to find Goal!");

	}

	// Calculating Manhatttan Distance
	public static int calcManhattanDist(State state) {
		int result = Math.abs(state.getX() - goalState.getX()) + Math.abs(state.getY() - goalState.getY());
		return result;

	}

	// Checking if goal is reached or not
	public static boolean goalReached(State state) {
		if (state.equals(goalState)) {
			return true;
		}

		return false;
	}
	
	// This method saves the direction the state went to 
	public static void checkDirections()
	{
		
		ArrayList<State> al = new ArrayList<State>();
		
		for(int i = optimalPath.size() - 1; i >=0; i--)
		{
			al.add(optimalPath.get(i));
		}
		
		
		for(int i = 0; i < al.size() - 1; i++)
		{
			if(al.get(i).getX() + 1 == al.get(i + 1).getX())
			{
				al.get(i).setDirection("Down");
			}
			
			if(al.get(i).getX() - 1 == al.get(i + 1).getX())
			{
				al.get(i).setDirection("Up");
			}
			
			if(al.get(i).getY() + 1 == al.get(i + 1).getY())
			{
				al.get(i).setDirection("Right");

			}
			
			if(al.get(i).getY() - 1 == al.get(i + 1).getY())
			{
				al.get(i).setDirection("Left");
			}
			
		}
		return;
	}

}
