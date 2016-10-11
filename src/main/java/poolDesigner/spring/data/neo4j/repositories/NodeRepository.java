package poolDesigner.spring.data.neo4j.repositories;

import poolDesigner.spring.data.neo4j.domain.Node;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author nicholas roehner
 * @since 12.14.15
 */
@RepositoryRestResource(collectionResourceRel = "poolDesigner", path = "poolDesigner")
public interface NodeRepository extends GraphRepository<Node> {
	
	Node findByNodeID(@Param("nodeID") String nodeID);
	
}
