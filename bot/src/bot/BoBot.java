package bot;

import ai.abstraction.AbstractionLayerAI;
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
import java.util.List;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/*
 * 
 * @author Sam 
 */

public class BoBot extends AbstractionLayerAI {    
    private UnitTypeTable utt;
    private UnitType worker;
    
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType rangedType;
    
    Unit resource = null;
    Unit base = null;
    
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
        
        for (Unit unit : pgs.getUnits()) {
            // TODO: issue commands to units
        	
    		if(unit.getType().isResource)
    		{
    			resource = unit;
    		}
        	
        	if(unit.getPlayer() == player)
        	{
        		if(unit.getType() == baseType)
        		{
        			base = unit;
        		}
        		
            	// Get the worker to harvest resources
        		else if(unit.getType().canHarvest)
            	{
            		harvest(unit, resource, base);
            	}
        	}
        }
        
        return translateActions(player, gs);
    }
    
    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
    }
}