package org.fiware.apps.marketplace.rdf;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
 * Copyright (C) 2015 CoNWeT Lab, Universidad Politécnica de Madrid
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

@Service("rdfHelper")
public class RdfHelper {

	private static final Logger logger = LoggerFactory.getLogger(RdfHelper.class);

	// TODO resolve prefixes reasonably depending on the preferences...
	/**
	 * Preliminary List of prefixes for queries
	 */
	private static final String QUERY_PREFIXES = "PREFIX gr: <http://purl.org/goodrelations/v1#> "
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX usdl: <http://www.linked-usdl.org/ns/usdl-core#>  "
			+ "PREFIX legal: <http://www.linked-usdl.org/ns/usdl-legal#>  "
			+ "PREFIX price: <http://www.linked-usdl.org/ns/usdl-pricing#>  "
			+ "PREFIX sla: <http://www.linked-usdl.org/ns/usdl-sla#> "
			+ "PREFIX gn: <http://www.geonames.org/ontology#> "
			+ "PREFIX dcterms: <http://purl.org/dc/terms/> "
			+ "PREFIX genVoc: <http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_General_001#> "
			+ "PREFIX cloud: <http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_Cloud_004#> "
			+ "PREFIX os: <http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_OperatingSystem_003#> "
			+ "PREFIX support: <http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_Support_003#> ";

	public static String getQueryPrefixes() {
		return QUERY_PREFIXES;
	}

	public Model loadModel(String serviceURI) throws MalformedURLException {
		Model model = ModelFactory.createDefaultModel();

		URL url = new URL(serviceURI);

		try (InputStream in = url.openStream()) {
			model.read(in, null);
		} catch (IOException e) {
			logger.warn("Unexpected IOException", e);
			return null;
		}

		return model;
	}

	/**
	 * Returns the model of the RDF-file located at the given URL or null if
	 * reading failed.
	 * 
	 * @param uri
	 * @return
	 */
	public Model getModelFromUri(String uri) {		
		int posOfHash = uri.lastIndexOf("#");
		String shortUri = uri;

		if(posOfHash > 0) {
			shortUri = shortUri.substring(0, posOfHash);	
		}

		Model model = ModelFactory.createDefaultModel();

		// Turtle models don't seem to work...
		FileManager.get().readModel(model, uri);

		return model;
	}

	/**
	 * Executes the given query and returns the results as list or an empty list
	 * when failed.
	 * 
	 * @param model
	 * @param queryString
	 * @return
	 */
	public List<QuerySolution> query(Model model, String queryString) {
		Query query = null;
		QueryExecution queryExec = null;
		List<QuerySolution> solutions = new ArrayList<QuerySolution>();

		try {
			query = QueryFactory.create(queryString);
			queryExec = QueryExecutionFactory.create(query, model);
			ResultSet results = queryExec.execSelect();

			while(results.hasNext()) {
				solutions.add(results.nextSolution());
			}
		} catch (Exception ex) {
			logger.warn("SPARQL query failed {}", queryString.replace(QUERY_PREFIXES, ""), ex);
		} finally {
			if (queryExec != null) {
				queryExec.close();
			}
		}
		return solutions;
	}

	/**
	 * Returns the literal that is found via the given query,
	 * @param model 
	 * @param query
	 * @param queriedVar
	 * @return The literal or null
	 */
	public String queryLiteral(Model model, String query, String queriedVar) {
		List<QuerySolution> solutions = this.query(model, query);

		if (solutions.size() > 0) {
			Literal literal = solutions.get(0).getLiteral(queriedVar);

			if (literal != null) {
				return literal.getLexicalForm();
			}
		}

		return null;
	}

	/**
	 * Returns a list of URIs which are found via the given query.
	 * 
	 * @param model
	 * @param query
	 * @param queriedVar
	 * @return List of found URIs or empty list
	 */
	public List<String> queryUris(Model model, String query, String queriedVar) {

		List<String> uris = new ArrayList<String>();
		for (QuerySolution solution : this.query(model, query)) {
			Resource res = solution.getResource(queriedVar);

			if (res != null) {
				uris.add(res.getURI());
			}
		}

		return uris;
	}

	/**
	 * Returns the first found URI found via the given query.
	 * 
	 * @param model
	 * @param query
	 * @param queriedVar
	 * @return Found URI or null
	 */
	public String queryUri(Model model, String query, String queriedVar) {
		List<QuerySolution> solutions = this.query(model, query);
		if (solutions.size() > 0) {

			Resource resource = solutions.get(0).getResource(queriedVar);
			if (resource != null) {
				return resource.getURI();
			}
		}

		return null;
	}

	/**
	 * Returns the URI of the object of the corresponding triple or null if such
	 * a triple does not exist.
	 * 
	 * @param model
	 * @param subject
	 * @param predicate
	 * @return URI or null
	 */
	public String getObjectUri(Model model, String subject, String predicate) {
		String query = QUERY_PREFIXES + "SELECT ?x WHERE { <" + subject + "> "
				+ predicate + " ?x . } ";
		logger.info(query);
		return queryUri(model, query, "x");
	}

	/**
	 * Returns the URIs of the object of the corresponding triple or null if such
	 * a triple does not exist.
	 * 
	 * @param model
	 * @param subject
	 * @param predicate
	 * @return URI or null
	 */
	public List<String> getObjectUris(Model model, String subject, String predicate) {
		String query = QUERY_PREFIXES + "SELECT ?x WHERE { <" + subject + "> "
				+ predicate + " ?x . } ";
		logger.info(query.replace(QUERY_PREFIXES, ""));
		return queryUris(model, query, "x");
	}

	/**
	 * Returns the literal of the object of the corresponding triple of null if the
	 * literal cannot be found
	 * 
	 * @param model
	 * @param subject
	 * @param predicate
	 * @return The literal or null
	 */
	public String getLiteral(Model model, String subject, String predicate) {
		String query = QUERY_PREFIXES + "SELECT ?x WHERE { <" + subject + "> "
				+ predicate + " ?x . } ";
		return queryLiteral(model, query, "x");
	}

}
