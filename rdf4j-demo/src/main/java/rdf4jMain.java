import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;


public class rdf4jMain {
	public static void main(String args[]) throws Exception{
		Options opts=new Options();
		
		Option opt_directory=Option.builder("d")
				.longOpt("dir")
				.hasArg()
				.argName("DIR")
				.desc("read ontologies from a directory")
				.build();
		
		Option opt_ontology=Option.builder("o")
				.longOpt("ontology")
				.hasArg()
				.argName("FILE")
				.desc("ontology to read")
				.build();
		
		//maximal 1000 arguments for this option
		opt_ontology.setArgs(1000);
		
		Option opt_query=Option.builder("q")
				.longOpt("query")
				.hasArg()
				.argName("FILE")
				.desc("a SPARQL query")
				.build();
		
		Option opt_mode=Option.builder("m")
				.longOpt("mode")
				.hasArg()
				.argName("STRING")
				.desc("choose a repository in 'memory' or 'native'")
				.build();
		
		opts.addOption(opt_mode);
		opts.addOption(opt_query);
		opts.addOption(opt_ontology);
		opts.addOption(opt_directory);
		//automatically generate a help-message
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "RDF4j-DEMO", opts );
		
		CommandLineParser parser=new DefaultParser();
		CommandLine cmd;
		
		Repository repo=null;
		try {
			cmd = parser.parse(opts, args);
			String dir_path=cmd.getOptionValue("dir");
			String onto_paths[];
			if(dir_path!=null) {
				File directory=new File(dir_path);
	        	File ontology[]=directory.listFiles();
	        	onto_paths=new String[ontology.length];
	        	for(int i=0;i<ontology.length;i++) {
	        		String path=ontology[i].getAbsolutePath();
	        		onto_paths[i]=path;
	        	}
			}else if(cmd.getOptionValues("ontology")!=null){
				onto_paths=cmd.getOptionValues("ontology");
			}else {
				onto_paths=null;
			}
			
			String query_path=cmd.getOptionValue("query");
			String mode=cmd.getOptionValue("mode");
			
			//Create a new repository (either Memory or Native)
			
			File file=new File(System.getProperty("user.dir")+"/rdf4jDataDir");
			if(mode.equals("memory")) {
				repo=new SailRepository(new MemoryStore());
			}else if(mode.equals("native")) {
				repo=new SailRepository(new NativeStore(file));
			}else {
				repo=null;
			}
			repo.initialize();
			
			RepositoryConnection conn=repo.getConnection();
			
			//loading datasets into repository
			Long startTime=System.currentTimeMillis();
			
			if(onto_paths!=null) {
				for(int i=0;i<onto_paths.length;i++) {
					
					//System.out.print("####Ontology list: "+onto_paths[i]+"#######");
					
					if(onto_paths[i].substring(onto_paths[i].lastIndexOf(".")+1).equals("owl")) {
						InputStream input=new FileInputStream(new File(onto_paths[i]));
						conn.add(input,"", RDFFormat.RDFXML);
					}
					
				}
				
				Long loadTime=System.currentTimeMillis()-startTime;
				System.out.println("Loading cost: "+Long.toString(loadTime)+" msec.");
				
			}else {
				
			}
			
			if(query_path!=null) {
				//Querying
				startTime=System.currentTimeMillis();
				@SuppressWarnings("deprecation")
				String QueryString=FileUtils.readFileToString(new File(query_path));
				
				conn.prepareTupleQuery(QueryString)
				.evaluate(new SPARQLResultsCSVWriter(System.out));
				
				Long queryTime=System.currentTimeMillis()-startTime;
				System.out.println("Querying cost: "+Long.toString(queryTime)+" msec.");
				
				//for testing
				File f=new File("10xtime.txt");
				FileWriter writer=new FileWriter(f,true);
				writer.write(Long.toString(queryTime)+"\n");
				writer.close();
			}
			conn.close();
			
		}catch(ParseException e){
			e.printStackTrace();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		finally {
			repo.shutDown();
		}
		
		
		
	}
	
}
