package sat;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.Test;

import sat.env.*;
import sat.formula.*;


public class SATSolverTest {
    Literal a = PosLiteral.make("a");
    Literal b = PosLiteral.make("b");
    Literal c = PosLiteral.make("c");
    Literal na = a.getNegation();
    Literal nb = b.getNegation();
    Literal nc = c.getNegation();


    private static List<String> readFile(String filename)
    {
      List<String> records = new ArrayList<String>();
      try
      {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null)
        {
          records.add(line);
        }
        reader.close();
        return records;
      }
      catch (Exception e)
      {
        System.err.format("Exception occurred trying to read '%s'.", filename);
        e.printStackTrace();
        return null;
      }
    }

	@Test
	public void main(){
//		final String file = "C:\\Users\\Dhrubajit\\Documents\\eclipse workspace\\2d\\testcases\\t6000-4000.cnf";
		final String file = "C:\\Users\\Agrim\\Documents\\GitHub\\2dproject\\sampleCNF\\largeSat.cnf";
		Formula f = new Formula();
		HashMap<String,Literal> lit = new HashMap<String,Literal>();
		int var_num=-1;
		int clause_num=-1;
		try{
			List<String> content = readFile(file);
			List<Integer> list = new ArrayList<Integer>();
			for (String line:content){
				if (!line.isEmpty()){ //ignoring empty lines
					if(line.charAt(0)!='c'){ //ignoring comments
						if (line.charAt(0)=='p'){
							if (var_num!=-1)
								throw new Exception("Multiple problem lines specified");
							String[] split = line.split(" ");
							if (split.length!=4 || !split[0].equals("p")){
								throw new Exception("Invalid problem line");
							}
							if (!split[1].toLowerCase().contains("cnf")){
								throw new Exception("Invalid format specified");
							}
							var_num = Integer.parseInt(split[2]);
							clause_num = Integer.parseInt(split[3]);
						}
						if (Pattern.matches(line, "[^0-9 -]"))
							throw new Exception("Characters detected at inappropriate locations");
						Scanner in = new Scanner(line); //default delimiter is white spaces and newline
						while(in.hasNextInt()){
							list.add(in.nextInt());
						}
						in.close();
					}
				}
			}
			for(int i=1;i<=var_num;i++){
				String name = new Integer(i).toString();
				lit.put(name, PosLiteral.make(name));
			}
			
			List<Literal> literal_in_clause = new ArrayList<Literal>();
			System.out.println(list);
			for (Integer i:list){
				if(i==0){
					if (!literal_in_clause.isEmpty()){
						Clause c=null;
						try{
							c = makeCl(literal_in_clause.toArray(new Literal[literal_in_clause.size()]));
						} catch (Exception e){
							System.out.println("Unable to create clause "+literal_in_clause.toString());
						}
						if (c!=null){
							f = f.addClause(c);
						} else {
							f = f.addClause(new Clause());
						}
						literal_in_clause = new ArrayList<Literal>();
					}
				}
				if (i>0){
					literal_in_clause.add(lit.get(new Integer(i).toString()));
				}
				if (i<0){
					literal_in_clause.add(lit.get(new Integer(-i).toString()).getNegation());
				}
			}
			if (literal_in_clause.size()>0){ //add all remaining data
				f = f.addClause(makeCl(literal_in_clause.toArray(new Literal[literal_in_clause.size()])));
			}
			if (f.getSize()!=clause_num){
				System.out.println("Warning: Clause number mismatch. Expected "+clause_num+". Found "+f.getSize()+".");
			}
		} catch (Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		//execution time
		System.out.println("SAT solver starts!!!");
		long started = System.nanoTime(); 
		Environment e = SATSolver.solve(f);
		long time = System.nanoTime();
		long timeTaken= time - started;
		System.out.println("Time:" + timeTaken/1000000.0 + "ms");
		
		
		String x ="";
		try{
			for (int i=1;i<=var_num;i++){
				//this will throw exception if unsatisfiable
				x += String.valueOf(i)+": "+e.get(lit.get(new Integer(i).toString()).getVariable()).toString()+"\n";
			}
			System.out.println("satisfiable");
			System.out.println(x);
			try {
		         File file2 = new File("C:\\Users\\Agrim\\Documents\\GitHub\\2dproject\\sampleCNF\\BoolAssignment.txt");
		         BufferedWriter output = new BufferedWriter(new FileWriter(file2));
		         output.write(x);
		         output.close();
			 }catch ( IOException e1 ) {
		           e1.printStackTrace();
		     }
		}
			
		catch(NullPointerException exception){
			System.out.println("unsatisfiable");
		}
	
	}
	
    @Test
    public void testSATSolver1(){
    	// (a v b)
    	Environment e = SATSolver.solve(makeFm(makeCl(a,b))	);
    	assertTrue( "one of the literals should be set to true",
    			Bool.TRUE == e.get(a.getVariable())  
    			|| Bool.TRUE == e.get(b.getVariable())	);
    	
    	
    }
    
    
    @Test
    public void testSATSolver2(){
    	// (~a)
    	Environment e = SATSolver.solve(makeFm(makeCl(na)));
    	assertEquals( Bool.FALSE, e.get(na.getVariable()));
    	
    }
    
    private static Formula makeFm(Clause... e) {
        Formula f = new Formula();
        for (Clause c : e) {
            f = f.addClause(c);
        }
        return f;
    }
    
    private static Clause makeCl(Literal... e) {
        Clause c = new Clause();
        for (Literal l : e) {
            c = c.add(l);
        }
        return c;
    }
    
    
    
}