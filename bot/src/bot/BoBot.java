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
import java.util.Random;

import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/*
 * @author Sam Auber!
 */

// Strategy implemented by this class:
// Step 1: check the size of the map to determine the strategy to take
// Step 2: Get all the player units and sort them out
// Step 3: Apply behaviour to each unit in the map
// Step 4: Profit?

public class BoBot extends AbstractionLayerAI {    
    private UnitTypeTable utt;
    
    // Buildings
    private UnitType baseType;
    private UnitType barracksType;
    
    // Units
    private UnitType worker;
    private UnitType rangedType;
    private UnitType heavyType;
    private UnitType lightType;
    
    Unit base = null;
    Unit enemyBase = null;
    Unit green = null;
    
    public int attackerWorker = 1;
    public int defenderWorker = 1;
    public int attackerAmount = 10;
    public int defenderAmount = 4;
    public int barracksAmount = 1;
   
    // Map size
    public int LARGE_MAP = 3;
    public int MEDIUM_MAP = 2;
    public int SMALL_MAP = 1;
   
    int CurrentMapSize = 0; 
    
    public BoBot(UnitTypeTable utt) {
        super(new AStarPathFinding());
        this.utt = utt;
        
        // Apply each unit type
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        
        worker = utt.getUnitType("Worker");
        heavyType = utt.getUnitType("Heavy");
        rangedType = utt.getUnitType("Ranged");
        lightType = utt.getUnitType("Light");
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
        
        // Unit Lists
        
        // Resources list
        List<Unit> resources = new LinkedList<Unit>();
        
        // Buildings Lists
        List<Unit> bases = new LinkedList<Unit>();
        List<Unit> barracks = new LinkedList<Unit>();
        
        // special units lists
        List<Unit> workers = new LinkedList<Unit>();
        List<Unit> ranged = new LinkedList<Unit>();
        List<Unit> heavy = new LinkedList<Unit>();
        List<Unit> light = new LinkedList<Unit>();

        List<Unit> resourceWorkers = new LinkedList<Unit>();
        //List<Unit> attackWorkers = new LinkedList<Unit>();
        List<Unit> barrackWorkers = new LinkedList<Unit>();
        List<Unit> defenders = new LinkedList<Unit>();
        List<Unit> attackers = new LinkedList<Unit>();
        
        // Enemy Lists
        List<Unit> ennemies = new LinkedList<Unit>();
        List<Unit> enemyBases = new LinkedList<Unit>();    
        
        //The player
        Player p = gs.getPlayer(player);  
        
        // get and set map size
        getAndSetMapSize(pgs);
        
        // Do basic setup
        for (Unit unit : pgs.getUnits()) 
        {
        	// Find all the resources and add them to the list =====================================================================================================
    		if(unit.getType().isResource && !resources.contains(unit))
    		{
    			resources.add(unit);
    		}
        	
        	// Find the enemies and add them to the relevant lists =================================================================================================
    		if(unit.getPlayer() != player)
        	{
    			if(unit.getType() == baseType && !enemyBases.contains(unit))
        		{
        			enemyBases.add(unit);
        		}
    			else if(!ennemies.contains(unit) && !unit.getType().isResource)
    			{
    				ennemies.add(unit);
    			}
        	}
    		
        	// Find player units and add them to the relevant lists =================================================================================================
    		if(unit.getPlayer() == player)
        	{
    			if(unit.getType() == baseType && !bases.contains(unit))
        		{
    				bases.add(unit);
        		}
    			if(unit.getType() == barracksType && !barracks.contains(unit))
        		{
    				barracks.add(unit);
        		}
    			
    			if(unit.getType() == worker && !workers.contains(unit) && !resourceWorkers.contains(unit) && !barrackWorkers.contains(unit))
        		{
    				workers.add(unit);
        		}
    			if(unit.getType() == rangedType && !ranged.contains(unit))
        		{
    				ranged.add(unit);
        		}
    			if(unit.getType() == heavyType && !heavy.contains(unit))
        		{
    				heavy.add(unit);
        		}
    			if(unit.getType() == lightType && !light.contains(unit))
        		{
    				light.add(unit);
        		}
        	}
        }

		
		// Base Strategy =============================================================================================================
		if(bases.size() != 0)
		{
        	if(resourceWorkers.size() != bases.size())
			{
        		if(workers.size() > 0)
        		{
        			resourceWorkers.add(workers.remove(0));
        		}
	    		for(Unit base:bases)
	    		{
	    			train(base, worker);
	    		}
			}
			
			else
			{
	    		for(Unit base:bases)
	    		{
	    			if(CheckUnitsAround(pgs, base) < 2)
	    			{
	    				train(base, worker);
	    				if(workers.size() > 0 && barrackWorkers.size() < 1)
	    					barrackWorkers.add(workers.remove(0));

	    			}
	    			else
	    			{
	    				if(workers.size() > 0)
	    					barrackWorkers.add(workers.remove(0));
	    			}
	    		}
			}
			
			for(Unit b:barracks)
			{
				train(b, rangedType);
			}
			
			// ====================================================== BEAHVIOURS AND ACTIONS ========================================================================
			
			// WORKERS =======================================================================================================
			
			// RESOURCE WORKERS =================================================
			for(Unit w:resourceWorkers)
			{
				if(resources.size() > 0)
					findResourceToHarvest(w, resources, findClosestBase(w,bases));
				else
				{
					workers.add(resourceWorkers.remove(0));
				}
			}
			
			// BARRACKS WORKERS =================================================
			for(Unit b:barrackWorkers)
			{
	        	if(p.getResources() > 8)
	        	{
	        		Unit base = bases.get(0);
	        		build(b, barracksType, base.getX() + 1, base.getY() - 1);
	        		
	        	}
	        	else if(barracks.size() >= 1)
	        	{
	        		findResourceToHarvest(b, resources, findClosestBase(b,bases));
	        	}
	        	
	        	else
	        	{	
	        		if(CurrentMapSize == MEDIUM_MAP)
	        			findResourceToHarvest(b, resources, findClosestBase(b,bases));
	        	}
			}	
			
			// ATTACKERS ==========================================================================================================
			if(workers.size() > 0)
				attackers = workers;
			for (Unit a:attackers)
			{
				if(ennemies.size() > 0)
	    			findEnemyToAttack(a, ennemies);
	    		else if(resources.size() > 0 && a.getType().canHarvest && enemyBases.get(0).getResources() > 0)
	   				findResourceToHarvest(a, resources, findClosestBase(a,bases));
	    		else
	    			findEnemyToAttack(a, enemyBases);
			}
			
			// Special UNITS ========================================================
			for (Unit r:ranged)
			{
				if(ennemies.size() > 0)
	    			findEnemyToAttack(r, ennemies);
	    		else
	   				findEnemyToAttack(r, enemyBases);
			}
			
			for (Unit r:heavy)
			{
				if(ennemies.size() > 0)
	    			findEnemyToAttack(r, ennemies);
	    		else
	   				findEnemyToAttack(r, enemyBases);
			}
			
			for (Unit r:light)
			{
				if(ennemies.size() > 0)
	    			findEnemyToAttack(r, ennemies);
	    		else
	   				findEnemyToAttack(r, enemyBases);
			}
			
	        
			
	        // Time for the special strategy
	        if(CurrentMapSize == SMALL_MAP)
	        {
	        	//Do worker rush strategy
	        }
	        else if(CurrentMapSize == MEDIUM_MAP)
	        {
	        	//Do LightType rush strategy
	        }
	        else if(CurrentMapSize == LARGE_MAP)
	        {
	        	//Do Ranged and light rush strategy
	        }
    	}
		else
		{
			//MAKE BASES IF POSSIBLE
		}
        
        return translateActions(player, gs);
    }

//============================================== FUNCTIONS TO USE ================================================//

