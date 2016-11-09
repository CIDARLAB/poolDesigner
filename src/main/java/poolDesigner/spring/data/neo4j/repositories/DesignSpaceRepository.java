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

package poolDesigner.spring.data.neo4j.repositories;

import poolDesigner.spring.data.neo4j.domain.DesignSpace;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author nicholas roehner
 * @since 12.14.15
 */
@RepositoryRestResource(collectionResourceRel = "poolDesigner", path = "poolDesigner")
public interface DesignSpaceRepository extends GraphRepository<DesignSpace> {
	@Query("CREATE (output:DesignSpace {spaceID: {outputSpaceID}, idIndex: size({allCompIDs}) + 1}) "
			+ "WITH output "
			+ "UNWIND range(0, size({allCompIDs})) AS i "
			+ "CREATE (output)-[:CONTAINS]->(n:Node {nodeID: 'n' + i}) "
			+ "WITH COLLECT(n) AS ns "
			+ "UNWIND range(0, size(ns) - 1) AS i "
			+ "WITH ns[0] as nStart, ns[size(ns) - 1] as nAccept, ns[i] AS n1, ns[i + 1] AS n2, {allCompIDs}[i] AS compIDs, {allCompRoles}[i] AS compRoles "
			+ "MERGE (n1)-[:PRECEDES {componentIDs: compIDs, componentRoles: compRoles}]->(n2) "
			+ "SET nStart.nodeType = 'start' "
			+ "SET nAccept.nodeType = 'accept'")
	void createDesignSpace(@Param("outputSpaceID") String outputSpaceID, 
			@Param("allCompIDs") ArrayList<ArrayList<String>> allCompIDs, 
			@Param("allCompRoles") ArrayList<ArrayList<String>> allCompRoles);
	
	@Query("MATCH (target:DesignSpace {spaceID: {targetSpaceID}}) "
			+ "OPTIONAL MATCH (target)-[:CONTAINS]->(n:Node) "
			+ "DETACH DELETE target "
			+ "DETACH DELETE n ")
	void deleteDesignSpace(@Param("targetSpaceID") String targetSpaceID);
	
	@Query("MATCH (n) DETACH DELETE n")
	void deleteAll();

	@Query("MATCH (:DesignSpace {spaceID: {targetSpaceID}})-[:CONTAINS]->(n:Node) "
			+ "REMOVE n.copyIndex")
	void deleteNodeCopyIndices(@Param("targetSpaceID") String targetSpaceID);

	DesignSpace findBySpaceID(@Param("spaceID") String spaceID);

	@Query("MATCH (target:DesignSpace {spaceID: {targetSpaceID}}) "
			+ "RETURN ID(target) as graphID")
	Set<Integer> getGraphID(@Param("targetSpaceID") String targetSpaceID);
	
	@Query("MATCH (target:DesignSpace {spaceID: {targetSpaceID}})-[:CONTAINS]->(n:Node) "
			+ "RETURN n.nodeID")
	Set<String> getNodeIDs(@Param("targetSpaceID") String targetSpaceID);
	
//	@Query("MATCH (target:DesignSpace {spaceID: {targetSpaceID}})-[:CONTAINS]->(n:Node) "
//			+ "RETURN count(n)")
//	Integer getNumNodes(@Param("targetSpaceID") String targetSpaceID);
	
	@Query("MATCH (d:DesignSpace) "
			+ "RETURN d.spaceID")
	Set<String> getDesignSpaceIDs();
	
	@Query("MATCH (d:DesignSpace)-[:CONTAINS]->(n:Node) "
			+ "WITH d, count(n) AS dSize "
			+ "WHERE dSize > 2  "
			+ "RETURN d.spaceID")
	Set<String> getCompositeDesignSpaceIDs();
	
	@Query("MATCH (target:DesignSpace)-[:CONTAINS]->(n:Node)-[e:PRECEDES]->(m:Node)<-[:CONTAINS]-(target:DesignSpace) "
			+ "WHERE target.spaceID = {targetSpaceID} AND has(e.componentIDs) "
			+ "WITH COLLECT(e) AS es "
			+ "WITH reduce(output = [], e IN es | output + e.componentIDs) AS compIDs "
			+ "UNWIND compIDs AS compID "
			+ "RETURN compID")
	Set<String> getComponentIDs(@Param("targetSpaceID") String targetSpaceID);
	
