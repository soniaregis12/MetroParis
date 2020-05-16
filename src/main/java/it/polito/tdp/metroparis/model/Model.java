package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer,Fermata> fermateIdMap;
	
	public Model() {
		this.grafo = new SimpleDirectedGraph<Fermata, DefaultEdge>(DefaultEdge.class);
		
		MetroDAO dao = new MetroDAO();
		
		fermate = dao.getAllFermate();
		
		fermateIdMap = new HashMap<Integer, Fermata>();			
		for(Fermata f: this.fermate) {
			fermateIdMap.put(f.getIdFermata(),f);
		}
		
		Graphs.addAllVertices(this.grafo, fermate);
		
		// Creazione degli archi
		
		// Metodo 1: coppie di vertici
		/*
		for(Fermata fa: this.fermate) {
			for(Fermata fb: this.fermate) {
				if(dao.fermateConnesse(fa, fb)) {
					this.grafo.addEdge(fa, fb);
				}
			}
		}
		
		// Primo metodo troppo lungo, non va bene, ci mette circa 5 min per andare a vedere tutte le connessioni
		// Metodo 2: da un vertice trova tutti quelli connessi a quel vertice
		
		for(Fermata fp: this.fermate) {
			List<Fermata> connesse = dao.fermateSuccessive(fp, fermateIdMap);
			for(Fermata fa: connesse) {
				this.grafo.addEdge(fp, fa);
			}
		}
		
		*/
		// Funziona bene, è veloce
		// Metodo 3: chiedo al DB l'elenco degli archi
		
		List<CoppiaFermata> coppie = dao.coppieFermate(fermateIdMap);
		for(CoppiaFermata cf : coppie) {
			this.grafo.addEdge(cf.getFp(), cf.getFa());
		}
		System.out.println(this.grafo);
		System.out.println("Il grafo contiene " + this.grafo.vertexSet().size() + " vertici e " + this.grafo.edgeSet().size() + " archi");
	}
	
	//  Visita il grafo con la strategia breadth first e ritorna l'insieme dei vertici incontrati
	public List<Fermata> visitaAmpiezza(Fermata source){
		
		List<Fermata> visita = new ArrayList<Fermata>();
		BreadthFirstIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<Fermata, DefaultEdge>(grafo, source);
		//GraphIterator<Fermata, DefaultEdge> dfv = new DepthFirstIterator<Fermata, DefaultEdge>(grafo, source);
		
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		return visita;
	}
	
	public Map<Fermata, Fermata> alberoVisita(Fermata source) {
		
		Map<Fermata, Fermata> albero = new HashMap<>();
		albero.put(source, null);
		BreadthFirstIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<Fermata, DefaultEdge>(grafo, source);
		
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {	
			}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {			
			}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				// la visita sta considerando un nuovo arco
				// questo arco ha scoperto un nuovo vertice?
				// se si, provenendo da dove?
				DefaultEdge edge = e.getEdge(); // (a,b) ho scoperto a da b o b da a?
				Fermata a = grafo.getEdgeSource(edge);
				Fermata b = grafo.getEdgeTarget(edge);
				if(albero.containsKey(a) && !albero.containsKey(b)) {
					albero.put(b, a);
				}else {
					albero.put(a, b);
				}
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {	
			}
		});
		
		while(bfv.hasNext()) {
			bfv.next();
		}
		
		
		return albero;	
	}
	
	public List<Fermata> camminiMinimi(Fermata partenza, Fermata arrivo) {
		DijkstraShortestPath<Fermata, DefaultEdge> dij = new DijkstraShortestPath<Fermata, DefaultEdge>(this.grafo);
		GraphPath<Fermata, DefaultEdge> cammino = dij.getPath(partenza, arrivo);
		return cammino.getVertexList();
	}
	
	public static void main(String args[]) {
		Model m = new Model();
		
		List<Fermata> visita = m.visitaAmpiezza(m.fermate.get(0));
		System.out.println(visita);

		System.out.println("*************************************************************************");
		Map<Fermata, Fermata> albero = m.alberoVisita(m.fermate.get(0));
		for(Fermata f: albero.keySet()) {
			System.out.format("%s <- %s", f, albero.get(f) + "\n");
		}
		
		System.out.println("*************************************************************************");
		List<Fermata> cammino = m.camminiMinimi(m.fermate.get(0), m.fermate.get(1));
		System.out.println(cammino);
	}

}
