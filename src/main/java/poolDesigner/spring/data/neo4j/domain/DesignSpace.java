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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import poolDesigner.spring.data.neo4j.domain.Node.NodeType;
import poolDesigner.spring.data.neo4j.services.DesignSpaceService;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@JsonIdentityInfo(generator=JSOGGenerator.class)
@NodeEntity
public class DesignSpace {
	
	@GraphId
    Long id;
    
    String spaceID;
    
    int idIndex;
    
	@Relationship(type = "CONTAINS") 
    Set<Node> nodes;

    public DesignSpace() {
    	
    }
    
    public DesignSpace(String spaceID) {
    	this.spaceID = spaceID;
    }
    
    public DesignSpace(String spaceID, int idIndex) {
    	this.spaceID = spaceID;
    	this.idIndex = idIndex;
    }
    
    public String getSpaceID() {
    	return spaceID;
    }
    
    public Set<String> getComponentIDs() {
    	Set<String> compIDs = new HashSet<String>();
    	
    	if (hasNodes()) {
    		for (Node node : nodes) {
    			if (node.hasEdges()) {
    				for (Edge edge : node.getEdges()) {
    					if (edge.hasComponentIDs()) {
    						compIDs.addAll(edge.getComponentIDs());
    					}
    				}
    			}
    		}
    	}
    	
    	return compIDs;
    }
    
    public Set<String> getComponentRoles() {
    	Set<String> compRoles = new HashSet<String>();
    	
    	if (hasNodes()) {
    		for (Node node : nodes) {
    			if (node.hasEdges()) {
    				for (Edge edge : node.getEdges()) {
    					if (edge.hasComponentRoles()) {
    						compRoles.addAll(edge.getComponentRoles());
    					}
    				}
    			}
    		}
    	}
    	
    	return compRoles;
    }
    
    public void addNode(Node node) {
		if (nodes == null) {
			nodes = new HashSet<Node>();
		}
		nodes.add(node);
	}
	
	public Set<Node> removeAllNodes() {
		Set<Node> removedNodes = nodes;
		nodes.clear();
		return removedNodes;
	}
	
	public DesignSpace copy(String copyID) {
		DesignSpace spaceCopy = new DesignSpace(copyID, idIndex);
		
		if (hasNodes()) {
			HashMap<String, Node> idToNodeCopy = new HashMap<String, Node>();

			for (Node node : nodes) {
				idToNodeCopy.put(node.getNodeID(), spaceCopy.copyNodeWithID(node));
			}

			for (Node node : nodes) {
				if (node.hasEdges()) {
					Node nodeCopy = idToNodeCopy.get(node.getNodeID());
					
					for (Edge edge : node.getEdges()) {
						nodeCopy.copyEdge(edge, idToNodeCopy.get(edge.getHead().getNodeID()));
					}
				} 
			}
		}
		
		return spaceCopy;
	}
	
	public Node copyNodeWithEdges(Node node) {
		Node nodeCopy = copyNode(node);
		
		if (node.hasEdges()) {
			for (Edge edge : node.getEdges()) {
				nodeCopy.copyEdge(edge);
			}
		}
		
		return nodeCopy;
	}
	
	public Node copyNodeWithID(Node node) {
		if (node.hasNodeType()) {
			return createTypedNode(node.getNodeID(), node.getNodeType());
		} else {
			return createNode(node.getNodeID());
		} 
	}
	
	public Node copyNode(Node node) {
		if (node.hasNodeType()) {
			return createTypedNode(node.getNodeType());
		} else {
			return createNode();
		}
	}
	
	public Node createNode() {
		Node node = new Node("n" + idIndex++);
		addNode(node);
		return node;
	}
	
	public Node createNode(String nodeID) {
		Node node = new Node(nodeID);
		addNode(node);
		return node;
	}
	
	public Node createTypedNode(String nodeType) {
		Node node = new Node("n" + idIndex++, nodeType);
		addNode(node);
		return node;
	}
	
	public Node createTypedNode(String nodeID, String nodeType) {
		Node node = new Node(nodeID, nodeType);
		addNode(node);
		return node;
	}
	
	public Node createAcceptNode() {
		return createTypedNode(NodeType.ACCEPT.getValue());
	}
	
	public Node createStartNode() {
		return createTypedNode(NodeType.START.getValue());
	}
	
	public boolean deleteEdges(Set<Edge> deletedEdges) {
		boolean isDeleted = false;
		
		if (hasNodes()) {
			for (Node node : nodes) {
				if (node.deleteEdges(deletedEdges)) {
					isDeleted = true;
				}
			}
    	}
		
		return isDeleted;
	}
	
