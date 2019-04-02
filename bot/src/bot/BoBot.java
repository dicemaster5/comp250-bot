package bot;

import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.Harvest;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/*
 * @author Sam 
 */

public class BoBot extends AbstractionLayerAI {    
    private UnitTypeTable utt;
    private UnitType worker;
    
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType rangedType;
    
    Unit base = null;
    Unit green = null; 

    
    // Strategy implemented by this class:
    //Just try to do anything
    
    
    public BoBot(UnitTypeTable utt) {
        super(new AStarPathFinding());
        this.utt = utt;
        worker = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
    }
    

    @Override
    public void reset() {

    }

    
    @Override
    public AI clone() {
        return new BoBot(utt);
    }
   
    
    @Override
    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        List<Unit> resources = new LinkedList<Unit>();
        List<Unit> workers = new LinkedList<Unit>();
        List<Unit> defenders = new LinkedList<Unit>();
        List<Unit> attackers = new LinkedList<Unit>();
        
        List<Unit> ennemies = new LinkedList<Unit>();        
        for (Unit unit : pgs.getUnits()) 
        {
            // TODO: issue commands to units
        	
    		if(unit.getType().isResource)
    		{
    			green = unit;
    			resources.add(unit);
    		}
        	
        	if(unit.getPlayer() == player)
        	{
        		if(unit.getType() == baseType)
        		{
        			base = unit;
        			train(base, worker);
        		}
        		

    	        if (unit.getType() == worker)
    	        {
    	        	if(workers.size() < 2)
    	        	{
    	        		workers.add(unit);
    	        	}
    	        	else
    	        	{
    	        		attackers.add(unit);
    	        	}
    	        }
    	        
    	        for (Unit w : workers)
    	        {
        	        harvest(w, resources.get(0), base);
    	        }
    	        
    	        for (Unit a : attackers)
    	        {
    	        	attack(a, ennemies.get(ennemies.size() - 1));
    	        }
    	        
        	}
        	else if(unit.getPlayer() != player && !unit.getType().isResource)
        	{
        		if(!ennemies.contains(unit))
        			ennemies.add(unit);
        	}
        }
        
        return translateActions(player, gs);
    }
    
    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
    }
}
