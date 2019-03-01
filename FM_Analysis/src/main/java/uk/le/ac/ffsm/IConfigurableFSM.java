package uk.le.ac.ffsm;

import java.util.List;
import java.util.Map;

import org.prop4j.Node;

import net.automatalib.words.Alphabet;

public interface IConfigurableFSM<I,O> {
	
	public List<Node> getConfiguration();
	public void 	  setConfiguration(List<Node> configuration);
	
	public Map<I, List<SimplifiedTransition<I, O>>> getSimplifiedTransitions(Integer si, I input, O output, Integer sj);
	public Map<I, List<SimplifiedTransition<I, O>>> getSimplifiedTransitions(Integer si);
	
	public Alphabet<I> getInputAlphabet();

}
