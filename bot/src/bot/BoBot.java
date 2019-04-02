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
    	        	findResourceToHarvest(w, resources, base);
    	        }
    	        
    	        for (Unit a : attackers)
    	        {
    	        	findEnnemyToAttack(a, ennemies);
    	        	
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
    
    // Finds the closest Enemy to attack
    public void findEnnemyToAttack(Unit u, List<Unit> e)
    {
    	 Unit closestEnemy = null;
    	 int closestDistance = 0;
         for(Unit e2: e) {
             int d = Math.abs(e2.getX() - u.getX()) + Math.abs(e2.getY() - u.getY());
             if (closestEnemy==null || d<closestDistance) 
             {
                 closestEnemy = e2;
                 closestDistance = d;
             }
             if (closestEnemy!=null)
             {
                 attack(u,closestEnemy);
             }
         }
    }
    
    public void findResourceToHarvest(Unit u, List<Unit> r, Unit b)
    {
    	 Unit closestResource = null;
    	 int closestDistance = 0;
         for(Unit r2: r) {
             int d = Math.abs(r2.getX() - u.getX()) + Math.abs(r2.getY() - u.getY());
             if (closestResource==null || d<closestDistance) 
             {
            	 closestResource = r2;
                 closestDistance = d;
             }
             if (closestResource!=null)
             {
            	 harvest(u,closestResource, b);
             }
         }
    }
    
    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
    }
}
