import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class Route
{
	public static Vertex[] vertices;
	public static String source, destination;
	public static Heap heap;
	
	public static void main(String[] args) throws FileNotFoundException
	{
		
		createVertexArray();
		createEdgeArray();

		heap = new Heap(vertices.length);
		source = args[0]; 
		destination = args[1];
		
		//insert source into the first position of heap
		heap.insert(vertices[getHashCode(source)], 0, null);
		Dijkstraa();
		
		//keep adding and extracting until destination is reached
		while(!heap.extractMin().getName().equals(destination)) 
			Dijkstraa();
		
		//output
		System.out.printf("%s-%s\n", retracePath(vertices[getHashCode(destination)]), vertices[getHashCode(destination)].getTime());
	}
	
	public static void createVertexArray() throws FileNotFoundException
	{
		File inputFile = new File("airports.txt");
		Scanner scan = new Scanner(inputFile);
		vertices = new Vertex[1000];
		int total = scan.nextInt();
		for(int i = 0; i < total; i++) 
		{ 
			String airportName = scan.next();
			int collisions = 0;
			while(vertices[hashFunction(airportName) + collisions] != null) 
			{
				collisions++;
				if(hashFunction(airportName) + collisions >= vertices.length)
					collisions = -hashFunction(airportName);
			}
			vertices[hashFunction(airportName) + collisions] = new Vertex(airportName, hashFunction(airportName));
			collisions = 0;
		}	
		scan.close();
	}
	
	public static void createEdgeArray() throws FileNotFoundException
	{
		File inputFile = new File("flights.txt");
		Scanner scan = new Scanner(inputFile);
		scan.nextLine();
		scan.nextLine();
		while(scan.hasNextLine() && scan.hasNext()) 
		{
			scan.next(); 
			int flightNumber = scan.nextInt();
			String airportName = scan.next();

			vertices[getHashCode(airportName)].addToList(new Edge(flightNumber, airportName, scan.next(), scan.nextInt(), scan.nextInt()));
			scan.nextInt();
		}
		scan.close();
	} 
	
	public static int hashFunction(String s)
	{
        int p0 = s.charAt(0) - 'A' + 1;
        int p1 = s.charAt(1) - 'A' + 1;
        int p2 = s.charAt(2) - 'A' + 1;
        int p3 = p0 * 467 * 467 + p1 * 467 + p2;
        int p4 = p3 % 7193;
        return p4 % 1000;
    }
	
	public static int getHashCode(String airportName)
	{
		int collisions = 0;
		while(!vertices[hashFunction(airportName) + collisions].getName().equals(airportName)) 
		{
			collisions++;
			if(hashFunction(airportName) + collisions >= vertices.length)
				collisions = -hashFunction(airportName);			
		}
		return hashFunction(airportName) + collisions;
	}
	
	public static void Dijkstraa()
	{
		String minHeapAirportName = heap.getArray()[0].getName();
		LinkedList<Edge> flights = vertices[getHashCode(minHeapAirportName)].getEdges();
		
		//add flights from LinkedList of array[0] to heap
		for(int i = 0; i < flights.size(); i++)
		{
			String airport = flights.get(i).getAirportEnd();
			
			//makes sure it only adds the possible flights
			if(flights.get(i).getDepartureTime() < vertices[getHashCode(minHeapAirportName)].getTime())
				continue;

			//if the vertex already exists in the heap, decrease its key if its new time is smaller
			if(vertices[getHashCode(airport)].getHeapPosition() != -1) 
			{
				if(flights.get(i).getArrivalTime() < vertices[getHashCode(airport)].getTime())
				{
					heap.decreaseKey(vertices[getHashCode(airport)], flights.get(i).getArrivalTime());
					//sets a different path if its source airport has changed
					if(!flights.get(i).getAirportStart().equals(vertices[getHashCode(airport)].getPath().getName()))
							vertices[getHashCode(airport)].setPath(vertices[getHashCode(flights.get(i).getAirportStart())]);					
				}
			}
			else //the airport does not exist in the heap yet
			{
				heap.insert(vertices[getHashCode(airport)], flights.get(i).getArrivalTime(), vertices[getHashCode(flights.get(i).getAirportStart())]);
				vertices[getHashCode(airport)].setTime(flights.get(i).getArrivalTime());
			}
		}
	}

	public static String retracePath(Vertex v)
	{
		if(v.getPath() == null)
			return source;
		return retracePath(vertices[getHashCode(v.getName())].getPath()) + "-" + vertices[getHashCode(v.getName())].getName();
	}
	
	private static class Vertex 
	{
		
		private String airport;
		private LinkedList<Edge> edges;
		private int heapPosition = -1;
		private Vertex path;
		private int time;
		
		public Vertex(String airport, int hashPosition) 
		{
			this.airport = airport;
			edges = new LinkedList<Edge>();
			time = 0;
		}
		
		public void addToList(Edge edge) { edges.add(edge);}
		public String getName() { return airport;}
		public LinkedList<Edge> getEdges(){ return this.edges;}
		public int getHeapPosition() { return this.heapPosition;}
		public Vertex getPath() { return this.path;}
		public int getTime() { return this.time;}
		public void setHeapPosition(int heapPosition) { this.heapPosition = heapPosition;}
		public void setPath(Vertex path) { this.path = path;}
		public void setTime(int time) { this.time = time;}
		
	}

	private static class Edge
	{
		
		private String airportStart;
		private String airportEnd;
		private int departureTime;
		private int arrivalTime;
		
		public Edge(int flightNumber, String airportStart, String airportEnd, int departureTime, int arrivalTime) 
		{
			this.airportStart = airportStart;
			this.airportEnd = airportEnd;
			this.departureTime = departureTime;
			this.arrivalTime = arrivalTime;
		}
		
		public String getAirportStart() { return this.airportStart;}
		public String getAirportEnd() { return this.airportEnd;}
		public int getDepartureTime() { return this.departureTime;}
		public int getArrivalTime() { return this.arrivalTime;}

	}

	private static class Heap
	{
		
		private int heapIndex;
		private Vertex[] array;
		
		public Heap(int size) 
		{
			heapIndex = 0;
			array = new Vertex[size];
		}
		
		public void insert(Vertex v, int time, Vertex path) 
		{
			array[heapIndex] = v;
			array[heapIndex].setTime(time);
			array[heapIndex].setPath(path);
			v.setHeapPosition(heapIndex);
			bubbleUp(heapIndex);
			heapIndex++;
		}
		
		public void decreaseKey(Vertex v, int newKey) 
		{
			v.setTime(newKey);
			bubbleUp(v.getHeapPosition());
		}
		
		public Vertex extractMin() 
		{
			Vertex temp = array[0];
			array[0].setHeapPosition(-1);
			swap(0, heapIndex-1);
			array[heapIndex-1] = null;
			bubbleDown(0);
			heapIndex--;
			return temp;
		}
		
		private void bubbleDown(int index){
	        while (smallestChild(index) != -1 && array[smallestChild(index)] != null && array[index].getTime() > array[smallestChild(index)].getTime())
	        {
	            swap(index, smallestChild(index));
	            index = smallestChild(index);
	        }
	    }
		
		private void bubbleUp(int index){
	        while(array[index] != null && array[index].getTime() < array[parent(index)].getTime())
	        {
	            swap(index,parent(index));
	            index = parent(index);
	        }
	    }
		
		private int smallestChild(int i)
		{
			if(array[2*i + 1] == null)
				return -1;
	        if(array[2*i + 2] == null || array[2*i + 1].getTime() < array[2*i + 2].getTime()) 
	        	return 2*i + 1;
	        else 
	        	return 2*i + 2;
	    }
		
		private int parent(int i)
		{
	        return (i-1)/2;
	    }
		
		public void swap(int a, int b) 
		{
			Vertex temp = array[a];
			array[a].setHeapPosition(b);
			array[b].setHeapPosition(a);
			array[a] = array[b];
			array[b] = temp;
		}
		
		public Vertex[] getArray() 
		{
			return array;
		}
		
	}
	
}
