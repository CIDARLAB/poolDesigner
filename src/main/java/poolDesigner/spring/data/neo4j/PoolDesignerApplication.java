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

package poolDesigner.spring.data.neo4j;

import poolDesigner.spring.data.neo4j.exception.DesignSpaceNotFoundException;
import poolDesigner.spring.data.neo4j.services.DesignSpaceService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author nicholas roehner
 * @since 12.14.15
 */
@Configuration
@Import(MyNeo4jConfiguration.class)
@RestController("/")
public class PoolDesignerApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(PoolDesignerApplication.class, args);
    }

    @Autowired
    DesignSpaceService designSpaceService;
    
    @ResponseBody @RequestMapping(value = "/delete/all", method = RequestMethod.POST)
    public ResponseEntity<String> deleteAll() {
    	designSpaceService.deleteAll();
    	
    	return new ResponseEntity<String>("\"Database was successfully cleared.\"", 
    			HttpStatus.OK);
    }
    
    @ResponseBody @RequestMapping(value = "/design/pool", method = RequestMethod.POST)
    public ResponseEntity<String> designPools(@RequestBody String poolSpecJSON) {
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			List<String> poolSpecs = mapper.readValue(poolSpecJSON, new TypeReference<List<String>>(){});
			
			return new ResponseEntity<String>(mapper.writeValueAsString(designSpaceService.designPools(poolSpecs)), 
					HttpStatus.OK);
		} catch (JsonParseException ex) {
			return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (JsonMappingException ex) {
			return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (IOException ex) {
			return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (DesignSpaceNotFoundException ex) {
			return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		}
    }
    
    @RequestMapping(value = "/import/sbol", method = RequestMethod.POST)
    public ResponseEntity<String> importSBOL(@RequestParam(value = "inputSBOLFiles[]", required = true) List<MultipartFile> inputSBOLFiles) {
    	Set<SBOLDocument> sbolDocs = new HashSet<SBOLDocument>();
    	
    	for (MultipartFile inputSBOLFile : inputSBOLFiles) {
    		if (!inputSBOLFile.isEmpty()) {
    			try {
    				sbolDocs.add(SBOLReader.read(inputSBOLFile.getInputStream()));
    			} catch (SBOLValidationException ex) {
    				return new ResponseEntity<String>("{\"message\": \"" + ex.getMessage() + "\"}", 
    	    				HttpStatus.BAD_REQUEST);
    			} catch (IOException ex) {
    				return new ResponseEntity<String>("{\"message\": \"" + ex.getMessage() + "\"}", 
    	    				HttpStatus.BAD_REQUEST);
    			} catch (SBOLConversionException ex) {
    				return new ResponseEntity<String>("{\"message\": \"" + ex.getMessage() + "\"}", 
    	    				HttpStatus.BAD_REQUEST);
    			}
    		}
    	}
    	
    	designSpaceService.importSBOL(sbolDocs);
    	
    	return new ResponseEntity<String>("{\"message\": \"SBOL was successfully imported.\"}", 
				HttpStatus.NO_CONTENT);
    }
}
