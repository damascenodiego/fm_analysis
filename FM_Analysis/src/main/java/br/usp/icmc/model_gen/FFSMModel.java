package br.usp.icmc.model_gen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import br.usp.icmc.ffsm.FFSM;
import br.usp.icmc.ffsm.FState;
import br.usp.icmc.ffsm.FTransition;
import br.usp.icmc.fsm.common.FileHandler;
import br.usp.icmc.fsm.common.FiniteStateMachine;
import br.usp.icmc.fsm.common.Node;
import br.usp.icmc.fsm.common.Ntree;
import br.usp.icmc.fsm.common.State;
import br.usp.icmc.fsm.common.Transition;
import br.usp.icmc.reader.FFSMModelReader;
import br.usp.icmc.reader.FsmModelReader;

public class FFSMModel {
	
	Ntree tree;
	ArrayList<State> s_found;
	Node current_node;
	String clause;
	int count;
	ArrayList<Integer> conf_count;
	HashMap<String, Integer> conf_map;
	
	public FFSMModel(){
		conf_map = new HashMap<String, Integer>();
	}
	
	public HashMap<String, Integer> getMap(){
		return conf_map;
	}
	
	public void gen_FFSM(String folder, int amount) throws IOException{
		
		for(int k=1; k<=amount; k++){
			String header = "s0@f0 -- a@f0 / 0 -> s0@f0\n";
					
			String clause = "";
			for(int i=1; i<=k; i++){
				clause = clause.concat("s0@f0 -- a"+i+"@f"+i+" / 0 -> s"+i+"@f"+i+"\n");			
			}
			String prop_aux = header.concat(clause);
			
			clause = "";
			for(int i=1; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- a@f"+i+" / 1 -> s0@f0\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=1; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- b@f"+i+" / o"+i+" -> s"+i+"@f"+i+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			
			String path = "./"+folder+"/ffsm"+k+".txt";
			FileHandler fh = new FileHandler();
			fh.print_file(prop_aux, path);
		}
		
	}
	
	public void gen_FFSM_best(String folder, int amount) throws IOException{
		
		for(int k=1; k<=amount; k++){
			String header = "s0@f0 -- a@f0 / 0 -> s0@f0\n";
					
			String clause = "";
			for(int i=1; i<=k; i++){
				clause = clause.concat("s"+(i-1)+"@f"+(i-1)+" -- a"+i+"@f"+i+" / 0 -> s"+i+"@f"+i+"\n");			
			}
			String prop_aux = header.concat(clause);
			
			clause = "";
			for(int i=1; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- a@f"+i+" / 1 -> s"+(i-1)+"@f"+(i-1)+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=1; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- b@f"+i+" / o"+i+" -> s"+i+"@f"+i+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			
			String path = "./"+folder+"/ffsm"+k+".txt";
			FileHandler fh = new FileHandler();
			fh.print_file(prop_aux, path);
		}
		
	}
	

	public void gen_FFSM_mid(String folder, int amount) throws IOException{
		
		for(int k=1; k<=amount; k++){
			String header = "s0@f0 -- a@f0 / 0 -> s0@f0\n";
					
			String clause = "";
			for(int i=1; i<=(k/2)+1; i++){
				clause = clause.concat("s0@f0 -- a"+i+"@f"+i+" / 0 -> s"+i+"@f"+i+"\n");			
			}
			String prop_aux = header.concat(clause);
			
			clause = "";
			for(int i=1; i<=(k/2)+1; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- a@f"+i+" / 1 -> s0@f0\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=1; i<=(k/2)+1; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- b@f"+i+" / o"+i+" -> s"+i+"@f"+i+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			
			clause = "";
			for(int i=(k/2)+2; i<=k; i++){
				clause = clause.concat("s"+(i-1)+"@f"+(i-1)+" -- a"+i+"@f"+i+" / 0 -> s"+i+"@f"+i+"\n");			
			}
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=(k/2)+2; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- a@f"+i+" / 1 -> s"+(i-1)+"@f"+(i-1)+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=(k/2)+2; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- b@f"+i+" / o"+i+" -> s"+i+"@f"+i+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			
			String path = "./"+folder+"/ffsm"+k+".txt";
			FileHandler fh = new FileHandler();
			fh.print_file(prop_aux, path);
		}
		
	}
	
	public void gen_FFSM_mid_low(String folder, int amount) throws IOException{
		
		for(int k=1; k<=amount; k++){
			String header = "s0@f0 -- a@f0 / 0 -> s0@f0\n";
					
			String clause = "";
			for(int i=1; i<=(k/3)+1; i++){
				clause = clause.concat("s0@f0 -- a"+i+"@f"+i+" / 0 -> s"+i+"@f"+i+"\n");			
			}
			String prop_aux = header.concat(clause);
			
			clause = "";
			for(int i=1; i<=(k/3)+1; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- a@f"+i+" / 1 -> s0@f0\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=1; i<=(k/3)+1; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- b@f"+i+" / o"+i+" -> s"+i+"@f"+i+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			
			clause = "";
			for(int i=(k/3)+2; i<=k; i++){
				clause = clause.concat("s"+(i-1)+"@f"+(i-1)+" -- a"+i+"@f"+i+" / 0 -> s"+i+"@f"+i+"\n");			
			}
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=(k/3)+2; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- a@f"+i+" / 1 -> s"+(i-1)+"@f"+(i-1)+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			clause = "";
			for(int i=(k/3)+2; i<=k; i++){
				clause = clause.concat("s"+i+"@f"+i+" -- b@f"+i+" / o"+i+" -> s"+i+"@f"+i+"\n");			
			}			
			prop_aux = prop_aux.concat(clause);
			
			
			String path = "./"+folder+"/ffsm"+k+".txt";
			FileHandler fh = new FileHandler();
			fh.print_file(prop_aux, path);
		}
		
	}
	
	public void gen_dot_FFSM(String ffsm_path, String dotpath,
			boolean pop_unix_pdf) throws IOException, InterruptedException{
		FileHandler fh = new FileHandler();
				
		File file = new File(ffsm_path);
		FFSMModelReader reader = new FFSMModelReader(file);
		FFSM ffsm = reader.getFFSM();
		
		String clause = "";
		clause = clause.concat("digraph MefGraph{\n");
		clause = clause.concat("	node [fontsize=\"10\"]\n\n");
		clause = clause.concat("                  	rankdir=LR\n");
		for(FState f : ffsm.getFStates()){
			clause = clause.concat("	"+f.getStateName()+" [label=\""+f.getStateName()
					+"("+f.getCondition()+")"
					+"\"]\n");
		}
		for(FTransition t : ffsm.getFTransitions()){
			clause = clause.concat("	"+t.getSource().getStateName()+" -> "+t.getTarget().getStateName()
					+" [label=\""
					+t.getCInput().getIn()
					+"("+t.getCInput().getCond()+")/"
					+t.getOutput()
					+"\"]\n");
		}
		
		clause = clause.concat("}");
		
		fh.print_file(clause, dotpath); 
		if(pop_unix_pdf){
			//dot -T pdf -o ffsm.pdf ffsm.dot
			String[] commands = {"dot","-T", "pdf", "-o", "./increase_random/dots/ffsm.pdf", "./increase_random/dots/ffsm.dot"};
			String result = fh.getProcessOutput(commands);
			
			String[] commands2 = {"gnome-open","./increase_random/dots/ffsm.pdf"};
			result = fh.getProcessOutput(commands2);	
		}				
	}
	
	public void append_fstate_clause(FState state, int depth, int index, String father){
		String r_name = "_"+state.getStateName()+"_"+depth+"_"+index+"_"+father;
		String r_cond = state.getCondition();
		clause = clause.concat("	"+r_name+" [label=\""+state.getStateName());
		if(!r_cond.equals("true")){
			if(r_cond.startsWith("(")){
				clause = clause.concat(r_cond);
			}else {
				clause = clause.concat("("+r_cond+")");
			}
		}	
		clause = clause.concat("\"]\n");
	}
	
	public void append_ftransition_clause(int findex, Node child, int depth, int index, String nfather,
			String father){
		FTransition tr = child.getFTransition();
		
		String l_name = "_"+tr.getSource().getStateName()+"_"+(depth-1)+"_"+findex+"_"+father;
		String r_name = "_"+tr.getTarget().getStateName()+"_"+depth+"_"+index+"_"+nfather;
		String input = tr.getCInput().getIn();
		String r_cond = tr.getCInput().getCond();
		clause = clause.concat("	"+l_name+ " -> "+r_name+" [label=\""+input);
		if(!r_cond.equals("true")){
			if(r_cond.startsWith("(")){
				clause = clause.concat(r_cond);
			}else {
				clause = clause.concat("("+r_cond+")");
			}
		}	
		clause = clause.concat("\"]\n");		
	}
	
	public void append_same_rank(ArrayList<Node> nodes, int depth, int findex, String father){
		clause = clause.concat("	{rank = same; ");
		ArrayList<FState> right = new ArrayList<FState>();
		for(Node n : nodes){
			right.add(n.getFTransition().getTarget());
		}
		int index = findex;
		for(FState s : right){			
			clause = clause.concat("_"+s.getStateName()+"_"+depth+"_"+index+"_"+father+",");
			index++;
		}
		clause = clause.substring(0,clause.length()-1);
		clause = clause.concat("}\n");
		
	}
	
	private void browse(String level, Node n){
		System.out.println(level + n);		
		for(Node n1 : n.getChildren()){			
			browse(level + "----", n1);
		}
	}	
	
	private void build_dot(Node n, int depth, int findex, String father){
		//System.out.println("RECURSIVE IN "+n + " "+depth);
		int index = findex;
		for(Node n1 : n.getChildren()){
			String nfather = n1.getFTransition().getSource().getStateName()
					+n1.getFTransition().getTarget().getStateName();
			append_fstate_clause(n1.getFTransition().getTarget(), depth, index, nfather);
			build_dot(n1, (depth+1), index, nfather);
			append_ftransition_clause(findex, n1, depth, index, nfather, father);
			index++;
			//father = nfather;
		}
		//if(n.getChildren().size() > 1){
		//	append_same_rank(n.getChildren(), depth, findex, father);
		//}
	}
	
	public void gen_dot_transition_tree(FFSM ffsm,
			Map<FTransition,ArrayList<ArrayList<FTransition>>> transition_map,
			String folder, String namefile, boolean pop_unix_pdf) throws IOException, InterruptedException{
		
		FileHandler fh = new FileHandler();	
		String clause = "";
		clause = clause.concat("digraph MefGraph{\n");
		clause = clause.concat("	node [fontsize=\"10\"]\n\n");
		clause = clause.concat("                  	rankdir=LR\n");
		
		//create tree
		Ntree tree = new Ntree();				
		Node root = new Node(ffsm.getFInitialState());		
		tree.setRoot(root);
				
		for(FTransition ft: transition_map.keySet()){							
			for(ArrayList<FTransition> path : transition_map.get(ft)){
				Node last_node = root;
				for(FTransition t : path){
					Node path_node = new Node(t);
					boolean added = false;
					for(Node n : last_node.getChildren()){
						FTransition f = n.getFTransition();
						if(f.equals(t)){
							added = true;
							last_node = n;
							break;
						}
					}						
					if(!added){
						tree.addNode(last_node, path_node);
						last_node = path_node;
					}
				}
			}
		}
		//tree.print();
		browse("-", root);	
		
		this.clause = clause;
		int depth = 0;
		String father = root.getFState().getStateName();
		append_fstate_clause(ffsm.getFInitialState(), depth, 1, father);
		depth++;		
		build_dot(root, depth, 1, father);
		clause = this.clause;
		this.clause = "";
					
		clause = clause.concat("}");
		System.out.println(clause);
		
		fh.print_file(clause, folder+namefile+".dot"); 
		if(pop_unix_pdf){
			//dot -T pdf -o ffsm.pdf ffsm.dot
			String[] commands = {"dot","-T", "pdf", "-o", folder+namefile+".pdf", folder+namefile+".dot"};
			fh.getProcessOutput(commands);
			
			String[] commands2 = {"gnome-open", folder+namefile+".pdf"};
			fh.getProcessOutput(commands2);	
		}
	}
	
	public void gen_dot_state_tree(FFSM ffsm, Map<FState,ArrayList<ArrayList<FTransition>>> path_map,
			String folder, String namefile, boolean pop_unix_pdf) throws IOException, InterruptedException{
		
		FileHandler fh = new FileHandler();	
		String clause = "";
		clause = clause.concat("digraph MefGraph{\n");
		clause = clause.concat("	node [fontsize=\"10\"]\n\n");
		clause = clause.concat("                  	rankdir=LR\n");
		
		//create tree
		Ntree tree = new Ntree();				
		Node root = new Node(ffsm.getFInitialState());		
		tree.setRoot(root);
				
		for(FState fs: path_map.keySet()){	
			if(!fs.equals(ffsm.getFInitialState())){				
				for(ArrayList<FTransition> path : path_map.get(fs)){
					Node last_node = root;
					for(FTransition t : path){
						Node path_node = new Node(t);
						boolean added = false;
						for(Node n : last_node.getChildren()){
							FTransition f = n.getFTransition();
							if(f.equals(t)){
								added = true;
								last_node = n;
								break;
							}
						}						
						if(!added){
							tree.addNode(last_node, path_node);
							last_node = path_node;
						}
					}
				}
			}
		}
		//tree.print();
		browse("-", root);	
		
		this.clause = clause;
		int depth = 0;
		String father = root.getFState().getStateName();
		append_fstate_clause(ffsm.getFInitialState(), depth, 1, father);
		depth++;		
		build_dot(root, depth, 1, father);
		clause = this.clause;
		this.clause = "";
					
		clause = clause.concat("}");
		System.out.println(clause);
		
		fh.print_file(clause, folder+namefile+".dot"); 
		if(pop_unix_pdf){
			//dot -T pdf -o ffsm.pdf ffsm.dot
			String[] commands = {"dot","-T", "pdf", "-o", folder+namefile+".pdf", folder+namefile+".dot"};
			fh.getProcessOutput(commands);
			
			String[] commands2 = {"gnome-open", folder+namefile+".pdf"};
			fh.getProcessOutput(commands2);	
		}
	}
	
	public void gen_dot_FFSM2(String ffsm_path, String dotpath, String folder,
			String namefile, boolean pop_unix_pdf) throws IOException, InterruptedException{
		FileHandler fh = new FileHandler();
				
		File file = new File(ffsm_path);
		FFSMModelReader reader = new FFSMModelReader(file);
		FFSM ffsm = reader.getFFSM();
		
		String clause = "";
		clause = clause.concat("digraph MefGraph{\n");
		clause = clause.concat("	node [fontsize=\"10\"]\n\n");
		clause = clause.concat("                  	rankdir=LR\n");
		clause = clause.concat("     i [color=Black, style=filled, fillcolor=black, shape=point];\n");
		clause = clause.concat("     i -> "+ffsm.getFInitialState().getStateName().replaceAll("\\*", "_")+"\n");
		//clause = clause.concat("     {rank = same; i,"+ffsm.getFInitialState().getStateName()+"}\n");
		for(FState f : ffsm.getFStates()){
			clause = clause.concat("	"+f.getStateName().replaceAll("\\*", "_")+" [label=\""+f.getStateName()
					+"("+f.getCondition()+")"
					+"\"]\n");
		}
		HashMap<String, ArrayList<FTransition>> map = getSameTarget(ffsm.getFTransitions());
		for(String key : map.keySet()){
			FTransition t = map.get(key).get(0);
			clause = clause.concat("	"+t.getSource().getStateName().replaceAll("\\*", "_")+" -> "
					+t.getTarget().getStateName().replaceAll("\\*", "_") +" [label=\"");
			for(FTransition tr : map.get(key)){
				clause = clause.concat(tr.getCInput().getIn()+
						"("+tr.getCInput().getCond()+")/"+tr.getOutput()+"\n");
			}
			clause = clause.substring(0, clause.length()-1);
			clause = clause.concat("\"]\n");
		}			
		clause = clause.concat("}");
		
		fh.print_file(clause, dotpath); 
		//dot -T pdf -o ffsm.pdf ffsm.dot
		String[] commands = {"dot","-T", "pdf", "-o", folder+namefile+".pdf", folder+namefile+".dot"};
		String result = fh.getProcessOutput(commands);
		System.out.println(result);
		if(pop_unix_pdf){
			String[] commands2 = {"gnome-open", folder+namefile+".pdf"};
			fh.getProcessOutput(commands2);	
		}
	}
	
	public HashMap<String, ArrayList<FTransition>> getSameTarget(ArrayList<FTransition> trs){
		HashMap<String, ArrayList<FTransition>> map = new HashMap<String, ArrayList<FTransition>>();
		for(FTransition f : trs){
			String pair = f.getSource().getStateName()+","+f.getTarget().getStateName();
			ArrayList<FTransition> list = null;
			if(map.get(pair) != null){
				list = map.get(pair);				
			}else{
				list = new ArrayList<FTransition>();
			}
			list.add(f);
			map.put(pair, list);
		}
		return map;
	}
	
	public HashMap<String, ArrayList<Transition>> getSameTarget2(ArrayList<Transition> trs){
		HashMap<String, ArrayList<Transition>> map = new HashMap<String, ArrayList<Transition>>();
		for(Transition t : trs){
			String pair = t.getIn().getLabel()+","+t.getOut().getLabel();
			ArrayList<Transition> list = null;
			if(map.get(pair) != null){
				list = map.get(pair);				
			}else{
				list = new ArrayList<Transition>();
			}
			list.add(t);
			map.put(pair, list);
		}
		return map;
	}
	
	public void gen_dot_derived_FSM(String ffsm_path, String dotpath, String folder,
			String namefile, boolean pop_unix_pdf) throws Exception{
		FileHandler fh = new FileHandler();
				
		File file = new File(ffsm_path);
		FsmModelReader reader = new FsmModelReader(file, true);
		FiniteStateMachine fsm = reader.getFsm();
		
		String clause = "";
		clause = clause.concat("digraph MefGraph{\n");
		clause = clause.concat("	node [fontsize=\"10\"]\n\n");
		clause = clause.concat("                  	rankdir=LR\n");
		clause = clause.concat("     i [color=Black, style=filled, fillcolor=black, shape=point];\n");
		clause = clause.concat("     i -> "+fsm.getInitialState().getLabel()+"\n");
		//clause = clause.concat("     {rank = same; i,"+ffsm.getFInitialState().getStateName()+"}\n");
		for(State s : fsm.getStates()){
			clause = clause.concat("	"+s.getLabel()+"\n");
		}		
		HashMap<String, ArrayList<Transition>> map = getSameTarget2(fsm.getTransitions());
		for(String key : map.keySet()){
			Transition t = map.get(key).get(0);
			clause = clause.concat("	"+t.getIn().getLabel()+" -> "
					+t.getOut().getLabel() +" [label=\"");
			for(Transition tr : map.get(key)){
				clause = clause.concat(tr.getInput()+"/"+tr.getOutput()+"\n");
			}
			clause = clause.substring(0, clause.length()-1);
			clause = clause.concat("\"]\n");
		}		
		clause = clause.concat("}");
		
		fh.print_file(clause, dotpath); 		
		if(pop_unix_pdf){
			//dot -T pdf -o ffsm.pdf ffsm.dot
			String[] commands = {"dot","-T", "pdf", "-o", folder+namefile+".pdf", folder+namefile+".dot"};
			fh.getProcessOutput(commands);
			
			String[] commands2 = {"gnome-open", folder+namefile+".pdf"};
			fh.getProcessOutput(commands2);	
		}
				
	}
	
	public void gen_dot_derived_FSM2(String ffsm_path, String dotpath, String folder,
			String namefile, boolean pop_unix_pdf) throws IOException, InterruptedException{
		FileHandler fh = new FileHandler();
				
		File file = new File(ffsm_path);
		FFSMModelReader reader = new FFSMModelReader(file);
		FFSM ffsm = reader.getFFSM();
		
		String clause = "";
		clause = clause.concat("digraph MefGraph{\n");
		clause = clause.concat("	node [fontsize=\"10\"]\n\n");
		clause = clause.concat("                  	rankdir=LR\n");
		clause = clause.concat("     i [color=Black, style=filled, fillcolor=black, shape=point];\n");
		clause = clause.concat("     i -> "+ffsm.getFInitialState().getStateName()+"\n");
		//clause = clause.concat("     {rank = same; i,"+ffsm.getFInitialState().getStateName()+"}\n");
		for(FState f : ffsm.getFStates()){
			clause = clause.concat("	"+f.getStateName()+"\n");
		}
		HashMap<String, ArrayList<FTransition>> map = getSameTarget(ffsm.getFTransitions());
		for(String key : map.keySet()){
			FTransition t = map.get(key).get(0);
			clause = clause.concat("	"+t.getSource().getStateName()+" -> "
					+t.getTarget().getStateName() +" [label=\"");
			for(FTransition tr : map.get(key)){
				clause = clause.concat(tr.getCInput().getIn()+"/"+tr.getOutput()+"\n");
			}
			clause = clause.substring(0, clause.length()-1);
			clause = clause.concat("\"]\n");
		}		
		clause = clause.concat("}");
		
		fh.print_file(clause, dotpath); 
		if(pop_unix_pdf){
			//dot -T pdf -o ffsm.pdf ffsm.dot
			String[] commands = {"dot","-T", "pdf", "-o", folder+namefile+".pdf", folder+namefile+".dot"};
			fh.getProcessOutput(commands);
			
			String[] commands2 = {"gnome-open", folder+namefile+".pdf"};
			fh.getProcessOutput(commands2);	
		}			
	}
	
	public int get_configs_for_ffsm(String fsm_path)throws IOException, InterruptedException{
				
		File file = new File(fsm_path);
		FsmModelReader reader = new FsmModelReader(file, true);
		try {
			FiniteStateMachine fsm = reader.getFsm();
			s_found = new ArrayList<State>();
			s_found.add(fsm.getInitialState());
			
			tree = new Ntree();				
			tree.setRoot(new Node(fsm.getInitialState(), "and"));
			current_node = tree.getRoot();
			count = 0;
			
			rec_tree(fsm.getInitialState(), tree.getRoot());				
			//tree.print();
			//calculate number of configurations		
			int c = calc_config(tree.getRoot());			
			return c;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public int get_configs_for_ffsm(String folder, int nfeatures)throws IOException, InterruptedException{
		
		FileHandler fh = new FileHandler();
		Random ran = new Random();
		int inputs = ran.nextInt(nfeatures) + 1;
		//int inputs = nfeatures;
		int states = nfeatures;
		int transitions = inputs*states;			
		String[] commands = {"./increase_random/fsm-gen-fsm","2", ""+inputs, ""+states, ""+transitions, "1"};
		String result = fh.getProcessOutput(commands);
		
		String path = "./"+folder+"/metafsm/fsm_xxx.txt";			
		fh.print_file(result, path);
		
		// create meta tree for FM			
		File file = new File(path);
		FsmModelReader reader = new FsmModelReader(file, true);
		try {
			FiniteStateMachine fsm = reader.getFsm();
			ArrayList<Transition> ts = fsm.getInitialState().getOut();
			s_found = new ArrayList<State>();
			s_found.add(fsm.getInitialState());
			
			tree = new Ntree();				
			tree.setRoot(new Node(fsm.getInitialState(), "and"));
			current_node = tree.getRoot();
			count = 0;
			
			rec_tree(fsm.getInitialState(), tree.getRoot());				
			//tree.print();
			//calculate number of configurations		
			int c = calc_config(tree.getRoot());	
			gen_random_FM(tree.getRoot(), folder, 1, nfeatures);
			return c;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public int gen_random_FFSM_sample(String folder, int population, int sample, int nfeatures) 
			throws IOException, InterruptedException{
				
		ArrayList<HashMap<Ntree, Integer>> q1 = new ArrayList<HashMap<Ntree, Integer>>();
		ArrayList<HashMap<Ntree, Integer>> q2 = new ArrayList<HashMap<Ntree, Integer>>();
		ArrayList<HashMap<Ntree, Integer>> q3 = new ArrayList<HashMap<Ntree, Integer>>();
		ArrayList<HashMap<Ntree, Integer>> q4 = new ArrayList<HashMap<Ntree, Integer>>();
		ArrayList<HashMap<Ntree, Integer>> qall = new ArrayList<HashMap<Ntree, Integer>>();
		int min_sample = sample;
		
		// pick random trees from the population
		for(int k=1; k<=population; k++){						
			FileHandler fh = new FileHandler();			
			//./fsm-gen-fsm num-out num-in num-states num-trans randseed
			// random inputs=1 to n-1 
			// out=2
			// transitions inputs*states
			
			Random ran = new Random();
			int inputs = ran.nextInt(nfeatures) + 1;
			//int inputs = nfeatures;
			int states = nfeatures;
			int transitions = inputs*states;			
			String[] commands = {"./increase_random/fsm-gen-fsm","2", ""+inputs, ""+states, ""+transitions, ""+k};
			String result = fh.getProcessOutput(commands);
			
			String path = "./"+folder+"/metafsm/fsm_xxx.txt";			
			fh.print_file(result, path);
			
			// create meta tree for FM			
			File file = new File(path);
			FsmModelReader reader = new FsmModelReader(file, true);
			try {
				FiniteStateMachine fsm = reader.getFsm();
				ArrayList<Transition> ts = fsm.getInitialState().getOut();
				s_found = new ArrayList<State>();
				s_found.add(fsm.getInitialState());
				
				tree = new Ntree();				
				tree.setRoot(new Node(fsm.getInitialState(), "and"));
				current_node = tree.getRoot();
				count = 0;
				
				rec_tree(fsm.getInitialState(), tree.getRoot());				
				//tree.print();
				//calculate number of configurations		
				int c = calc_config(tree.getRoot());				
				
				//int max = (int) Math.pow(2, nfeatures);
				int min = 2;
				int count = 1;
				while(min < (nfeatures+1)){
					count++;
					min = (int) Math.pow(2, count);
				}
				
				int y1 = (int) Math.pow(2,(60*nfeatures)/100);
				int y2 = (int) Math.pow(2,(70*nfeatures)/100);
				int y3 = (int) Math.pow(2,(80*nfeatures)/100);
				int y4 = (int) Math.pow(2, nfeatures);
				//System.out.println("fsm_"+nfeatures+"_"+k+" Confs " + c);
				
				HashMap<Ntree, Integer> this_fsm = new HashMap<Ntree, Integer>();
				this_fsm.put(tree, c);
				if(c <= y1){
					if(q1.size() <= sample){
						//System.out.println("Added to 1");
						q1.add(this_fsm);
					}					
				}else if(c <= y2){
					if(q2.size() <= sample){
						//System.out.println("Added to 2");
						q2.add(this_fsm);
					}
				}else if(c <= y3){
					if(q3.size() <= sample){
						//System.out.println("Added to 3");
						q3.add(this_fsm);
					}
				}else if(c <= y4){
					if(q4.size() <= sample){
						//System.out.println("Added to 4");
						q4.add(this_fsm);
					}
				}else{
					System.out.println("Error!!! Invalid number of configurations");
				}
				
				//got enough?
				if(q1.size() == sample && q2.size() == sample &&
					 q3.size() == sample && q4.size() == sample){
					break;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// equilize samples		
		int s1 = q1.size();
		int s2 = q2.size();
		int s3 = q3.size();
		int s4 = q4.size();
		int min = sample;
		if(s1 < min){
			min = s1;
		}
		if(s2 < min){
			min = s2;
		}
		if(s3 < min){
			min = s3;
		}
		if(s4 < min){
			min = s4;
		}
		while(q1.size() > min){
			q1.remove(0);
		}
		while(q2.size() > min){
			q2.remove(0);
		}
		while(q3.size() > min){
			q3.remove(0);
		}
		while(q4.size() > min){
			q4.remove(0);
		}
		min_sample = min;
		
		//put all samples into one bowl
		for(int k=0; k<min_sample; k++){
			//qall.add(q1.get(k));
			qall.add(q2.get(k));
			qall.add(q3.get(k));
			//qall.add(q4.get(k));
		}

		if(min_sample < sample){
			System.out.println("Sample for "+nfeatures + " features is less than expected! "+min_sample
					+" "+q1.size()+" "+q2.size()+" "+q3.size()+" "+q4.size());
		}
		
		//work on samples (gen. FM and FFSMs)
		conf_map = new HashMap<String, Integer>();
		for(int k=1; k<=(min_sample*2); k++){
			for(Ntree tr : qall.get(k-1).keySet()){
				int confs = qall.get(k-1).get(tr);
				gen_random_FM(tr.getRoot(), folder, k, nfeatures);
				
				//gen FFSM
				String path_ffsm = "./"+folder+"/ffsms/ffsm_"+nfeatures+"_"+k+".txt";
				gen_FFSM_tree(tr.getRoot(), path_ffsm);
				
				//gen maximal fsm for the ffsm
				FsmModel gen = new FsmModel();
				String fsmpath = "./"+folder+"/fsm/fsm_"+nfeatures+"_"+k+".txt";
			    try {
			      gen.gen_FSM_tree(tr.getRoot(), fsmpath);
			    } catch (IOException e) {
			      e.printStackTrace();
			      throw new RuntimeException("Problems with creating the fsm model files");
			    }
				conf_map.put(fsmpath, confs);
			}
		}
		return (min_sample*2);
		
	}
	
	public void gen_random_FFSM(String folder, int amount, int nfeatures) 
			throws IOException, InterruptedException{
		
		conf_map = new HashMap<String, Integer>();
		
		for(int k=1; k<=amount; k++){						
			FileHandler fh = new FileHandler();			
			//./fsm-gen-fsm num-out num-in num-states num-trans randseed
			// random inputs=1 to n-1 
			// out=2
			// transitions inputs*states
			
			Random ran = new Random();
			int inputs = ran.nextInt(nfeatures) + 1;
			int states = nfeatures;
			int transitions = inputs*states;			
			String[] commands = {"./increase_random/fsm-gen-fsm","2", ""+inputs, ""+states, ""+transitions, ""+k};
			String result = fh.getProcessOutput(commands);
			
			String path = "./"+folder+"/metafsm/fsm_"+nfeatures+"_"+k+".txt";			
			fh.print_file(result, path);
			
			// create meta tree for FM			
			File file = new File(path);
			FsmModelReader reader = new FsmModelReader(file, true);
			try {
				FiniteStateMachine fsm = reader.getFsm();
				ArrayList<Transition> ts = fsm.getInitialState().getOut();
				s_found = new ArrayList<State>();
				s_found.add(fsm.getInitialState());
				
				tree = new Ntree();				
				tree.setRoot(new Node(fsm.getInitialState(), "and"));
				current_node = tree.getRoot();
				count = 0;
				
				rec_tree(fsm.getInitialState(), tree.getRoot());				
				//tree.print();
				//calculate number of configurations		
				int c = calc_config(tree.getRoot());
				//System.out.println("fsm_"+nfeatures+"_"+k+" Confs " + c);				

				gen_random_FM(tree.getRoot(), folder, k, nfeatures);
							
				//gen FFSM
				String path_ffsm = "./"+folder+"/ffsms/ffsm_"+nfeatures+"_"+k+".txt";
				gen_FFSM_tree(tree.getRoot(), path_ffsm);
				
				//gen maximal fsm for the ffsm
				FsmModel gen = new FsmModel();
				String fsmpath = "./"+folder+"/fsm/fsm_"+nfeatures+"_"+k+".txt";
			    try {
			      gen.gen_FSM_tree(tree.getRoot(), fsmpath);
			    } catch (IOException e) {
			      e.printStackTrace();
			      throw new RuntimeException("Problems with creating the fsm model files");
			    }
				conf_map.put(fsmpath, c);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private int rec_find_conf(Node n, Node father){
		
		int sum = 1;
		if(n.getType().equals("and")){
			for(Node n1 : n.getChildren()){				
				sum = sum * rec_find_conf(n1, n);
			}
			if(father.getType().equals("alt")){
				return sum;
			}else{
				return sum+1;
			}
		}		
		if(n.getType().equals("or")){
			for(Node n1 : n.getChildren()){				
				sum = sum * rec_find_conf(n1, n);
			}
			if(father.getType().equals("alt")){
				return sum-1;
			}else{
				return sum;
			}
		}
		if(n.getType().equals("alt")){	
			for(Node n1 : n.getChildren()){
				sum = sum + rec_find_conf(n1, n);
			}
			if(father.getType().equals("alt")){
				return sum-1;
			}else{
				return sum;
			}			
		}		
		if(n.getType().equals("feature")){			
			if(father.getType().equals("feature")){
				if(n.getChildren().size() > 0){
					Node child = n.getChildren().get(0);
					return rec_find_conf(child, n)+1;
				}else return 2;
			}
			if(father.getType().equals("alt")){
				if(n.getChildren().size() > 0){
					Node child = n.getChildren().get(0);
					return rec_find_conf(child, n);
				}else return 1;				
			}if(father.getType().equals("and")){
				if(n.getChildren().size() > 0){
					Node child = n.getChildren().get(0);
					return rec_find_conf(child, n)+1;
				}else return 2;
			}
			if(father.getType().equals("or")){
				if(n.getChildren().size() > 0){
					Node child = n.getChildren().get(0);
					return rec_find_conf(child, n)+1;
				}else return 2;
			}
		}	
		return 0;
	}
	
	private int calc_config(Node n)
	{	
		int confs = 1;
		for(Node n1 : n.getChildren()){		
			int c = rec_find_conf(n1, n);
			confs = confs * c;
			//System.out.println(" root "+ n.getState().getLabel()+" child "
			//+ n1.getState().getLabel() + " confs "+c+" types "+n.getType()+" "+n1.getType());
		}
		return confs;
	}
	
	private void rec_genFFSM(Node n){		
		for(Node n1 : n.getChildren()){	
			String state1 = n.getState().getLabel();
			String state2 = n1.getState().getLabel();
			String input = state2;
			String output = state2;
			
			clause = clause.concat("s"+state1+"@f"+state1+" -- a"+input+"@f"+input+" / 0 -> s"+state2+"@f"+state2+"\n");
			clause = clause.concat("s"+state2+"@f"+state2+" -- a@f"+input+" / 1 -> s"+state1+"@f"+state1+"\n");	
			clause = clause.concat("s"+state2+"@f"+state2+" -- b@f"+input+" / o"+output+" -> s"+state2+"@f"+state2+"\n");
			rec_genFFSM(n1);			
		}
	}
	
	public void gen_FFSM_tree(Node root, String path_ffsm) throws IOException {
		
		String header = "s0@f0 -- a@f0 / 0 -> s0@f0\n";		
		clause = "";
		rec_genFFSM(root);
		
		String prop_aux = header.concat(clause);			
		
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, path_ffsm);
	}
	
	public void gen_random_FM(Node root, String folder, int k, int nfeatures) throws IOException {
		//System.out.println("*****************");
		//System.out.println("Testing Tree");
		
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
		
		header = header.concat("	<featureModel chosenLayoutAlgorithm=\"4\">\n");
		header = header.concat("		<struct>\n");
		header = header.concat("			<and mandatory=\"true\" name=\"Root [f0]\">\n");
		
		clause = "";
		rec_genFM("			", root);
		//System.out.println("*****************");
		String bottom = "			</and>\n";
		bottom = bottom.concat("		</struct>\n");
		bottom = bottom.concat("		<constraints/>\n");
		bottom = bottom.concat("		<calculations Auto=\"true\" Constraints=\"true\" Features=\"true\" Redundant=\"true\" Tautology=\"true\"/>\n");
		bottom = bottom.concat("		<comments/>\n");
		bottom = bottom.concat("		<featureOrder userDefined=\"false\"/>\n");
		bottom = bottom.concat("	</featureModel>\n");
		
		String prop_aux = header.concat(clause);
		prop_aux = prop_aux.concat(bottom);		

		String path = "./"+folder+"/feature_models/example_"+nfeatures+"_"+k+".xml";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, path);
	}
	
	private void rec_genFM(String level, Node n)
	{
		//System.out.println(level + n.getState().getLabel() + "("+arclabel+")");
		//Iterator<String> number = n.getLabels().iterator();
		for(Node n1 : n.getChildren())
		{
			//print(level + "----", n1, arclabels.next());
			if(n1.getChildren().size() <= 0){
				clause = clause.concat(level + "	"+"<"+n1.getType()+" name=\"Optional [f"+n1.getState().getLabel()+"]\"/>\n");
			}else{
				clause = clause.concat(level + "	"+"<"+n1.getType()+" name=\"Optional [f"+n1.getState().getLabel()+"]\">\n");
				rec_genFM(level + "	", n1);
				clause = clause.concat(level + "	"+"</"+n1.getType()+">\n");				
			}
		}
	}
	
	public void rec_tree(State state, Node c_node){
		ArrayList<Transition> ts = state.getOut();	
		ArrayList<Node> st_cicle = new ArrayList<Node>();		
		//current_node = c_node;
		
		for(Transition t : ts){
			if(!s_found.contains(t.getOut())){				
				s_found.add(t.getOut());
				Node node = new Node(t.getOut(), "feature");
				//current_node.addChild(node);
				st_cicle.add(node);
				tree.addNode(c_node, node);
			}
		}
		if(st_cicle.size() > 1 && !c_node.equals(tree.getRoot())){
			String[] type = {"and", "or", "alt"};
			Random ran = new Random();
			int x = ran.nextInt(3);
			c_node.setType(type[x]);					
		}
		for(Node n : st_cicle){			
			rec_tree(n.getState(), n);
		}
	}
	
}
