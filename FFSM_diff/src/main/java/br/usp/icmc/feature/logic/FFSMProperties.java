package br.usp.icmc.feature.logic;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.usp.icmc.ffsm.CommonPath;
import br.usp.icmc.ffsm.Cond_in_seq;
import br.usp.icmc.ffsm.FFSM;
import br.usp.icmc.ffsm.FState;
import br.usp.icmc.ffsm.FTransition;
import br.usp.icmc.ffsm.PairTable;
import br.usp.icmc.fsm.common.FileHandler;
import br.usp.icmc.fsm.common.TestSequence;
import br.usp.icmc.model_gen.FFSMModel;
import br.usp.icmc.reader.FFSMModelReader;
import br.usp.icmc.uml.CState;
import br.usp.icmc.uml.CTransition;
import br.usp.icmc.uml.HFFSM;

public class FFSMProperties 
{
	
	Map<FState,ArrayList<ArrayList<FTransition>>> path_map;
	Map<FTransition,ArrayList<ArrayList<FTransition>>> transition_map;
	Map<String,ArrayList<CommonPath>> seq_map;
	ArrayList<FTransition> no_loop_ft;
	ArrayList<FState> found_fc;
	ArrayList<FState> nfound_fc;
	ArrayList<FState> covered_fc;
	String folder;
	Map<String,PairTable> pre_table;
	Map<FState,ArrayList<Cond_in_seq>> hsi_table;
	Map<FTransition,ArrayList<Cond_in_seq>> trans_table;
	Map<FState,ArrayList<Cond_in_seq>> state_table;
	ArrayList<Cond_in_seq> transition_set;
	ArrayList<Cond_in_seq> state_set;
	ArrayList<Cond_in_seq> hsi_set;
	ArrayList<Cond_in_seq> w_set;
	ArrayList<Cond_in_seq> w_set_prefixes;
	HashMap<String, ArrayList<PairTable>> hsi_select_table;
	
	ArrayList<String> input_index_set;
	Map<String,ArrayList<Cond_in_seq>> alt_table;
	Map<String,ArrayList<Cond_in_seq>> nalt_table;
	
	boolean yak_mode = false;
	String project_path = "";
	String inner_path = "";
	
	static Document doc;
	
	String state_suite;
	String transition_suite;
	String full_suite;
	static String op_cond;
	
	FFSM ffsm;
	String prop;
	static String clause;
	static int features;
	static boolean optional;
	static ArrayList<String> all_ids;
	static ArrayList<String> all_full_ids;
	
	boolean islog, debug;
	static String log;
	
	public ArrayList<String> getFeatures(){
		return all_ids;
	}
	
	public ArrayList<Cond_in_seq> getStateSet(){
		return state_set;
	}
	public ArrayList<Cond_in_seq> getTransitionSet(){
		return transition_set;
	}
	public ArrayList<Cond_in_seq> getHSISet(){
		return hsi_set;
	}
	public Map<FState,ArrayList<ArrayList<FTransition>>> get_path_map(){
		return path_map;
	}
	
	public FFSMProperties(String folder, boolean islog, boolean debug){
		this.folder = folder;
		this.islog = islog;
		this.debug = debug;
		log = "";
		pre_table = new HashMap<String,PairTable>();
	}
	
	public String getStateSuite(){
		return state_suite;
	}
	public String getTransitionSuite(){
		return transition_suite;
	}
	public String getFullSuite(){
		return full_suite;
	}
	
	public ArrayList<String> removeNodesDerive(HFFSM hsm, String constraint){
		ArrayList<String> ids = new ArrayList<String>();
		try {
			for(CState cs : hsm.getStruct().getStateSet()){
				String cond = "(and "+cs.getFCondition()+" "+constraint+")";
				if(!check_condition(cond)){
					//System.out.println("STATE "+cs.getID()+" "+cond);
					if(!ids.contains(cs.getID())) ids.add(cs.getID());
				}
			}
			for(CTransition ct : hsm.getTransitions()){
				String cond = "(and "+ct.getFCondition()+" "+constraint+")";			
				if(!check_condition(cond)){					
					if(!ids.contains(ct.getID())){
						System.out.println("TR "+ct.getID()+" "+cond);
						ids.add(ct.getID());
					}
				}
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return ids;
	}
	
	public void genStateSuite() throws Exception{
		
		merge_cond_seqs(state_set, "true");
		remove_cond_prefix(state_set);
		ArrayList<Cond_in_seq> suite1 = printable_suite(state_set);
		if(debug)System.out.println("State Set");
		state_suite = "";		
		for(Cond_in_seq cin : suite1){
			if(debug)System.out.println(cin);
			state_suite = state_suite.concat(cin+"\n");
		}
	}
	
	public boolean is_valid_FFSM() throws IOException, InterruptedException{
		
		if(debug) System.out.println("DETERMINISTIC CHECK");
		boolean det = is_deterministic();		
		if(det){
			if(debug) System.out.println("INIT. CONNECTED CHECK");
			boolean ini = is_initially_connected();				
			if(ini){
				if(debug) System.out.println("MINIMAL CHECK");
				boolean min = is_minimal();				
				if(min){					
					return true;					
				}
			}
		}		
		return false;
	}
	
	public void genTransitionSuite() throws Exception{
		
		find_transition_cover_set();
				
		merge_cond_seqs(transition_set, "true");
		remove_cond_prefix(transition_set);
		transition_suite = "";		
		ArrayList<Cond_in_seq> suite2 = printable_suite(transition_set);
		if(debug)System.out.println("Transition Set");
		for(Cond_in_seq cin : suite2){
			if(debug)	System.out.println(cin);	
			transition_suite = transition_suite.concat(cin+"\n");
		}
	}
	
	public void derive_FSM(String folder, String name, String derivation_operator,
			boolean pop_unix_pdf) throws Exception{
		ArrayList<FTransition> transitions = ffsm.getFTransitions();
		String result_ffsm = derive_FSM_model(transitions, derivation_operator);
		
		FileHandler fh = new FileHandler();
		String path = folder+name+".txt";
		fh.print_file(result_ffsm, path);
			    	
    	String dotpath = folder+name+".dot";
    	String ffsm_path = folder+name+".txt";	    	
    	FFSMModel gen = new FFSMModel();
    	//gen.gen_dot_derived_FSM2(ffsm_path, dotpath, folder, name);
    	gen.gen_dot_derived_FSM(ffsm_path, dotpath, folder, name, pop_unix_pdf);
	}
	
	public void derive_FFSM(String folder, String name, String derivation_operator,
			boolean pop_unix_pdf) throws Exception{
		ArrayList<FTransition> transitions = ffsm.getFTransitions();
		String result_ffsm = derive_FFSM_model(transitions, derivation_operator);
		
		FileHandler fh = new FileHandler();
		String path = folder+name+".txt";
		fh.print_file(result_ffsm, path);
			    	
    	String dotpath = folder+name+".dot";
    	String ffsm_path = folder+name+".txt";	    	
    	FFSMModel gen = new FFSMModel();
    	//gen.gen_dot_derived_FSM2(ffsm_path, dotpath, folder, name);
    	gen.gen_dot_FFSM2(ffsm_path, dotpath, folder, name, pop_unix_pdf);
	}
	
	public boolean is_cond_prefix(Cond_in_seq c_prefix, Cond_in_seq c_seq) throws Exception{
		ArrayList<String> l1 = c_prefix.getSequence();
		String seq1 = "";
		for(String s : l1){
			seq1 = seq1.concat(s+",");
		}
		seq1 = seq1.substring(0, seq1.length()-1);
		
		ArrayList<String> l2 = c_seq.getSequence();
		String seq2 = "";
		for(String s : l2){
			seq2 = seq2.concat(s+",");
		}
		seq2 = seq2.substring(0, seq2.length()-1);
		
		if(TestSequence.isPrefixOf(seq1, seq2)){
			return check_cond_prefix(c_prefix.getCond(), c_seq.getCond());		
		}else return false;		
	}
	
	public boolean check_equiv_cond(String cond1, String cond2) throws Exception{
		if(check_cond_prefix(cond1,cond2) && check_cond_prefix(cond2,cond1)){
			return true;
		}
		return false;
	}
	
	public boolean check_equiv_cond(String header, String cond1, String cond2, 
			String project_path, String inner_path) throws Exception{
		
		this.project_path = project_path;
		this.inner_path = inner_path;
		this.prop = header;
		if(check_cond_prefix(cond1,cond2) && check_cond_prefix(cond2,cond1)){
			return true;
		}
		return false;		
	}
	
	public boolean check_cond_prefix(String cond_prefix, String cond_seq) throws Exception{
		String header = prop;		
		String clause = "";
		String cond1 = cond_prefix;
		String cond2 = cond_seq;
					
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert (and "+cond1+" "+cond2+"))\n");		
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n\n");
		
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+cond1+")\n");
		clause = clause.concat("(assert (and (not "+cond2+")))\n");		
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n\n");
		
		String prop_aux = header.concat(clause);				
		String[] outs = processZ3(prop_aux);
		
		if(outs.length < 2){
			return false;
		}
			
		if(outs[0].equals("sat") && outs[1].equals("unsat")){
			return true;
		}else return false;			
	}
	
	public boolean check_condition(String header, String condition, 
			String project_path, String inner_path) throws Exception{
		
		this.project_path = project_path;
		this.inner_path = inner_path;
		String clause = "";
								
		clause = clause.concat("(assert "+condition+")\n");		
		clause = clause.concat("(check-sat)\n");
		
		String prop_aux = header.concat(clause);
		String[] outs = processZ3(prop_aux);
		
		if(outs[0].equals("sat")){
			return true;
		}else return false;			
	}
	
	public boolean check_condition(String condition) throws Exception{
		String header = prop;		
		String clause = "";
						
		//clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+condition+")\n");		
		clause = clause.concat("(check-sat)\n");
		//clause = clause.concat("(pop)\n\n");	
		
		String prop_aux = header.concat(clause);
		String[] outs = processZ3(prop_aux);
		
		if(outs[0].equals("sat")){
			return true;
		}else return false;			
	}
	
	public String derive_suite(ArrayList<Cond_in_seq> suite, String constraint) throws Exception{
		ArrayList<String> result = new ArrayList<String>();
		for(Cond_in_seq test: suite){
			String cond = "(and ";
			cond = cond.concat(test.getCond()+" "+constraint+")");
			if(check_condition(cond)){
				String seq = "";
				for(String s : test.getSequence()){
					seq = seq.concat(s+",");
				}
				seq = seq.substring(0,seq.length()-1);
				result.add(seq);
			}
		}		
		//print result
		System.out.println("Derived Suite");
		result = TestSequence.getNoPrefixes(result);
		result = TestSequence.orderSet(result);
		String out = "";
		for(String seq : result){
			System.out.println(seq);
			out = out.concat(seq+"\n");
		}
		return out;
	}
	
	public String derive_FFSM_model(ArrayList<FTransition> transitions, String constraint) throws Exception{
		String result = "";
		for(FTransition t: transitions){
			String check = t.getSource().getCondition()+" "+t.getCInput().getCond()+" "
					+t.getTarget().getCondition();
			String cond = "(and "+check +" "+constraint+")";
			if(check_condition(cond)){
				result = result.concat(t+"\n");
			}
		}
		result = result.substring(0,result.length()-1);
		return result;		
	}
	
	public String derive_FSM_model(ArrayList<FTransition> transitions, String constraint) throws Exception{
		String result = "";
		for(FTransition t: transitions){
			String check = t.getSource().getCondition()+" "+t.getCInput().getCond()+" "
					+t.getTarget().getCondition();
			String cond = "(and "+check +" "+constraint+")";
			if(check_condition(cond)){
				result = result.concat(t.getSimple()+"\n");
			}
		}
		result = result.substring(0,result.length()-1);
		return result;		
	}
	
	public ArrayList<Cond_in_seq> printable_suite(ArrayList<Cond_in_seq> suite) throws IOException, InterruptedException{
		ArrayList<Cond_in_seq> result = new ArrayList<Cond_in_seq>();
		for(Cond_in_seq test: suite){
			String seq = "";
			for(String s : test.getSequence()){
				seq = seq.concat(s+",");
			}
			seq = seq.substring(0,seq.length()-1);
			ArrayList<String> u = new ArrayList<String>();
			u.add(seq);
			Cond_in_seq ci = new Cond_in_seq(u,test.getCond());
			result.add(ci);
		}
		result = TestSequence.orderConditionalSet(result);
		return result;
	}
	
	public void generate_W_set() throws Exception{		
		w_set = new ArrayList<Cond_in_seq>();
		w_set_prefixes = new ArrayList<Cond_in_seq>();
				
		for(String key : pre_table.keySet()){			
			ArrayList<String> prev_conds = new ArrayList<String>();
			//order seqs by condition size
			ArrayList<Cond_in_seq> order_list = new ArrayList<Cond_in_seq>();
			String smaller = "";			
			for(Cond_in_seq ci : pre_table.get(key).getMap().keySet()){
				String condition = ci.getCond();				
				if(condition.length() <= smaller.length()){
					smaller = condition;
					order_list.add(0, ci);
					continue;
				}
				order_list.add(ci);
				if(smaller.equals("")) smaller = condition;
			}
			//System.out.println(order_list);
			for(Cond_in_seq ci : order_list){
				String condition = ci.getCond();
				//partition coverage set				
				if(prev_conds.size() > 0){
					if(prev_conds.size() == 1){
						String neg_first = "(not "+prev_conds.get(0)+")";
						if(check_cond_prefix(neg_first, condition)){
							condition = neg_first;
						}
					}else{
						String acum_cond = "(not (and ";
						for(String pcond : prev_conds){
							acum_cond = acum_cond.concat(pcond+" ");
						}
						acum_cond = acum_cond.concat("))");
						if(check_cond_prefix(acum_cond, condition)){
							condition = acum_cond;
						}else{
							condition = "(and "+acum_cond+" "+condition+")";
						}
					}
				}
				prev_conds.add(condition);				
				if(condition.startsWith("(")){	
					//System.out.println("Cond:"+condition);
					//condition = reduce_condition(condition);
					ci.setCondition(condition);
					//System.out.println("Cond_after:"+condition);
				}
				boolean found = false;
				for(Cond_in_seq c : w_set){
					//System.out.println(c.toString() + " "+ ci.toString());
					if(c.getCond().equals(ci.getCond()) && c.getSequence().equals(ci.getSequence())){
						//System.out.println("FOUND");
						found = true;
						break;
					}
				}
				if(!found){
					//System.out.println("PAIR "+key+" "+ci);
					w_set.add(ci);							
				}
			}					
		}	
		w_set_prefixes.addAll(w_set);		
		if(debug) System.out.println("Constructing W set....");		
		remove_cond_prefix(w_set);
		merge_cond_seqs(w_set, "true");
		if(debug) System.out.println("Compact W set");
		/*print_w = "";
		for(Cond_in_seq s : w_set){
			if(debug) 	System.out.println(s);
			print_w = print_w.concat(s+"\n");
		}*/
	}
	
	public void generate_W_set_old() throws Exception{		
		w_set = new ArrayList<Cond_in_seq>();
		w_set_prefixes = new ArrayList<Cond_in_seq>();
		
		for(String key : pre_table.keySet()){
			//FState s1 = pre_table.get(key).getS1();
			//FState s2 = pre_table.get(key).getS2();
			for(Cond_in_seq ci : pre_table.get(key).getMap().keySet()){
				String condition = ci.getCond();
				if(condition.startsWith("(")){	
					//System.out.println("Cond:"+condition);
					//condition = reduce_condition(condition);
					ci.setCondition(condition);
					//System.out.println("Cond_after:"+condition);
				}
				boolean found = false;
				for(Cond_in_seq c : w_set){
					//System.out.println(c.toString() + " "+ ci.toString());
					if(c.getCond().equals(ci.getCond()) && c.getSequence().equals(ci.getSequence())){
						//System.out.println("FOUND");
						found = true;
						break;
					}
				}
				if(!found) w_set.add(ci);							
			}					
		}	
		w_set_prefixes.addAll(w_set);		
		System.out.println("Constructing W set....");		
		remove_cond_prefix(w_set);
		merge_cond_seqs(w_set, "true");
		System.out.println("Compact W set");
		for(Cond_in_seq s : w_set){
			System.out.println(s);
		}
	}
	
	public String list_to_string(ArrayList<String> list){
		String result = "";
		for(String s : list){
			result = result.concat(s+",");
		}
		result = result.substring(0,result.length()-1);
		return result;
	}
	
	public ArrayList<String> string_to_list(String str){
		ArrayList<String> list = new ArrayList<String>();
		String[] a = str.split(",");
		for(String s : a){
			list.add(s);
		}
		return list;
	}
	
	public void construct_HSI() throws Exception {
		//Hs = new HashMap<State, ArrayList<String>>();
		hsi_table = new HashMap<FState,ArrayList<Cond_in_seq>>();
		
		for(FState state : ffsm.getFStates()) {
			ArrayList<Cond_in_seq> hseqs = new ArrayList<Cond_in_seq>();
			
			//start with the W
			//System.out.println(" W PREFIXES");
			for(Cond_in_seq wseq : w_set_prefixes){
				hseqs.add(wseq);
				//System.out.println(wseq);
			}
	
			hsi_table.put(state, hseqs);
			//Hs.put(state, hseqs);			
		}
		alt_table = new HashMap<String,ArrayList<Cond_in_seq>>();
		nalt_table = new HashMap<String,ArrayList<Cond_in_seq>>();
		ArrayList<FState> l2 = (ArrayList<FState>) ffsm.getFStates().clone();
		for(FState si : ffsm.getFStates()) {
			l2.remove(si);
			for(FState sj : l2) {
				ArrayList<Cond_in_seq> dseqs = new ArrayList<Cond_in_seq>();
				ArrayList<Cond_in_seq> ndseqs = new ArrayList<Cond_in_seq>();
				String key1 = si+";"+sj;
				String key2 = sj+";"+si;
				if(hsi_select_table.get(key1) != null || hsi_select_table.get(key2) != null){
					alt_table.put(key1, dseqs);					
				}
				nalt_table.put(key1, ndseqs);
			}
		}		
		
		for(FState state : ffsm.getFStates()) {
			reduce_hsi2_simple(state);
		}
	}
	
	private void reduce_hsi2_simple(FState state) throws Exception {
		//greedy algorithm - try to remove all sequences and select the biggest.		
		int bigger_size = -1;
		FState bigger_state = null;
		int bigger_index = -1;
		
		ArrayList<Cond_in_seq> Hi = hsi_table.get(state);
		for(int i = Hi.size()-1; i >= 0 ; i--){
			Cond_in_seq seqq = Hi.remove(i);
			//System.out.println("REM "+seqq);
			String seq = list_to_string(seqq.getSequence());
			
			if(isSeparating2(state) && seq.length() > bigger_size) {
				bigger_size = seq.length();
				bigger_state = state;
				bigger_index = i;
				//System.out.println("TRUE");
			}
			
			Hi.add(i, seqq);
			if(bigger_size != -1) break;
		}
						
		if(bigger_size != -1) {
			//System.out.println("REMOVED2 "+ hsi_table.get(bigger_state).get(bigger_index)+ "//STATE "+bigger_state);
			//Hi.remove(bigger_index);
			hsi_table.get(bigger_state).remove(bigger_index);
			//hsi_table.get(state).remove(bigger_index);			
			reduce_hsi2_simple(state);
		}		
	}
	
