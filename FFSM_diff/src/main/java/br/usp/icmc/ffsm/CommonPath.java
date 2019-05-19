package br.usp.icmc.ffsm;

import java.io.IOException;
import java.util.ArrayList;

import br.usp.icmc.fsm.common.FileHandler;

public class CommonPath {

	ArrayList<FTransition> common_in1;
	ArrayList<FTransition> common_in2;
	FState s1,s2;
	boolean max = false;
	boolean disting;
	int ncstates;
	
	public CommonPath(FState s1, FState s2, int numberCStates){
		common_in1 = new ArrayList<FTransition>();
		common_in2 = new ArrayList<FTransition>();
		this.s1 = s1;
		this.s2 = s2;
		ncstates = numberCStates;
		disting = false;
	}
	
	public String toString(){
		return common_in1+" "+common_in2;
	}
	
	public CommonPath(FState s1, FState s2, int numberCStates, 
			ArrayList<FTransition> common_in1, ArrayList<FTransition> common_in2){
		this.common_in1 = new ArrayList<FTransition>();
		this.common_in2 = new ArrayList<FTransition>();
		this.s1 = s1;
		this.s2 = s2;
		ncstates = numberCStates;
		this.common_in1.addAll(common_in1);
		this.common_in2.addAll(common_in2);
		disting = false;
	}
	
	public boolean addCommon(FTransition t1, FTransition t2) 
			throws IOException, InterruptedException{		
			
		if(common_in1.size() < (ncstates-1) && !disting){	
			//check distinguish
			if(!t1.getOutput().equals(t2.getOutput())){
				disting = true;
			}
			//check dead end
			if(t1.getOutput().equals(t2.getOutput()) && t1.getTarget().equals(t2.getTarget())){
				return false;
			}
			//check for common path loops - when both paths repeat		
			if(common_in1.size() > 1){
				ArrayList<FState> visited1 = new ArrayList<FState>();
				ArrayList<FState> visited2 = new ArrayList<FState>();
				visited1.add(common_in1.get(0).getSource());
				visited2.add(common_in2.get(0).getSource());
				for(FTransition tr1 : common_in1){
					visited1.add(tr1.getTarget());
				}
				for(FTransition tr2 : common_in2){
					visited2.add(tr2.getTarget());
				}
				if(visited1.contains(t1.getTarget()) && visited2.contains(t2.getTarget())){
					return false;
				}
			}
			
			common_in1.add(t1);
			common_in2.add(t2);				
			return true;
		}else{
			max = true;
			return false;
		}	
	}
	
	public boolean addCommon_old(String header, FTransition t1, FTransition t2) 
			throws IOException, InterruptedException{		
			
		if(common_in1.size() < (ncstates-1) && !disting){	
			if(common_in1.size() >= (ncstates-2) && t1.getOutput().equals(t2.getOutput())){
				return false;
			}			
			if(!t1.getOutput().equals(t2.getOutput())){
				disting = true;
			}	
			if(isValid_old(header, t1, t2) ){
				common_in1.add(t1);
				common_in2.add(t2);	
			}else{
				return false;
			}						
			return true;
		}else{
			max = true;
			return false;
		}	
	}
	
	public int getN(){
		return ncstates;
	}
	
	public FState getS1(){
		return s1;
	}
	
	public FState getS2(){
		return s2;
	}
	
	public ArrayList<FTransition> get1(){
		return common_in1;
	}
	
	public ArrayList<FTransition> get2(){
		return common_in2;
	}
	
	public boolean getDistinguish(){
		return disting;
	}
	
	public boolean getIsMax(){
		return max;
	}	
	
	public boolean isValid_old(String header, FTransition co1, FTransition co2) 
			throws IOException, InterruptedException{
				
		String clause = "";
		clause = clause.concat("(assert (and "+s1.getCondition()+" "+s2.getCondition()+"))\n");
		clause = clause.concat("(assert (and ");
		for(FTransition t : common_in1){
			clause = clause.concat(t.getCInput().getCond()+" "
					+t.getTarget().getCondition()+" ");
		}
		for(FTransition t : common_in2){
			clause = clause.concat(t.getCInput().getCond()+" "
					+t.getTarget().getCondition()+" ");
		}
		clause = clause.concat(co1.getCInput().getCond()+" "
					+co1.getTarget().getCondition()+" ");	
		clause = clause.concat(co2.getCInput().getCond()+" "
				+co2.getTarget().getCondition());
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)");
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = "./ffsm/f_ccpairpath.smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		String[] commands = {"./ffsm/z3","./ffsm/f_ccpairpath.smt2"};
		String result = fh.getProcessOutput(commands);
		//System.out.println(result);
		String[] outs = result.split("\n");
						
		if(outs[0].equals("sat")){			
			return true;						
		}else{
			return false;
		}		
	}
	
}
