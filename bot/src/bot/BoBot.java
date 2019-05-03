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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/*
 * @author Sam Auber
 */

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
   
    // Map sizes
    public int LARGE_MAP = 3;
    public int MEDIUM_MAP = 2;
    public int SMALL_MAP = 1;
   
    int CurrentMapSize = 0; 
    
    /**
     * @param utt UnitTypeTable
     */
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
    

    /* (non-Javadoc)
     * @see ai.abstraction.AbstractionLayerAI#reset()
     */
    @Override
    public void reset() {

    }

    /* (non-Javadoc)
     * @see ai.core.AI#clone()
     */
    @Override
    public AI clone() {
        return new BoBot(utt);
    }
   
//================================================= MAIN LOOP ==================================================//   
    /* (non-Javadoc)
     * @see ai.core.AI#getAction(int, rts.GameState)
     */
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
        
        Unit dangerEnemy = null;
        
        //worker Map
        Map<Unit, String> wokerRoles = new HashMap<Unit, String>();
        
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
    		if(unit.getPlayer() != player && unit.getPlayer() != -1)
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
    			// BASE TYPES
    			if(unit.getType() == baseType && !bases.contains(unit))
        		{
    				bases.add(unit);
        		}
    			if(unit.getType() == barracksType && !barracks.contains(unit))
        		{
    				barracks.add(unit);
        		}	
    			
    			// WORKERS    			
    			if(unit.getType() == worker)
        		{
    				if(resourceWorkers.size() < bases.size())
    					resourceWorkers.add(unit);
    				else if(barrackWorkers.size() < 1)
    					barrackWorkers.add(unit);
    				else
    					workers.add(unit);
        		}
    			
    			// SPECIAL UNITS
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
			for(Unit base:bases)
    		{
				// Train Workers
    			if(CheckUnitsAround(pgs, base) < 3)
    			{
    				train(base, worker);
    			}
    		}
			
			// Train Special Units
			for(Unit b:barracks)
			{
				if(light.size() < ranged.size())
				{
					train(b, lightType);
				}
				else
				{
					train(b, rangedType);
				}
				
			}
			
			// ====================================================== BEAHVIOURS AND ACTIONS ========================================================================
			
			// RESOURCE WORKERS =================================================
			dangerEnemy = CheckForEnemyNeaby(pgs, bases.get(0), ennemies);
			for(Unit w:resourceWorkers)
			{
				if(dangerEnemy != null)
					attack(w, dangerEnemy);
				else if(resources.size() > 0)
					findResourceToHarvest(w, resources, findClosestBase(w,bases));
				else
				{
					workers.add(w);
				}
			}
			
			// BARRACKS WORKERS =================================================
			for(Unit b:barrackWorkers)
			{
	        	if(p.getResources() >= 7)
	        	{
	        		Unit base = bases.get(0);
	        		// CHANGE THIS TO BE DYNAMIC !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! //
	        		build(b, barracksType, base.getX() + 1, base.getY() - 1);
	        		
	        	}
	        	else if(barracks.size() >= 1)
	        	{
	        		findResourceToHarvest(b, resources, findClosestBase(b,bases));
	        	}
	        	
	        	else
	        	{	
	        		if(CurrentMapSize == MEDIUM_MAP || CurrentMapSize == LARGE_MAP  )
	        			findResourceToHarvest(b, resources, findClosestBase(b,bases));
	        		else
	        			workers.add(b);
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
			
			for (Unit w:workers)
			{
				if(ennemies.size() > 0)
	    			findEnemyToAttack(w, ennemies);
	    		else
	   				findEnemyToAttack(w, enemyBases);
			}
			
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
		}
        
        return translateActions(player, gs);
    }

//============================================== FUNCTIONS TO USE ================================================//

    /** Check Get and set map size
     * @param pgs Use the PhysicalGameState to find the size of the map
     * and then set the CurrentMapSize var to the spcefied size
     */
    public void getAndSetMapSize(PhysicalGameState pgs)
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
    }
    
    /** Finds the closest Enemy to attack
     * @param u My unit looking to attack
     * @param e List of enemies
     */
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
    
    /** Function doesn't currently work ==== TO BE FIXED!
     * @param u My unit
     * @param e The List of enemies to check for
     * @return True or False bool depending on if x y z
     */
    public boolean canAttackEnemy(Unit u, List<Unit> e)
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
    
    /** Finds the closest resource to harvest
     * @param u My Unit worker
     * @param r List of resources to check
     * @param b the base to bring resources back to
     */
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
    
    /** Finds the closest base and returns it
     * @param u My unit
     * @param r List of bases
     */
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
    
    /** Check how many units are around the base to see if they are blocking
     * @param pgs PhysicalGameState
     * @param base The base to check
     * @return returns the amount Of Units Around the base
     */
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
    }
    
    /** Looks for a enemy that is of 2 positions away from my unit and returns that enemy
     * @param pgs PhysicalGameState
     * @param u My unit
     * @param enemies List of enemies to check for
     * @return returns the closest enemy near to my unit
     */
    public Unit CheckForEnemyNeaby(PhysicalGameState pgs , Unit u, List<Unit> enemies)
    {		 
    	Unit closestEnemy = null;
    	int closestDistance = 2;
	    for(Unit e: enemies) 
	    {
	        int d = Math.abs(e.getX() - u.getX()) + Math.abs(e.getY() - u.getY());
	        if (d < closestDistance) 
	        {
	        	closestEnemy = e;
	        	closestDistance = d;
	        }
	    }
	    
	    return closestEnemy;
    }
    
    /* (non-Javadoc)
     * @see ai.core.AI#getParameters()
     */
    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
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
