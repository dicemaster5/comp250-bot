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
    UnitType heavyType;

    
    Unit base = null;
    Unit green = null;
    
    public int attackerWorker = 1;
    public int defenderWorker = 1;
    public int attackerAmount = 10;
    public int defenderAmount = 4;
    public int barracksAmount = 1;
    
    // Strategy implemented by this class:
    //Just try to do anything
    
    public BoBot(UnitTypeTable utt) {
        super(new AStarPathFinding());
        this.utt = utt;
        worker = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        heavyType = utt.getUnitType("Heavy");
    }
    

    @Override
    public void reset() {

    }

    @Override
    public AI clone() {
        return new BoBot(utt);
    }
   
//================================================= MAIN LOOP ==================================================//   
    @Override
    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        List<Unit> resources = new LinkedList<Unit>();
        List<Unit> attackWorkers = new LinkedList<Unit>();
        List<Unit> defendWorkers = new LinkedList<Unit>();
        List<Unit> defenders = new LinkedList<Unit>();
        List<Unit> attackers = new LinkedList<Unit>();
        
        List<Unit> bases = new LinkedList<Unit>();
        List<Unit> barracks = new LinkedList<Unit>();
        
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
        		// Get the main base
        		if(unit.getType() == baseType)
        		{
        			//bases.add(unit);
        			base = unit;
        			train(base, worker);
        		}
        		
        		// Get the main barracks
        		if(unit.getType() == barracksType)
        		{
        			green = unit;
        			train(green, heavyType);

        		}
        		
        		// Find all workers
    	        if (unit.getType() == worker)
    	        {
    	        	// Add workers to the worker list
    	        	if(attackWorkers.size() < attackerWorker)
    	        	{
    	        		attackWorkers.add(unit);
    	        	}
    	        	
    	        	else if(defendWorkers.size() < defenderWorker)
    	        	{
    	        		defendWorkers.add(unit);
    	        	}
    	        	
    	        	else
    	        	{
    	        		attackers.add(unit);
    	        	}
    	        }
    	        
    	        // Add heavy units to attackers list
    	        if(unit.getType() == heavyType)
    	        {
    	        	attackers.add(unit);
    	        }
    	        
    	        // Make workers harvest closest resource
    	        for (Unit w : attackWorkers)
    	        {
    	        	findResourceToHarvest(w, resources, base);
    	        }
    	        
    	        // Make attackers attack closest enemy
    	        for (Unit a : attackers)
    	        {
    	        	findEnemyToAttack(a, ennemies);
    	        }
    	        
    	        // 
    	        for (Unit d : defendWorkers)
    	        {
    	        	
    	        	if(gs.getPlayer(player).getResources() > 5)
    	        	{
    	        		build(d, barracksType, 5, 1);
    	        		
    	        	}
    	        	else if(barracks.size() == 1)
    	        	{
    	        		findResourceToHarvest(d, resources, barracks.get(0));
    	        	}
    	        	
    	        	else
    	        	{
    	        		findResourceToHarvest(d, resources, base);
    	        	}
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
    
//============================================== FUNCTIONS TO USE ================================================//
    
    // Finds the closest Enemy to attack
    public void findEnemyToAttack(Unit u, List<Unit> e)
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
    
    // Finds the closest resource to harvest
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