	@Query("MATCH (target:DesignSpace)-[:CONTAINS]->(n:Node)-[e:PRECEDES]->(m:Node)<-[:CONTAINS]-(target:DesignSpace) "
			+ "WHERE target.spaceID = {targetSpaceID} AND has(e.componentRoles) "
			+ "WITH COLLECT(e) AS es "
			+ "WITH reduce(output = [], e IN es | output + e.componentRoles) AS compRoles "
			+ "UNWIND compRoles AS compRole "
			+ "RETURN compRole")
	Set<String> getComponentRoles(@Param("targetSpaceID") String targetSpaceID);
	
	@Query("MATCH (target:DesignSpace)-[:CONTAINS]->(m:Node)-[e:PRECEDES]->(n:Node)<-[:CONTAINS]-(target:DesignSpace) "
			+ "WHERE target.spaceID = {targetSpaceID} "
			+ "RETURN target.spaceID as spaceID, m.nodeID as tailID, m.nodeType as tailType, e.componentRoles as componentRoles, "
			+ "n.nodeID as headID, n.nodeType as headType")
	List<Map<String, Object>> mapDesignSpace(@Param("targetSpaceID") String targetSpaceID);

	@Query("MATCH (target:DesignSpace {spaceID: {targetSpaceID}}) "
			+ "SET target.spaceID = {outputSpaceID}")
	void renameDesignSpace(@Param("targetSpaceID") String targetSpaceID, @Param("outputSpaceID") String outputSpaceID);

//	@Query("MATCH (target:DesignSpace {spaceID: {targetSpaceID}})-[:CONTAINS]->(n:Node {nodeID: {targetNodeID}}) "
//			+ "SET n.nodeType = {nodeType}")
//	void setNodeType(@Param("targetSpaceID") String targetSpaceID, @Param("targetNodeID") String targetNodeID,
//			@Param("nodeType") String nodeType);
	
	@Query("MATCH (input:DesignSpace {spaceID: {inputSpaceID}})-[:CONTAINS]->(n:Node) "
			+ "WITH input, collect(n) as nodes "
			+ "MERGE (output:DesignSpace {spaceID: {outputSpaceID}}) "
			+ "ON CREATE SET output.idIndex = size(nodes) "
			+ "ON MATCH SET output.idIndex = output.idIndex + size(nodes) "
			+ "WITH input, nodes, output "
			+ "UNWIND range(0, size(nodes) - 1) as nodeIndex "
			+ "WITH input, nodeIndex, nodes[nodeIndex] as n, output "
			+ "FOREACH(ignoreMe IN CASE WHEN NOT has(n.nodeType) THEN [1] ELSE [] END | "
			+ "CREATE (output)-[:CONTAINS]->(:Node {nodeID: 'n' + (output.idIndex - nodeIndex - 1), copyIndex: ID(n)})) "
			+ "FOREACH(ignoreMe IN CASE WHEN has(n.nodeType) THEN [1] ELSE [] END | "
			+ "CREATE (output)-[:CONTAINS]->(:Node {nodeID: 'n' + (output.idIndex - nodeIndex - 1), copyIndex: ID(n), nodeType: n.nodeType})) "
			+ "WITH input, n as m, output "
			+ "MATCH (m)-[e:PRECEDES]->(n:Node)<-[:CONTAINS]-(input) "
			+ "FOREACH(ignoreMe IN CASE WHEN NOT has(e.componentIDs) AND NOT has(e.componentRoles) THEN [1] ELSE [] END | "
			+ "CREATE UNIQUE (output)-[:CONTAINS]->(:Node {copyIndex: ID(m)})-[:PRECEDES]->(:Node {copyIndex: ID(n)})<-[:CONTAINS]-(output)) "
			+ "FOREACH(ignoreMe IN CASE WHEN has(e.componentIDs) AND has(e.componentRoles) THEN [1] ELSE [] END | "
			+ "CREATE UNIQUE (output)-[:CONTAINS]->(:Node {copyIndex: ID(m)})-[:PRECEDES {componentIDs: e.componentIDs, componentRoles: e.componentRoles}]->(:Node {copyIndex: ID(n)})<-[:CONTAINS]-(output))")
	void unionDesignSpace(@Param("inputSpaceID") String inputSpaceID, @Param("outputSpaceID") String outputSpaceID);
}
