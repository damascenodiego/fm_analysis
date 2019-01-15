package br.usp.icmc.ffsm;

import java.util.ArrayList;

import br.usp.icmc.fsm.common.State;

public class FState{
	
	private String name;
	private String condition;
	protected ArrayList<FTransition> in, out;
	
	public FState(String name, String condition) {			
			this.name = name;
			this.condition = condition;
			this.in = new ArrayList<FTransition>();
			this.out = new ArrayList<FTransition>();
	}
	@Override
	public String toString() 
	{
		return name.trim()+"@"+condition;
	}
	
	public String getStateName(){
		return name;
	}
	public String getCondition(){
		return condition;
	}
	
	public ArrayList<FTransition> getIn() {
		return in;
	}

	public void setIn(ArrayList<FTransition> in) {
		this.in = in;
	}

	public ArrayList<FTransition> getOut() {
		return out;
	}

	public void setOut(ArrayList<FTransition> out) {
		this.out = out;
	}
	
	public void addInTransition(FTransition t) {
		in.add(t);
	}

	public void addOutTransition(FTransition t) {
		out.add(t);
	}
	
	public boolean isDefinedForCInput(CIn cinput) 
	{
		String input = cinput.getIn();
		String condition = cinput.getCond();
		for(FTransition ft : out)
		{
			if(ft.getCInput().getIn().equals(input) &&
					ft.getCInput().getCond().equals(condition))
				return true;
		}
		return false;
	}
	
}