    // Get map size and do it once
    public void getAndSetMapSize(PhysicalGameState pgs)
    {
    	boolean doOnce = false;
    	if(!doOnce)
    	{
	        if(pgs.getHeight() < 13)
	        {
	        	CurrentMapSize = SMALL_MAP;
	        }
	        if(pgs.getHeight() >= 13 && pgs.getHeight() <= 18)
	        {
	        	CurrentMapSize = MEDIUM_MAP;
	        }
	        if(pgs.getHeight() > 18)
	        {
	        	CurrentMapSize = LARGE_MAP;
	        }
	        doOnce = true;
    	}
    }
    
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
    
    public boolean canAttackEnemy(Unit u, List<Unit> e, GameState gs)
    {
    	 Unit closestEnemy = null;
    	 int closestDistance = 0;
         for(Unit e2: e) 
         {
             int d = Math.abs(e2.getX() - u.getX()) + Math.abs(e2.getY() - u.getY());
             if (closestEnemy==null || d<closestDistance) 
             {
                 closestEnemy = e2;
                 closestDistance = d;
             }
         }
         if (closestEnemy!=null && u.getType().canAttack)
         {
             return true;
         }
         else
         {
        	 return false;
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
    
    // Finds the closest base
    public Unit findClosestBase(Unit u, List<Unit> r)
    {
    	 Unit closestBase = null;
    	 int closestDistance = 0;
         for(Unit r2: r) {
             int d = Math.abs(r2.getX() - u.getX()) + Math.abs(r2.getY() - u.getY());
             if (closestBase==null || d<closestDistance) 
             {
            	 closestBase = r2;
                 closestDistance = d;
             }
         }
         
         if (closestBase!=null)
         {
        	 return closestBase;
         }
         else
        	 return null;
    }
    
    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
    }
    
    public int CheckUnitsAround(PhysicalGameState pgs , Unit base)
    {
    	int basePosX = base.getX();
    	int basePosY = base.getY();
    	
    	int amountOfUnitsAround = 0;
    	
    	if(pgs.getUnitAt(basePosX + 1, basePosY) != null)
    		amountOfUnitsAround++;
    	if(pgs.getUnitAt(basePosX - 1, basePosY) != null)
    		amountOfUnitsAround++;
    	if(pgs.getUnitAt(basePosX, basePosY + 1) != null)
    		amountOfUnitsAround++;
    	if(pgs.getUnitAt(basePosX, basePosY - 1) != null)
    		amountOfUnitsAround++;

    	return amountOfUnitsAround;
    	
    	
		//gs.free(x, y);
		//pgs.getUnitAt(x, y);
		//base.getPosition(pgs);
    }
}



/* //============================================== DEAD CODE ==================================================================//      
if(CurrentMapSize == 0)
{
	for (Unit unit : pgs.getUnits()) 
    {
		if(unit.getPlayer() != player)
    	{
			if(unit.getType() == baseType)
    		{
    			enemyBases.add(unit);
    		}
    	}
		
        // TODO: issue commands to units
    	
		if(unit.getType().isResource)
		{
			green = unit;
			resources.add(unit);
		}
    	
    	if(unit.getPlayer() == player)
    	{
    		// Get the main bases
    		if(unit.getType() == baseType && !bases.contains(unit))
    		{
    			bases.add(unit);
    			//base = unit;
    		}
    		
			for(Unit b:bases)
			{
				train(b, worker);
			}
    		
    		
    		// Find all workers
	        if (unit.getType() == worker)
	        {
	        	// Add workers to the worker list
	        	if(attackWorkers.size() < attackerWorker)
	        	{
	        		attackWorkers.add(unit);
	        	}
	        	else
	        	{
	        		attackers.add(unit);
	        	}
	        }
	        
	        // Make workers harvest closest resource
	        for (Unit w : attackWorkers)
	        {
	        	if(!bases.isEmpty())
	        		findResourceToHarvest(w, resources, findClosestBase(w,bases));
	        }
	        
	        // Make attackers attack closest enemy
	        for (Unit a : attackers)
	        {
	        	findEnemyToAttack(a, ennemies);
	        }
	        
    	}
    	else if(unit.getPlayer() != player && !unit.getType().isResource)
    	{
    		if(!ennemies.contains(unit))
    			ennemies.add(unit);
    	}
    }
}
//=====================================================================================//
else if (CurrentMapSize == 0 || CurrentMapSize == 0)
{
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
    			//train(green, rangedType);
    			//train(green, heavyType);
    			train(green, lightType);
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
	        
	        // Add special units to attackers list
	        if(unit.getType() == heavyType || unit.getType() == rangedType || unit.getType() == lightType)
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
	        	
	        	if(gs.getPlayer(player).getResources() > 8)
	        	{
	        		build(d, barracksType, base.getX() + 1, base.getY() + 1);
	        		
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
}   */
