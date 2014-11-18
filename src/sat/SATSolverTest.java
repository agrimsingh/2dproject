package sat;

import static org.junit.Assert.*;
import java.io.BufferedReader;
//import java.io.File;
import java.io.FileReader;
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
	
	public static void main(String[] args) {
		final String file = "C:\\Users\\Agrim\\Desktop\\ISTD\\50.001\\2d\\sampleCNF\\s8.cnf";
		Formula f = new Formula();
		try {
			List<String> content = readFile(file);
			List<Integer> list = new ArrayList<Integer>();
			int var_num=-1;
			int clause_num=-1;
			for (String line:content){
				if (!line.isEmpty()){ //ignoring empty lines
					if(line.charAt(0)!='c'){ //ignoring comments
						if (line.charAt(0)=='p'){
							if (var_num!=-1)
								throw new Exception("Multiple problem lines specified");
							String[] split = line.split(" ");
							if (split.length!=4 || split[0]!="p"){
								throw new Exception("Invalid problem line");
							}
							if (split[1]!="cnf"){
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
			HashMap<String,Literal> lit = new HashMap<String,Literal>();
			for(int i=0;i<var_num;i++){
				String name = new Integer(i).toString();
				lit.put(name, PosLiteral.make(name));
			}
			
			List<Literal> literal_in_clause = new ArrayList<Literal>();
			for (int i:list){
				if(i==0){
					f.addClause(makeCl((Literal[])literal_in_clause.toArray()));
					literal_in_clause = new ArrayList<Literal>();
				}
				if (i>0){
					literal_in_clause.add(lit.get(new Integer(i).toString()));
				}
				if (i<0){
					literal_in_clause.add(lit.get(new Integer(i).toString()).getNegation());
				}
			}
			if (f.getSize()!=clause_num){
				throw new Exception("Clause number mismatch");
			}
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		Environment e = SATSolver.solve(f);
		//asserts
		
	}
    
	// TODO: put your test cases for SATSolver.solve here
	
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