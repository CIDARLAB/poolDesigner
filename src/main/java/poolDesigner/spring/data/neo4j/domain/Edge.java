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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.*;

import poolDesigner.spring.data.neo4j.services.DesignSpaceService;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@JsonIdentityInfo(generator=JSOGGenerator.class)
@RelationshipEntity(type = "PRECEDES")
public class Edge {
	
    @GraphId
    Long id;
    
    @StartNode
    Node tail;
    
    @EndNode
    Node head;
    
    ArrayList<String> componentIDs;
    
    ArrayList<String> componentRoles;

    public Edge() {
    	
    }
    
    public Edge(Node tail, Node head) {
    	this.tail = tail;
    	this.head = head;
    }
    
    public Edge(Node tail, Node head, ArrayList<String> componentIDs, ArrayList<String> componentRoles) {
    	this.tail = tail;
    	this.head = head;
    	this.componentIDs = componentIDs;
    	this.componentRoles = componentRoles;
    }
    
    public void addComponent(String compID, ArrayList<String> compRoles) {
    	if (!hasComponentIDs()) {
    		componentIDs = new ArrayList<String>();
    		
    		componentRoles = new ArrayList<String>();
    	}
    	
    	if (!componentIDs.contains(compID)) {
    		componentIDs.add(compID);
    	}
    	
    	for (String compRole : compRoles) {
    		if (!componentRoles.contains(compRole)) {
    			componentRoles.add(compRole);
    		}
    	}
    }
    
    public Edge copy(Node tail, Node head) {
    	if (hasComponentIDs() && hasComponentRoles()) {
    		return new Edge(tail, head, new ArrayList<String>(componentIDs), new ArrayList<String>(componentRoles));
    	} else {
    		return new Edge(tail, head);
    	}
    }
    
    public boolean deleteComponent(String compID) {
    	if (hasComponentIDs()) {
    		boolean result = componentIDs.remove(compID);
    		
    		if (componentIDs.size() == 0) {
    			componentIDs = null;
    			
    			componentRoles = null;
    		}
    		
    		return result;
    	} else {
    		return false;
    	}
    }

    public Node getTail() {
        return tail;
    }

    public Node getHead() {
        return head;
    }
    
    public ArrayList<String> getComponentIDs() {
    	return componentIDs;
    }
    
    public String getComponentID(int i) {
    	return componentIDs.get(i);
    }
    
    public ArrayList<String> getComponentRoles() {
    	return componentRoles;
    }
    
    public boolean hasComponentID(String compID) {
    	if (hasComponentIDs()) {
    		return componentIDs.contains(compID);
    	} else {
    		return false;
    	}
    }
    
    public boolean hasComponentIDs() {
    	if (componentIDs == null) {
    		return false;
    	} else {
    		return componentIDs.size() > 0;
    	}
    }
    
    public boolean hasComponentRole(String compRole) {
    	if (hasComponentRoles()) {
    		return componentRoles.contains(compRole);
    	} else {
    		return false;
    	}
    }
    
    public boolean hasComponentRoles() {
    	if (componentRoles == null) {
    		return false;
    	} else {
    		return componentRoles.size() > 0;
    	}
    }
    
    public boolean hasComponents() {
    	return hasComponentIDs() && hasComponentRoles();
    }
    
    public void intersectWithEdge(Edge edge) {
    	if (hasComponentIDs() && edge.hasComponentIDs()) {
    		componentIDs.retainAll(edge.getComponentIDs());
    	}
    	
    	if (hasComponentRoles() && edge.hasComponentRoles()) {
    		componentRoles.retainAll(edge.getComponentRoles());
    	}
    	
    	if (componentIDs != null && componentIDs.size() == 0) {
    		componentIDs = null;
    		componentRoles = null;
    	}
    }
    
    public boolean isComponentEdge() {
    	return hasComponentIDs() && hasComponentRoles();
    }
    
    public boolean isIdenticalTo(Edge edge) {
    	if (tail.equals(edge.getTail()) && head.equals(edge.getHead())) {
    		return hasSameComponents(edge);
    	} else {
    		return false;
    	}
    }
    