	public boolean deleteNodes(Set<Node> deletedNodes) {
		if (hasNodes()) {
    		return nodes.removeAll(deletedNodes);
    	} else {
    		return false;
    	}
	}
	
	public boolean detachDeleteNodes(Set<Node> deletedNodes) {	
		if (hasNodes()) {
			for (Node node : nodes) {
				if (node.hasEdges() && !deletedNodes.contains(node)) {
					Set<Edge> deletedEdges = new HashSet<Edge>();
					
					for (Edge edge : node.getEdges()) {
						if (deletedNodes.contains(edge.getHead())) {
							deletedEdges.add(edge);
						}
					}
					
					node.deleteEdges(deletedEdges);
				}
			}
    		return deleteNodes(deletedNodes);
    	} else {
    		return false;
    	}
	}
	
	public Set<Node> getAcceptNodes() {
    	Set<Node> acceptNodes = new HashSet<Node>();
    	if (hasNodes()) {
    		for (Node node : nodes) {
        		if (node.isAcceptNode()) {
        			acceptNodes.add(node);
        		}
        	}
    	}
    	return acceptNodes;
    }
	
	 public Set<Edge> getMinimizableEdges(Node node, HashMap<String, Set<Edge>> nodeIDsToIncomingEdges) {
		 Set<Edge> minimizableEdges = new HashSet<Edge>();

		 if (nodeIDsToIncomingEdges.containsKey(node.getNodeID())) {
			 Set<Edge> incomingEdges = nodeIDsToIncomingEdges.get(node.getNodeID());

			 if (incomingEdges.size() == 1) {
				 Edge incomingEdge = incomingEdges.iterator().next();

				 Node predecessor = incomingEdge.getTail();

				 if (!incomingEdge.hasComponents() && !incomingEdge.isCyclic()
						 && (predecessor.getNumEdges() == 1 || !node.hasConflictingNodeType(predecessor))) {
					 minimizableEdges.add(incomingEdge);
				 }
			 } else if (incomingEdges.size() > 1) {
				 for (Edge incomingEdge : incomingEdges) {
					 Node predecessor = incomingEdge.getTail();

					 if (!incomingEdge.hasComponents() && !incomingEdge.isCyclic() 
							 && predecessor.getNumEdges() == 1 && !predecessor.hasConflictingNodeType(node)) {
						 minimizableEdges.add(incomingEdge);
					 }
				 }
			 }
		 }

		 return minimizableEdges;
	 }
	
	public int getIdIndex() {
		return idIndex;
	}
	
	public int getNumNodes() {
		if (hasNodes()) {
			return nodes.size();
		} else {
			return 0;
		}
	}
	
	public Node getNode(String nodeID) {
		if (hasNodes()) {
			for (Node node : nodes) {
				if (node.getNodeID().equals(nodeID)) {
					return node;
				}
			}
			return null;
		} else {
			return null;
		}
	}
    
    public Set<Node> getNodes() {
    	return nodes;
    }
    
    public int getSize() {
    	if (hasNodes()) {
    		return nodes.size();
    	} else {
    		return 0;
    	}
    }
    
    public Node getStartNode() {
    	if (hasNodes()) {
    		for (Node node : nodes) {
        		if (node.isStartNode()) {
        			return node;
        		}
        	}
        	return null;
    	} else {
    		return null;
    	}
    }
    
    public Set<Node> getStartNodes() {
    	Set<Node> startNodes = new HashSet<Node>();
    	
    	if (hasNodes()) {
    		for (Node node : nodes) {
        		if (node.isStartNode()) {
        			startNodes.add(node);
        		}
        	}
    	} 
    	
    	return startNodes;
    }
    
    public Set<Node> getTypedNodes() {
    	Set<Node> typedNodes = new HashSet<Node>();
    	
    	if (hasNodes()) {
    		for (Node node : nodes) {
        		if (node.hasNodeType()) {
        			typedNodes.add(node);
        		}
        	}
    	} 
    	
    	return typedNodes;
    }
    
    public boolean hasNodes() {
    	if (nodes == null) {
    		return false;
    	} else {
    		return nodes.size() > 0;
    	}
    }
    
