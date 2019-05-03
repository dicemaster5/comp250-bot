package bot;

import java.util.List;

import rts.PhysicalGameState;
import rts.units.Unit;

public class BotUtils {
	//============================================== FUNCTIONS TO USE ================================================//
    
    // Finds the closest Enemy to attack
    /**
     * @param u
     * @param e
     */
    public static Unit findEnemyToAttack(Unit u, List<Unit> e)
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
         }
         if (closestEnemy!=null)
         {
        	 return closestEnemy;
        	 //attack(u,closestEnemy);
         }
         else
        	 return closestEnemy;
    }
    
    /**
     * @param u The unit that does x y z
     * @param e The List of enemies to check for
     * @return True or False bool depending on if x y z
     */
    public static boolean canAttackEnemy(Unit u, List<Unit> e)
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
    /**
     * @param u
     * @param r
     * @param b
     */
    public static Unit findResourceToHarvest(Unit u, List<Unit> r)
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

         }
         
         if (closestResource!=null)
         {	
        	 return closestResource;
        	 //harvest(u,closestResource, b);
         }
         else
        	 return closestResource;
    }
    
    // Finds the closest base
    /**
     * @param u
     * @param r
     * @return
     */
    public static Unit findClosestBase(Unit u, List<Unit> r)
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
    
    
    /**
     * @param pgs
     * @param base
     * @return
     */
    public static int CheckUnitsAround(PhysicalGameState pgs , Unit base)
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
    
    /**
     * @param pgs
     * @param u
     * @param enemies
     * @return
     */
    public static Unit CheckForEnemyNeaby(PhysicalGameState pgs , Unit u, List<Unit> enemies)
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
}
