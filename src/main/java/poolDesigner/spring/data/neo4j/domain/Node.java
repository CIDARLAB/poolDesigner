/*Copyright (c) 2015, Nicholas Roehner
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided 
that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and 
the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse 
or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.
*/

package poolDesigner.spring.data.neo4j.domain;

import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@JsonIdentityInfo(generator=JSOGGenerator.class)
@NodeEntity
public class Node {
	
    @GraphId 
    Long id;
    
    String nodeID;

    @Relationship(type = "PRECEDES") 
    Set<Edge> edges;
    
    String nodeType;

    public Node() {
    	
    }
    
    public Node(String nodeID) {
    	this.nodeID = nodeID;
    }
    
    public Node(String nodeID, String nodeType) {
    	this.nodeID = nodeID;
    	this.nodeType = nodeType;
    }
    
    public void addEdge(Edge edge) {
    	if (edges == null) {
    		edges = new HashSet<Edge>();
    	}
    	edges.add(edge);
    }
    
    public void clearEdges() {
    	if (hasEdges()) {
    		edges.clear();
    	}
    }
    
    public Edge copyEdge(Edge edge) {
    	if (edge.hasComponentIDs() && edge.hasComponentRoles()) {
    		return createEdge(edge.getHead(), new ArrayList<String>(edge.getComponentIDs()), new ArrayList<String>(edge.getComponentRoles()));
    	} else {
    		return createEdge(edge.getHead());
    	}
    	
    }
    
    public Edge copyEdge(Edge edge, Node head) {
    	if (edge.hasComponentIDs() && edge.hasComponentRoles()) {
    		return createEdge(head, new ArrayList<String>(edge.getComponentIDs()), new ArrayList<String>(edge.getComponentRoles()));
    	} else {
    		return createEdge(head);
    	}
    }
    
    public Edge createEdge(Node head) {
    	Edge edge = new Edge(this, head);
    	addEdge(edge);
    	return edge;
    }
    
    public Edge createEdge(Node head, ArrayList<String> compIDs, ArrayList<String> compRoles) {
    	Edge edge = new Edge(this, head, compIDs, compRoles);
    	addEdge(edge);
    	return edge;
    }
    
    public Long getGraphID() {
    	return id;
    }
    
    public String getNodeID() {
    	return nodeID;
    }
    
    public int getNumEdges() {
    	return edges.size();
    }

    public Set<Edge> getEdges() {
        return edges;
    }
    
//    public Set<Edge> getIncomingEdges() {
//    	Set<Edge> incomingEdges = new HashSet<Edge>();
//    	
//    	for (Edge edge : edges) {
//    		if (edge.getHead().getNodeID().equals(nodeID)) {
//    			incomingEdges.add(edge);
//    		}
//    	}
//    	
//    	return incomingEdges;
//    }
    
    public String getNodeType() {
    	return nodeType;
    }
    
//    public Set<Edge> getOutgoingEdges() {
//    	Set<Edge> outgoingEdges = new HashSet<Edge>();
//    	
//    	for (Edge edge : edges) {
//    		if (edge.getTail().getNodeID().equals(nodeID)) {
//    			outgoingEdges.add(edge);
//    		}
//    	}
//    	
//    	return outgoingEdges;
//    }
    
    public boolean hasEdges() {
    	if (edges == null) {
    		return false;
    	} else {
    		return edges.size() > 0;
    	}
    }
    
    public boolean hasEdge(Node head) {
    	if (hasEdges()) {
    		for (Edge edge : edges) {
    			if (edge.getHead().equals(head)) {
    				return true;
    			}
    		}
    		
    		return false;
    	} else {
    		return false;
    	}
    }
    
    public boolean hasEdge(Edge edge) {
    	if (hasEdges()) {
    		for (Edge e : edges) {
    			if (edge.isIdenticalTo(e)) {
    				return true;
    			}
    		}
    		return false;
    	} else {
    		return false;
    	}
    }
    
    public boolean hasConflictingNodeType(Node node) {
    	return hasNodeType() && (!node.hasNodeType() || !nodeType.equals(node.getNodeType()));
    }
    
    public boolean hasNodeType() {
    	return nodeType != null;
    }
    
    public boolean isAcceptNode() {
    	return hasNodeType() && nodeType.equals(NodeType.ACCEPT.getValue());
    }
    
    public boolean isStartNode() {
    	return hasNodeType() && nodeType.equals(NodeType.START.getValue());
    }
    
    public boolean deleteEdges(Set<Edge> edges) {
    	if (hasEdges()) {
    		return this.edges.removeAll(edges);
    	} else {
    		return false;
    	}
    }
    
    public void setEdges(Set<Edge> edges) {
    	this.edges = edges;
    }
    
    public enum NodeType {
    	START ("start"),
    	ACCEPT ("accept");
    	
    	private final String value;
    	
    	NodeType(String value) {
    		this.value = value;
    	}
    	
    	public String getValue() {
    		return value;
    	}
    }
    
    public void setNodeType(String nodeType) {
    	this.nodeType = nodeType;
    }
}