	public boolean isSeparating2(FState state) throws Exception {
		//for each pair of states
		ArrayList<FState> states = (ArrayList<FState>) ffsm.getFStates().clone();				
		states.remove(state);
		FState si = state;
		next:for (FState sj : states) {	
			//check for 
			ArrayList<Cond_in_seq> Hi = hsi_table.get(si);
			ArrayList<Cond_in_seq> Hj = hsi_table.get(sj);
					
			//check for valid state pair
			String key1 = si+";"+sj;
			String key2 = sj+";"+si;
			//System.out.println("CHECK "+key1);
			String pair_cond = "(and "+si.getCondition()+" "+sj.getCondition()+")";
			try {
				if(!check_condition(pair_cond)){
					continue next;
				}
			} catch (IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//get all common prefixes
			ArrayList<Cond_in_seq> common_prefixes = new ArrayList<Cond_in_seq>();
			for(Cond_in_seq ci : Hi){
				for(Cond_in_seq cj : Hj){
					String seqi = list_to_string(ci.getSequence());
					String seqj = list_to_string(cj.getSequence());
					if(seqi.equals(seqj) && ci.getCond().equals(cj.getCond())){
						//if(!common_prefixes.contains(ci)){
						if(!is_inside(common_prefixes, ci)){
							common_prefixes.add(ci);
						}
					}
				}
			}
			if(common_prefixes.size() <= 0){
				//System.out.println("NO PREFIX "+common_prefixes);
				return false;
			}
			
			//check fast
			if(hsi_select_table.get(key1) != null && hsi_select_table.get(key1).size() > 0){	
				select:for(PairTable pt : hsi_select_table.get(key1)){
					ArrayList<Cond_in_seq> beta = pt.getCSequence();
					//System.out.println("C PREF "+common_prefixes);
					//System.out.println("BETA "+beta);
					for(Cond_in_seq ci : beta){
						if(!is_inside(common_prefixes, ci)){
							continue select;
						}
					}
					//System.out.println("KEY "+key1);
					continue next;
				}
			}
			if(hsi_select_table.get(key2) != null && hsi_select_table.get(key2).size() > 0){	
				select:for(PairTable pt : hsi_select_table.get(key2)){
					ArrayList<Cond_in_seq> beta = pt.getCSequence();				
					for(Cond_in_seq ci : beta){
						if(!is_inside(common_prefixes, ci)){
							continue select;
						}
					}
					//System.out.println("KEY "+key2);
					continue next;
				}
			}
			
			//System.out.println("SLOW "+key1);
			//slow check
			boolean found = false;
			for(int i=0; i<hsi_table.get(si).size(); i++){
				try {
					Cond_in_seq seq = hsi_table.get(si).get(i);				
					if(is_inside(common_prefixes, seq)){
						//System.out.println("INSIDE "+common_prefixes+" "+ seq);
						if(alt_table.get(key1) != null){
							ArrayList<Cond_in_seq> dseqs = alt_table.get(key1);
							ArrayList<Cond_in_seq> ndseqs = nalt_table.get(key1);
							//System.out.println("DSEQ "+dseqs+" "+ seq);
							if(ndseqs.size()>0 && is_inside(ndseqs, seq)){
								continue;
							}
							if(dseqs.size()>0 && is_inside(dseqs, seq)){
								//System.out.println("IS INSIDE "+common_prefixes+" "+ seq);
								found = true;
								break;
							}else{								
								if(check_disting_seq(si, sj, seq)){
									//System.out.println("ADD INSIDE "+common_prefixes+" "+ seq);
									dseqs.add(seq);
									alt_table.put(key1, dseqs);
									found = true;
									break;
								}else{
									ndseqs.add(seq);
									nalt_table.put(key1, ndseqs);
								}
							}
						}						
						if(alt_table.get(key2) != null){
							ArrayList<Cond_in_seq> dseqs = alt_table.get(key2);
							ArrayList<Cond_in_seq> ndseqs = nalt_table.get(key2);
							if(ndseqs.size()>0 && is_inside(ndseqs, seq)){
								continue;
							}
							if(dseqs.size()>0 && is_inside(dseqs, seq)){
								found = true;
								break;
							}else{								
								if(check_disting_seq(si, sj, seq)){
									dseqs.add(seq);
									alt_table.put(key2, dseqs);
									found = true;
									break;
								}else{
									ndseqs.add(seq);
									nalt_table.put(key2, ndseqs);
								}							
							}
						}
					}
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(!found){
				//System.out.println("NO "+key1);
				return false;
			}			
		}
		//System.out.println("TRUE");
		return true;
	} 
	
	public boolean check_disting_seq(FState fs1, FState fs2, Cond_in_seq cin)
			throws Exception{
							
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();	
		
		int count = 0;	
		String in = cin.getSequence().get(count);		
		for(FTransition co1 : current_out1){
			if(co1.getCInput().getIn().equals(in)){
				for(FTransition co2 : current_out2){
					if(co2.getCInput().getIn().equals(in)){
						CommonPath cnew = new CommonPath(fs1, fs2, ffsm.getNumberOfFStates());
						//still valid path
						String pair_cond = "(and "+fs1.getCondition()+" "+fs2.getCondition()+")";
						if(cnew.addCommon(co1, co2) && check_condition(pair_cond) && 
								check_common_seq_pair(fs1, fs2, cnew) && check_cond_prefix(pair_cond,cin.getCond())){
							String whole_cond = "(and "+fs1.getCondition()+" "+fs2.getCondition()+" ";
							for(FTransition t : cnew.get1()){
								whole_cond = whole_cond.concat(t.getCInput().getCond()+" "
										+t.getTarget().getCondition()+" ");
							}
							for(FTransition t : cnew.get2()){
								whole_cond = whole_cond.concat(t.getCInput().getCond()+" "
										+t.getTarget().getCondition()+" ");
							}
							whole_cond = whole_cond.concat(")");
							//is conditio						
							if(cnew.getDistinguish()){ 
								if (check_cond_prefix(pair_cond, whole_cond)) return true;
							}else if(rec_create_path(co1.getTarget(), co2.getTarget(), cin, cnew, count, pair_cond)){								
								return true;
							}
						}
					}
				}
			}			
		}		
		return false;
	}
	
	public boolean rec_create_path(FState fs1, FState fs2, Cond_in_seq cin, CommonPath cnew, int index, String pair_cond)
			throws Exception{
		
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();
				
		index++;
		if(index >= cin.getSequence().size()){
			return false;
		}
		String in = cin.getSequence().get(index);
		for(FTransition co1 : current_out1){
			if(co1.getCInput().getIn().equals(in)){
				for(FTransition co2 : current_out2){
					if(co2.getCInput().getIn().equals(in)){
						//still valid path
						if(cnew.addCommon(co1, co2) && check_common_seq_pair(fs1, fs2, cnew)){
							//check cond prefix of common path
							String whole_cond = "(and "+fs1.getCondition()+" "+fs2.getCondition()+" ";
							for(FTransition t : cnew.get1()){
								whole_cond = whole_cond.concat(t.getCInput().getCond()+" "
										+t.getTarget().getCondition()+" ");
							}
							for(FTransition t : cnew.get2()){
								whole_cond = whole_cond.concat(t.getCInput().getCond()+" "
										+t.getTarget().getCondition()+" ");
							}
							whole_cond = whole_cond.concat(")");							
							if(cnew.getDistinguish()){ 
								if (check_cond_prefix(pair_cond, whole_cond)) return true;
							}else if(rec_create_path(co1.getTarget(), co2.getTarget(), cin, cnew, index, pair_cond)){								
								return true;
							}
						}
					}
				}				
			}
		}
		return false;
	}
		
	private void reduce_hsi(FState state) {
		//greedy algorithm - try to remove all sequences and select the biggest.		
		int bigger_size = -1;
		//FState bigger_state = null;
		int bigger_index = -1;
		
		ArrayList<Cond_in_seq> Hi = hsi_table.get(state);
		
		for(int i = Hi.size()-1; i >= 0 ; i--){
			Cond_in_seq seqq = Hi.remove(i);
			String seq = list_to_string(seqq.getSequence());
			
			if(isSeparating(state) && seq.length() > bigger_size) {
				bigger_size = seq.length();
				//bigger_state = state;
				bigger_index = i;
				//System.out.println("FOUND larger");
			}
			
			Hi.add(i, seqq);
		}
			
		if(bigger_size != -1) {
			//ArrayList<Cond_in_seq> Hi = getHi(bigger_state);	
			//System.out.println("REMOVED "+ Hi.get(bigger_index)+ "//STATE "+bigger_state+ " //FROM "+Hi);
			hsi_table.get(state).remove(bigger_index);			
			reduce_hsi(state);
		}		
	}
	
	public boolean isSeparating(FState state) {
		//for each pair of states
		ArrayList<FState> states = (ArrayList<FState>) ffsm.getFStates().clone();				
		states.remove(state);
		FState si = state;
		for (FState sj : states) {					
			//check for valid state pair
			String key1 = si+";"+sj;
			String key2 = sj+";"+si;
			String key;
			boolean iskey1 = false;			
			if(hsi_select_table.get(key1) == null || hsi_select_table.get(key1).size() <= 0){				
				if(hsi_select_table.get(key2) == null || hsi_select_table.get(key2).size() <= 0){
					continue;
				}
			}else iskey1 = true;
			
			if(iskey1) key = key1;
			else key = key2;
			
			ArrayList<Cond_in_seq> Hi = hsi_table.get(si);
			ArrayList<Cond_in_seq> Hj = hsi_table.get(sj);
			
			//get all common prefixes
			ArrayList<Cond_in_seq> common_prefixes = new ArrayList<Cond_in_seq>();
			for(Cond_in_seq ci : Hi){
				for(Cond_in_seq cj : Hj){
					String seqi = list_to_string(ci.getSequence());
					String seqj = list_to_string(cj.getSequence());
					if(seqi.equals(seqj) && ci.getCond().equals(cj.getCond())){
						//if(!common_prefixes.contains(ci)){
						if(!is_inside(common_prefixes, ci)){
							common_prefixes.add(ci);
						}
					}
				}
			}
			if(common_prefixes.size() <= 0){
				return false;
			}				
			//boolean hasAlfas = false;
			//still distinguish?
			//System.out.println("COMMON "+key);
			//System.out.println(common_prefixes);
			boolean found_pair = false;			
			select:for(PairTable pt : hsi_select_table.get(key)){
				ArrayList<Cond_in_seq> beta = pt.getCSequence();
				//System.out.println("BETA "+beta);
				
				//if(common_prefixes.containsAll(beta)){
				for(Cond_in_seq ci : beta){
					if(!is_inside(common_prefixes, ci)){
						continue select;
					}
				}
				found_pair = true;
				break;
			}
			if(!found_pair){
				return false;					
			}	
		}
		//System.out.println("TRUE");
		return true;
	} 
	
	public boolean is_inside(ArrayList<Cond_in_seq> list, Cond_in_seq elem){
		boolean has_it = false;
		String seqi = list_to_string(elem.getSequence());
		for(Cond_in_seq cp : list){
			String a = list_to_string(cp.getSequence());
			if(a.equals(seqi) && cp.getCond().equals(elem.getCond())){
				has_it = true;
				break;
			}
		}
		return has_it;
	}
	
	public void generate_hsi_table() throws Exception{		
		//hsi_table = new HashMap<FState,ArrayList<Cond_in_seq>>();
		//for(FState s : ffsm.getFStates()){
		//	hsi_table.put(s, new ArrayList<Cond_in_seq>());
		//}
		System.out.println("Constructing HSI sets....");
		construct_HSI();
		
		
		/*
		//get all valid prefixes of W
		HashMap<Integer, ArrayList<String>> count_hsi_map = new HashMap<Integer, ArrayList<String>>();
		int size = hsi_select_table.keySet().size();
		for(int i=0; i <= size*size; i++){
			count_hsi_map.put(i, new ArrayList<String>());
		}		
		ArrayList<ArrayList<String>> pre_list = new ArrayList<ArrayList<String>>();
		for(Cond_in_seq seq : w_set_prefixes){
			ArrayList<String> sel = seq.getSequence();
			if(!pre_list.contains(sel)){
				pre_list.add(sel);
				ArrayList<String> list = count_hsi_map.get(0);
				list.add(list_to_string(sel));
				count_hsi_map.put(0, list);
			}
		}
		//print prefixes
		System.out.println("HSI prefixes");
		for(ArrayList<String> pre : pre_list){
			System.out.println(pre);
		}
		//calculate number of prefixes - greed option		
		ArrayList<String> all_pre = new ArrayList<String>();
		for(String key : hsi_select_table.keySet()){	
			//int weight = 1+ (w_set_prefixes.size() - hsi_select_table.get(key).size());
			//if(weight < 0) weight = 1;
			for(PairTable pt : hsi_select_table.get(key)){
				for(Cond_in_seq ins: pt.getCSequence()){
					String seq = list_to_string(ins.getSequence());
					//for(int w=1; w<=weight; w++){
						all_pre.add(seq);
					//}
				}
			}
		}		
		ArrayList<String> l = (ArrayList<String>) count_hsi_map.get(0).clone();
		for(String s : l){
			int count = 0;
			for(String seq : all_pre){
				if(seq.equals(s)){
					count++;
				}
			}
			ArrayList<String> list = count_hsi_map.get(count);
			list.add(s);
			count_hsi_map.put(count, list);
		}		
		System.out.println("Prefix count ...");
		//System.out.println(all_pre);
		all_pre.clear();
		for(int i : count_hsi_map.keySet()){
			if(i>0 && count_hsi_map.get(i).size() > 0){
				for(String s : count_hsi_map.get(i)){
					System.out.println("prefix "+s+ " size "+i);
					//size order
					all_pre.add(0,s);
				}
			}
		}
		
		//select and build the HSI table
		key:for(String key : hsi_select_table.keySet()){
			FState s1 = hsi_select_table.get(key).get(0).getS1();
			FState s2 = hsi_select_table.get(key).get(0).getS2();
			for(String order : all_pre){
				for(PairTable pt : hsi_select_table.get(key)){				
					//ArrayList<String> list = string_to_list(order);
					boolean contain = false;
					for(Cond_in_seq cin : pt.getCSequence()){	
						String seq = list_to_string(cin.getSequence());
						if(seq.equals(order)){
							contain = true;
							break;
						}						
					}
					if(contain){
						System.out.println("ADD "+key +" SELECTED "+pt.getCSequence()+ " ");
						for(Cond_in_seq ci : pt.getCSequence()){
							add_to_HSI_table(ci, s1, s2);
						}
						continue key;
					}
				}
			}
		}*/
			
		//print hsi table
		System.out.println("FULL HSI sets....");
		for(FState s : ffsm.getFStates()){
			System.out.println(s+" -> "+hsi_table.get(s));			
		}
		
		System.out.println("Removing conditional prefixes");
		//remove conditional prefixes
		for(FState s : ffsm.getFStates()){
			remove_cond_prefix(hsi_table.get(s));
		}
		
		//merge conditional sequences
		for(FState s : ffsm.getFStates()){			
			merge_cond_seqs(hsi_table.get(s), s.getCondition());
		}
		
		//print hsi table
		System.out.println("Compact HSI sets....");
		for(FState s : ffsm.getFStates()){
			System.out.println(s+" -> "+hsi_table.get(s));			
		}
	}
	
	public void generate_hsi_table_1() throws Exception{		
		hsi_table = new HashMap<FState,ArrayList<Cond_in_seq>>();
		for(FState s : ffsm.getFStates()){
			hsi_table.put(s, new ArrayList<Cond_in_seq>());
		}
		System.out.println("Constructing HSI sets....");
		
		//get all valid prefixes of W
		HashMap<Integer, ArrayList<String>> count_hsi_map = new HashMap<Integer, ArrayList<String>>();
		int size = hsi_select_table.keySet().size();
		for(int i=0; i <= size*size; i++){
			count_hsi_map.put(i, new ArrayList<String>());
		}		
		ArrayList<ArrayList<String>> pre_list = new ArrayList<ArrayList<String>>();
		for(Cond_in_seq seq : w_set_prefixes){
			ArrayList<String> sel = seq.getSequence();
			if(!pre_list.contains(sel)){
				pre_list.add(sel);
				ArrayList<String> list = count_hsi_map.get(0);
				list.add(list_to_string(sel));
				count_hsi_map.put(0, list);
			}
		}
		//print prefixes
		System.out.println("HSI prefixes");
		for(ArrayList<String> pre : pre_list){
			System.out.println(pre);
		}
		//calculate number of prefixes - greed option		
		ArrayList<String> all_pre = new ArrayList<String>();
		for(String key : hsi_select_table.keySet()){	
			//int weight = 1+ (w_set_prefixes.size() - hsi_select_table.get(key).size());
			//if(weight < 0) weight = 1;
			for(PairTable pt : hsi_select_table.get(key)){
				for(Cond_in_seq ins: pt.getCSequence()){
					String seq = list_to_string(ins.getSequence());
					//for(int w=1; w<=weight; w++){
						all_pre.add(seq);
					//}
				}
			}
		}		
		ArrayList<String> l = (ArrayList<String>) count_hsi_map.get(0).clone();
		for(String s : l){
			int count = 0;
			for(String seq : all_pre){
				if(seq.equals(s)){
					count++;
				}
			}
			ArrayList<String> list = count_hsi_map.get(count);
			list.add(s);
			count_hsi_map.put(count, list);
		}		
		System.out.println("Prefix count ...");
		//System.out.println(all_pre);
		all_pre.clear();
		for(int i : count_hsi_map.keySet()){
			if(i>0 && count_hsi_map.get(i).size() > 0){
				for(String s : count_hsi_map.get(i)){
					System.out.println("prefix "+s+ " size "+i);
					//size order
					all_pre.add(0,s);
				}
			}
		}
		
		//select and build the HSI table
		key:for(String key : hsi_select_table.keySet()){
			FState s1 = hsi_select_table.get(key).get(0).getS1();
			FState s2 = hsi_select_table.get(key).get(0).getS2();
			for(String order : all_pre){
				for(PairTable pt : hsi_select_table.get(key)){				
					//ArrayList<String> list = string_to_list(order);
					boolean contain = false;
					for(Cond_in_seq cin : pt.getCSequence()){	
						String seq = list_to_string(cin.getSequence());
						if(seq.equals(order)){
							contain = true;
							break;
						}						
					}
					if(contain){
						System.out.println("ADD "+key +" SELECTED "+pt.getCSequence()+ " ");
						for(Cond_in_seq ci : pt.getCSequence()){
							add_to_HSI_table(ci, s1, s2);
						}
						continue key;
					}
				}
			}
		}
			
		//print hsi table
		System.out.println("FULL HSI sets....");
		for(FState s : ffsm.getFStates()){
			System.out.println(s+" -> "+hsi_table.get(s));			
		}
		
		System.out.println("Removing conditional prefixes");
		//remove conditional prefixes
		for(FState s : ffsm.getFStates()){
			remove_cond_prefix(hsi_table.get(s));
		}
		
		//merge conditional sequences
		for(FState s : ffsm.getFStates()){			
			merge_cond_seqs(hsi_table.get(s), s.getCondition());
		}
		
		//print hsi table
		System.out.println("Compact HSI sets....");
		for(FState s : ffsm.getFStates()){
			System.out.println(s+" -> "+hsi_table.get(s));			
		}
	}
	
	public void add_to_HSI_table(Cond_in_seq ci, FState s1, FState s2){
		String condition = ci.getCond();
		if(condition.startsWith("(")){	
			//System.out.println("Cond:"+condition);
			//condition = reduce_condition(condition);
			ci.setCondition(condition);
			//System.out.println("Cond_after:"+condition);
		}
		boolean found = false;
		for(Cond_in_seq c : hsi_table.get(s1)){
			//System.out.println(c.toString() + " "+ ci.toString());
			if(c.getCond().equals(ci.getCond()) && c.getSequence().equals(ci.getSequence())){
				//System.out.println("FOUND 1 "+ c.toString() + " "+ ci.toString());
				found = true;
				break;
			}
		}
		if(!found){
			//System.out.println("ADD "+ci +" ON "+s1);
			hsi_table.get(s1).add(ci);
		}
		found = false;
		for(Cond_in_seq c : hsi_table.get(s2)){					
			if(c.getCond().equals(ci.getCond()) && c.getSequence().equals(ci.getSequence())){
				//System.out.println("FOUND 2 "+ c.toString() + " "+ ci.toString());
				found = true;
				break;
			}					
		}
		if(!found){
			//System.out.println("ADD "+ci +" ON "+s2);
			hsi_table.get(s2).add(ci);	
		}
	}
	
	public void generate_hsi_table_old() throws Exception{		
		hsi_table = new HashMap<FState,ArrayList<Cond_in_seq>>();
		for(FState s : ffsm.getFStates()){
			hsi_table.put(s, new ArrayList<Cond_in_seq>());
		}
		for(String key : pre_table.keySet()){
			FState s1 = pre_table.get(key).getS1();
			FState s2 = pre_table.get(key).getS2();
			for(Cond_in_seq ci : pre_table.get(key).getMap().keySet()){
				String condition = ci.getCond();
				if(condition.startsWith("(")){	
					//System.out.println("Cond:"+condition);
					//condition = reduce_condition(condition);
					ci.setCondition(condition);
					//System.out.println("Cond_after:"+condition);
				}
				boolean found = false;
				for(Cond_in_seq c : hsi_table.get(s1)){
					//System.out.println(c.toString() + " "+ ci.toString());
					if(c.getCond().equals(ci.getCond()) && c.getSequence().equals(ci.getSequence())){
						//System.out.println("FOUND");
						found = true;
						break;
					}
				}
				if(!found) hsi_table.get(s1).add(ci);
				found = false;
				for(Cond_in_seq c : hsi_table.get(s2)){					
					if(c.getCond().equals(ci.getCond()) && c.getSequence().equals(ci.getSequence())){
						found = true;
						break;
					}					
				}
				if(!found) hsi_table.get(s2).add(ci);
				/*
				if(!hsi_table.get(s1).contains(ci)){
					hsi_table.get(s1).add(ci);
				}
				if(!hsi_table.get(s2).contains(ci)){
					hsi_table.get(s2).add(ci);
				}*/
			}					
		}	
		System.out.println("Constructing HSI sets....");
		System.out.println("Removing conditional prefixes");
		//remove conditional prefixes
		for(FState s : ffsm.getFStates()){
			remove_cond_prefix(hsi_table.get(s));
		}
		
		//merge conditional sequences
		for(FState s : ffsm.getFStates()){			
			merge_cond_seqs(hsi_table.get(s), s.getCondition());
		}
		
		//print hsi table
		System.out.println("Compact HSI sets....");
		for(FState s : ffsm.getFStates()){
			System.out.println(s+" -> "+hsi_table.get(s));			
		}
	}
	
	public void remove_cond_prefix(ArrayList<Cond_in_seq> set) throws Exception{
		ArrayList<Cond_in_seq> lt1 = (ArrayList<Cond_in_seq>) set.clone();
		ArrayList<Cond_in_seq> lt2 = (ArrayList<Cond_in_seq>) set.clone();
		for(Cond_in_seq ci1 : lt1){
			lt2.remove(ci1);
			for(Cond_in_seq ci2 : lt2){
				boolean c1 = is_cond_prefix(ci1,ci2);
				boolean c2 = is_cond_prefix(ci2,ci1);
				if(c1 && c2){
					if(ci1.getCond().length() > ci2.getCond().length()){
						set.remove(ci1);						
					}else{
						set.remove(ci2);						
					}
					continue;
				}
				if(c1){
					//System.out.println("REMOVE "+ci1+" AND "+ci2);
					set.remove(ci1);
					continue;
				}
				if(c2){
					//System.out.println("REMOVE "+ci2+" AND "+ci1);
					set.remove(ci2);
				}
			}
		}
	}
	
	public void merge_cond_seqs(ArrayList<Cond_in_seq> set, String condition) throws Exception{
		ArrayList<Cond_in_seq> lt1 = (ArrayList<Cond_in_seq>) set.clone();
		ArrayList<Cond_in_seq> lt2 = (ArrayList<Cond_in_seq>) set.clone();
		Map<String, ArrayList<Cond_in_seq>> merge_map = new HashMap<String, ArrayList<Cond_in_seq>>();
		for(Cond_in_seq ci1 : lt1){
			ArrayList<String> l = ci1.getSequence();
			String seq = "";
			for(String st : l){
				seq = seq.concat(st+",");
			}
			seq = seq.substring(0, seq.length()-1);
							
			ArrayList<Cond_in_seq> equal = new ArrayList<Cond_in_seq>();
			equal.add(ci1);	
			lt2.remove(ci1);
			for(Cond_in_seq ci2 : lt2){					
				if(ci1.getSequence().equals(ci2.getSequence())){
					if(ci1.getCond().equals(ci2.getCond())){
						set.remove(ci2);
					}else{
						equal.add(ci2);						
					}
				}					
			}
			if(merge_map.get(seq) == null && equal.size() > 1){					
				merge_map.put(seq, equal);
				//System.out.println("FOUND "+ci1.getSequence()+ " FOR "+equal);
			}				
		}
		for(String key: merge_map.keySet()){
			if(merge_map.get(key).size() > 1){
				String m_cond = "(or ";	
				ArrayList<String> conds = new ArrayList<String>();
				for(Cond_in_seq c : merge_map.get(key)){
					if(!conds.contains(c.getCond())){						
						conds.add(c.getCond());						
						if(conds.size() > 1){
							String temp_cond = m_cond+")";
							//System.out.println("CHECK "+temp_cond+" FOR "+c.getCond());
							if(!check_cond_prefix(c.getCond(), temp_cond)){
								m_cond = m_cond.concat(c.getCond()+ " ");
							}
							if(check_cond_prefix(temp_cond, c.getCond())){
								conds.clear();
								conds.add(c.getCond());
								m_cond = "(or " + c.getCond()+ " ";
							}
						}else {
							m_cond = m_cond.concat(c.getCond()+ " ");
						}
					}									
				}
				if(conds.size() > 1){					
					m_cond = m_cond.substring(0, m_cond.length()-1);
					m_cond = m_cond.concat(")");
				}else if(conds.size() == 1) {
					m_cond = conds.get(0);
				}
				//System.out.println("MERGED "+key+ " FOR "+m_cond);
				
				set.removeAll(merge_map.get(key)); //remove redundant
				ArrayList<String> inseq = merge_map.get(key).get(0).getSequence();
				Cond_in_seq merged = new Cond_in_seq(inseq, m_cond);
				Cond_in_seq all = new Cond_in_seq(inseq, "true");
				Cond_in_seq all_s = new Cond_in_seq(inseq, condition);
				if(is_cond_prefix(merged,all) && is_cond_prefix(all,merged)){//equivalent
					set.add(all);
				}else if(is_cond_prefix(merged,all_s) && is_cond_prefix(all_s,merged)){//equivalent
					set.add(all_s);
				}else {
					set.add(merged);
				}
			}
		}
	}
	
	public void print_predecessor_table(){	
		for(String key : pre_table.keySet()){
			System.out.println(pre_table.get(key).toString());			
		}		
	}
	
	public FFSMProperties(String folder){
		this.folder = folder;
	}
	
	public String getlog(){
		return log;
	}
	
	public FFSM getFFSM(){
		return ffsm;
	}
	
	public void setFFSM(String ffsm_path, String prop, String project_path, String inner_path)
			throws IOException, InterruptedException{
		File file = new File(ffsm_path);
		FFSMModelReader reader = new FFSMModelReader(file);
		ffsm = reader.getFFSM();
		this.prop = prop;
		this.project_path = project_path;
		this.inner_path = inner_path;
		yak_mode = true;
	}
	
	public boolean checkFFSM_condition(String condition) throws IOException, InterruptedException{
		
		return yak_check_condition(condition, project_path, inner_path);	
	}
	
	public boolean checkFFSM_state_path_condition(FState state) throws Exception{
						
		return yak_check_conditional_state_path(state, project_path, inner_path);	
	}
	
	public ArrayList<FTransition> checkFFSM_transitions()
			throws IOException, InterruptedException{
		
		ArrayList<FTransition> list = yak_check_conditional_transitions(ffsm.getFTransitions(), project_path, inner_path);
		
		for(FTransition fs : list){
			System.out.println("Invalid conditional transition "+ fs);
			//return fs;
		}	
		return list;
		//return null;
	}
	
	public ArrayList<FState> checkFFSM_states() throws IOException, InterruptedException{
						
		ArrayList<FState> list = yak_check_conditional_state(ffsm.getFStates(), project_path, inner_path);
		
		for(FState fs : list){
			System.out.println("Invalid conditional state "+ fs );
			//return fs;
		}	
		return list;
		//return null;
	}
	
	public boolean checkFFSM_transitions_old() throws IOException, InterruptedException{
		
		FTransition t = check_transition(ffsm.getFTransitions());
		if(t != null){
			System.out.println("Invalid transition "+ t);
			return false;
		}		
		return true;
	}
	
	public boolean set_checkFFSM(String ffsm_path, String prop) throws IOException, InterruptedException{
		File file = new File(ffsm_path);
		FFSMModelReader reader = new FFSMModelReader(file);
		ffsm = reader.getFFSM();
		this.prop = prop;
				
		FState fs = check_conditional_state(ffsm.getFStates());
		if(fs != null){
			System.out.println("Invalid conditional state "+ fs + " on "+ ffsm_path);
			return false;
		}
		
		FTransition t = check_transition(ffsm.getFTransitions());
		if(t != null){
			System.out.println("Invalid transition "+ t + " on "+ ffsm_path);
			return false;
		}
		
		return true;
	}	
	
	public boolean yak_is_minimal(FState fs1, FState fs2){
		
		try	{	
			if(fs1 == null || fs2 == null) return true;
			if(fs1.getCondition() == null || fs2.getCondition() == null) return true;
			hsi_select_table = new HashMap<String, ArrayList<PairTable>>();			
			//if both states exist in the same product
			if(yak_check_state_pair(fs1,fs2)){				
				String key = fs1+";"+fs2;
				pre_table.put(key, new PairTable(fs1, fs2));
				boolean found = find_disting_seq(prop, fs1, fs2, ffsm.getNumberOfFStates());
				//if the first cycle could not find distinguish pairs - go recursive
				if(!found){
					String pair = key;
					Map<String,ArrayList<CommonPath>> s_map = pre_table.get(pair).getSeqMap();
					seq_map = s_map;
					pre_table.get(pair).resetMap();
					
					ina:for(String in : ffsm.getInputAlphabet()){
						if(!seq_map.keySet().contains(in) || seq_map.get(in) == null){
							continue ina;
						}				
						//is it worth checking recursively?
						ArrayList<String> incheck = new ArrayList<String>();
						for(String i : ffsm.getInputAlphabet()){
							ok:for(CommonPath h : seq_map.get(i)){
								if(h.getDistinguish()){
									incheck.add(i);
									break ok;
								}
							}
						}
						incheck.add(in);
						if(!check_disting_combinations_coverage(prop, fs1, fs2, seq_map, incheck)){
							continue ina;
						}
						
						ArrayList<CommonPath> caux = (ArrayList<CommonPath>) seq_map.get(in).clone();				
						for(CommonPath cp : caux){
							FState a1 = cp.get1().get(cp.get1().size()-1).getTarget();
							FState a2 = cp.get2().get(cp.get2().size()-1).getTarget();				
							//if both do not lead to the same state, and both are not self loops
							if(!a1.equals(a2) && !(fs1.equals(a1) && (fs2.equals(a2)))){
								if(!cp.getDistinguish()){
									seq_map.get(in).remove(cp);
								}
								ArrayList<String> seq = recursive_common_path2(prop, a1, a2, cp, in, fs1, fs2);
								if(seq != null){						
									return true;									
								}
							}				
						}
					}					
					pre_table.get(pair).resetMap();
					return false;
				}
			}
			
			/*
			find_disting_seq();
			for(String key : pre_table.keySet()){
				PairTable p = pre_table.get(key);
				if(p.getPaths().size() == 0){
					System.out.println("Could no find a d. seq. for "+key);
					return false;
				}
			}*/
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}		
		return true;
	}
	
	public String yak_is_deterministic(FState fst){
		//System.out.println("DET "+fst);
		try	{
			log = "";
			String clause = "";	
			ArrayList<String> inputs = new ArrayList<String>();
			if(fst == null){
				//System.out.println("NULL "+fst);
				return "";
			}
									
			ArrayList<FTransition> stx1 = (ArrayList<FTransition>) fst.getOut().clone();
			ArrayList<FTransition> stx2 = (ArrayList<FTransition>) fst.getOut().clone();				
			for(FTransition ft: stx1){					
				stx2.remove(ft);					
				for(FTransition ft2 : stx2){
					if(ft.getCInput().getIn().equals(ft2.getCInput().getIn())){
						inputs.add(ft.getCInput().getIn());
						//if(islog) log = log.concat("Checking transitions "+ft+" and "+ft2+"\n");
						//if(debug) System.out.println("Checking transitions "+ft+" and "+ft2);							
						clause = clause.concat("(push)\n");				
						clause = clause.concat("(assert (and \n");
						clause = clause.concat("    (and "+
								ft.getSource().getCondition()+" "+
								ft.getCInput().getCond()+" "+
								ft.getTarget().getCondition()+")\n");
						clause = clause.concat("    (and "+
								ft2.getSource().getCondition()+" "+
								ft2.getCInput().getCond()+" "+
								ft2.getTarget().getCondition()+")\n");
						clause = clause.concat("))\n");
						clause = clause.concat("(check-sat)\n");
						clause = clause.concat("(pop)\n");							
					}						
				}					
			}
			if(!clause.equals("")){
				String prop_aux = prop.concat(clause);
				String[] outs = processZ3(prop_aux);
							
				for(int i=0; i<outs.length; i++){
					if(i >= inputs.size()){
						//System.out.println("RETURN SIZE");
						//System.out.println(prop_aux);
						return "";
					}
					if(!outs[i].equals("unsat")){
						//System.out.println("FOUND");
						return inputs.get(i);
					}
				}
			}			
		}
		catch(Exception ex)		{
			ex.printStackTrace();
			//System.out.println("EXCEPTION");
			return "";
		}
		return "";
	}
	
	public boolean yak_check_state_pair(FState fs1, FState fs2) 
			throws IOException, InterruptedException{
				
		String header = prop;		
		String clause = "";
	
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");		
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n");	
				
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = project_path+inner_path+".smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		//System.out.println(prop_aux);
		
		String z3_path = inner_path.substring(0,inner_path.lastIndexOf("/")+1);
		String[] commands = {project_path+z3_path+"z3",project_path+inner_path+".smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
		if(result.length() > 0 && outs[0].equals("sat")){
			return true;
		}
		return false;
	}
	
	public boolean check_common_seq_pair(FState fs1, FState fs2, CommonPath cp)
			throws Exception{
			
		String clause = "";
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
		clause = clause.concat("(assert (and ");
		for(FTransition t : cp.get1()){
			clause = clause.concat(t.getCInput().getCond()+" "
					+t.getTarget().getCondition()+" ");
		}
		for(FTransition t : cp.get2()){
			clause = clause.concat(t.getCInput().getCond()+" "
					+t.getTarget().getCondition()+" ");
		}		
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n");
		
		String prop_aux = prop.concat(clause);				
		String[] outs = processZ3(prop_aux);		
		
		if(!outs[0].equals("sat")){
			return false;
		}	
		return true;
	}
	
	
	public boolean yak_check_condition(String condition, String project_path, String inner_path) 
			throws IOException, InterruptedException{
				
		String header = prop;		
		String clause = "";
				
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+condition+")\n");		
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n");
		
				
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = project_path+inner_path+".smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		//System.out.println(prop_aux);
		
		String z3_path = inner_path.substring(0,inner_path.lastIndexOf("/")+1);
		String[] commands = {project_path+z3_path+"z3",project_path+inner_path+".smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
		if(result.length() > 0 && outs[0].equals("sat")){
			return true;
		}
		return false;
	}
	
	public ArrayList<FTransition> yak_check_conditional_transitions(ArrayList<FTransition> l, 
			String project_path, String inner_path) 
			throws IOException, InterruptedException{
				
		String header = prop;		
		String clause = "";
		
		for(FTransition fs: l){
			clause = clause.concat("(push)\n");
			clause = clause.concat("(assert "+fs.getCInput().getCond()+")\n");		
			clause = clause.concat("(check-sat)\n");
			clause = clause.concat("(pop)\n");
		}
		
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = project_path+inner_path+".smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		//System.out.println(prop_aux);
		String z3_path = inner_path.substring(0,inner_path.lastIndexOf("/")+1);
		String[] commands = {project_path+z3_path+"z3",project_path+inner_path+".smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
		
		ArrayList<FTransition> list = new ArrayList<FTransition>();
		for(int i=0; i<outs.length; i++){
			if(!outs[i].equals("sat") && l.size() > 0 && result.length() > 0){
				list.add(l.get(i));
				//return l.get(i);
			}
		}
		return list;
		// Ok there is no invalid conditional state
		//return null;	
	}
	
	public ArrayList<FState> yak_check_conditional_state(ArrayList<FState> l, String project_path, String inner_path) 
			throws IOException, InterruptedException{
				
		String header = prop;		
		String clause = "";
		
		for(FState fs: l){
			clause = clause.concat("(push)\n");
			clause = clause.concat("(assert "+fs.getCondition()+")\n");		
			clause = clause.concat("(check-sat)\n");
			clause = clause.concat("(pop)\n");
		}
		
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = project_path+inner_path+".smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		//System.out.println(prop_aux);
		String z3_path = inner_path.substring(0,inner_path.lastIndexOf("/")+1);
		String[] commands = {project_path+z3_path+"z3",project_path+inner_path+".smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
		
		ArrayList<FState> list = new ArrayList<FState>();
		for(int i=0; i<l.size(); i++){
			if(!outs[i].equals("sat") && l.size() > 0 && result.length() > 0){
				list.add(l.get(i));
				//return l.get(i);
			}
		}
		return list;
		// Ok there is no invalid conditional state
		//return null;	
	}
	
	public boolean yak_check_conditional_state_path(FState s,
			String project_path, String inner_path) 
			throws Exception{
				
		String header = prop;		
		String clause = "";
		
		if(path_map == null || s == null || path_map.get(s) == null){			
			return false;
		}
				
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+s.getCondition()+")\n");
		clause = clause.concat("(assert (and \n");
		for(ArrayList<FTransition> path : path_map.get(s)){
			clause = clause.concat("    (not (and ");
			for(FTransition t : path){
				clause = clause.concat(t.getSource().getCondition()+" "
						+t.getCInput().getCond()+" ");
			}
			clause = clause.concat("))\n");
		}
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n");
		
		String prop_aux = header.concat(clause);				
		String[] outs = processZ3(prop_aux);
		
		if(!outs[0].equals("unsat")){
			return false;
		}
		return true;			
	}
	
	public boolean check_path_coverage(FState s, ArrayList<ArrayList<FTransition>> temp_map) throws Exception{
		
		
		String clause = "";		
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+s.getCondition()+")\n");
		clause = clause.concat("(assert (and \n");
		for(ArrayList<FTransition> path : temp_map){
			clause = clause.concat("    (not (and ");
			for(FTransition t : path){
				clause = clause.concat(t.getSource().getCondition()+" "
						+t.getCInput().getCond()+" ");
			}
			clause = clause.concat("))\n");
		}
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n");
	
	
		if(!clause.equals("")){			
			String prop_aux = prop.concat(clause);				
			String[] outs = processZ3(prop_aux);
			
			if(!outs[0].equals("unsat")){	
				return false;						
			}
		}
		return true;
	}
	
	public String[] processZ3(String clause) throws Exception{
		//if(yak_mode){			
			String fpath = project_path+inner_path+".smt2";
			FileHandler fh = new FileHandler();
			fh.print_file(clause, fpath);
			
			String z3_path = inner_path.substring(0,inner_path.lastIndexOf("/")+1);
			String[] commands = {project_path+z3_path+"z3",project_path+inner_path+".smt2"};
			String result = fh.getProcessOutput(commands);		
			String[] outs = result.split("\n");
			return outs;
		/*}else{			
			String fpath = "./"+folder+"/f_cds.smt2";
			FileHandler fh = new FileHandler();
			fh.print_file(clause, fpath);
			
			String[] commands = {"./ffsm/z3","./"+folder+"/f_cds.smt2"};
			String result = fh.getProcessOutput(commands);		
			String[] outs = result.split("\n");
			return outs;
		}*/
	}
	
	public ArrayList<FState> yak_check_product_coverage(String header) throws Exception{
		
		String clause = "";
		ArrayList<FState> invalid = new ArrayList<FState>();
		
		for(FState s: path_map.keySet()){
			if(path_map.get(s) != null){
				clause = clause.concat("(push)\n");
				clause = clause.concat("(assert "+s.getCondition()+")\n");
				clause = clause.concat("(assert (and \n");
				for(ArrayList<FTransition> path : path_map.get(s)){
					clause = clause.concat("    (not (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
				}
				clause = clause.concat("))\n");
				clause = clause.concat("(check-sat)\n");
				clause = clause.concat("(pop)\n");
			}			
		}
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);					
			String[] outs = processZ3(prop_aux);
			//System.out.println(prop_aux);
			//System.out.println(outs[0]);
			int count = 0;
			for(FState s: path_map.keySet()){
				if(path_map.get(s) != null){
					if(!outs[count].equals("unsat")){
						invalid.add(s);
						//return s;
						return invalid;
					}
					count++;
				}
			}				
		}
		return invalid;
	}
	
	public FState check_conditional_state(ArrayList<FState> l) 
			throws IOException, InterruptedException{
				
		String header = prop;		
		String clause = "";
		
		for(FState fs: l){
			clause = clause.concat("(push)\n");
			clause = clause.concat("(assert "+fs.getCondition()+")\n");		
			clause = clause.concat("(check-sat)\n");
			clause = clause.concat("(pop)\n");
		}
		
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = "./"+folder+"/f_cds.smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		//System.out.println(prop_aux);
		
		String[] commands = {"./ffsm/z3","./"+folder+"/f_cds.smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
		
		for(int i=0; i<outs.length; i++){
			if(outs[i].equals("unsat")){
				return l.get(i);
			}
		}
		// Ok there is no invalid conditional state
		return null;	
	}
		
	public FTransition check_transition(ArrayList<FTransition> l) 
			throws IOException, InterruptedException{
				
		String header = prop;
		String clause = "";
		
		for(FTransition ft: l){
			clause = clause.concat("(push)\n");
			clause = clause.concat("(assert (and "+ft.getSource().getCondition()
					+" "+ft.getCInput().getCond()+" "+ft.getTarget().getCondition()+"))\n");		
			clause = clause.concat("(check-sat)\n");
			clause = clause.concat("(pop)\n");
		}
		
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = "./"+folder+"/f_cts.smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
				
		String[] commands = {"./ffsm/z3","./"+folder+"/f_cts.smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
						
		for(int i=0; i<outs.length; i++){
			if(outs[i].equals("unsat")){
				return l.get(i);
			}
		}
		// Ok there is no invalid conditional state
		return null;	
	}
	
	public String update_feature_model(String featuremodel, HashMap<String, ArrayList<String>> map, String cond){
		
		File fXmlFile = new File(featuremodel);
		try {			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);		
			doc.getDocumentElement().normalize();
			
			Element elements = doc.getDocumentElement();				
			NodeList nodeList = elements.getChildNodes();
			
			read_XML_FeatureModel(featuremodel);
						
			updateNodes(nodeList, map, cond);
			
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			//StreamResult result = new StreamResult(new File(filepath));
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			
			return xmlString;
			
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return "";
	}
	
	private static void updateNodes(NodeList nodeList, HashMap<String, ArrayList<String>> map, String op) throws Exception {
		
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				//update comment
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if(node.getNodeName().equals("comments")){
						node.setTextContent(op);
					}
				}
				//update constraint
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if(node.getNodeName().equals("constraints")){						
						Element cond = doc.createElement("rule");						
						node.appendChild(cond);
						Element last = cond;
						//for true features
						int count = 0;
						for(String in : map.get("in")){	
							if(count >= map.get("in").size()-1){
								Element var = doc.createElement("var");
								int index = all_ids.indexOf(in);
								var.appendChild(doc.createTextNode(all_full_ids.get(index)));
								last.appendChild(var);
								break;
							}else{
								Element conj = doc.createElement("conj");
								last.appendChild(conj);
								Element var = doc.createElement("var");
								int index = all_ids.indexOf(in);
								var.appendChild(doc.createTextNode(all_full_ids.get(index)));
								conj.appendChild(var);
								last = conj;
							}
							count++;
						}
						//for false features
						count = 0;
						for(String in : map.get("out")){	
							if(count >= map.get("out").size()-1){
								Element not = doc.createElement("not");
								Element var = doc.createElement("var");
								int index = all_ids.indexOf(in);
								var.appendChild(doc.createTextNode(all_full_ids.get(index)));
								last.appendChild(not);
								not.appendChild(var);
								break;
							}else{
								Element conj = doc.createElement("conj");
								last.appendChild(conj);
								Element not = doc.createElement("not");
								Element var = doc.createElement("var");
								int index = all_ids.indexOf(in);
								var.appendChild(doc.createTextNode(all_full_ids.get(index)));
								conj.appendChild(not);
								not.appendChild(var);
								last = conj;
							}
							count++;
						}						
						//return;
					}					 
					updateNodes(node.getChildNodes(),map,op);
				}
			}
		}		
	}
	
	
	public String read_XML_FeatureModel(String featuremodel){
		try {
			File fXmlFile = new File(featuremodel);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
					
			doc.getDocumentElement().normalize();
			log = "";
	
			if(islog) log = log.concat("Root element :" + doc.getDocumentElement().getNodeName()+"\n");
			
			if (doc.hasChildNodes()) {	
				features = 0;
				clause = "";
				
				//Element elements = doc.getDocumentElement();				
				//NodeList nodeList = elements.getChildNodes();
				//Node root = getRootNode(nodeList);
				Node root = getRoot(doc.getChildNodes().item(0).getChildNodes()).item(0).getParentNode();
				//Node root = getRoot(doc.getChildNodes().item(0).getChildNodes()).item(0).getParentNode().getParentNode();
				//if(islog)printNote(root.getChildNodes());	
				
				all_ids = new ArrayList<String>();
				all_full_ids = new ArrayList<String>();
				
				createTree(root.getChildNodes(), "and", "root");
				//createTree(nodeList, "and", "root");
				
				clause = clause.concat("))\n\n");
				String body = clause;
				
				op_cond = "";
				Node uproot = getRoot(doc.getChildNodes().item(0).getChildNodes()).item(0).getParentNode().getParentNode();
				getCommentNode(uproot.getChildNodes());
				if(!op_cond.equals(""))body = body.concat("\n (assert "+op_cond+")");
						
				if(islog) log = log.concat("Features "+features+"\n");
				String aux_clause = "(define-sort Feature () Bool)\n";
				for(String id : all_ids){
					aux_clause = aux_clause.concat("(declare-const "+id+" Feature)\n");
				}				
				
				clause = aux_clause;
				clause = clause.concat(body);
									
				if(islog) log = log.concat(clause+"\n");
			}			
			
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return clause;
	}
	
	private void getCommentNode(NodeList nodeList) throws Exception {
		
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {	
					//Element eElement = (Element) node;			
					if(node.getNodeName().equals("comments")){
						op_cond = node.getTextContent();
						System.out.println(op_cond);
						return;
					}									 
					getCommentNode(node.getChildNodes());
				}
			}
		}		
	}
	
