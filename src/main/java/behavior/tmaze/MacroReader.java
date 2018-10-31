package behavior.tmaze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

public class MacroReader{

	public HashMap<String,State> getStateSet(URL url){
		HashMap<String,State> stateSet = new HashMap<String,State>();
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
      
	        String line;
			while((line = reader.readLine()) != null){
				if(line.startsWith("#")){ continue;}
				String[] tokens = line.split("\\s");
				if(tokens.length!=3){continue;}
				if(!stateSet.containsKey(tokens[0])){
					stateSet.put(tokens[0], new State(tokens[1],tokens[2]));
				}else{
					stateSet.get(tokens[0]).addTrigger(tokens[1],tokens[2]);
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return stateSet;
	}
}