    public boolean hasReverseComponents() {
    	if (hasNodes()) {
    		for (Node node : nodes) {
    			if (node.hasEdges()) {
    				for (Edge edge : node.getEdges()) {
    					if (edge.hasComponentIDs()) {
    						for (String compID : edge.getComponentIDs()) {
    							if (compID.startsWith(DesignSpaceService.REVERSE_PREFIX)) {
    								return true;
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	
    	return false;
    }
    
    public HashMap<String, Set<Edge>> mapNodeIDsToIncomingEdges() {
    	HashMap<String, Set<Edge>> nodeIDToIncomingEdges = new HashMap<String, Set<Edge>>();
		if (hasNodes()) {
			for (Node node : nodes) {
	    		if (node.hasEdges()) {
	    			for (Edge edge : node.getEdges()) {
	    				Node successor = edge.getHead();
	    				if (!nodeIDToIncomingEdges.containsKey(successor.getNodeID())) {
	    					nodeIDToIncomingEdges.put(successor.getNodeID(), new HashSet<Edge>());
	    				}
	    				nodeIDToIncomingEdges.get(successor.getNodeID()).add(edge);
	    			}
	    		}
	    	}
		}
		return nodeIDToIncomingEdges;
	}
    
    public HashMap<String, Node> mapNodeIDsToNodes() {
    	HashMap<String, Node> nodeIDToNode = new HashMap<String, Node>();
    	
    	if (hasNodes()) {
    		for (Node node : nodes) {
    			nodeIDToNode.put(node.getNodeID(), node);
    		}
    	}
 
    	return nodeIDToNode;
    }
    
    public Set<Node> getOtherNodes(Set<Node> nodes) {
    	Set<Node> diffNodes = new HashSet<Node>();

    	if (hasNodes()) {
    		for (Node node : this.nodes) {
    			if (!nodes.contains(node)) {
    				diffNodes.add(node);
    			}
    		}
    	}

    	return diffNodes;
    }
    
    public Set<Node> retainNodes(Set<Node> retainedNodes) {
    	Set<Node> diffNodes = getOtherNodes(retainedNodes);
    	
    	if (diffNodes.size() > 0) {
    		deleteNodes(diffNodes);
    	}
    	
    	return diffNodes;
    }
    
    public Set<Edge> getOtherEdges(Set<Edge> edges) {
    	Set<Edge> diffEdges = new HashSet<Edge>();

    	if (hasNodes()) {
    		for (Node node : nodes) {
    			diffEdges.addAll(getOtherEdges(node, edges));
    		}
    	}

    	return diffEdges;
    }
    
    private Set<Edge> getOtherEdges(Node node, Set<Edge> edges) {
    	Set<Edge> diffEdges = new HashSet<Edge>();

    	if (node.hasEdges()) {
    		for (Edge edge : node.getEdges()) {
    			if (!edges.contains(edge)) {
    				diffEdges.add(edge);
    			}
    		}
    	}
    	
    	return diffEdges;
    }
    
    public Set<Edge> retainEdges(Set<Edge> retainedEdges) {
    	Set<Edge> diffEdges = new HashSet<Edge>();
    	
    	if (hasNodes()) {
    		for (Node node : nodes) {
    			Set<Edge> localDiffEdges = getOtherEdges(node, retainedEdges);
    			
    			if (localDiffEdges.size() > 0) {
    				node.deleteEdges(localDiffEdges);

    				diffEdges.addAll(localDiffEdges);
    			}
    		}
    	}
    	
    	return diffEdges;
    }
    
    public void reverseComplement() {
    	if (hasNodes()) {
    		HashMap<String, Set<Edge>> nodeIDToEdges = new HashMap<String, Set<Edge>>();
    		
    		for (Node node : nodes) {
    			if (node.isAcceptNode()) {
    				node.setNodeType(NodeType.START.getValue());
    			} else if (node.isStartNode()) {
    				node.setNodeType(NodeType.ACCEPT.getValue());
    			}
    			
    			if (node.hasEdges()) {
    				for (Edge edge : node.getEdges()) {
    					if (!nodeIDToEdges.containsKey(edge.getHead().getNodeID())) {
    						nodeIDToEdges.put(edge.getHead().getNodeID(), new HashSet<Edge>());
    					}
    					
    					nodeIDToEdges.get(edge.getHead().getNodeID()).add(edge);
    					
    					edge.reverseComplement();
    				}
    			}
    		}
    		
    		for (Node node : nodes) {
    			if (nodeIDToEdges.containsKey(node.getNodeID())) {
    				node.setEdges(nodeIDToEdges.get(node.getNodeID()));
    			} else {
    				node.clearEdges();
    			}
    		}
    	}
    }
    
    public void setIDIndex(int idIndex) {
    	this.idIndex = idIndex;
    }
    
    public Set<Node> removeNodesByID(Set<String> nodeIDs) {
    	Set<Node> removedNodes = new HashSet<Node>();
    	for (Node node : nodes) {
    		if (nodeIDs.contains(node.getNodeID())) {
    			removedNodes.add(node);
    		}
    	}
    	nodes.removeAll(removedNodes);
    	return removedNodes;
    }
}