	public NodeList getRoot(NodeList nodeList){
		
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);	
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				if(tempNode.getNodeName().equals("struct")){					
					return tempNode.getChildNodes();
				}
			}			
		}		
		return null;
	}
	
	private static ArrayList<String> createTree(NodeList nodeList, String parent_type, String parent_id) {
		ArrayList<String> ids = new ArrayList<String>();
	    
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);			
			
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {	
				String id = "";
				if(tempNode.getNodeName().equals("feature") || tempNode.getNodeName().equals("and")
						|| tempNode.getNodeName().equals("alt") || tempNode.getNodeName().equals("or")){
					id = handleNode(tempNode);
					ids.add(id);
				}else continue;
				
				if((parent_type.equals("and") || parent_type.equals("feature")) && !parent_id.equals("root")){						
					if(optional){
						clause = clause.concat("   (=> "+id+" "+parent_id+")\n");
					}else{
						clause = clause.concat("   (= "+id+" "+parent_id+")\n");
					}
				}
				
				if(tempNode.getNodeName().equals("and")){					
					if(parent_id.equals("root")){
						clause = clause.concat("\n(assert "+id+")\n");
						clause = clause.concat("(assert (and\n");
					}
					createTree(tempNode.getChildNodes(), "and", id);
				}
				if(tempNode.getNodeName().equals("alt")){					
					
					ArrayList<String> child_ids = createTree(tempNode.getChildNodes(), "alt", id);
					ArrayList<String> child_ids2 = (ArrayList<String>) child_ids.clone();
					clause = clause.concat("   (= (or");
					for(String d1: child_ids){
						clause = clause.concat(" "+d1+" ");
					}
					clause = clause.concat(") "+id+")\n");
					for(String d1: child_ids){
						child_ids2.remove(d1);
						for(String d2: child_ids2){
							clause = clause.concat("   (not (and "+d1+" "+d2+"))\n");
						}
					}															
				}
				if(tempNode.getNodeName().equals("or")){
					ArrayList<String> child_ids = createTree(tempNode.getChildNodes(), "or", id);
					clause = clause.concat("   (= (or");
					for(String d1: child_ids){
						clause = clause.concat(" "+d1+" ");
					}					
					clause = clause.concat(") "+id+")\n");
				}
				if(tempNode.getNodeName().equals("feature")){
											
					createTree(tempNode.getChildNodes(), "feature", id);	
				}
							
			}	
		}
	    return ids;
	}
	
	private static ArrayList<String> createTree_old(NodeList nodeList, String parent_type, String parent_id) {
		ArrayList<String> ids = new ArrayList<String>();
	    for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {								
				if(tempNode.getNodeName().equals("and")){
					String id = handleNode(tempNode);
					ids.add(id);
					if(parent_id.equals("root")){
						clause = clause.concat("\n(assert "+id+")\n");
						clause = clause.concat("(assert (and\n");
					}					
					if(parent_id.equals("and") || parent_id.equals("feature")){
						if(optional){
							clause = clause.concat("   (=> "+id+" "+parent_id+")\n");
						}else{
							clause = clause.concat("   (= "+id+" "+parent_id+")\n");
						}						
					}
					if(parent_id.equals("alt") || parent_id.equals("or")){
						clause = clause.concat(" "+id+" ");
					}
					if(tempNode.getChildNodes() != null && tempNode.getChildNodes().getLength() > 0){
						createTree(tempNode.getChildNodes(), "and", id);
					}					
				}
				if(tempNode.getNodeName().equals("alt")){					
					String id = handleNode(tempNode);
					ids.add(id);
					if(parent_id.equals("and") || parent_id.equals("feature")){
						if(optional){
							clause = clause.concat("   (=> "+id+" "+parent_id+")\n");
						}else{
							clause = clause.concat("   (= "+id+" "+parent_id+")\n");
						}						
					}
					if(parent_id.equals("alt") || parent_id.equals("or")){
						clause = clause.concat(" "+id+" ");						
					}
					
					clause = clause.concat("   (= (or");
					ArrayList<String> child_ids = createTree(tempNode.getChildNodes(), "alt", id);
					ArrayList<String> child_ids2 = (ArrayList<String>) child_ids.clone();
					clause = clause.concat(") "+id+")\n");
					for(String d1: child_ids){
						child_ids2.remove(d1);
						for(String d2: child_ids2){
							clause = clause.concat("   (not (and "+d1+" "+d2+"))\n");
						}
					}
					
					if(tempNode.getChildNodes() != null && tempNode.getChildNodes().getLength() > 0){
						createTree(tempNode.getChildNodes(), "and", id);
					}
										
				}
				if(tempNode.getNodeName().equals("or")){					
					String id = handleNode(tempNode);
					ids.add(id);
					
					if(optional){
						clause = clause.concat("   (=> "+id+" "+parent_id+")\n");
					}else{
						clause = clause.concat("   (= "+id+" "+parent_id+")\n");
					}						
					
					clause = clause.concat("   (= (or");
					ArrayList<String> child_ids = createTree(tempNode.getChildNodes(), "or", id);					
					clause = clause.concat(") "+id+")\n");
				}
				if(tempNode.getNodeName().equals("feature")){
					String id = handleNode(tempNode);
					ids.add(id);				
					if(parent_id.equals("and") || parent_id.equals("feature")){
						if(optional){
							clause = clause.concat("   (=> "+id+" "+parent_id+")\n");
						}else{
							clause = clause.concat("   (= "+id+" "+parent_id+")\n");
						}
					}
					if(parent_id.equals("alt") || parent_id.equals("or")){
						clause = clause.concat(" "+id+" ");
					}										
					
					if(tempNode.getChildNodes() != null && tempNode.getChildNodes().getLength() > 0){
						createTree(tempNode.getChildNodes(), "feature", id);
					}				
				}
							
			}	
		}
	    return ids;
	}
	
	public static String handleNode(Node tempNode){
		features++;
		optional = true;
		NamedNodeMap nodeMap = tempNode.getAttributes();
		String feature_id = "";
		for (int i = 0; i < nodeMap.getLength(); i++) {	
			Node node = nodeMap.item(i);
			if(node.getNodeName().equals("name")){
				String aux = node.getNodeValue();
				all_full_ids.add(aux);
				aux.trim();
				aux = aux.replace(" ", "");
				aux.replaceAll("[^A-Za-z]+", "");
				if(aux.lastIndexOf("_") <= 0){
					feature_id = aux;
				}else{
					feature_id = aux.substring(aux.lastIndexOf("_")+1,aux.length());
					if(feature_id.length() <= 0){
						feature_id = aux;
					}
				}		
				//feature_id = aux.substring(aux.lastIndexOf("[")+1,aux.lastIndexOf("]"));						 
			}
			if(node.getNodeName().equals("mandatory")){
				if(node.getNodeValue().equals("true")){
					optional = false;
				}
			}
		}
		if(feature_id.equals("")) return "";
		all_ids.add(feature_id);
		return feature_id;
	}
	
	private static void printNote(NodeList nodeList) {

	    for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);	
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {				
				// get node name and value				
				log = log.concat("\nNode Name =" + tempNode.getNodeName() + " [OPEN]"+"\n");				
				if (tempNode.hasAttributes()) {	
					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();	
					for (int i = 0; i < nodeMap.getLength(); i++) {	
						Node node = nodeMap.item(i);							
						log = log.concat("attr name : " + node.getNodeName()+"\n");
						log = log.concat("attr value : " + node.getNodeValue()+"\n");
					}	
				}	
				if (tempNode.hasChildNodes()) {	
					// loop again if has child nodes
					printNote(tempNode.getChildNodes());	
				}					
				log = log.concat("Node Name =" + tempNode.getNodeName() + " [CLOSE]"+"\n");
			}	
		}
	}	
	
	public boolean is_deterministic(){
		
		boolean deterministic = true;
		try	{
			log = "";
			String clause = "";	
			
			deter:for(FState fst : ffsm.getFStates()){				
				ArrayList<FTransition> stx1 = (ArrayList<FTransition>) fst.getOut().clone();
				ArrayList<FTransition> stx2 = (ArrayList<FTransition>) fst.getOut().clone();				
				for(FTransition ft: stx1){					
					stx2.remove(ft);					
					for(FTransition ft2 : stx2){
						if(ft.getCInput().getIn().equals(ft2.getCInput().getIn())){
							if(islog) log = log.concat("Checking transitions "+ft+" and "+ft2+"\n");
							if(debug) System.out.println("Checking transitions "+ft+" and "+ft2);							
							clause = clause.concat("(push)\n");				
							clause = clause.concat("(assert (and \n");
							clause = clause.concat("    (and "+
									ft.getSource().getCondition()+" "+
									ft.getCInput().getCond()+" "+
									ft.getTarget().getCondition()+")\n");
							clause = clause.concat("    (and "+
									ft2.getSource().getCondition()+" "+
									ft2.getCInput().getCond()+" "+
									ft2.getTarget().getCondition()+")\n");
							clause = clause.concat("))\n");
							clause = clause.concat("(check-sat)\n");
							clause = clause.concat("(pop)\n");							
						}						
					}					
				}
			}
			if(!clause.equals("")){
				String prop_aux = prop.concat(clause);								
				String[] outs = processZ3(prop_aux);
							
				for(int i=0; i<outs.length; i++){
					if(!outs[i].equals("unsat")){
						return false;
					}
				}
			}			
		}
		catch(Exception ex)		{
			ex.printStackTrace();
			return false;
		}
		return deterministic;
	}
	
	public boolean is_deterministic_old2(){
		
		boolean deterministic = true;
		try	{						
			int count = 0;
			log = "";
			
			deter:for(FState fst : ffsm.getFStates()){				
				ArrayList<FTransition> stx1 = (ArrayList<FTransition>) fst.getOut().clone();
				ArrayList<FTransition> stx2 = (ArrayList<FTransition>) fst.getOut().clone();				
				for(FTransition ft: stx1){					
					stx2.remove(ft);					
					for(FTransition ft2 : stx2){
						if(ft.getCInput().getIn().equals(ft2.getCInput().getIn())){
							if(islog) log = log.concat("Checking transitions "+ft+" and "+ft2+"\n");
							if(debug) System.out.println("Checking transitions "+ft+" and "+ft2);
							count++;
							String clause = "";							
							clause = clause.concat("(assert (and \n");
							clause = clause.concat("    (and "+
									ft.getSource().getCondition()+" "+
									ft.getCInput().getCond()+" "+
									ft.getTarget().getCondition()+")\n");
							clause = clause.concat("    (and "+
									ft2.getSource().getCondition()+" "+
									ft2.getCInput().getCond()+" "+
									ft2.getTarget().getCondition()+")\n");
							clause = clause.concat("))\n");
							clause = clause.concat("(check-sat)");
							String prop_aux = prop.concat(clause);
							// print stm2 file and execute
							//String path = "./"+folder+"/f_deter_"+count+".smt2";
							String path = "./"+folder+"/f_deter.smt2";
							FileHandler fh = new FileHandler();
							fh.print_file(prop_aux, path);
							
							//String[] commands = {"./ffsm/z3","./"+folder+"/f_deter_"+count+".smt2"};
							String[] commands = {"./ffsm/z3","./"+folder+"/f_deter.smt2"};
							String result = fh.getProcessOutput(commands);							
							String[] outs = result.split("\n");
							
							if(outs[0].equals("sat")){
								deterministic = false;
								break deter;								
							}else{
								if(islog) log = log.concat("OK"+"\n");
								if(debug) System.out.println("OK");
							}
						}						
					}					
				}
			}					
		}
		catch(Exception ex)		{
			ex.printStackTrace();
			return false;
		}
		return deterministic;
	}
	
	public boolean is_complete(){
		
		boolean complete = true;
		try	{								
			log = "";			
			String clause = "";
			
			for(FState fst : ffsm.getFStates()){					
				if(islog) log = log.concat("Conditional State "+fst+"\n");
				for(String in: ffsm.getInputAlphabet()){					
					if(islog) log = log.concat("		"+in+"\n");					
					
					clause = clause.concat("(push)\n");
					clause = clause.concat("(assert "+fst.getCondition()+")\n");
					clause = clause.concat("(assert (and \n");
					boolean in_found = false;
					for(FTransition ft: fst.getOut()){							
						if(ft.getCInput().getIn().equals(in)){
							in_found = true;
							clause = clause.concat("    (not (and "+									
									ft.getCInput().getCond()+" "+
									ft.getTarget().getCondition()+"))\n");
						}						
					}
					if(in_found){
						clause = clause.concat("))\n");
						clause = clause.concat("(check-sat)\n");
						clause = clause.concat("(pop)\n");										
					}else{
						if(islog) log = log.concat("Not OK"+"\n");
						//System.out.println("NOT OK");
						return false;						
					}
				}
			}
			if(!clause.equals("")){
				String prop_aux = prop.concat(clause);
				// print stm2 file and execute
				//String path = "./"+folder+"/f_comp_"+count+".smt2";
				String path = "./"+folder+"/f_comp.smt2";
				FileHandler fh = new FileHandler();
				fh.print_file(prop_aux, path);
				
				//String[] commands = {"./ffsm/z3","./"+folder+"/f_comp_"+count+".smt2"};
				String[] commands = {"./ffsm/z3","./"+folder+"/f_comp.smt2"};
				String result = fh.getProcessOutput(commands);						
				String[] outs = result.split("\n");
				
				for(int i=0; i<outs.length; i++){
					if(outs[i].equals("sat")){
						return false;
					}
				}
			}
		}
		catch(Exception ex)	{
			ex.printStackTrace();
			return false;			
		}		
		return complete;
	}
	
	
	private void reduce_input_index_sequences(String index, FState s1, FState s2) 
			throws Exception {
		//greedy algorithm - try to remove all sequences and select the biggest.
				
		int bigger_index = -1;
				
		ArrayList<CommonPath> Si = seq_map.get(index);
		for(int i = Si.size()-1; i >= 0 ; i--){
			//remove one path
			CommonPath seq = Si.remove(i);
			// still ok
			if(check_disting_combinations_lv2(prop, s1, s2, seq_map, input_index_set) != null){				
				bigger_index = i;
			}
			//put it back
			Si.add(i, seq);
			if(bigger_index != -1) break;
		}		
		//remove largest
		if(bigger_index != -1) {
			//ArrayList<ArrayList<FTransition>> Si = transition_map.get(bigger_state);
			Si.remove(bigger_index);
			reduce_input_index_sequences(index, s1,s2);
		}		
	}
	
	public boolean is_complete_old2(){
		
		boolean complete = true;
		try	{								
			log = "";
			int count = 0;
			
			complete:for(FState fst : ffsm.getFStates()){					
				if(islog) log = log.concat("Conditional State "+fst+"\n");
				for(String in: ffsm.getInputAlphabet()){					
					if(islog) log = log.concat("		"+in+"\n");
					count++;
					String clause = "";
					clause = clause.concat("(assert "+fst.getCondition()+")\n");
					clause = clause.concat("(assert (and \n");
					boolean in_found = false;
					for(FTransition ft: fst.getOut()){							
						if(ft.getCInput().getIn().equals(in)){
							in_found = true;
							clause = clause.concat("    (not (and "+									
									ft.getCInput().getCond()+" "+
									ft.getTarget().getCondition()+"))\n");
						}						
					}
					if(in_found){
						clause = clause.concat("))\n");
						clause = clause.concat("(check-sat)");
						String prop_aux = prop.concat(clause);
						// print stm2 file and execute
						//String path = "./"+folder+"/f_comp_"+count+".smt2";
						String path = "./"+folder+"/f_comp.smt2";
						FileHandler fh = new FileHandler();
						fh.print_file(prop_aux, path);
						
						//String[] commands = {"./ffsm/z3","./"+folder+"/f_comp_"+count+".smt2"};
						String[] commands = {"./ffsm/z3","./"+folder+"/f_comp.smt2"};
						String result = fh.getProcessOutput(commands);						
						String[] outs = result.split("\n");
												
						if(outs[0].equals("sat")){
							complete = false; 
							break complete;							
						}					
					}else{
						if(islog) log = log.concat("Not OK"+"\n");
						complete = false;												 
						break complete;						
					}
				}
			}
		}
		catch(Exception ex)	{
			ex.printStackTrace();
			return false;			
		}		
		return complete;
	}
	
	private void reduce_common_distinguish(String header, FState fs1, FState fs2) throws Exception {
		//greedy algorithm - try to remove all sequences and select the biggest.
		
		int bigger_size = -1;
		int bigger_index = -1;
						
		ArrayList<String> Si = input_index_set;
		for(int i = 0; i < Si.size(); i++){
			//remove one path
			String seq = Si.remove(i);
			// still ok 				
			if(check_disting_combinations(header, fs1, fs2, seq_map, input_index_set, false) != null
					&& seq.length() >= bigger_size){
				bigger_size = seq.length();				
				bigger_index = i;
			}
			//put it back
			Si.add(i, seq);					
		}			
		
		//remove largest
		if(bigger_size != -1) {			
			Si.remove(bigger_index);
			reduce_common_distinguish(header, fs1, fs2);
		}		
	}
	
	private void reduce_transition_cover(FTransition t) throws Exception {
		//greedy algorithm - try to remove all sequences and select the biggest.
		
		int bigger_size = -1;
		//FTransition bigger_state = null;
		int bigger_index = -1;
		
		//for(FTransition state: transition_map.keySet()){			
			ArrayList<ArrayList<FTransition>> Si = transition_map.get(t);
			for(int i = Si.size()-1; i >= 0 ; i--){
				//remove one path
				ArrayList<FTransition> seq = Si.remove(i);
				// still ok 
				if(check_product_coverage_transition(prop) == null && seq.size() > bigger_size){
					bigger_size = seq.size();
					//bigger_state = state;
					bigger_index = i;
				}
				//put it back
				Si.add(i, seq);
				if(bigger_size != -1) break;
			}
		//}
		//remove largest
		if(bigger_size != -1) {
			//ArrayList<ArrayList<FTransition>> Si = transition_map.get(bigger_state);
			Si.remove(bigger_index);
			reduce_transition_cover(t);
		}		
	}
	
	private void reduce_state_cover(FState state) throws Exception {
		//greedy algorithm - try to remove all sequences and select the biggest.
		
		int bigger_size = -1;		
		int bigger_index = -1;		
	
		ArrayList<ArrayList<FTransition>> Si = path_map.get(state);
		for(int i = Si.size()-1; i >= 0 ; i--){
			//remove one path
			ArrayList<FTransition> seq = Si.remove(i);
			// still ok 
			if(check_product_coverage(prop) == null && seq.size() > bigger_size){
				bigger_size = seq.size();						
				bigger_index = i;
			}
			//put it back
			Si.add(i, seq);	
			if(bigger_size != -1) break;
		}	
		
		//remove largest
		if(bigger_size != -1) {			
			Si.remove(bigger_index);
			reduce_state_cover(state);
		}		
	}
	
	public FState check_product_coverage(String header) throws Exception{
		
		String clause = "";
		
		for(FState s: path_map.keySet()){
			if(path_map.get(s) != null){
				clause = clause.concat("(push)\n");
				clause = clause.concat("(assert "+s.getCondition()+")\n");
				clause = clause.concat("(assert (and \n");
				for(ArrayList<FTransition> path : path_map.get(s)){
					clause = clause.concat("    (not (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
				}
				clause = clause.concat("))\n");
				clause = clause.concat("(check-sat)\n");
				clause = clause.concat("(pop)\n");
			}			
		}
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);					
			String[] outs = processZ3(prop_aux);
			int count = 0;
			for(FState s: path_map.keySet()){
				if(path_map.get(s) != null){
					if(!outs[count].equals("unsat")){	
						return s;						
					}
					count++;
				}
			}
		}
		return null;
	}
	
	private void yak_reduce_state_cover(FState state) throws Exception {
		//greedy algorithm - try to remove all sequences and select the biggest.
		
		int bigger_size = -1;		
		int bigger_index = -1;		
	
		ArrayList<ArrayList<FTransition>> Si = path_map.get(state);
		for(int i = Si.size()-1; i >= 0 ; i--){
			//remove one path
			ArrayList<FTransition> seq = Si.remove(i);
			// still ok 
			if(yak_check_conditional_state_path(state, project_path, inner_path) && seq.size() > bigger_size){
				bigger_size = seq.size();						
				bigger_index = i;
			}
			//put it back
			Si.add(i, seq);	
			if(bigger_size != -1) break;
		}	
		
		//remove largest
		if(bigger_size != -1) {			
			Si.remove(bigger_index);
			reduce_state_cover(state);
		}		
	}
	
	public void create_state_cover_set(){
		//if(debug)System.out.println("GEN. STATE TABLE");				
		state_table = new HashMap<FState,ArrayList<Cond_in_seq>>();
		for(FState fs: path_map.keySet()){	
			if(!fs.equals(ffsm.getFInitialState())){
				ArrayList<Cond_in_seq> cin_list = new ArrayList<Cond_in_seq>(); 
				for(ArrayList<FTransition> path : path_map.get(fs)){
					ArrayList<String> inseq = new ArrayList<String>();
					
					//add condition variables
					ArrayList<String> vars = new ArrayList<String>();
					add_set_item(vars, path.get(0).getSource().getCondition());
					//String condition = "(and "+path.get(0).getSource().getCondition();
					for(FTransition t : path){
						inseq.add(t.getCInput().getIn());
						//condition = condition.concat(" "+t.getCInput().getCond()+" "
						//		+t.getTarget().getCondition());
						add_set_item(vars, t.getCInput().getCond());
						add_set_item(vars, t.getTarget().getCondition());
					}
					
					String condition = "(and ";
					for(String s : vars){
						condition = condition.concat(s+" ");
					}
					condition = condition.substring(0,condition.length()-1);				
					condition = condition.concat(")");		
					//condition = reduce_condition(condition);
					Cond_in_seq cin = new Cond_in_seq(inseq,condition);
					if(!state_set.contains(cin)){
						state_set.add(cin);
						//System.out.println("NEW TEST "+cin);
					}
					cin_list.add(cin);
				}
				state_table.put(fs, cin_list);
			}				
		}
	}
	
	public ArrayList<FState> yak_is_initially_connected(){
		
		ArrayList<FState> dead_list = new ArrayList<FState>();
		state_set = new ArrayList<Cond_in_seq>();
		covered_fc = new ArrayList<FState>();
		try	{	
			find_all_paths();	
			//print_paths(path_map);
			
			//remove invalid paths
			check_valid_paths(prop);
			print_paths(path_map);
			
			//check reachability of products			
			ArrayList<FState> states = yak_check_product_coverage(prop);			
			dead_list.addAll(states);
			if(dead_list.size() > 0){
				return dead_list;
			}
			
			//reduce redundant paths
			if(debug) System.out.println("REDUCE REDUNDANT PATHS");
			reduce_redundant_paths();
			print_paths(path_map);
			
			//reduce set of paths
			for(FState s: path_map.keySet()){
				if(!ffsm.getFInitialState().equals(s)){
					yak_reduce_state_cover(s);
				}
			}
			
			//generate table with conditional inputs
			//if(debug) System.out.println("CREATE STATE COVER SET");
			create_state_cover_set();
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();			
		}				
		return null;
	}
	
	public boolean is_initially_connected(){
		
		state_set = new ArrayList<Cond_in_seq>();
		covered_fc = new ArrayList<FState>();
		try	{			
			log = "";
			if(islog) log = log.concat("Conditional States "+ffsm.getFStates()+"\n");		
			//if(islog) log = log.concat("Transitions "+ffsm.getFTransitions()+"\n");		
			if(islog) log = log.concat("Conditional Inputs "+ffsm.getInputAlphabet()+"\n");		
			if(islog) log = log.concat("Outputs "+ffsm.getOutputAlphabet()+"\n");
						
			//find valid paths
			if(debug)System.out.println("FIND PATHS");
			find_all_paths();			
			//if(islog) print_paths(path_map);
			if(islog) log = log.concat("\n\nConditional States "+ffsm.getFStates()+"\n\n");
			
			//System.out.println(" CHECK PATHS");
			//check valid paths
			for(FState s : ffsm.getFStates()){
				if(!ffsm.getFInitialState().equals(s)){
					if(path_map.get(s) != null){
						if(path_map.get(s).size() <= 0){
							return false; //there is no path for this c. state 
						}
					}else return false; //there is no path for this c. state 
				}
			}
			
			//remove invalid paths
			boolean epath = check_valid_paths(prop);			
			if(islog) log = log.concat("\nRemoving invalid paths\n"+"\n");	
			//if(islog) print_paths(path_map);
			// if a state has no path that reach it
			if(!epath){
				return false;
			}
			
			//check reachability of products
			if(debug) System.out.println("CHECK REACH");	
			FState state = check_product_coverage(prop);
			if(state != null){				
				if(debug) System.out.println("Conditional state "+state +" cannot be reached by all products"+"\n");
				return false;
			}
			
			//reduce redundant paths
			if(debug) System.out.println("REDUCE REDUNDANT PATHS");
			reduce_redundant_paths();
			
			
			//reduce set of paths
			if(debug) System.out.println("REDUCE PROCEDURE");
			for(FState s: path_map.keySet()){
				if(!ffsm.getFInitialState().equals(s)){
					reduce_state_cover(s);
				}
			}	
			
			//check reachability of products 2
			if(debug) System.out.println("CHECK REACH 2");	
			state = check_product_coverage(prop);
			if(state != null){				
				if(debug) System.out.println("ERROR ON REDUCTION ON STATE "+state+"\n");
				return false;
			}
						
			//generate table with conditional inputs
			if(debug) System.out.println("CREATE STATE COVER SET");
			create_state_cover_set();
			
			if(debug)System.out.println("IS INIT CON.");				
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}				
		return true;
	}
	
	public boolean is_initially_connected_old(){
	
		state_set = new ArrayList<Cond_in_seq>();
		covered_fc = new ArrayList<FState>();
		try	{			
			log = "";
			if(islog) log = log.concat("Conditional States "+ffsm.getFStates()+"\n");		
			//if(islog) log = log.concat("Transitions "+ffsm.getFTransitions()+"\n");		
			if(islog) log = log.concat("Conditional Inputs "+ffsm.getInputAlphabet()+"\n");		
			if(islog) log = log.concat("Outputs "+ffsm.getOutputAlphabet()+"\n");
						
			//find valid paths
			if(debug)System.out.println("FIND PATHS");
			find_all_paths();			
			if(islog) print_paths(path_map);
			if(islog) log = log.concat("\n\nConditional States "+ffsm.getFStates()+"\n\n");
			
			//System.out.println(" CHECK PATHS");
			//check valid paths
			for(FState s : ffsm.getFStates()){
				if(!ffsm.getFInitialState().equals(s)){
					if(path_map.get(s) != null){
						if(path_map.get(s).size() <= 0){
							return false; //there is no path for this c. state 
						}
					}else return false; //there is no path for this c. state 
				}
			}
			
			//remove invalid paths
			boolean epath = check_valid_paths(prop);			
			if(islog) log = log.concat("\nRemoving invalid paths\n"+"\n");	
			if(islog) print_paths(path_map);
			// if a state has no path that reach it
			if(!epath){
				return false;
			}
			
			//check reachability of products			
			FState state = check_product_coverage(prop);
			if(state != null){				
				if(debug) System.out.println("Conditional state "+state +" cannot be reached by all products"+"\n");
				return false;
			}
			
			//reduce redundant paths
			if(debug) System.out.println("REDUCE REDUNDANT PATHS");
			reduce_redundant_paths();
			
			//reduce set of paths
			for(FState s: path_map.keySet()){
				if(!ffsm.getFInitialState().equals(s)){
					reduce_state_cover(s);
				}
			}
			
			//generate table with conditional inputs
			create_state_cover_set();
			
			if(debug)System.out.println("IS INIT CON.");				
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}				
		return true;
	}
	
	public void reduce_redundant_paths() throws Exception{
		new_state:for(FState s: path_map.keySet()){
			if(!ffsm.getFInitialState().equals(s)){
				ArrayList<ArrayList<FTransition>> checked_paths = new ArrayList<ArrayList<FTransition>>();
				ArrayList<ArrayList<FTransition>> valid_paths = new ArrayList<ArrayList<FTransition>>();
				ArrayList<String> checked_cond = new ArrayList<String>();
				ArrayList<ArrayList<FTransition>> original_paths = (ArrayList<ArrayList<FTransition>>) path_map.get(s).clone();
				new_path:for(ArrayList<FTransition> path : original_paths){
					String path_cond = "(and ";
					for(FTransition ft: path){
						String ft_cond = ft.getSource().getCondition()+" "+
								ft.getCInput().getCond()+" "+ft.getTarget().getCondition();
						path_cond = path_cond.concat(ft_cond+" ");						
					}
					path_cond = path_cond.substring(0,path_cond.length()-1);
					path_cond = path_cond.concat(")");
					
					//check if this path has a cond prefix of another path
					int i=0;
					for(String ccond : checked_cond){
						if(check_cond_prefix(path_cond, ccond)){
							path_map.get(s).remove(path);
							continue new_path;
						}
						if(check_cond_prefix(ccond, path_cond)){
							path_map.get(s).remove(checked_paths.get(i));
							valid_paths.remove(checked_paths.get(i));
						}						
						i++;
					}					
					checked_cond.add(path_cond);
					checked_paths.add(path);
					valid_paths.add(path);
					if(check_path_coverage(s, valid_paths)){
						// remove the rest
						path_map.get(s).clear();
						path_map.get(s).addAll(valid_paths);						
						continue new_state;
					}
				}			
			}
		}
	}

	public void reduce_redundant_paths_old() throws Exception{
		new_state:for(FState s: path_map.keySet()){
			if(!ffsm.getFInitialState().equals(s)){
				ArrayList<ArrayList<FTransition>> checked_paths = new ArrayList<ArrayList<FTransition>>();
				ArrayList<String> checked_cond = new ArrayList<String>();
				ArrayList<ArrayList<FTransition>> original_paths = (ArrayList<ArrayList<FTransition>>) path_map.get(s).clone();
				new_path:for(ArrayList<FTransition> path : original_paths){
					String path_cond = "(and ";
					for(FTransition ft: path){
						String ft_cond = ft.getSource().getCondition()+" "+
								ft.getCInput().getCond()+" "+ft.getTarget().getCondition();
						path_cond = path_cond.concat(ft_cond+" ");
						
					}
					path_cond = path_cond.substring(0,path_cond.length()-1);
					path_cond = path_cond.concat(")");
					
					//check if this path has a cond prefix of another path
					int i=0;
					for(String ccond : checked_cond){
						if(check_cond_prefix(path_cond, ccond)){
							path_map.get(s).remove(checked_paths.get(i));
							continue new_path;
						}
						i++;
					}					
					checked_cond.add(path_cond);
					checked_paths.add(path);					
					if(check_path_coverage(s, checked_paths)){
						// remove the rest
						path_map.get(s).clear();
						path_map.get(s).addAll(checked_paths);						
						continue new_state;
					}
				}			
			}
		}
	}
	
	public void gen_state_tree(String folder, String dot_name, boolean pop_dot) 
			throws IOException, InterruptedException{		
		
		FFSMModel gen = new FFSMModel();				
		//generate dot
		gen.gen_dot_state_tree(ffsm, path_map, folder, dot_name, pop_dot);
		
	}
	
	public void gen_transition_tree(String folder, String dot_name, boolean pop_dot) 
			throws IOException, InterruptedException{		
		
		FFSMModel gen = new FFSMModel();				
		//generate dot
		gen.gen_dot_transition_tree(ffsm, transition_map, folder, dot_name, pop_dot);
		
	}
	
	public boolean is_initially_connected_old2(){
		
		boolean init_con = true;
		try	{			
			log = "";
			if(islog) log = log.concat("Conditional States "+ffsm.getFStates()+"\n");		
			//if(islog) log = log.concat("Transitions "+ffsm.getFTransitions()+"\n");		
			if(islog) log = log.concat("Conditional Inputs "+ffsm.getInputAlphabet()+"\n");		
			if(islog) log = log.concat("Outputs "+ffsm.getOutputAlphabet()+"\n");
						
			//find paths
			find_all_paths();
			if(islog) print_paths(path_map);
			if(islog) log = log.concat("\n\nConditional States "+ffsm.getFStates()+"\n\n");
			
			//check valid paths
			boolean epath = check_valid_paths_old(prop);			
			if(islog) log = log.concat("\nRemoving invalid paths\n"+"\n");	
			if(islog) print_paths(path_map);			
			if(!epath){
				return false;
			}
			
			//check reachability of products
			boolean cpath = check_product_coverage_old(prop);
			if(!cpath){
				return false;
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}				
		return init_con;
	}
	
	public boolean is_minimal_old2(){
		
		boolean minimal = true;
		try	{									
			log = "";
			if(islog) log = log.concat("Conditional States "+ffsm.getFStates()+"\n");		
			//if(islog) log = log.concat("Transitions "+ffsm.getFTransitions()+"\n");		
			if(islog) log = log.concat("Conditional Inputs "+ffsm.getInputAlphabet()+"\n");		
			if(islog) log = log.concat("Outputs "+ffsm.getOutputAlphabet()+"\n");
			
			//String[] outs = check_state_pairs();
			
			int count = 0;
			ArrayList<FState> fs_aux = (ArrayList<FState>) ffsm.getFStates().clone();
			for(FState fs1 : ffsm.getFStates()){
				fs_aux.remove(fs1);
				for(FState fs2 : fs_aux){
					//if(outs[count].equals("sat")){
					if(check_state_pair_old(prop, fs1, fs2)){
						boolean stilltrue = find_and_check_disting_seq_old(prop, fs1, fs2, ffsm.getNumberOfFStates());
						if(!stilltrue){
							return false;
						}
					}
					count++;
				}					
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}		
		return minimal;
	}
	public boolean is_minimal_old3(){
		
		boolean minimal = true;
		try	{									
			log = "";
			if(islog) log = log.concat("Conditional States "+ffsm.getFStates()+"\n");		
			//if(islog) log = log.concat("Transitions "+ffsm.getFTransitions()+"\n");		
			if(islog) log = log.concat("Conditional Inputs "+ffsm.getInputAlphabet()+"\n");		
			if(islog) log = log.concat("Outputs "+ffsm.getOutputAlphabet()+"\n");
			
			String[] outs = check_state_pairs();
			
			int count = 0;
			ArrayList<FState> fs_aux = (ArrayList<FState>) ffsm.getFStates().clone();
			for(FState fs1 : ffsm.getFStates()){
				fs_aux.remove(fs1);
				for(FState fs2 : fs_aux){
					if(outs[count].equals("sat")){
						boolean stilltrue = find_and_check_disting_seq(prop, fs1, fs2, ffsm.getNumberOfFStates());
						if(!stilltrue){
							return false;
						}
					}
					count++;
				}					
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}		
		return minimal;
	}
	
	public boolean is_minimal(){
		
		boolean minimal = false;
		try	{									
			log = "";
			if(islog) log = log.concat("Conditional States "+ffsm.getFStates()+"\n");		
			//if(islog) log = log.concat("Transitions "+ffsm.getFTransitions()+"\n");		
			if(islog) log = log.concat("Conditional Inputs "+ffsm.getInputAlphabet()+"\n");		
			if(islog) log = log.concat("Outputs "+ffsm.getOutputAlphabet()+"\n");
			
			find_disting_seq();
			for(String key : pre_table.keySet()){
				PairTable p = pre_table.get(key);
				if(p.getPaths().size() == 0){
					System.out.println("Could no find a d. seq. for "+key);
					return false;
				}
			}
			minimal = true;
			//System.out.println("The machine is minimal!");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}		
		return minimal;
	}
	
	public boolean check_transition_coverage(String header) throws Exception{
				
		String clause = "";		
		for(FTransition ft: transition_map.keySet()){	
			String ft_cond = ft.getSource().getCondition()+" "+
					ft.getCInput().getCond()+" "+ft.getTarget().getCondition();
			if(transition_map.get(ft) != null){
				clause = clause.concat("(push)\n");
				clause = clause.concat("(assert (and "+ft_cond+"))\n");
				clause = clause.concat("(assert (and \n");
				for(ArrayList<FTransition> path : transition_map.get(ft)){		
					clause = clause.concat("    (not (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
				}
				clause = clause.concat("))\n");
				clause = clause.concat("(check-sat)\n");
				clause = clause.concat("(pop)\n");
			}			
		}
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);
			String[] outs = processZ3(prop_aux);
			int count = 0;
			for(FTransition ft: transition_map.keySet()){
				if(transition_map.get(ft) != null){
					if(count >= outs.length){
						break;
					}
					if(outs[count].equals("sat")){					
						if(islog) log = log.concat("Conditional transition "+ft +" cannot be reached by all products"+"\n");
						System.out.println("Conditional transition "+ft +" cannot be reached by all products"+"\n");
						return false;						
					}else{					
						if(islog) log = log.concat("Conditional transition "+ft +" OK"+"\n");
					}
					count++;
				}
			}
		}
		return true;
	}
	
	public boolean check_valid_transition_paths(String header) throws Exception{
		
		String clause = "";
		
		for(FTransition ft: transition_map.keySet()){	
			String ft_cond = ft.getSource().getCondition()+" "+
					ft.getCInput().getCond()+" "+ft.getTarget().getCondition();
			if(transition_map.get(ft) != null){				
				for(ArrayList<FTransition> path : transition_map.get(ft)){					
					clause = clause.concat("(push)\n");
					clause = clause.concat("(assert (and "+ft_cond+"))\n");
					clause = clause.concat("(assert (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
					clause = clause.concat("(check-sat)\n");
					clause = clause.concat("(pop)\n");					
				}				
			}			
		}
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);
			String[] outs = processZ3(prop_aux);
			int count = 0;
			
			for(FTransition ft: transition_map.keySet()){					
				if(transition_map.get(ft) != null){			
					ArrayList<ArrayList<FTransition>> aux_paths = (ArrayList<ArrayList<FTransition>>) transition_map.get(ft).clone();
					for(ArrayList<FTransition> path : aux_paths){						
						if(outs[count].equals("unsat")){
							transition_map.get(ft).remove(path);								
						}
						count++;
					}
					if(transition_map.get(ft).size() < 1){
						return false;
					}
				}
			}
		}		
		return true;
	}
		
	public boolean find_transition_cover_set() throws Exception{
				
		if(debug) print_paths(path_map);
		
		//Map<FTransition,ArrayList<ArrayList<FTransition>>> transition_map;
		transition_set = new ArrayList<Cond_in_seq>();
		transition_map = new HashMap<FTransition,ArrayList<ArrayList<FTransition>>>();
		for(FState s: ffsm.getFStates()){
			for(FTransition ft: s.getOut()){
				ArrayList<ArrayList<FTransition>> statepath = new ArrayList<ArrayList<FTransition>>();
				if(path_map.get(s) != null){
					statepath.addAll(path_map.get(s));
				}
				ArrayList<ArrayList<FTransition>> trans_path = new ArrayList<ArrayList<FTransition>>();
				//for empty paths
				if(statepath.size() == 0){
					ArrayList<FTransition> t_path = new ArrayList<FTransition>();
					t_path.add(ft);
					trans_path.add(t_path);
				}
				//for normal paths				
				for(ArrayList<FTransition> path : statepath){
					ArrayList<FTransition> t_path = (ArrayList<FTransition>) path.clone();
					t_path.add(ft);
					trans_path.add(t_path);
				}				
				transition_map.put(ft, trans_path);
			}
		}
		//if(debug) print_transition_paths(transition_map);
		//remove invalid paths
		check_valid_transition_paths(prop);
		//reduce required paths
		for(FTransition t : transition_map.keySet()){
			reduce_transition_cover(t);
		}
		//if(debug) print_transition_paths(transition_map);
		boolean correct = check_transition_coverage(prop);
		if(!correct) return false;
		
		//generate table with conditional inputs	
		trans_table = new HashMap<FTransition,ArrayList<Cond_in_seq>>();
		for(FTransition ft: transition_map.keySet()){	
			ArrayList<Cond_in_seq> cin_list = new ArrayList<Cond_in_seq>(); 
			for(ArrayList<FTransition> path : transition_map.get(ft)){
				ArrayList<String> inseq = new ArrayList<String>();
				
				//add condition variables
				ArrayList<String> vars = new ArrayList<String>();
				add_set_item(vars, path.get(0).getSource().getCondition());
				//String condition = "(and "+path.get(0).getSource().getCondition();
				for(FTransition t : path){
					inseq.add(t.getCInput().getIn());
					//condition = condition.concat(" "+t.getCInput().getCond()+" "
					//		+t.getTarget().getCondition());
					add_set_item(vars, t.getCInput().getCond());
					add_set_item(vars, t.getTarget().getCondition());
				}
				
				String condition = "(and ";
				for(String s : vars){
					condition = condition.concat(s+" ");
				}
				condition = condition.substring(0,condition.length()-1);				
				condition = condition.concat(")");		
				//condition = reduce_condition(condition);
				Cond_in_seq cin = new Cond_in_seq(inseq,condition);
				if(!transition_set.contains(cin)){
					transition_set.add(cin);
					//System.out.println("NEW TEST "+cin);
				}
				cin_list.add(cin);
			}
			trans_table.put(ft, cin_list);
		}	
		if(debug) print_transition_table(trans_table);
		return true;
	}
	
	public String reduce_condition_old(String condition){	
		
		ArrayList<String> vars = new ArrayList<String>();		
		single_reduction(vars, condition);
		//System.out.println("VARS"+vars);
		if(vars.size() <= 0){
			return "true";
		}else if(vars.size() == 1){
			if(condition.startsWith("(not")){
				return "(not "+vars.get(0)+")";
			}
			return ""+vars.get(0); 
		}else {
			String out = "";
			if(condition.startsWith("(and")){
				out = "(and ";
			}	
			if(condition.startsWith("(or")){
				out = "(or ";
			}
			if(condition.startsWith("(not")){
				out = "(not ";
			}
			for(String v: vars){
				out = out.concat(v+" ");
			}
			out = out.substring(0, out.length()-1);
			out = out.concat(")");
			return out;
		}
	}
	
	public void single_reduction(ArrayList<String> vars, String condition){
		//String backup = condition;
		String red = condition.substring(4, condition.length()-1);// avoid operator
		red = red.trim();
		if(red.indexOf("(") <= 0){
			ArrayList<String> vs = get_c_vars(red);
			add_set_item(vars, vs);		
			return;
		}
		while(condition.indexOf("(") != condition.lastIndexOf("(")){			
			String ext = red;
			String before = ext.substring(0, ext.indexOf("("));
			String in = ext.substring(ext.indexOf("("), ext.lastIndexOf(")")+1);
			String after = ext.substring(ext.lastIndexOf(")")+1, ext.length());
			before = before.trim();
			after = after.trim();			
			ArrayList<String> vs = get_c_vars(before);
			vs.add(in);
			add_set_item(vars, vs);			
			condition = after;
			//System.out.println("after:"+after);
		}
		if(condition.indexOf("(") <= 0){
			ArrayList<String> vs = get_c_vars(condition);
			add_set_item(vars, vs);			
			return;
		}
	}
	
	public void add_set_item(ArrayList<String> vars, String cond){		
		if(!vars.contains(cond)){
			vars.add(cond);
		}
	}
	
	public void add_set_item(ArrayList<String> vars, ArrayList<String> add){
		for(String s : add){
			if(!vars.contains(s)){
				vars.add(s);
			}
		}		
	}
	
	public ArrayList<String> get_c_vars(String get){
		ArrayList<String> vars = new ArrayList<String>();
		ArrayList<String> ops = new ArrayList<String>();
		ops.add("and"); ops.add("or"); ops.add("not");
		
		String[] vs = get.split(" ");
		for(String s : vs){
			if(!ops.contains(s) && !vars.contains(s) && !s.equals("true") && !s.equals("")){
				//System.out.println("var:"+s);
				vars.add(s);
			}
		}		
		return vars;
	}
	
	public void gen_hsi() throws Exception{
	/*	//print state set		
		merge_cond_seqs(state_set, "true");
		remove_cond_prefix(state_set);
		ArrayList<Cond_in_seq> suite1 = printable_suite(state_set);
		System.out.println("State Set");
		for(Cond_in_seq cin : suite1){
			System.out.println(cin);			
		}
		
		//print transition set		
		merge_cond_seqs(transition_set, "true");
		remove_cond_prefix(transition_set);	
		ArrayList<Cond_in_seq> suite2 = printable_suite(transition_set);
		System.out.println("Transition Set");
		for(Cond_in_seq cin : suite2){
			System.out.println(cin);			
		}*/
		
		ArrayList<Cond_in_seq> test_suite = new ArrayList<Cond_in_seq>();
		for(FTransition key: trans_table.keySet()){
			for(Cond_in_seq ci : trans_table.get(key)){
				//if(!transition_set.contains(ci)){
				//	System.out.println("SKIP "+ci);
				//	continue;
				//}
				FState reach = key.getTarget();
				for(Cond_in_seq ext : hsi_table.get(reach)){
					ArrayList<String> seq = (ArrayList<String>) ci.getSequence().clone();
					seq.addAll(ext.getSequence());					
					String cd1 = ci.getCond();
					String cd2 = ext.getCond();
					String cdm = cd1;
					boolean valid = true;
					if(!cd1.equals(cd2)){
						if(check_cond_prefix(cd1, cd2)){
							cdm = cd1;
						}else if(check_cond_prefix(cd2, cd1)){
							cdm = cd2;
						}else{
							cdm = "(and "+cd1+" "+cd2+")";
							if(!check_condition(cdm)){
								valid = false;
							}
						}
					}
					if(valid){
						Cond_in_seq test = new Cond_in_seq(seq,cdm);
						test_suite.add(test);
					}											
				}
			}
		}
		
		merge_cond_seqs(test_suite, "true");
		remove_cond_prefix(test_suite);
		
		//print test suite	
		hsi_set = test_suite;
		ArrayList<Cond_in_seq> suite3 = printable_suite(hsi_set);
		if(debug)System.out.println("Test suite...");
				
		full_suite = "";		
		for(Cond_in_seq i : suite3){
			if(debug)	System.out.println(i);
			full_suite = full_suite.concat(i+"\n");
		}		
		
	}
	
	public boolean check_disting_combinations_coverage(String header, FState fs1, FState fs2, 
			Map<String,ArrayList<CommonPath>> map, ArrayList<String> inputcheck) 
			throws Exception{
				
		String clause = "";		
		//Map<Cond_in_seq,CommonPath> temp_map = new HashMap<Cond_in_seq,CommonPath>();
		
		//ArrayList<String> inputset = new ArrayList<String>();
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
		clause = clause.concat("(assert (and \n");		
		for(String in : inputcheck){			
			ArrayList<CommonPath> caux = map.get(in);
			for(CommonPath cp : caux){				
				//if(cp.getDistinguish()){	
					ArrayList<String> inseq = new ArrayList<String>();
					String inaux = "";
					//add condition variables
					ArrayList<String> vars = new ArrayList<String>();
					add_set_item(vars, fs1.getCondition());
					add_set_item(vars, fs2.getCondition());					
					//String condition = "(and "+fs1.getCondition()+" "+fs2.getCondition();
					clause = clause.concat("    (not (and ");
					for(FTransition t : cp.get1()){
						//inaux = inaux.concat(t.getCInput().getIn());
						inseq.add(t.getCInput().getIn());
						inaux = inaux.concat(t.getCInput().getIn() + ",");
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
						//condition = condition.concat(" "+t.getCInput().getCond()+" "
						//		+t.getTarget().getCondition());
						add_set_item(vars, t.getCInput().getCond());
						add_set_item(vars, t.getTarget().getCondition());
					}
					for(FTransition t : cp.get2()){
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
						//condition = condition.concat(" "+t.getCInput().getCond()+" "
						//		+t.getTarget().getCondition());
						add_set_item(vars, t.getCInput().getCond());
						add_set_item(vars, t.getTarget().getCondition());
					}
					clause = clause.concat("))\n");
					String condition = "(and ";
					for(String s : vars){
						condition = condition.concat(s+" ");
					}
					condition = condition.substring(0,condition.length()-1);
					condition = condition.concat(")");
					//condition = reduce_condition(condition);
					inaux = inaux.substring(0, inaux.length()-1);
					//inputset.add(inaux);
					
					//Cond_in_seq cin = new Cond_in_seq(inseq,condition);
					//temp_map.put(cin, cp);
				//}				
			}
		}				
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)");
		String prop_aux = header.concat(clause);			
		String[] outs = processZ3(prop_aux);
						
		if(outs[0].equals("unsat")){			
			//return inputset;
			return true;
		}else{
			//return null;
			return false;
		}
	}	
	
	public boolean find_disting_seq() throws Exception{
		String[] outs = check_state_pairs();
		int count = 0;		
		ArrayList<FState> fs_aux = (ArrayList<FState>) ffsm.getFStates().clone();
		//ArrayList<PairTable> unc_table = new ArrayList<PairTable>();
		ArrayList<String> unc_pairs = new ArrayList<String>();
		hsi_select_table = new HashMap<String, ArrayList<PairTable>>();
		
		for(FState fs1 : ffsm.getFStates()){
			fs_aux.remove(fs1);
			for(FState fs2 : fs_aux){
				if(outs[count].equals("sat")){
					if(islog) log = log.concat("Pair "+fs1+" "+fs2+"\n");
					if(debug)System.out.println("Pair "+fs1+" "+fs2);
					String key = fs1+";"+fs2;
					pre_table.put(key, new PairTable(fs1, fs2));
					//boolean stilltrue = find_and_check_disting_seq(prop, fs1, fs2, ffsm.getNumberOfFStates());					
					//ArrayList<Cond_in_seq> ds = find_disting_seq(prop, fs1, fs2, ffsm.getNumberOfFStates());
					boolean found = find_disting_seq(prop, fs1, fs2, ffsm.getNumberOfFStates());
					if(!found){
						unc_pairs.add(key);
						//unc_table.add(new PairTable(new ArrayList<String>(), fs1, fs2, seq_map));
					}					
				}
				count++;
			}					
		}
		
		//go recursive
		unc:for(String pair : unc_pairs){
			if(islog) log = log.concat("\nUncovered State pair "+pair+"\n");
			if(debug)System.out.println("\nUncovered State pair "+pair);
			
			FState fs1 = pre_table.get(pair).getS1();
			FState fs2 = pre_table.get(pair).getS2();
			Map<String,ArrayList<CommonPath>> s_map = pre_table.get(pair).getSeqMap();
			seq_map = s_map;
			pre_table.get(pair).resetMap();
			
			//for(String in : ffsm.getInputAlphabet()){
			ina:for(String in : ffsm.getInputAlphabet()){
				if(!seq_map.keySet().contains(in) ||	seq_map.get(in) == null){
					continue ina;
				}				
				//is it worth checking recursively?
				ArrayList<String> incheck = new ArrayList<String>();
				for(String i : ffsm.getInputAlphabet()){
					ok:for(CommonPath h : seq_map.get(i)){
						if(h.getDistinguish()){
							incheck.add(i);
							break ok;
						}
					}
				}
				incheck.add(in);
				if(!check_disting_combinations_coverage(prop, fs1, fs2, seq_map, incheck)){
					continue ina;
				}
				
				ArrayList<CommonPath> caux = (ArrayList<CommonPath>) seq_map.get(in).clone();				
				for(CommonPath cp : caux){
					FState a1 = cp.get1().get(cp.get1().size()-1).getTarget();
					FState a2 = cp.get2().get(cp.get2().size()-1).getTarget();				
					//if both do not lead to the same state, and both are not self loops
					if(!a1.equals(a2) && !(fs1.equals(a1) && (fs2.equals(a2)))){
						if(!cp.getDistinguish()){
							seq_map.get(in).remove(cp);
						}
						//seq_map.get(in).clear();
						if(islog) log = log.concat(" WHAT? "+ fs1+" -> "+a1 + " "+fs2+" -> "+" "+a2+ " input "+in +"\n");
						if(debug) System.out.println(" WHAT? "+ fs1+" -> "+a1 + " "+fs2+" -> "+" "+a2+ " input "+in);
						ArrayList<String> seq = recursive_common_path2(prop, a1, a2, cp, in, fs1, fs2);
						if(seq != null){						
							if(islog) log = log.concat("GOT "+ fs1+" "+fs2 + " "+a1+" "+" "+a2+ " "+seq+"\n");
							if(debug) System.out.println("GOT "+ fs1+" "+fs2 + " "+a1+" "+" "+a2+ " "+seq);
							//return seq;
							continue unc;
						}
					}				
				}
			}					
			if(islog) log = log.concat("Could not find a seq. for "+ fs1 + " and "+fs2+"\n");
			if(debug) System.out.println("Could not find a seq. for "+ fs1 + " and "+fs2);
			pre_table.get(pair).resetMap();
			return false;
		}
		return true;
		
	}	
	
	public boolean find_disting_seq(String header, FState fs1, FState fs2, int n) 
			throws Exception{
							
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();	
		
		seq_map = new HashMap<String,ArrayList<CommonPath>>();	
		boolean found_input = false;
										
		//process distinguish seq size 1
		String[] outs = check_common_pair(header, current_out1, current_out2, fs1, fs2);
		
		int count = 0;
		nin:for(String in : ffsm.getInputAlphabet()){			
			seq_map.put(in, new ArrayList<CommonPath>());
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//Is there a product for both transitions?
						if(outs[count].equals("sat")){
							CommonPath cnew = new CommonPath(fs1, fs2, n);							
							//1-Can be distinguished? (identification)
							//2-Max size path is less than n-1?							
							if(cnew.addCommon(co1, co2)){
								seq_map.get(in).add(cnew);
								found_input = true;
								
								//new single check
								if(cnew.getDistinguish()){
									//new check by reduction	
									ArrayList<String> incheck = new ArrayList<String>();
									for(String inp : ffsm.getInputAlphabet()){
										if(seq_map.keySet().contains(inp)){
											ok:for(CommonPath h : seq_map.get(inp)){
												if(h.getDistinguish()){
													incheck.add(inp);
													break ok;
												}
											}
										}
									}
									boolean found_distinguish = false;	
									ArrayList<String> inputset = check_disting_combinations(header, fs1, fs2,
											seq_map, incheck, found_distinguish);
									if(inputset != null){
										input_index_set = new ArrayList<String>();
										input_index_set.addAll(incheck);										
										reduce_common_distinguish(header, fs1, fs2);
										inputset = check_disting_combinations(header, fs1, fs2, seq_map,
												input_index_set, found_distinguish);
										//if(debug) System.out.println("FOUND inputs "+input_index_set+" for "+ fs1 + " and "+fs2);
										return true;
									}
								}
							}
						}
						count++;
					}					
				}
			}	
		}
		
		if(!found_input){			
			if(islog) log = log.concat("Could not find an input for "+ fs1 + " and "+fs2+"\n");
			if(debug) System.out.println("Could not find an input for "+ fs1 + " and "+fs2);
			return false;
		}
		
		return false;		 
	}
	
	public boolean find_and_check_disting_seq(String header, FState fs1, FState fs2, int n) 
			throws Exception{
							
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();				
		seq_map = new HashMap<String,ArrayList<CommonPath>>();	
		boolean found_input = false;
						
		//process distinguish seq size 1
		String[] outs = check_common_pair(header, current_out1, current_out2, fs1, fs2);
		
		int count = 0;
		for(String in : ffsm.getInputAlphabet()){			
			seq_map.put(in, new ArrayList<CommonPath>());
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//Is there a product for both transitions?
						if(outs[count].equals("sat")){
							CommonPath cnew = new CommonPath(fs1, fs2, n);
							//They...
							//1-Can be distinguished? (identification)
							//2-Max size path is less than n-1?
							//3-Is there a valid path (products) for both transitions?
							if(cnew.addCommon(co1, co2)){
								seq_map.get(in).add(cnew);
								found_input = true;
							}
						}
						count++;
					}					
				}
			}	
		}	
		
		if(!found_input){			
			if(islog) log = log.concat("Could not find a input for "+ fs1 + " and "+fs2+"\n");
			if(debug) System.out.println("Could not find a input for "+ fs1 + " and "+fs2);
			return false;
		}
		
		//print
		if(islog || debug) print_common_pairs(fs1, fs2);	
		
		//check input		
		ArrayList<String> alp = new ArrayList<String>();
		for(String s : ffsm.getInputAlphabet()){
			alp.add(s);
		}			
		//for(int i=1; i<=alp.size(); i++){			
			//ArrayList<ArrayList<String>> inchecklist = find_inset(i,alp.size(),alp);
			ArrayList<ArrayList<String>> inchecklist = find_inset(1,alp.size(),alp);
			if(islog) log = log.concat("CHECKING"+"\n");
			if(debug) System.out.println("CHECKING");
			for(ArrayList<String> incheck : inchecklist){				
				if(islog) log = log.concat(incheck+"\n");
				if(debug) System.out.println(incheck);
				ArrayList<String> inputset = check_disting_old(header, fs1, fs2, seq_map, incheck);
				if(inputset != null){					
					if(islog) log = log.concat("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n"+"\n");
					if(debug) System.out.println("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n");
					return true;
				}
			}			
		//}	
					
		
		//recursive call to n-1
		for(String in : ffsm.getInputAlphabet()){
			ArrayList<CommonPath> caux = (ArrayList<CommonPath>) seq_map.get(in).clone();
			for(CommonPath cp : caux){
				FState a1 = cp.get1().get(cp.get1().size()-1).getTarget();
				FState a2 = cp.get2().get(cp.get2().size()-1).getTarget();				
				//if both do not lead to the same state, and both are not self loops
				if(!a1.equals(a2) && !(fs1.equals(a1) && (fs2.equals(a2)))){
					seq_map.get(in).clear();					
					if(islog) log = log.concat(" WHAT? "+ fs1+" -> "+a1 + " "+fs2+" -> "+" "+a2+ " input "+in +"\n");
					if(debug) System.out.println(" WHAT? "+ fs1+" -> "+a1 + " "+fs2+" -> "+" "+a2+ " input "+in);
					boolean got = rec_common(header, a1, a2, cp, in);
					if(got){						
						if(islog) log = log.concat("GOT "+ fs1+" "+fs2 + " "+a1+" "+" "+a2+ " "+in+"\n");
						if(debug) System.out.println("GOT "+ fs1+" "+fs2 + " "+a1+" "+" "+a2+ " "+in);
						return true;
					}
				}				
			}
		}
				
		if(islog) log = log.concat("Could no find a seq. for "+ fs1 + " and "+fs2+"\n");
		if(debug) System.out.println("Could no find a seq. for "+ fs1 + " and "+fs2);
		//could not find a distinguishing sequence...
		return false;
	}
	
	public boolean find_and_check_disting_seq_old(String header, FState fs1, FState fs2, int n) 
			throws IOException, InterruptedException{
							
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();				
		seq_map = new HashMap<String,ArrayList<CommonPath>>();	
		boolean found_input = false;
						
		//process distinguish seq size 1
		//String[] outs = check_common_pair(header, current_out1, current_out2, fs1, fs2);
		
		int count = 0;
		for(String in : ffsm.getInputAlphabet()){			
			seq_map.put(in, new ArrayList<CommonPath>());
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//Is there a product for both transitions?
						//if(outs[count].equals("sat")){
						if(check_common_pair_old(header, fs1, fs2, co1, co2)){
							CommonPath cnew = new CommonPath(fs1, fs2, n);
							//They...
							//1-Can be distinguished? (identification)
							//2-Max size path is less than n-1?
							//3-Is there a valid path (products) for both transitions?
							if(cnew.addCommon_old(header, co1, co2)){
								seq_map.get(in).add(cnew);
								found_input = true;
							}
						}
						count++;
					}					
				}
			}	
		}	
		
		if(!found_input){			
			if(islog) log = log.concat("Could not find a input for "+ fs1 + " and "+fs2+"\n");
			if(debug) System.out.println("Could not find a input for "+ fs1 + " and "+fs2);
			return false;
		}
		
		//print
		if(islog || debug) print_common_pairs(fs1, fs2);	
		
		//check input		
		ArrayList<String> alp = new ArrayList<String>();
		for(String s : ffsm.getInputAlphabet()){
			alp.add(s);
		}				
		for(int i=1; i<=alp.size(); i++){			
			ArrayList<ArrayList<String>> inchecklist = find_inset(i,alp.size(),alp);			
			if(islog) log = log.concat("CHECKING"+"\n");
			if(debug) System.out.println("CHECKING");
			for(ArrayList<String> incheck : inchecklist){				
				if(islog) log = log.concat(incheck+"\n");
				if(debug) System.out.println(incheck);
				ArrayList<String> inputset = check_disting_old(header, fs1, fs2, seq_map, incheck);
				if(inputset != null){					
					if(islog) log = log.concat("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n"+"\n");
					if(debug) System.out.println("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n");
					return true;
				}
			}			
		}				
		
		//recursive call to n-1
		for(String in : ffsm.getInputAlphabet()){
			ArrayList<CommonPath> caux = (ArrayList<CommonPath>) seq_map.get(in).clone();
			for(CommonPath cp : caux){
				FState a1 = cp.get1().get(cp.get1().size()-1).getTarget();
				FState a2 = cp.get2().get(cp.get2().size()-1).getTarget();				
				//if both do not lead to the same state, and both are not self loops
				if(!a1.equals(a2) && !(fs1.equals(a1) && (fs2.equals(a2)))){
					seq_map.get(in).clear();					
					if(islog) log = log.concat(" WHAT? "+ fs1+" -> "+a1 + " "+fs2+" -> "+" "+a2+ " input "+in +"\n");
					if(debug) System.out.println(" WHAT? "+ fs1+" -> "+a1 + " "+fs2+" -> "+" "+a2+ " input "+in);
					boolean got = rec_common_old(header, a1, a2, cp, in);
					if(got){						
						if(islog) log = log.concat("GOT "+ fs1+" "+fs2 + " "+a1+" "+" "+a2+ " "+in+"\n");
						if(debug) System.out.println("GOT "+ fs1+" "+fs2 + " "+a1+" "+" "+a2+ " "+in);
						return true;
					}
				}				
			}
		}
				
		if(islog) log = log.concat("Could no find a seq. for "+ fs1 + " and "+fs2+"\n");
		if(debug) System.out.println("Could no find a seq. for "+ fs1 + " and "+fs2);
		//could not find a distinguishing sequence...
		return false;
	}
	
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	    	sets.add(new HashSet<T>());
	    	return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	    	Set<T> newSet = new HashSet<T>();
	    	newSet.add(head);
	    	newSet.addAll(set);
	    	sets.add(newSet);
	    	sets.add(set);
	    }	   
	    return sets;
	}
	
	public ArrayList<ArrayList<String>> find_inset(int size, int max, ArrayList<String> alp){	
		
		ArrayList<ArrayList<String>> inchecklist = new ArrayList<ArrayList<String>>();		
		Set<String> p_set = new HashSet<String>();
		p_set.addAll(alp);	
		for(int i=size; i<=max; i++){
			for (Set<String> set : powerSet(p_set)) {
				if(set.size() == i){
					ArrayList<String> arr = new ArrayList<String>();
					for(String s : set){
						arr.add(s);
					}
					inchecklist.add(arr);
				}
			}
		}		
		return inchecklist;
	}	
	
	public ArrayList<String> check_input_list(String header, FState in_s1, FState in_s2, String changed)
			throws Exception{		
		
		//new check by reduction		
		ArrayList<String> incheck = new ArrayList<String>();
		for(String in : ffsm.getInputAlphabet()){
			ok:for(CommonPath h : seq_map.get(in)){
				if(h.getDistinguish()){
					incheck.add(in);
					break ok;
				}
			}
		}			
		//check if all distinguishable inputs will do...
		ArrayList<String> inputset = check_disting_combinations_lv2(header, in_s1, in_s2, seq_map, incheck);		
		if(inputset != null){
			// ok then lets reduce
			input_index_set = new ArrayList<String>();
			input_index_set.addAll(incheck);			
			//reduce input_index_set
			reduce_common_distinguish(header, in_s1, in_s2);
			
			//now reduce inside the table
			for(String in : input_index_set){
				reduce_input_index_sequences(in, in_s1, in_s2);
			}
			//get final reduced combination of input sequences
			inputset = check_disting_combinations_lv2(header, in_s1, in_s2, seq_map, input_index_set);
						
			//if(islog) log = log.concat("\nState pair "+in_s1+" and "+in_s2+" OK for inputset "+inputset+"\n"+"\n");
			if(debug) System.out.println("\nState pair "+in_s1+" and "+in_s2+" OK for inputset "+inputset+"\n");
			
			return inputset;
		}
		return null;
	}
	
	public void order_common_paths(ArrayList<FTransition> current_out1, ArrayList<FTransition> current_out2){
		
		ArrayList<FTransition> ts1 = (ArrayList<FTransition>) current_out1.clone();
		ArrayList<FTransition> ts2 = (ArrayList<FTransition>) current_out2.clone();
		for(String in : ffsm.getInputAlphabet()){			
			for(FTransition co1 : ts1){
				for(FTransition co2 : ts2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//check same condition
						if(co1.getCInput().getCond().equals(co2.getCInput().getCond())){
							current_out1.remove(co1);
							current_out1.add(0,co1);
							current_out2.remove(co2);
							current_out2.add(0,co2);
						}
					}
				}
			}
		}
		for(String in : ffsm.getInputAlphabet()){			
			for(FTransition co1 : ts1){
				for(FTransition co2 : ts2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//true condition above others
						if(co1.getCInput().getCond().equals("true") && 
								co2.getCInput().getCond().equals("true")){
							current_out1.remove(co1);
							current_out1.add(0,co1);
							current_out2.remove(co2);
							current_out2.add(0,co2);
						}
					}
				}
			}
		}
	}
	
	public ArrayList<String> recursive_common_path2(String header, FState fs1, FState fs2, CommonPath cp, 
			String begininput, FState in_s1, FState in_s2) throws Exception{
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();
		boolean found_input = false;
		boolean found_distinguish = false;
	
		if(islog) log = log.concat(" BEFORE " + fs1 + " " +fs2+" via "+begininput+"\n");
		//if(debug) System.out.println(" BEFORE " + fs1 + " " +fs2+" via "+begininput+"\n");
		//if(islog || debug) print_common_pairs(fs1, fs2);
		
		String[] outs = check_common_valid_pair(header, current_out1, current_out2, fs1, fs2, cp);
				
		int count = 0;
		for(String in : ffsm.getInputAlphabet()){			
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//Is there a product for both transitions? and the last pair does not distinguish
						if(outs[count].equals("sat") && !cp.getDistinguish()){						
							CommonPath cnew = new CommonPath(cp.getS1(), cp.getS2(), cp.getN(), cp.get1(), cp.get2());							
							if(cnew.addCommon(co1, co2)){				
								seq_map.get(begininput).add(cnew);
								if(cnew.getDistinguish()){
									found_distinguish = true;
								}
								found_input = true;
							}
						}
						count++;
					}					
				}
			}	
		}	
		if(!found_input){			
			if(islog) log = log.concat("\nNo input available!"+"\n");
			if(debug) System.out.println("\nNo input available!");
			return null;
		}
		if(islog) log = log.concat(" AFTER " + fs1 + " " +fs2+"\n");
		//if(debug) System.out.println(" AFTER " + fs1 + " " +fs2);
		//if(islog || debug) print_common_pairs(fs1, fs2);
		
		//check extra paths 
		if(found_distinguish){
			ArrayList<String> inputset = check_input_list(header, in_s1, in_s2, begininput);
			if(inputset != null){
				return inputset;
			}
		}
				
		//is it worth checking recursively?
		ArrayList<String> incheck = new ArrayList<String>();
		for(String i : ffsm.getInputAlphabet()){
			ok:for(CommonPath h : seq_map.get(i)){
				if(h.getDistinguish()){
					incheck.add(i);
					break ok;
				}
			}
		}
		incheck.add(begininput);
		if(!check_disting_combinations_coverage(prop, fs1, fs2, seq_map, incheck)){
			return null;
		}
				
		//recursive calls
		//ArrayList<CommonPath> caux = seq_map.get(lastinput);
		ArrayList<CommonPath> caux = (ArrayList<CommonPath>) seq_map.get(begininput).clone();
		for(CommonPath p : caux){
			FState a1 = p.get1().get(p.get1().size()-1).getTarget();
			FState a2 = p.get2().get(p.get2().size()-1).getTarget();
			//String in = p.get1().get(p.get1().size()-1).getCInput().getIn();
			FState a11 = p.get1().get(p.get1().size()-1).getSource();
			FState a22 = p.get2().get(p.get2().size()-1).getSource();	
			//if both do not lead to the same state, and both are not self loops				
			if(!a1.equals(a2) && !(a11.equals(a1) && (a22.equals(a2)))){
				if(!p.getDistinguish()){
					seq_map.get(begininput).remove(p);							
					//call recursive
					ArrayList<String> seq = recursive_common_path2(header, a1, a2, p, begininput, in_s1, in_s2);
					if(seq != null){
						return seq;
					}
				}
			}				
		}	
		
		return null;
	}
	
	public ArrayList<String> recursive_common_path(String header, FState fs1, FState fs2, CommonPath cp, 
			String lastinput) throws Exception{
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();
		boolean found_input = false;
				
		if(islog) log = log.concat(" TEST0 " + fs1 + " " +fs2+" via "+lastinput+"\n");
		if(debug) System.out.println(" TEST0 " + fs1 + " " +fs2+" via "+lastinput+"\n");
		if(islog || debug) print_common_pairs(fs1, fs2);
		
		String[] outs = check_common_valid_pair(header, current_out1, current_out2, fs1, fs2, cp);
				
		int count = 0;
		for(String in : ffsm.getInputAlphabet()){			
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//Is there a product for both transitions?
						if(outs[count].equals("sat") && !cp.getDistinguish()){						
							CommonPath cnew = new CommonPath(cp.getS1(), cp.getS2(), cp.getN(), cp.get1(), cp.get2());							
							if(cnew.addCommon(co1, co2)){									
								seq_map.get(lastinput).add(cnew);
								found_input = true;								
							}
						}
						count++;
					}					
				}
			}	
		}	
		if(!found_input){			
			if(islog) log = log.concat("\nNo input available!"+"\n");
			if(debug) System.out.println("\nNo input available!");
			return null;
		}
		if(islog) log = log.concat(" TEST1 " + fs1 + " " +fs2+"\n");
		if(debug) System.out.println(" TEST1 " + fs1 + " " +fs2);
		if(islog || debug) print_common_pairs(fs1, fs2);	
			
		//check input				
		ArrayList<String> alp = new ArrayList<String>();
		for(String s : ffsm.getInputAlphabet()){
			alp.add(s);
		}
		//for(int i=1; i<=alp.size(); i++){			
			ArrayList<ArrayList<String>> inchecklist = find_inset(1,alp.size(),alp);			
			if(islog) log = log.concat("CHECKING 2"+"\n");
			if(debug) System.out.println("CHECKING 2");
			for(ArrayList<String> incheck : inchecklist){				
				if(islog) log = log.concat(incheck+"\n");
				if(debug) System.out.println(incheck);
				ArrayList<String> inputset = check_disting_old(header, fs1, fs2, seq_map, incheck);
				if(inputset != null){					
					if(islog) log = log.concat("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n"+"\n");
					if(debug) System.out.println("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n");
					return inputset;
				}
			}			
		//}	
		
		/*
		ArrayList<CommonPath> caux = seq_map.get(lastinput);
		for(CommonPath p : caux){
			FState a1 = p.get1().get(p.get1().size()-1).getTarget();
			FState a2 = p.get2().get(p.get2().size()-1).getTarget();
			String in = p.get1().get(p.get1().size()-1).getCInput().getIn();
			FState a11 = p.get1().get(p.get1().size()-1).getSource();
			FState a22 = p.get2().get(p.get2().size()-1).getSource();				
			if(!a1.equals(a2) && !(a11.equals(a1) && (a22.equals(a2)))){				
				if(islog) log = log.concat("\n States \n"+a1+" "+a2+" "+a11+" "+a22+ " "+in+"\n");
				if(islog) log = log.concat("\nGoing recursive\n"+in+"\n");
				if(debug) System.out.println("\n States \n"+a1+" "+a2+" "+a11+" "+a22+ " "+in+"\n");
				if(debug) System.out.println("\nGoing recursive\n"+in+"\n");
				ArrayList<String> seq = recursive_common_path(header, a1, a2, p, in);
				if(seq != null){
					return seq;
				}
			}				
		}		
		*/
		
		return null;
	}
		
	public boolean rec_common(String header, FState fs1, FState fs2, CommonPath cp, String lastinput) 
			throws Exception{
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();
		boolean found_input = false;
				
		if(islog) log = log.concat(" TEST0 " + fs1 + " " +fs2+"\n");
		if(debug) System.out.println(" TEST0 " + fs1 + " " +fs2+"\n");
		if(islog || debug) print_common_pairs(fs1, fs2);
		
		String[] outs = check_common_valid_pair(header, current_out1, current_out2, fs1, fs2, cp);
				
		int count = 0;
		for(String in : ffsm.getInputAlphabet()){			
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//Is there a product for both transitions?
						if(outs[count].equals("sat") && !cp.getDistinguish()){						
							CommonPath cnew = new CommonPath(cp.getS1(), cp.getS2(), cp.getN(), cp.get1(), cp.get2());							
							if(cnew.addCommon(co1, co2)){									
								seq_map.get(lastinput).add(cnew);
								found_input = true;								
							}
						}
						count++;
					}					
				}
			}	
		}	
		if(!found_input){			
			if(islog) log = log.concat("\nNo input available!"+"\n");
			if(debug) System.out.println("\nNo input available!");
			return false;
		}
		if(islog) log = log.concat(" TEST1 " + fs1 + " " +fs2+"\n");
		if(debug) System.out.println(" TEST1 " + fs1 + " " +fs2);
		if(islog || debug) print_common_pairs(fs1, fs2);	
			
		//check input				
		ArrayList<String> alp = new ArrayList<String>();
		for(String s : ffsm.getInputAlphabet()){
			alp.add(s);
		}
		for(int i=1; i<=alp.size(); i++){			
			ArrayList<ArrayList<String>> inchecklist = find_inset(i,alp.size(),alp);			
			if(islog) log = log.concat("CHECKING 2"+"\n");
			if(debug) System.out.println("CHECKING 2");
			for(ArrayList<String> incheck : inchecklist){				
				if(islog) log = log.concat(incheck+"\n");
				if(debug) System.out.println(incheck);
				ArrayList<String> inputset = check_disting_old(header, fs1, fs2, seq_map, incheck);
				if(inputset != null){					
					if(islog) log = log.concat("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n"+"\n");
					if(debug) System.out.println("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n");
					return true;
				}
			}			
		}	
		
		
		ArrayList<CommonPath> caux = seq_map.get(lastinput);
		for(CommonPath p : caux){
			FState a1 = p.get1().get(p.get1().size()-1).getTarget();
			FState a2 = p.get2().get(p.get2().size()-1).getTarget();
			String in = p.get1().get(p.get1().size()-1).getCInput().getIn();
			FState a11 = p.get1().get(p.get1().size()-1).getSource();
			FState a22 = p.get2().get(p.get2().size()-1).getSource();				
			if(!a1.equals(a2) && !(a11.equals(a1) && (a22.equals(a2)))){				
				if(islog) log = log.concat("\n States \n"+a1+" "+a2+" "+a11+" "+a22+ " "+in+"\n");
				if(islog) log = log.concat("\nGoing recursive\n"+in+"\n");
				if(debug) System.out.println("\n States \n"+a1+" "+a2+" "+a11+" "+a22+ " "+in+"\n");
				if(debug) System.out.println("\nGoing recursive\n"+in+"\n");
				boolean got = rec_common(header, a1, a2, p, in);
				if(got){
					return true;
				}
			}				
		}		
		
		return false;
	}
	
	public boolean rec_common_old(String header, FState fs1, FState fs2, CommonPath cp, String lastinput) 
			throws IOException, InterruptedException{
		ArrayList<FTransition> current_out1 = fs1.getOut();
		ArrayList<FTransition> current_out2 = fs2.getOut();
		boolean found_input = false;
				
		if(islog) log = log.concat(" TEST0 " + fs1 + " " +fs2+"\n");
		if(debug) System.out.println(" TEST0 " + fs1 + " " +fs2+"\n");
		if(islog || debug) print_common_pairs(fs1, fs2);
				
		for(String in : ffsm.getInputAlphabet()){			
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){	
						//Is there a product for both transitions?
						if(check_common_pair_old(header, fs1, fs2, co1, co2) && !cp.getDistinguish()){
							CommonPath cnew = new CommonPath(cp.getS1(), cp.getS2(), cp.getN(), cp.get1(), cp.get2());							
							if(cnew.addCommon_old(header, co1, co2)){									
								seq_map.get(lastinput).add(cnew);
								found_input = true;								
							}
						}
					}					
				}
			}	
		}	
		if(!found_input){			
			if(islog) log = log.concat("\nNo input available!"+"\n");
			if(debug) System.out.println("\nNo input available!");
			return false;
		}
		if(islog) log = log.concat(" TEST1 " + fs1 + " " +fs2+"\n");
		if(debug) System.out.println(" TEST1 " + fs1 + " " +fs2);
		if(islog || debug) print_common_pairs(fs1, fs2);	
			
		//check input				
		ArrayList<String> alp = new ArrayList<String>();
		for(String s : ffsm.getInputAlphabet()){
			alp.add(s);
		}	
		for(int i=1; i<=alp.size(); i++){			
			ArrayList<ArrayList<String>> inchecklist = find_inset(i,alp.size(),alp);			
			if(islog) log = log.concat("CHECKING 2"+"\n");
			if(debug) System.out.println("CHECKING 2");
			for(ArrayList<String> incheck : inchecklist){				
				if(islog) log = log.concat(incheck+"\n");
				if(debug) System.out.println(incheck);
				ArrayList<String> inputset = check_disting_old(header, fs1, fs2, seq_map, incheck);
				if(inputset != null){					
					if(islog) log = log.concat("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n"+"\n");
					if(debug) System.out.println("\nState pair "+fs1+" and "+fs2+" OK for inputset "+inputset+"\n");
					return true;
				}
			}			
		}			 
		
		ArrayList<CommonPath> caux = seq_map.get(lastinput);
		for(CommonPath p : caux){
			FState a1 = p.get1().get(p.get1().size()-1).getTarget();
			FState a2 = p.get2().get(p.get2().size()-1).getTarget();
			String in = p.get1().get(p.get1().size()-1).getCInput().getIn();
			FState a11 = p.get1().get(p.get1().size()-1).getSource();
			FState a22 = p.get2().get(p.get2().size()-1).getSource();				
			if(!a1.equals(a2) && !(a11.equals(a1) && (a22.equals(a2)))){				
				if(islog) log = log.concat("\n States \n"+a1+" "+a2+" "+a11+" "+a22+ " "+in+"\n");
				if(islog) log = log.concat("\nGoing recursive\n"+in+"\n");
				if(debug) System.out.println("\n States \n"+a1+" "+a2+" "+a11+" "+a22+ " "+in+"\n");
				if(debug) System.out.println("\nGoing recursive\n"+in+"\n");
				boolean got = rec_common_old(header, a1, a2, p, in);
				if(got){
					return true;
				}
			}				
		}		
		
		return false;
	}
	
	public ArrayList<String> check_disting(String header, ArrayList<String> alp, FState fs1, FState fs2, 
			Map<String,ArrayList<CommonPath>> map) 
			throws IOException, InterruptedException{
				
		String clause = "";
		ArrayList<ArrayList<String>> inputsetlist = new ArrayList<ArrayList<String>>();
		
		for(int i=1; i<=alp.size(); i++){			
			ArrayList<ArrayList<String>> inchecklist = find_inset(i,alp.size(),alp);
			for(ArrayList<String> incheck : inchecklist){	
				if(islog) log = log.concat(incheck+"\n");
				if(debug) System.out.println(incheck);
				
				ArrayList<String> inputset = new ArrayList<String>();
				clause = clause.concat("(push)\n");
				clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
				clause = clause.concat("(assert (and \n");
				for(String in : incheck){			
					ArrayList<CommonPath> caux = map.get(in);
					for(CommonPath cp : caux){				
						if(cp.getDistinguish()){	
							String inaux = "";
							clause = clause.concat("    (not (and ");
							for(FTransition t : cp.get1()){
								inaux = inaux.concat(t.getCInput().getIn() + "+");
								clause = clause.concat(t.getCInput().getCond()+" "
										+t.getTarget().getCondition()+" ");
							}
							for(FTransition t : cp.get2()){
								clause = clause.concat(t.getCInput().getCond()+" "
										+t.getTarget().getCondition()+" ");
							}
							clause = clause.concat("))\n");	
							inputset.add(inaux);
						}				
					}
				}				
				clause = clause.concat("))\n");
				clause = clause.concat("(check-sat)\n");
				clause = clause.concat("(pop)\n");
				inputsetlist.add(inputset);				
			}
		}		
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);
			// print stm2 file and execute
			String fpath = "./"+folder+"/f_dpair.smt2";
			FileHandler fh = new FileHandler();
			fh.print_file(prop_aux, fpath);
			
			String[] commands = {"./ffsm/z3","./"+folder+"/f_dpair.smt2"};
			String result = fh.getProcessOutput(commands);		
			String[] outs = result.split("\n");
			for(int i=0; i<inputsetlist.size(); i++){
				if(outs[i].equals("unsat")){			
					return inputsetlist.get(i);
				}
			}
		}		
		return null;
	}	
	
	public ArrayList<String> check_disting_combinations_lv2(String header, FState fs1, FState fs2, 
			Map<String,ArrayList<CommonPath>> map, ArrayList<String> inputcheck) 
			throws Exception{
				
		String clause = "";		
		Map<Cond_in_seq,CommonPath> temp_map = new HashMap<Cond_in_seq,CommonPath>();
		
		ArrayList<String> inputset = new ArrayList<String>();
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
		clause = clause.concat("(assert (and \n");		
		for(String in : inputcheck){			
			ArrayList<CommonPath> caux = map.get(in);
			for(CommonPath cp : caux){				
				if(cp.getDistinguish()){	
					ArrayList<String> inseq = new ArrayList<String>();
					String inaux = "";
					String condition = "(and "+fs1.getCondition()+" "+fs2.getCondition();
					clause = clause.concat("    (not (and ");
					for(FTransition t : cp.get1()){
						//inaux = inaux.concat(t.getCInput().getIn());
						inseq.add(t.getCInput().getIn());
						inaux = inaux.concat(t.getCInput().getIn() + ",");
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
						condition = condition.concat(" "+t.getCInput().getCond()+" "
								+t.getTarget().getCondition());
					}
					for(FTransition t : cp.get2()){
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
						condition = condition.concat(" "+t.getCInput().getCond()+" "
								+t.getTarget().getCondition());
					}
					clause = clause.concat("))\n");	
					condition = condition.concat(")");
					inaux = inaux.substring(0, inaux.length()-1);
					inputset.add(inaux);
					
					Cond_in_seq cin = new Cond_in_seq(inseq,condition);
					temp_map.put(cin, cp);
				}				
			}
		}				
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)");
		String prop_aux = header.concat(clause);		
		String[] outs = processZ3(prop_aux);
				
		
		String key = fs1+";"+fs2;
		PairTable p = pre_table.get(key);		
		p.resetMap();
		for(Cond_in_seq k : temp_map.keySet()){
			//if(islog) log = log.concat(k+"");
			//if(debug) System.out.println(k);
			//if(islog) log = log.concat(temp_map.get(k)+"");
			//if(debug) System.out.println(temp_map.get(k));
			p.addSequence(k,temp_map.get(k),map);
		}
		
		if(outs[0].equals("unsat")){				
			return inputset;						
		}else{
			return null;
		}		
	}	
	
	public ArrayList<String> check_disting_combinations(String header, FState fs1, FState fs2, 
			Map<String,ArrayList<CommonPath>> map, ArrayList<String> inputcheck, boolean found_distinguish) 
			throws Exception{
				
		String clause = "";		
		Map<Cond_in_seq,CommonPath> temp_map = new HashMap<Cond_in_seq,CommonPath>();
		
		ArrayList<String> inputset = new ArrayList<String>();
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
		clause = clause.concat("(assert (and \n");		
		for(String in : inputcheck){			
			ArrayList<CommonPath> caux = map.get(in);
			for(CommonPath cp : caux){				
				if(cp.getDistinguish()){	
					ArrayList<String> inseq = new ArrayList<String>();
					String inaux = "";
					//add condition variables
					ArrayList<String> vars = new ArrayList<String>();
					add_set_item(vars, fs1.getCondition());
					add_set_item(vars, fs2.getCondition());					
					//String condition = "(and "+fs1.getCondition()+" "+fs2.getCondition();
					clause = clause.concat("    (not (and ");
					for(FTransition t : cp.get1()){
						//inaux = inaux.concat(t.getCInput().getIn());
						inseq.add(t.getCInput().getIn());
						inaux = inaux.concat(t.getCInput().getIn() + ",");
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
						//condition = condition.concat(" "+t.getCInput().getCond()+" "
						//		+t.getTarget().getCondition());
						add_set_item(vars, t.getCInput().getCond());
						add_set_item(vars, t.getTarget().getCondition());
					}
					for(FTransition t : cp.get2()){
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
						//condition = condition.concat(" "+t.getCInput().getCond()+" "
						//		+t.getTarget().getCondition());
						add_set_item(vars, t.getCInput().getCond());
						add_set_item(vars, t.getTarget().getCondition());
					}
					clause = clause.concat("))\n");
					String condition = "(and ";
					for(String s : vars){
						condition = condition.concat(s+" ");
					}
					condition = condition.substring(0,condition.length()-1);
					condition = condition.concat(")");
					//condition = reduce_condition(condition);
					inaux = inaux.substring(0, inaux.length()-1);
					inputset.add(inaux);
					
					Cond_in_seq cin = new Cond_in_seq(inseq,condition);
					temp_map.put(cin, cp);
				}				
			}
		}				
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)");
		String prop_aux = header.concat(clause);				
		String[] outs = processZ3(prop_aux);
		
		String key = fs1+";"+fs2;
		if(!found_distinguish){			
			PairTable p = pre_table.get(key);		
			p.resetMap();
			for(Cond_in_seq k : temp_map.keySet()){
				//if(islog) log = log.concat(k+"");
				//if(debug) System.out.println(k);
				//if(islog) log = log.concat(temp_map.get(k)+"");
				//if(debug) System.out.println(temp_map.get(k));
				p.addSequence(k,temp_map.get(k),map);
			}			
		}				
			
		if(outs[0].equals("unsat")){
			//add to all list
			PairTable p = new PairTable(fs1,fs2);
			for(Cond_in_seq k : temp_map.keySet()){			
				p.addSequence(k,temp_map.get(k),map);
			}			
			ArrayList<PairTable> pt_list = null;
			if(hsi_select_table.get(key) != null){
				pt_list = hsi_select_table.get(key);
			}else{
				pt_list = new ArrayList<PairTable>();				
			}
			pt_list.add(p);
			hsi_select_table.put(key, pt_list);	
			return inputset;						
		}else{
			return null;
		}
	}	
	
	public ArrayList<String> check_disting_old(String header, FState fs1, FState fs2, 
			Map<String,ArrayList<CommonPath>> map, ArrayList<String> inputcheck) 
			throws IOException, InterruptedException{
				
		String clause = "";
		ArrayList<String> inputset = new ArrayList<String>();
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
		clause = clause.concat("(assert (and \n");
		for(String in : inputcheck){			
			ArrayList<CommonPath> caux = map.get(in);
			for(CommonPath cp : caux){				
				if(cp.getDistinguish()){	
					String inaux = "";
					clause = clause.concat("    (not (and ");
					for(FTransition t : cp.get1()){
						//inaux = inaux.concat(t.getCInput().getIn());
						inaux = inaux.concat(t.getCInput().getIn() + ",");
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
					}
					for(FTransition t : cp.get2()){
						clause = clause.concat(t.getCInput().getCond()+" "
								+t.getTarget().getCondition()+" ");
					}
					clause = clause.concat("))\n");	
					inaux = inaux.substring(0, inaux.length()-1);
					inputset.add(inaux);
				}				
			}
		}				
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)");
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = "./"+folder+"/f_dpair.smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		String[] commands = {"./ffsm/z3","./"+folder+"/f_dpair.smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
						
		if(outs[0].equals("unsat")){			
			return inputset;						
		}else{
			return null;
		}		
	}	
	
	public String[] check_state_pairs() 
			throws Exception{
				
		String clause = "";			
		ArrayList<FState> fs_aux = (ArrayList<FState>) ffsm.getFStates().clone();
		for(FState fs1 : ffsm.getFStates()){
			fs_aux.remove(fs1);
			for(FState fs2 : fs_aux){
				clause = clause.concat("(push)\n");
				clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");		
				clause = clause.concat("(check-sat)\n");
				clause = clause.concat("(pop)\n");			
			}
		}	
		if(!clause.equals("")){
			String prop_aux = prop.concat(clause);							
			String[] outs = processZ3(prop_aux);
			return outs;
		}
		return new String[0];
	}
	
	public boolean check_state_pair_old(String header, FState fs1, FState fs2) 
			throws IOException, InterruptedException{
				
		String clause = "";
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");		
		clause = clause.concat("(check-sat)");
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = "./"+folder+"/f_cscpair.smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		String[] commands = {"./ffsm/z3","./"+folder+"/f_cscpair.smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
						
		if(outs[0].equals("sat")){			
			return true;						
		}else{
			return false;
		}		
	}
	
	
	public String[] check_common_pair(String header, ArrayList<FTransition> current_out1,
			ArrayList<FTransition> current_out2, FState fs1, FState fs2) 
			throws Exception{
			
		String clause = "";
		for(String in : ffsm.getInputAlphabet()){
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){						
						clause = clause.concat("(push)\n");
						clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
						clause = clause.concat("(assert (and ");
						clause = clause.concat(co1.getCInput().getCond()+" "
									+co1.getTarget().getCondition()+" ");	
						clause = clause.concat(co2.getCInput().getCond()+" "
								+co2.getTarget().getCondition());
						clause = clause.concat("))\n");
						clause = clause.concat("(check-sat)\n");
						clause = clause.concat("(pop)\n");						
					}					
				}
			}	
		}	
		String prop_aux = header.concat(clause);			
		String[] outs = processZ3(prop_aux);
			
		return outs;
	}
	
	public String[] check_common_valid_pair(String header, ArrayList<FTransition> current_out1,
			ArrayList<FTransition> current_out2, FState fs1, FState fs2, CommonPath cp) 
			throws Exception{
			
		String clause = "";
		for(String in : ffsm.getInputAlphabet()){
			for(FTransition co1 : current_out1){
				for(FTransition co2 : current_out2){
					//check the same input
					if(co1.getCInput().getIn().equals(in) 
							&& co2.getCInput().getIn().equals(in)){						
						clause = clause.concat("(push)\n");
						clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
						clause = clause.concat("(assert (and ");
						for(FTransition t : cp.get1()){
							clause = clause.concat(t.getCInput().getCond()+" "
									+t.getTarget().getCondition()+" ");
						}
						for(FTransition t : cp.get2()){
							clause = clause.concat(t.getCInput().getCond()+" "
									+t.getTarget().getCondition()+" ");
						}
						clause = clause.concat(co1.getCInput().getCond()+" "
									+co1.getTarget().getCondition()+" ");	
						clause = clause.concat(co2.getCInput().getCond()+" "
								+co2.getTarget().getCondition());
						clause = clause.concat("))\n");
						clause = clause.concat("(check-sat)\n");
						clause = clause.concat("(pop)\n");						
					}					
				}
			}	
		}	
		String prop_aux = header.concat(clause);				
		String[] outs = processZ3(prop_aux);
			
		return outs;
	}
	
	public boolean check_common_pair_old(String header, FState fs1, FState fs2,
			FTransition co1, FTransition co2) 
			throws IOException, InterruptedException{
				
		String clause = "";
		clause = clause.concat("(assert (and "+fs1.getCondition()+" "+fs2.getCondition()+"))\n");
		clause = clause.concat("(assert (and ");
		clause = clause.concat(co1.getCInput().getCond()+" "
					+co1.getTarget().getCondition()+" ");	
		clause = clause.concat(co2.getCInput().getCond()+" "
				+co2.getTarget().getCondition());
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)");
		String prop_aux = header.concat(clause);
		// print stm2 file and execute
		String fpath = "./"+folder+"/f_ccpair.smt2";
		FileHandler fh = new FileHandler();
		fh.print_file(prop_aux, fpath);
		
		String[] commands = {"./ffsm/z3","./"+folder+"/f_ccpair.smt2"};
		String result = fh.getProcessOutput(commands);		
		String[] outs = result.split("\n");
						
		if(outs[0].equals("sat")){			
			return true;						
		}else{
			return false;
		}		
	}
	
	public boolean check_combination_coverage(String header, FState fs, ArrayList<String> incheck) throws Exception{
		
		boolean combin = false;
		String clause = "";
		
		if(path_map.get(fs) == null) return false;
			
		//filter a subset of paths of the set
		ArrayList<ArrayList<FTransition>> sub_map = new ArrayList<ArrayList<FTransition>>();		
		for(String i : incheck){
			int ix = Integer.parseInt(i);
			sub_map.add(path_map.get(fs).get(ix));
		}
		
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+fs.getCondition()+")\n");
		clause = clause.concat("(assert (and \n");
		for(ArrayList<FTransition> path : sub_map){
			clause = clause.concat("    (not (and ");
			for(FTransition t : path){
				clause = clause.concat(t.getSource().getCondition()+" "
						+t.getCInput().getCond()+" ");
			}
			clause = clause.concat("))\n");
		}
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)\n");
	
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);					
			String[] outs = processZ3(prop_aux);
			if(outs[0].equals("unsat")){
				path_map.put(fs, sub_map);
				combin = true;		
			}
		}
		return combin;
	}
	
