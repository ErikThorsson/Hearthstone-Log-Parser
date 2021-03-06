import java.util.HashMap;


public class CardDatabase {
	HashMap<String,DBCard> cards = new HashMap<>();
	
	public CardDatabase() {
		DBCard reckless_Rocketeer = new DBCard();
		reckless_Rocketeer.name = "reckless_Rocketeer";
		reckless_Rocketeer.charge = 1;
		cards.put("reckless_Rocketeer", reckless_Rocketeer);
		
		DBCard Wolfrider = new DBCard();
		Wolfrider.name = "Wolfrider";
		Wolfrider.charge = 1;
		cards.put("Wolfrider", Wolfrider);
		
		DBCard c = new DBCard();
		c.name = "Frostbolt";
		c.spell = 1;
		c.atk = 3;
		c.cost = 2;
		cards.put("Frostbolt", c);

		c = new DBCard();
		c.name = "MageHeroPower";
		c.spell = 1;
		c.atk = 1;
		c.cost = 2;
		c.heroP = 1;
		cards.put("MageHeroPower", c);
		
		c = new DBCard();
		c.name = "Arcane Intellect";
		c.spell = 1;
		c.cost = 3;
		cards.put("Arcane Intellect", c);
		
		c = new DBCard();
		c.name = "Fireball";
		c.spell = 1;
		c.cost = 4;
		c.atk = 6;
		cards.put("Fireball", c);
		
		c = new DBCard();
		c.name = "Arcane Explosion";
		c.spell = 1;
		c.atk = 1;
		c.cost = 2;
		c.globalSpell = 1;
		cards.put("Arcane Explosion", c);
		
		c = new DBCard();
		c.name = "Arcane Missiles";
		c.spell = 1;
		c.cost = 1;
		c.atk = 3;
		c.globalSpell = 1;
		cards.put("Arcane Missiles", c);
		
		c = new DBCard();
		c.name = "Polymorph";
		c.cost = 4;
		c.spell = 1;
		c.atk = 1000; 
		cards.put("Polymorph", c);

	}
}