    public boolean isMatchingTo(Edge edge, int strength) {
    	if (strength == 0 && hasSameComponents(edge)
				|| strength == 1 && hasSharedComponents(edge) 
				|| strength == 2 && hasSharedRoles(edge)) {
			return true;
		} else {
			return false;
		}
    }
    
    public boolean hasSameComponents(Edge edge) {
    	if (hasComponentIDs() && edge.hasComponentIDs() && 
    			hasComponentRoles() && edge.hasComponentRoles()) {
        	Set<String> compIDs1 = new HashSet<String>(componentIDs);
    		Set<String> compIDs2 = new HashSet<String>(edge.getComponentIDs());
    		
    		Set<String> compRoles1 = new HashSet<String>(componentRoles);
    		Set<String> compRoles2 = new HashSet<String>(edge.getComponentRoles());
    		
    		return compIDs1.equals(compIDs2) && compRoles1.equals(compRoles2);
    	} else if (!hasComponentIDs() && !edge.hasComponentIDs() && !hasComponentRoles() 
    			&& !edge.hasComponentRoles()) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean hasSharedComponents(Edge edge) {
    	if (hasComponentIDs() && edge.hasComponentIDs() && 
    			hasComponentRoles() && edge.hasComponentRoles()) {
    		Set<String> compIDs1 = new HashSet<String>(componentIDs);
    		Set<String> compIDs2 = new HashSet<String>(edge.getComponentIDs());
    		
    		Set<String> compRoles1 = new HashSet<String>(componentRoles);
    		Set<String> compRoles2 = new HashSet<String>(edge.getComponentRoles());
    		
    		compIDs1.retainAll(compIDs2);
    		compRoles1.retainAll(compRoles2);
    		
    		return compIDs1.size() > 0 && compRoles1.size() > 0;
    	} else if (!hasComponentIDs() && !edge.hasComponentIDs() 
    			&& !hasComponentRoles() && !edge.hasComponentRoles()) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean hasSharedRoles(Edge edge) {
    	if (hasComponentRoles() && edge.hasComponentRoles()) {
    		Set<String> compRoles1 = new HashSet<String>(componentRoles);
    		Set<String> compRoles2 = new HashSet<String>(edge.getComponentRoles());
    		
    		compRoles1.retainAll(compRoles2);
    		
    		return compRoles1.size() > 0;
    	} else if (!hasComponentRoles() && !edge.hasComponentRoles()) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean isCyclic() {
    	return tail.equals(head);
    }
    
    public void reverseComplement() {
    	Node temp = head;
		
		head = tail;
		
		tail = temp;
		
		if (hasComponentIDs()) {
			ArrayList<String> reverseComponentIDs = new ArrayList<String>(componentIDs.size());

			for (String compID : componentIDs) {
				if (compID.startsWith(DesignSpaceService.REVERSE_PREFIX)) {
					reverseComponentIDs.add(compID.substring(DesignSpaceService.REVERSE_PREFIX.length()));
				} else {
					reverseComponentIDs.add(DesignSpaceService.REVERSE_PREFIX + compID);
				}
			}
			
			this.componentIDs = reverseComponentIDs;
		}
    }
    
    public void setHead(Node head) {
    	this.head = head;
    }
    
    public void setTail(Node tail) {
    	this.tail = tail;
    }
    
    public void unionWithEdge(Edge edge) {
    	Set<String> mergedCompIDs = new HashSet<String>();
    	
    	Set<String> mergedCompRoles = new HashSet<String>();
    	
    	if (hasComponentIDs() && edge.hasComponentIDs()) {
    		mergedCompIDs.addAll(componentIDs);
    		
    		for (String compID : edge.getComponentIDs()) {
        		if (!mergedCompIDs.contains(compID)) {
        			componentIDs.add(compID);
        			
        			mergedCompIDs.add(compID);
        		}
        	}
    	}
    	
    	if (hasComponentRoles() && edge.hasComponentRoles()) {
    		mergedCompRoles.addAll(componentRoles);
    		
    		for (String compRole : edge.getComponentRoles()) {
        		if (!mergedCompRoles.contains(compRole)) {
        			componentRoles.add(compRole);
        			
        			mergedCompRoles.add(compRole);
        		}
        	}
    	}
    }
    
}