public FTransition check_product_coverage_transition(String header) throws Exception{
		
		String clause = "";
		
		for(FTransition s: transition_map.keySet()){
			if(transition_map.get(s) != null){
				clause = clause.concat("(push)\n");
				String t_cond = "(and "+s.getSource().getCondition()+" "+s.getCInput().getCond()+
						" "+s.getTarget().getCondition()+")";
				clause = clause.concat("(assert "+t_cond+")\n");
				clause = clause.concat("(assert (and \n");
				for(ArrayList<FTransition> path : transition_map.get(s)){
					clause = clause.concat("    (not (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
				}
				clause = clause.concat("))\n");
				clause = clause.concat("(check-sat)\n");
				clause = clause.concat("(pop)\n");
			}			
		}
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);			
			String[] outs = processZ3(prop_aux);
			int count = 0;
			for(FTransition s: transition_map.keySet()){
				if(transition_map.get(s) != null){
					if(count >= outs.length){
						return s;
					}
					if(!outs[count].equals("unsat")){	
						return s;						
					}
					count++;
				}
			}
		}
		return null;
	}
	
	public FState check_product_coverage_old2(String header) throws IOException, InterruptedException{
		
		String clause = "";
		
		for(FState s: path_map.keySet()){
			if(path_map.get(s) != null){
				clause = clause.concat("(push)\n");
				clause = clause.concat("(assert "+s.getCondition()+")\n");
				clause = clause.concat("(assert (and \n");
				for(ArrayList<FTransition> path : path_map.get(s)){
					clause = clause.concat("    (not (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
				}
				clause = clause.concat("))\n");
				clause = clause.concat("(check-sat)\n");
				clause = clause.concat("(pop)\n");
			}			
		}
		if(!clause.equals("")){
			if(yak_mode){
				String prop_aux = header.concat(clause);
				String fpath = project_path+inner_path+".smt2";
				FileHandler fh = new FileHandler();
				fh.print_file(prop_aux, fpath);
				
				String z3_path = inner_path.substring(0,inner_path.lastIndexOf("/")+1);
				String[] commands = {project_path+z3_path+"z3",project_path+inner_path+".smt2"};
				String result = fh.getProcessOutput(commands);		
				String[] outs = result.split("\n");
				int count = 0;
				for(FState s: path_map.keySet()){
					if(path_map.get(s) != null){
						if(!outs[count].equals("unsat")){	
							return s;						
						}
						count++;
					}
				}
			}else{
				String prop_aux = header.concat(clause);
				// print stm2 file and execute
				String fpath = "./"+folder+"/f_cpath.smt2";
				FileHandler fh = new FileHandler();
				fh.print_file(prop_aux, fpath);
				
				String[] commands = {"./ffsm/z3","./"+folder+"/f_cpath.smt2"};
				String result = fh.getProcessOutput(commands);				
				String[] outs = result.split("\n");
				int count = 0;
				for(FState s: path_map.keySet()){
					if(path_map.get(s) != null){
						if(!outs[count].equals("unsat")){	
							return s;						
						}
						count++;
					}
				}
			}			
		}
		return null;
	}
	
	public boolean check_product_coverage_old(String header) throws IOException, InterruptedException{
		
		boolean init_con = true;
		for(FState s: path_map.keySet()){
			if(path_map.get(s) != null){
				String clause = "";
				clause = clause.concat("(assert "+s.getCondition()+")\n");
				clause = clause.concat("(assert (and \n");
				for(ArrayList<FTransition> path : path_map.get(s)){
					clause = clause.concat("    (not (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
				}
				clause = clause.concat("))\n");
				clause = clause.concat("(check-sat)");
				String prop_aux = header.concat(clause);
				// print stm2 file and execute
				String fpath = "./"+folder+"/f_cpath.smt2";
				FileHandler fh = new FileHandler();
				fh.print_file(prop_aux, fpath);
				
				String[] commands = {"./ffsm/z3","./"+folder+"/f_cpath.smt2"};
				String result = fh.getProcessOutput(commands);				
				String[] outs = result.split("\n");
								
				if(outs[0].equals("sat")){					
					if(islog) log = log.concat("Conditional state "+s +" cannot be reached by all products"+"\n");
					System.out.println("Conditional state "+s +" cannot be reached by all products"+"\n");
					return false;						
				}else{					
					if(islog) log = log.concat("Conditional state "+s +" OK"+"\n");
				}
			}			
		}
		return init_con;
	}
	
	public boolean check_valid_path(String header, ArrayList<FTransition> path) throws Exception{
		
		boolean valid = true;
		String clause = "";		
		if(path.size() <= 0) return false;
		FState target = path.get(path.size()-1).getTarget();
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+target.getCondition()+")\n");
		clause = clause.concat("(assert (and ");
		for(FTransition t : path){
			clause = clause.concat(t.getSource().getCondition()+" "
					+t.getCInput().getCond()+" ");
		}
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n");					
	
		if(!clause.equals("")){
			String prop_aux = header.concat(clause);						
			String[] outs = processZ3(prop_aux);
			if(outs[0].equals("unsat")){
				path_map.get(target).remove(path);
				valid = false;
			}
		}		
		return valid;
	}
	
	public boolean check_valid_path_old(String header, ArrayList<FTransition> path) throws IOException, InterruptedException{
		
		boolean valid = true;
		String clause = "";		
		if(path.size() <= 0) return false;
		FState target = path.get(path.size()-1).getTarget();
		clause = clause.concat("(push)\n");
		clause = clause.concat("(assert "+target.getCondition()+")\n");
		clause = clause.concat("(assert (and ");
		for(FTransition t : path){
			clause = clause.concat(t.getSource().getCondition()+" "
					+t.getCInput().getCond()+" ");
		}
		clause = clause.concat("))\n");
		clause = clause.concat("(check-sat)\n");
		clause = clause.concat("(pop)\n");					
	
		if(!clause.equals("")){
			if(yak_mode){
				String prop_aux = header.concat(clause);
				String fpath = project_path+inner_path+".smt2";
				FileHandler fh = new FileHandler();
				fh.print_file(prop_aux, fpath);
				
				//System.out.println(prop_aux);
				
				String z3_path = inner_path.substring(0,inner_path.lastIndexOf("/")+1);
				String[] commands = {project_path+z3_path+"z3",project_path+inner_path+".smt2"};
				String result = fh.getProcessOutput(commands);		
				String[] outs = result.split("\n");
				if(result.length() > 0 && outs[0].equals("sat")){
					return true;
				}
			}else{
				String prop_aux = header.concat(clause);
				// print stm2 file and execute
				String fpath = "./"+folder+"/f_vpath.smt2";
				FileHandler fh = new FileHandler();
				fh.print_file(prop_aux, fpath);
				
				String[] commands = {"./ffsm/z3","./"+folder+"/f_vpath.smt2"};
				String result = fh.getProcessOutput(commands);					
				String[] outs = result.split("\n");
				if(outs[0].equals("unsat")){
					path_map.get(target).remove(path);
					valid = false;
				}
			}
		}		
		return valid;
	}
	
	public boolean check_valid_paths(String header) throws Exception{
	
		String clause = "";
		
		for(FState s: path_map.keySet()){	
			if(path_map.get(s) != null){				
				for(ArrayList<FTransition> path : path_map.get(s)){					
					clause = clause.concat("(push)\n");
					clause = clause.concat("(assert "+s.getCondition()+")\n");
					clause = clause.concat("(assert (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
					clause = clause.concat("(check-sat)\n");
					clause = clause.concat("(pop)\n");					
				}				
			}			
		}
		if(!clause.equals("")){
			if(yak_mode){
				String prop_aux = header.concat(clause);
				String[] outs = processZ3(prop_aux);
				int count = 0;
				for(FState s: path_map.keySet()){	
					if(path_map.get(s) != null){
						ArrayList<ArrayList<FTransition>> aux_paths = (ArrayList<ArrayList<FTransition>>) path_map.get(s).clone();
						for(ArrayList<FTransition> path : aux_paths){					
							if(!outs[count].equals("sat")){
								path_map.get(s).remove(path);								
							}
							count++;
						}
						if(path_map.get(s).size() < 1){
							return false;
						}
					}
				}
			}else{
				String prop_aux = header.concat(clause);						
				String[] outs = processZ3(prop_aux);
				int count = 0;
				for(FState s: path_map.keySet()){	
					if(path_map.get(s) != null){
						ArrayList<ArrayList<FTransition>> aux_paths = (ArrayList<ArrayList<FTransition>>) path_map.get(s).clone();
						for(ArrayList<FTransition> path : aux_paths){					
							if(!outs[count].equals("sat")){
								path_map.get(s).remove(path);								
							}
							count++;
						}
						if(path_map.get(s).size() < 1){
							return false;
						}
					}
				}
			}			
		}		
		return true;
	}
	
	public boolean check_valid_paths_old(String header) throws IOException, InterruptedException{
		
		boolean init_con = true;
		for(FState s: path_map.keySet()){	
			if(path_map.get(s) != null){
				ArrayList<ArrayList<FTransition>> aux_paths = (ArrayList<ArrayList<FTransition>>) path_map.get(s).clone();
				for(ArrayList<FTransition> path : aux_paths){
					String clause = "";
					clause = clause.concat("(assert "+s.getCondition()+")\n");
					clause = clause.concat("(assert (and ");
					for(FTransition t : path){
						clause = clause.concat(t.getSource().getCondition()+" "
								+t.getCInput().getCond()+" ");
					}
					clause = clause.concat("))\n");
					clause = clause.concat("(check-sat)");
					String prop_aux = header.concat(clause);
					// print stm2 file and execute
					String fpath = "./"+folder+"/f_vpath.smt2";
					FileHandler fh = new FileHandler();
					fh.print_file(prop_aux, fpath);
					
					String[] commands = {"./ffsm/z3","./"+folder+"/f_vpath.smt2"};
					String result = fh.getProcessOutput(commands);					
					String[] outs = result.split("\n");
									
					if(outs[0].equals("unsat")){
						path_map.get(s).remove(path);								
					}	
				}
				if(path_map.get(s).size() < 1){
					return false;
				}
			}			
		}		
		return init_con;
	}
	
	public void find_all_paths() throws Exception{
	
		path_map = new HashMap<FState,ArrayList<ArrayList<FTransition>>>();
		no_loop_ft = (ArrayList<FTransition>) ffsm.getFTransitions().clone();
		for(FTransition ft : ffsm.getFTransitions()){
			if(ft.getSource().equals(ft.getTarget())){
				no_loop_ft.remove(ft);
			}
		}
					
		FState current = ffsm.getFInitialState();
		if(current == null || current.getOut() == null || current.getOut().size() <= 0) return;
		ArrayList<FTransition> current_out = current.getOut();
					
		found_fc = new ArrayList<FState>();
		nfound_fc = (ArrayList<FState>) ffsm.getFStates().clone();		
		nfound_fc.remove(current);
		found_fc.add(current);
		
		for(FState s : nfound_fc){
			path_map.put(s, new ArrayList<ArrayList<FTransition>>());
		}			
		//process first cycle c. state
		// need to order transitions, check constraint equivalence, and include only relevant to cover...
		for(FTransition ct : current_out){
			if(no_loop_ft.contains(ct)){
				if(!found_fc.contains(ct.getTarget())){
					nfound_fc.remove(ct.getTarget());
					found_fc.add(ct.getTarget());
				}					
				ArrayList<FTransition> current_path = new ArrayList<FTransition>();
				current_path.add(ct);					
				ArrayList<ArrayList<FTransition>> c_paths;
				if(path_map.keySet().contains(ct.getTarget())){
					c_paths = path_map.get(ct.getTarget());
				}else c_paths = new ArrayList<ArrayList<FTransition>>();					
				c_paths.add(current_path);
				path_map.put(ct.getTarget(), c_paths);
				//no needed as we check for transitions
				//check_valid_path(prop, current_path);
			}				
		}	
		//recursive calls until n-1 (number of states)
		//or there is no transition that find new cond. states
		ArrayList<FState> found_aux = (ArrayList<FState>) found_fc.clone();
		for(FState cs : found_aux){
			if(!cs.equals(ffsm.getFInitialState())){
				rec_find_path(cs);
			}				
		}
	}
	
	public void rec_find_path(FState current) throws Exception{
		System.out.println("RECURSIVE CALL "+current);
		ArrayList<FTransition> current_out = current.getOut();		
		for(FTransition ct : current_out){
			if(no_loop_ft.contains(ct) && ct.getTarget() != ffsm.getFInitialState()){				
				ArrayList<ArrayList<FTransition>> c_paths = path_map.get(ct.getTarget());
				ArrayList<ArrayList<FTransition>> lc_paths = path_map.get(current);	
				if(check_path_coverage(ct.getTarget(), path_map.get(ct.getTarget()))){
					if(!covered_fc.contains(ct.getTarget())){
						covered_fc.add(ct.getTarget());
					}
					continue;
				}
				boolean contribute = false;
				prepath: for(ArrayList<FTransition> inc_path : lc_paths){					
					//if this c. state was found before in the previous cycle (avoid loops)
					for(FTransition c : inc_path){
						if(c.getTarget().equals(ct.getTarget())){
							continue prepath;
						}
					}
					/*boolean exist = false;
					for(FTransition p : inc_path){								
						if(ct.getTarget().equals(p.getSource()) || ct.getTarget().equals(p.getTarget())){
							exist = true;
							break;
						}
					}*/
					
					FState last = inc_path.get(inc_path.size()-1).getSource();
					ArrayList<FTransition> new_path = new ArrayList<FTransition>();
					if(!last.equals(ct.getTarget()) && c_paths != null){						
						new_path.addAll(inc_path);
						new_path.add(ct);
						if(!c_paths.contains(new_path) && check_valid_path(prop, new_path)){
							c_paths.add(new_path);
							contribute = true;
						}						
					}
				}				
				//update paths
				path_map.put(ct.getTarget(), c_paths);
				if(contribute){
					rec_find_path(ct.getTarget());
				}
			}			
		}
		//find more
		/*for(FTransition ct : current_out){			
			if(no_loop_ft.contains(ct) && ct.getTarget() != ffsm.getFInitialState()){				
				if(!found_fc.contains(ct.getTarget())){
					nfound_fc.remove(ct.getTarget());
					found_fc.add(ct.getTarget());
					//if(!covered_fc.contains(ct.getTarget())){
						rec_find_path(ct.getTarget());
					//}	
				}else for(ArrayList<FTransition> path : path_map.get(ct.getTarget())){
					if(path.size() < ffsm.getNumberOfFStates()-1){						
						for(FTransition ct1 : ct.getTarget().getOut()){	
							boolean choose = true;
							for(FTransition p : path){								
								if(ct1.getTarget().equals(p.getSource()) || ct1.getTarget().equals(p.getTarget())){
									choose = false;
									break;
								}
							}
							if(choose && !covered_fc.contains(ct1.getTarget())){
								System.out.println("RECURSIVE INNER CALL "+path);
								rec_find_path(ct.getTarget());
							}
						}
					}
				}
			}			
		}*/
	}	
	
	public void find_all_paths_old() throws Exception{
		no_loop_ft = (ArrayList<FTransition>) ffsm.getFTransitions().clone();
		for(FTransition ft : ffsm.getFTransitions()){
			if(ft.getSource().equals(ft.getTarget())){
				no_loop_ft.remove(ft);
			}
		}
					
		FState current = ffsm.getFInitialState();
		ArrayList<FTransition> current_out = current.getOut();
					
		found_fc = new ArrayList<FState>();
		nfound_fc = (ArrayList<FState>) ffsm.getFStates().clone();		
		nfound_fc.remove(current);
		found_fc.add(current);
					
		path_map = new HashMap<FState,ArrayList<ArrayList<FTransition>>>();
		
		for(FState s : nfound_fc){
			path_map.put(s, new ArrayList<ArrayList<FTransition>>());
		}			
		//process first cycle c. state
		// need to order transitions, check constraint equivalence, and include only relevant to cover...
		for(FTransition ct : current_out){
			if(no_loop_ft.contains(ct)){
				if(!found_fc.contains(ct.getTarget())){
					nfound_fc.remove(ct.getTarget());
					found_fc.add(ct.getTarget());
				}					
				ArrayList<FTransition> current_path = new ArrayList<FTransition>();
				current_path.add(ct);					
				ArrayList<ArrayList<FTransition>> c_paths = new ArrayList<ArrayList<FTransition>>();					
				c_paths.add(current_path);
				path_map.put(ct.getTarget(), c_paths);
				//no needed as we check for transitions
				//check_valid_path(prop, current_path);
			}				
		}	
		//recursive calls until n-1 (number of states)
		//or there is no transition that find new cond. states
		ArrayList<FState> found_aux = (ArrayList<FState>) found_fc.clone();
		for(FState cs : found_aux){
			if(!cs.equals(ffsm.getFInitialState())){
				rec_find_path(cs);
			}				
		}
	}
	
	public void rec_find_path_old2(FState current) throws Exception{
		ArrayList<FTransition> current_out = current.getOut();		
		for(FTransition ct : current_out){
			if(no_loop_ft.contains(ct) && ct.getTarget() != ffsm.getFInitialState()){				
				ArrayList<ArrayList<FTransition>> c_paths = path_map.get(ct.getTarget());
				ArrayList<ArrayList<FTransition>> lc_paths = path_map.get(current);	
				if(check_path_coverage(ct.getTarget(), path_map.get(ct.getTarget()))){
					if(!covered_fc.contains(ct.getTarget())){
						covered_fc.add(ct.getTarget());
					}
					continue;
				}
				prepath: for(ArrayList<FTransition> inc_path : lc_paths){					
					//if this c. state was found before in the previous cycle (avoid loops)
					for(FTransition c : inc_path){
						if(c.getTarget().equals(ct.getTarget())){
							continue prepath;
						}
					}
					FState last = inc_path.get(inc_path.size()-1).getSource();
					ArrayList<FTransition> new_path = new ArrayList<FTransition>();
					if(!last.equals(ct.getTarget()) && c_paths != null){						
						new_path.addAll(inc_path);
						new_path.add(ct);
						if(!c_paths.contains(new_path) && check_valid_path(prop, new_path)){
							c_paths.add(new_path);							
						}						
					}	
				}				
				path_map.put(ct.getTarget(), c_paths);
				//check_valid_path(prop, new_path);
				if(!found_fc.contains(ct.getTarget())){
					nfound_fc.remove(ct.getTarget());
					found_fc.add(ct.getTarget());
					if(!covered_fc.contains(ct.getTarget())){
						rec_find_path(ct.getTarget());
					}
				}				
			}			
		}		
	}	
	
	public void rec_find_path_old(FState current) throws Exception{
		ArrayList<FTransition> current_out = current.getOut();		
		for(FTransition ct : current_out){
			if(no_loop_ft.contains(ct)){				
				ArrayList<ArrayList<FTransition>> c_paths = path_map.get(ct.getTarget());
				ArrayList<ArrayList<FTransition>> lc_paths = path_map.get(current);				
				prepath: for(ArrayList<FTransition> inc_path : lc_paths){					
					//if this c. state was found before in the previous cycle (avoid loops)
					for(FTransition c : inc_path){
						if(c.getTarget().equals(ct.getTarget())){
							continue prepath;
						}
					}					
					FState last = inc_path.get(inc_path.size()-1).getSource();
					ArrayList<FTransition> new_path = new ArrayList<FTransition>();
					if(!last.equals(ct.getTarget()) && c_paths != null){						
						new_path.addAll(inc_path);
						new_path.add(ct);
						if(!c_paths.contains(new_path) && check_valid_path(prop, new_path)){
							c_paths.add(new_path);							
						}						
					}					
				}				
				path_map.put(ct.getTarget(), c_paths);
				//check_valid_path(prop, new_path);
				if(!found_fc.contains(ct.getTarget())){
					nfound_fc.remove(ct.getTarget());
					found_fc.add(ct.getTarget());
					rec_find_path(ct.getTarget());
				}				
			}			
		}		
	}	
	
	public void print_transition_table(Map<FTransition,ArrayList<Cond_in_seq>> transition_table){
		if(islog) log = log.concat("\n Printing conditional transition paths"+"\n");	
		if(debug) System.out.println("\n Printing conditional transition paths");
		for(FTransition ft: transition_table.keySet()){			
			if(islog) log = log.concat("Conditional transition "+ft+"\n");
			if(debug) System.out.println("Conditional transition "+ft);
			int count = 0;
			if(transition_table.get(ft) != null){
				for(Cond_in_seq cin : transition_table.get(ft)){
					count++;
					if(islog) log = log.concat("Cond. input seq. "+count+": "+cin+"\n");
					if(debug) System.out.println("Cond. input seq. "+count+": "+cin);
				}
			}			
		}
	}
	
	public void print_transition_paths(Map<FTransition,ArrayList<ArrayList<FTransition>>> transition_map){
		if(islog) log = log.concat("\n Printing conditional transition paths"+"\n");	
		if(debug) System.out.println("\n Printing conditional transition paths");
		for(FTransition ft: transition_map.keySet()){			
			if(islog) log = log.concat("Conditional transition "+ft+"\n");
			if(debug) System.out.println("Conditional transition "+ft);
			int count = 0;
			if(transition_map.get(ft) != null){
				for(ArrayList<FTransition> path : transition_map.get(ft)){
					count++;
					if(islog) log = log.concat("Path "+count+": "+path+"\n");
					if(debug) System.out.println("Path "+count+": "+path);
				}
			}			
		}
	}
	
	public void print_paths(Map<FState,ArrayList<ArrayList<FTransition>>> path_map){
		if(islog) log = log.concat("\n Printing conditional state paths"+"\n");	
		if(debug) System.out.println("\n Printing conditional state paths");
		for(FState s: path_map.keySet()){			
			if(islog) log = log.concat("Conditional State "+s+"\n");
			if(debug) System.out.println("Conditional State "+s);
			int count = 0;
			if(path_map.get(s) != null){
				for(ArrayList<FTransition> path : path_map.get(s)){
					count++;
					if(islog) log = log.concat("Path "+count+": "+path+"\n");
					if(debug) System.out.println("Path "+count+": "+path);
				}
			}			
		}
	}
	
	public void print_common_pairs(FState fs1, FState fs2){				
		if(islog) log = log.concat("Conditional State pair "+ fs1 + " and " + fs2+"\n");	
		if(debug) System.out.println("Conditional State pair "+ fs1 + " and " + fs2);
		for(String in : ffsm.getInputAlphabet()){			
			if(islog) log = log.concat("  Input "+in+"\n");
			if(debug) System.out.println("  Input "+in);
			ArrayList<CommonPath> caux = seq_map.get(in);
			for(CommonPath cp : caux){				
				if(islog) log = log.concat("     Pair "+cp.get1()+ " "+ cp.get2()+"\n");
				if(debug) System.out.println("     Pair "+cp.get1()+ " "+ cp.get2());
			}
		}
	}

	
}
