package it.cnr.icar.biograkn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import ai.grakn.Grakn;
import ai.grakn.GraknGraph;
import ai.grakn.exception.GraknValidationException;
import ai.grakn.graql.QueryBuilder;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import static ai.grakn.graql.Graql.*;

public class _04_Gene2GO {

	private static String timeConversion(long seconds) {

	    final int MINUTES_IN_AN_HOUR = 60;
	    final int SECONDS_IN_A_MINUTE = 60;

	    long minutes = seconds / SECONDS_IN_A_MINUTE;
	    seconds -= minutes * SECONDS_IN_A_MINUTE;

	    long hours = minutes / MINUTES_IN_AN_HOUR;
	    minutes -= hours * MINUTES_IN_AN_HOUR;

	    return hours + " hours " + minutes + " minutes " + seconds + " seconds";
	}
	
	public static void main(String[] args) throws IOException, GraknValidationException {
        disableInternalLogs();

        String homeDir = System.getProperty("user.home");
		
        // grep '^9606' gene2go | awk '{ print $2, $3; }' | uniq > gene2go_human
		String fileName = homeDir + "/biodb/gene2go_human";
		String line;
		int edgeCounter = 0;
        long startTime = System.currentTimeMillis();

        GraknGraph graph = Grakn.factory(Grakn.DEFAULT_URI, "biograph").getGraph();
        QueryBuilder qb = graph.graql();
        		
	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting gene2go associations from " + fileName + " ");
                
        while ((line = reader.readLine()) != null) {
        	String datavalue[] = line.split(" ");
        	
        	String geneId = datavalue[0];
        	String goId = datavalue[1];

        	qb.match(
        			var("gene").isa("gene").has("geneId", geneId),
        			var("go").isa("go").has("goId", goId)
        	).insert(
    				var().isa("annotation")
    				.rel("functionalAnnotation", "go")
    				.rel("annotatedEntity", "gene")
    		).execute();

    		edgeCounter++;
    		graph.commit();
    		
            if (edgeCounter % 10000 == 0) {
            	System.out.print("."); System.out.flush();
            }
        }
        
        graph.commit();
        
        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + edgeCounter + " relations in " + timeConversion(stopTime));
        
        reader.close();

	}
	
    public static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }
}