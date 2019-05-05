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
    public int LARGE_MAP = 1;
    public int MEDIUM_MAP = 2;
    public int SMALL_MAP = 3;
    public int VERY_SMALL_MAP = 4;
   
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
        
        pf = new AStarPathFinding();
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
        List<Unit> enemies = new LinkedList<Unit>();
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
    			else if(!enemies.contains(unit) && !unit.getType().isResource)
    			{
    				enemies.add(unit);
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
        
        // Check to see if there is a close enemy to my first base
        // Set that enemy to be a critical and dangerous enemy
		if(enemies.size() > 0 && bases.size() > 0)
			dangerEnemy = checkForEnemyNearby(pgs, bases.get(0), enemies);
		
		// Base Strategy =============================================================================================================
		if(bases.size() != 0)
		{
			for(Unit base:bases)
    		{
				// Train Workers
    			if(checkUnitsAround(pgs, base) < 3)
    			{
    				train(base, worker);
    			}
    		}
			
			// Train Special Units
			// Switch between making light types and ranged types for better
			// unit composition on the field
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
			for(Unit w:resourceWorkers)
			{
				if(dangerEnemy != null)
					attack(w, dangerEnemy);
				else if(resources.size() > 0)
	   				harvest(w, findClosestUnit(w, resources), findClosestUnit(w, bases));
				else
				{
					workers.add(w);
				}
			}
			
			// BARRACKS WORKERS =================================================
			for(Unit b:barrackWorkers)
			{
        		Unit base = findClosestUnit(b, bases);
        		
        		if(dangerEnemy != null)
					attack(b, dangerEnemy);
        		else if(p.getResources() >= 9 && CurrentMapSize != VERY_SMALL_MAP)
	        	{
	        		// I SHOULD CHANGE THIS TO BE DYNAMIC
	        		build(b, barracksType, base.getX() + 1, base.getY() - 1);	        		
	        	}
	        	else if(barracks.size() >= 1)
	        	{
        			harvest(b, findClosestUnit(b, resources), base);
	        	}
	        	else
	        	{
	        		if(CurrentMapSize == MEDIUM_MAP || CurrentMapSize == LARGE_MAP || CurrentMapSize == SMALL_MAP )
	        			harvest(b, findClosestUnit(b, resources), base);
	        		else
	        			workers.add(b);
	        	}
			}	
			
			// ATTACKERS ==========================================================================================================
			attackers.addAll(workers);
			attackers.addAll(ranged);
			attackers.addAll(heavy);
			attackers.addAll(light);
			for (Unit a:attackers)
			{	
				if(enemies.size() > 0)
					attack(a,findClosestUnit(a, enemies));
	    		else if(resources.size() > 0 && a.getType().canHarvest && enemyBases.get(0).getResources() > 0)
	   				harvest(a, findClosestUnit(a, resources), findClosestUnit(a, bases));
	    		else
	    			attack(a,findClosestUnit(a, enemyBases));
			}
    	}
		else
		{
			// Full on attack with every unit we have!
			for (Unit unit : pgs.getUnits())
			{
				if(enemies.size() > 0)
					attack(unit,findClosestUnit(unit, enemies));
	    		else
					attack(unit,findClosestUnit(unit, enemyBases));
			}
			
		}
        
        return translateActions(player, gs);
    }

//============================================== FUNCTIONS TO USE ================================================//

    /** Check Get and set map size
     * @param pgs Use the PhysicalGameState to find the size of the map
     * and then set the CurrentMapSize var to the specified size
     */
    public void getAndSetMapSize(PhysicalGameState pgs)
    {
        if(pgs.getHeight() < 9)
        {
        	CurrentMapSize = VERY_SMALL_MAP;
        }
        if(pgs.getHeight() >= 9)
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
    
    /** Finds the closest unit of a given unit list and returns it
     * @param myUnit the unit that is looking for a close unit of type X
     * @param targetUnits the list of units given to search in
     */
    public Unit findClosestUnit(Unit myUnit, List<Unit> targetUnits)
    {
    	 Unit closestUnit = null;
    	 int closestDistance = 0;
         for(Unit target: targetUnits) {
             int d = Math.abs(target.getX() - myUnit.getX()) + Math.abs(target.getY() - myUnit.getY());
             if (closestUnit==null || d<closestDistance)
             {
                 closestUnit = target;
                 closestDistance = d;
             }
         }
         
         if (closestUnit!=null)
         {
             return closestUnit;
         }
         else
         {
        	 return null;
         }
    }
    
    /** Checks if a units pathfinding is valid and that it can move to X unit
     * @param u My unit to check
     * @param e enemy to move to
     * @return True or False bool depending on if my unit's pathfinding is valid or not
     */
    public boolean canMoveTo(Unit u, GameState gs, Unit e)
    {
    	boolean checkPath = pf.pathExists(u, e.getPosition(gs.getPhysicalGameState()), gs, null);
    	if(checkPath)
    	{
    		return true;
    	}
    	else
    		return false;
    	
    }
    
    /** Check how many units are around the base to see if they are blocking
     * @param pgs PhysicalGameState
     * @param base The base to check
     * @return returns the amount Of Units Around the base
     */
    public int checkUnitsAround(PhysicalGameState pgs , Unit base)
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
    public Unit checkForEnemyNearby(PhysicalGameState pgs , Unit u, List<Unit> enemies)
    {		 
    	Unit closestEnemy = null;
    	int closestDistance = 3;
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