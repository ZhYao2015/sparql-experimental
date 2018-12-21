package org.semanticweb.arq.arq_mem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



public class App 
{
	public static void main( String[] args )
    {
		Logger logger=Logger.getLogger(org.semanticweb.arq.arq_mem.App.class);
		
		PropertyConfigurator.configure("log4j.properties");
		
        Options opts=new Options();
        
        Option opt_ontology=Option.builder("d")
        		.longOpt("dir")
        		.argName("DIRECTORY")
        		.hasArg()
        		.build();
        Option opt_query=Option.builder("q")
        		.longOpt("query")
        		.argName("FILE")
        		.hasArg()
        		.build();
        
        opt_ontology.setArgs(1000);
        
        opts.addOption(opt_ontology);
        opts.addOption(opt_query);
        
        HelpFormatter formatter=new HelpFormatter();
        formatter.printHelp("ARQ-MEM", opts);
        
        CommandLineParser parser=new DefaultParser();
        CommandLine cmd;
        Dataset ds;
        try {
			cmd=parser.parse(opts, args);
			ds=DatasetFactory.createMem();
			
			String d_path=cmd.getOptionValue("dir");
			File directory=new File(d_path);
			
			File o_file[]=directory.listFiles();
			
			long load_start=System.currentTimeMillis();
			logger.debug("start loading ontologies");
			for(int i=0;i<o_file.length;i++) {
				String o_path=o_file[i].getCanonicalPath();
				if(o_path.substring(o_path.lastIndexOf(".")+1).equals("owl")) {
					logger.debug("loading ontology:"+o_path);
					RDFDataMgr.read(ds, o_path);
				}
			}
			logger.debug("Finsish loading ontologies in:"+
			Long.toString(System.currentTimeMillis()-load_start)+".msec");
			
			String q_path=cmd.getOptionValue("query");
			
			if(q_path!=null) {
				long query_start=System.currentTimeMillis();
				
				Query query=QueryFactory.read(q_path);
				QueryExecution qEx=QueryExecutionFactory.create(query,ds);
				ResultSet results=qEx.execSelect();
				ResultSetFormatter.out(System.out,results);
				Long queryTime=System.currentTimeMillis()-query_start;
				logger.debug("Query finished in:"+
				Long.toString(queryTime)+".msec");
				//for testing
				File f=new File("10xtime.txt");
				FileWriter writer=new FileWriter(f,true);
				writer.write(Long.toString(queryTime)+"\n");
				writer.close();
			}
			ds.close();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
		}
        
    }
}
