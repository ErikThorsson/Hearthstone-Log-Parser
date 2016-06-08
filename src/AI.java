import java.awt.AWTException;
import java.awt.Robot;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class AI {
	boolean first = false;
	int handSize, MAIN_READY, myMana;
	CardDatabase cDB;
	
	
	public static void main(String[] args) throws IOException, AWTException, InterruptedException {	
		
		Parser p = new Parser();
		
		//get a new log file each game. Uncomment each game.
		//p.updateLog();
		
		p.parse();
		Bot b = new Bot(p);
		
		//p.printMyPlay();
		AI a = new AI();
		a.loadDB();
		
		//a.printCombatCombinations();
		//a.mainLoop(p, b);
		//a.printComboCombos(a.combinationsCombinations(a.combatCombinations(null)));
		
		a.loadMana(p);
		a.printBestCombat(p);
		
		//System.out.println(a.isMyTurn(p));
	}
	
	public void loadMana(Parser p) throws FileNotFoundException, IOException {
		int turn = p.findTurn();
		
			if(p.firstPlayer == true) {
				myMana = turn/2 + 1;
			} else {
				myMana = turn/2;
			}
	}
	
	public void mainLoop(Parser p, Bot b) throws InterruptedException, AWTException, IOException {
		handSize();
		first = p.firstPlayer;
		MAIN_READY = p.MAIN_READY;
		int counter = 1;
		
		while(true) {
			
			//every second re-parse the log for turn changes and see if it's your turn;
			p.checkTurnChange();
			p.parse();
			Thread.sleep(2000);	
			int t = isMyTurn();
			
			//only make a move if you haven't made one yet
			//this triggers twice... the second time is the real turn start. That's what the counter is for. 
			if(t == 1 && MAIN_READY < p.MAIN_READY) {
				counter++;
				//System.out.println(counter);
				MAIN_READY = p.MAIN_READY;
				
				int turn = p.findTurn();
				loadMana(p);
				
				//this is the flag for the turn. Should be its own method probably to keep this clean...
				//there are two turn flags thus the 2 counter count
				if(counter == 2) {
					System.out.println("NEXT TURN " + turn);
										
					//if there are cards in play lets compute combat moves and make moves
					if(p.cardsInPlay() || spellInHand(p)) {
						System.out.println("COMBAT AVAILABLE");
						//wait for spell animations?
						Thread.sleep(5000);
						combat(null, p, b);
					}
					
					Thread.sleep(1000);		
					playCurve(p, b);
					
					//charge combat
					
//					//now add the played cards to an array
//					int[] playedCards = new int[8];
//					
//					//initialize the played cards array to empty (-1s)
//					for(int i =0; i< 8; i++) {
//						playedCards[i] = -1;
//					}
//					
//					//if a card has been played find its position
//					if(played != null) {
//						playedCards[0] = a.findCard(p, played.EntityID);
//					}
//					
//					//check for charge combat
//					a.combat(playedCards, p, b);
					
					b.endTurn();
					counter = 0;
				}
			}
		}	
	}
	
	public void loadDB() {
		cDB = new CardDatabase();
	}
	
	public boolean spellInHand(Parser p) {
		DBCard c = null;
		for(int i = 0; i < 11; i++) {
			if(p.myHand[i] != null) {
				try{
					c = cDB.cards.get(p.myHand[i].name);
				} catch(Exception e) {
					//not a spell so continue
					continue;
				}
				if(c != null)
					if(c.spell == 1)
						return true;
			}
		}
		return false;
	}
	
	public void finishHim(Parser p, Bot bot) throws AWTException, InterruptedException {
		//see if the cards in play can kill ###need to add spells
				int attkDmg = 0;
				for(int h = 0; h < 8; h++) {
					//if not null and has attack
					if(p.myPlayCards[h] != null && p.myPlayCards[h].atk != -1) {
						attkDmg += p.myPlayCards[h].atk;
					}
				}
				
				//add spells 
				int mana = myMana;
				for(int h = 0; h < 11; h++) {
					if(p.myHand[h] != null) {
						if(p.myHand[h].spell == 1 && mana >= p.myHand[h].cost) {
							mana -= p.myHand[h].cost;
							attkDmg += p.myHand[h].atk;
						}
					}
				}
				
				//send every card in play to face. ###need to add spells to this
				if(attkDmg >= p.enHealth) {
					//attack face with cards in play
					for(int i = 0; i < 8; i++) {
						if(p.myPlayCards[i] != null) {
							bot.attackFace(i);
						}
					}
					//hit face with spells
					mana = myMana;
					
					DBCard c = null;
				
					for(int i = 0; i < 11; i++) {
						
						//try to get the spell cost
						
						try{
							c = cDB.cards.get(p.myHand[i].name);
						} catch (Exception e) {
							
						}
						
						if(c != null) {
							if(p.myHand[i].spell == 1 && mana >= c.cost) {
								mana -= p.myHand[i].cost;								
								bot.spellToEnemy(bot.c[i], bot.width/2, bot.enHero);
							}
						}
					}
					//exit because you have won
					return;
				}
	}
	public void combat(int[] played, Parser p, Bot bot) throws IOException, InterruptedException, AWTException {		
		
		//if no card has been played or the played card has charge 
		//if(played[0] == -1 || played[0] != -1 && p.myPlayCards[played[0]].charge == 1) {
			
		//gets the list of combat combinations and the list of best combinations of combat combinations
		Card[][][] combat = combatCombinations(played, p);
		
		//gets the list of values for each combination 
		int combatValues[][] =  combatCombinValues(combat);
		//printCombatCombValues(combatValues);

		//gets the best combination for each enemy
		int[]best = pickBestTrades(combatValues, combat);
		printBestTrades(best);
		
		//gets the best combination of best combinations per enemy (since some might not be exclusive)
		int[][] bestComs = bestCombinValues(combatValues);	
		
		//get list of the cards trading
		ArrayList<Card> trades = new ArrayList<Card>();
		
		//print the trades
		System.out.println("\nThe best combat moves are...");
		for(int i = 0; i< 8; i++) {
			if(best[i] != -1) {
				for(int j = 0; j < 19; j++) {
					if(combat[i][best[i]][j] != null) {
						System.out.println(combat[i][best[i]][j].name + " attacking " + p.enPlayCards[i].name);
						trades.add(combat[i][best[i]][j]);
					}
				}
			}
			}
		
		int enPlay = p.numEnCardsInPlay();
		System.out.println("en play is " + enPlay);
		boolean face = false;
		
		finishHim(p, bot);
		
		//now make each trade with the bot or go face
		if(enPlay > 0) {
		for(int i = 1; i < 8; i++) {
			//find the card that attacks enemy i
			if(best[i] != -1) {
				for(int j = 0; j < 19; j++) {
					
					//if this enemy has a trade
						if(combat[i][best[i]][j] != null && p.enPlayCards[i] !=  null) {
							
							System.out.println(combat[i][best[i]][j].name + " attacking " + p.enPlayCards[i].name);
							
							//get the card position from play
							int c = findCard(p, combat[i][best[i]][j].EntityID);
							
							//if spell...
							if(combat[i][best[i]][j].spell == 1) {
								
								//get spell hand position
								int spellPos =  spellHandPosition(p, combat[i][best[i]][j]);
								
								System.out.println("PLAYING SPELL targeting x position " + bot.enP[i]);
								
								bot.spellToEnemy(bot.c[spellPos], bot.enP[i], bot.enPlayHeight);
								
								//subtract current turn mana if a spell is played
								myMana -= combat[i][best[i]][j].cost;
								
								System.out.println("the mana left over is " + myMana);
							
							//else trade the minion
							} else {
								bot.attack(c, i, bot.enPlayHeight);
							}
							
							//now check if there are any minions that weren't traded
								for(int h = 0; h < 8; h++) {
									boolean shared = false;
									for(Card card: trades) {
										if(p.myPlayCards[h] != null) {
											if(p.myPlayCards[h].EntityID == card.EntityID) {
												shared = true;
											}
									}
								}
								//if this card in play was found not to be traded... face it
								if(shared == false && p.myPlayCards[h] != null) {
									System.out.println(p.myPlayCards[h].name + " wasn't traded so face it!");
									//get new play coordinates in case something has changed with trades
									bot = new Bot(p);
									bot.attackFace(h);
								}
							}
						}
					}
				}
			}
		} else {
			//if no enemies go face with each card in play.
			for(int i = 0; i< 8; i++) {
				if(p.myPlayCards[i] != null)
					bot.attackFace(i);
			}
			}
		//}
	}
	
	/**
	 * returns spells hand position int or -1 if not found
	 * @param p
	 * @param c
	 * @return
	 */
	public int spellHandPosition(Parser p, Card c) {
		for(int i = 0; i < 11; i++) {
			if(c.name.equals(p.hand[i]))
				return i;
		}
		return -1;
	}
	
	public void chargeCombat(Card c) {
		
	}
	
	public boolean faceOrTrade(int[][] comb, int index) throws IOException, InterruptedException {
		
		
		if(comb[index][1] > 20)
			return true;
		else
			return false;
	}
	
	public void printBestCombat(Parser p) throws IOException, InterruptedException {
				
		Card[][][] combat =combatCombinations(null, p);
		
		//printCombatCombinations();

		int combatValues[][] =  combatCombinValues(combat);
		//printCombatCombValues(combatValues);

		int[]best = pickBestTrades(combatValues, combat);
		printBestTrades(best);
		
		//int[][] bestComs = bestCombinValues(combatValues);	
		
	
	System.out.println("\nThe best combat moves are...");
	for(int i = 0; i< 8; i++) {
		if(best[i] != -1) {
			for(int j = 0; j < 19; j++) {
				if(combat[i][best[i]][j] != null) {
					System.out.println(combat[i][best[i]][j].name + " attacking " + p.enPlayCards[i].name);
				}
			}
		}
		}
	}
	
	
	public int findCard(Parser p, int id) {
		for(int i = 0; i < 8; i++) {
			if(p.myPlayCards[i] != null) {
				if(p.myPlayCards[i].EntityID == id)
					return i;
			}
		}
		return -1;
	}
	
	public void printBestTrades(int[] c) {
		for(int i = 0; i< 8; i++){
			 System.out.print(c[i] + " ");
			}
		}
	
	public void printCombatCombValues(int[][] c) {
		for(int i = 0; i< 8; i++){
				System.out.print("\nCombination " + i + "\n");
			for(int j = 0; j<50; j++) {
				if(c[i][j] != -1000)
					System.out.print(c[i][j] + " for enemy " + i + ", ");
			}
		}
		}
	
	public void printComboCombos(int[][] c) {
		for(int i = 0; i< 100; i++){
			if(c[i][0] != 0 && c[i][1] != -1000)
				System.out.print("\nCombination " + i + "\n");
			for(int j = 0; j<8; j++) {
					System.out.print(c[i][j] + ", ");
			}
		}
	}
	
	public int bestCombat(int[][] c){
		int best = 0;
			for(int i = 0; i< 100; i++){
				if(c[i][0] != -1000) {
					if(c[i][0] > best)
						best = i;
				}	
				}
		return best;
	}
	
	public void printCombatCombinations() throws IOException, InterruptedException {
		
		Parser p = new Parser();
		p.parse();
		
		Card[][][] c = combatCombinations(null, p);
        
        for(int i = 0; i < 8; i++) {
        	System.out.println("\nENEMY CARD " + i + "\n");
        	for (int j = 0; j< 50;  j++) {
        			if(c[i][j][0] != null)
        				System.out.println("combination " + j);
        		for( int k = 0; k < 8; k++) {
        			if(c[i][j][k] != null)
        				System.out.print(c[i][j][k].name + ", ");
        		}
    			if(c[i][j][0] != null)
    				System.out.println();
        	}
        }
	}
	
	public int myPlayLength(Card[] play){
		int count = 0;
		for(int i = 0; i < play.length; i++) {
			if(play[i] != null)
				count++;
		}
		return count;
	}
	
	public int enPlayLength(Card[][][] c){
		int count = 0;
		for(int i = 0; i < 8; i++) {
			if(c[i][0][0] != null)
				count++;
		}
		return count;
	}
	
	/** returns the difference between my attack combination's card value and the enemy's... so we can have a negative score**/
	public int getTradeValue(Card[][][] c, int cardNum, int combNum, Parser p) throws IOException, InterruptedException {
		int myCardValue = 0;
		
		//get value of this enemy card
		int enemyVal = 0;
		int enHP = 0;
		
		if(p.enPlayCards[cardNum] != null)
			enHP = p.enPlayCards[cardNum].hp;
		
		if(p.enPlayCards[cardNum] != null) {
			for(int j = 0; j<8; j++) {
				if(c[cardNum][combNum][j] != null) {
					//subtract enemy health for every attack on it from the combination
					enHP -= c[cardNum][combNum][j].atk;
				}
			}
			
			//only alive minions carry value by weighted stats
			if(enHP > 0) 
				enemyVal = enHP + p.enPlayCards[cardNum].atk * 2;
			else 
				enemyVal = 0;
			
			System.out.println("ENEMY " + p.enPlayCards[cardNum].name + " " + p.enPlayCards[cardNum].atk + "/"
					+ p.enPlayCards[cardNum].hp + " value " + enemyVal);
		}
		
		int enemHPBeforeSpell = p.enPlayCards[cardNum].hp;

		int myHP = 0;
		for(int i = 0; i < 8; i++) {
			
			if(c[cardNum][combNum][i] != null & p.enPlayCards[cardNum] != null) {
				
				myHP = c[cardNum][combNum][i].hp - p.enPlayCards[cardNum].atk;
				
				//if the minion survived the trade add its stats
				if(myHP > 0 && c[cardNum][combNum][i].spell != 1) {
					myCardValue += myHP + c[cardNum][combNum][i].atk * 2;
					
					System.out.println("MY " + c[cardNum][combNum][i].name + " " + c[cardNum][combNum][i].atk + "/"
							+ c[cardNum][combNum][i].hp + " value of " + myHP + c[cardNum][combNum][i].atk * 2);
					
				//else if this minion died.. calculate the traded attk values
				} else {
					//so a bonus is given to lower attack minions trading into larger attack minions or a negative for high stats into low
					if(c[cardNum][combNum][i].spell != 1) {
						myCardValue +=  (p.enPlayCards[cardNum].atk - c[cardNum][combNum][i].atk) * 2;
					
					System.out.println("MY " + c[cardNum][combNum][i].name + " " + c[cardNum][combNum][i].atk + "/"
							+ c[cardNum][combNum][i].hp + " value of " + (p.enPlayCards[cardNum].atk - c[cardNum][combNum][i].atk) * 2);
					}
				}
				
				//calculate spells
				if(c[cardNum][combNum][i].spell == 1) {
					//System.out.println("mycard val is " + myCardValue);
					int j  = spellValue(c, cardNum, combNum, c[cardNum][combNum][i], enemHPBeforeSpell, p.enPlayCards[cardNum], p);
					
					System.out.println("MY " + c[cardNum][combNum][i].name + " " + c[cardNum][combNum][i].atk + "/"
					+ c[cardNum][combNum][i].hp + " value of " + j);
					
					//System.out.println("new spell " + j);
					myCardValue += j;
					//lower en health after spell
					enemHPBeforeSpell -= c[cardNum][combNum][i].atk;
				}
				


			}
		}
		
		System.out.println("returned card value " + myCardValue);
		return myCardValue;
	}

///**
// * returns the stats you have versus the enemy on the board
// * @param c
// * @param p
// * @return
// */
//public int[][] statDifferential(Card[][][] c, Parser p) {
//	for(int i = 0; i < 8; i++) {
//		for(int j = 0 ;  j < 50; j++) {
//			int difference;
//			for(int k = 0; k < 19; k++) {
//				
//			}
//		}
//	}
//	return null;
//}
	
//	/**
//	 * Adjusts combination values for spell overkill
//	 * @param c
//	 * @param combos
//	 * @param p
//	 * @return
//	 */
//	public int[][] filterSpellCombos(int [][] c, Card[][][] combos, Parser p) {
//
//		int myCardValue = 0;
//		int enemyVal = 0;
//		int enHP = 0;
//
//		for(int i = 1; i < 8; i++) {
//			//get hp of this enemy card
//			if(p.enPlayCards[i] != null) {
//				enHP = p.enPlayCards[i].hp;
//
//				//now add value of spell power and subtract find overkill
//				for(int j = 0; j < 50 ; j++) {
//					int spellPwr = 0;
//					for(int k = 0; j < 19; j++) {
//						if(combos[i][j][k] != null) {
//							if(combos[i][j][k].spell == 1) {
//								//since poly has arbitrary high value attk
//								if(!combos[i][j][k].name.equals("Polymorph"))
//									spellPwr += combos[i][j][k].atk;
//							}
//						}
//						int overK = spellPwr - enHP;
//						c[i][j] = c[i][j] - overK;
//					}
//				}
//			}
//		}
//		return c;
//	}

	/**
	 * returns # of card sin the combination
	 * @param c
	 * @return
	 */
	public int numInCombo(Card[][][] c, int enNum, int combNum) {
		int counter  = 0;
		for(int i = 0; i < 19; i++) {
			if(c[enNum][combNum][i] != null) {
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Returns true if a minion in play is not in the selected combination
	 * @param c
	 * @param cardNum
	 * @param combNum
	 * @param p
	 * @return
	 */
	public boolean protectsMinion(Parser p) {
		for(int i = 0; i < 8; i++) {
			if(p.myPlayCards[i] != null)
				return true;
		}
		return false;
	}
	/**
	 * Gives the appropriate scaling value to spells.
	 * @param a
	 * @param enHealth
	 * @return
	 */
	public int spellValue(Card[][][] c,int cardNum, int combNum, Card a, int enHealth, Card enemy, Parser p) {
		int comboNum =  numInCombo(c, cardNum, combNum);
		int val = (enHealth - a.atk)/comboNum;
		
		if(a.atk == -1) {
			val = 0;
			return val;
		}
	
		//if the spell is a perfect amount to finish the kill add a bonus. If the spell is slightly overkill still give a small bonus
		if(val == 0)  {
			//divide by number of spells in the combo to get value of killed target from stats
			val = (enemy.atk * 2 + enemy.hp) / comboNum;
			return val;
		}
		//if overkill give decreasing value
		if(val < 0 && val >= 0) {
			val = (val * -1) + (enemy.atk * 2 + enemy.hp) / comboNum;
		}
		if(protectsMinion(p)) {
			//not sure what value is best...should be derived from the spell value and what it saves
			val += 5;
			System.out.println("protects");
		}
		
		//add arbitrarily high value for poly 
		if(a.name.equals("Polymorph")) {
			val = enemy.atk * 2 + enemy.hp;
			//add a cost associated with enemy cost vs this spell's
			int cost = cDB.cards.get(a.name).cost;
			val -= cost - enemy.cost;
			System.out.println("sheeped!");
		}
		
		//find spell cost
		if(!a.name.equals("Polymorph")) {
		try {
			int cost = cDB.cards.get(a.name).cost;
			//higher cost spells get less % of 5 bonus points
			val += Math.round(((double)(10-cost)/10) * 5);
		} catch (Exception e) {
			//blah
		}
		}
		
		return val;
	}
	
	/**
	 * Scoring of spells needs to be done after they are all combined into lethal combinations... can't just give each one a value
	 * accurately by itself
	 */
	
	/**Returns the combined trade values for each combination per enemy card
	 * @throws InterruptedException 
	 * @throws IOException **/
	public int[][] combatCombinValues(Card[][][] c) throws IOException, InterruptedException {
		
		Parser p = new Parser();
		p.parse();
		
		int enLength = enPlayLength(c);
		//System.out.println(enLength);
		int tradeValueSum = 0;
		int[][] cValues = new int[8][50];
		
		//initialize
		for(int i = 0; i< 8; i++) {
			for(int j = 0; j< 50; j++) {
				cValues[i][j] = -1000;
			}
		}

			for(int i= 1; i < 8; i++)  {
				System.out.println("\nEnemy card # " + i + "\n");
				for(int j = 0; j < 50; j++) {
					if(c[i][j][0] != null) {
						System.out.println("\nCombination # " + j + "\n");
						//loop through the cards in the combination and add their trade values
							tradeValueSum = getTradeValue(c, i, j, p);
							cValues[i][j] = tradeValueSum;
							System.out.println(tradeValueSum);
					}
				}
			}
			
//			
//			for(int i = 0; i <50; i++) {
//				System.out.println(cValues[1][i]);
//			}
			
//			cValues = filterSpellCombos(cValues, c, p);
			
//			for(int i = 0; i <50; i++) {
//				System.out.println(cValues[1][i]);
//			}
			
			return cValues;
	}
	
	public int[][]  bestCombinValues(int[][] c) {
		
		int[][] best = new int[8][2];
		int bestV = -1;
		int index = 0;
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j<50; j++) {
				if(c[i][j] != -1000)
				if(c[i][j] != -1000 && c[i][j] > bestV) {
					//System.out.println("best of " + i + " is " + c[i][j] + " with index " + j );
					bestV = c[i][j];
					index = j;
				}
			}
			best[i][0] = index;			
			best[i][1] = bestV;
			bestV = -1;
		}
		return best;
	}
	
	/**Returns indexes of the best combat combination for each enemy that don't intersect**/
	public int[] pickBestTrades(int[][] combatValues, Card[][][] c) {		
		
		int[][] best = bestCombinValues(combatValues);
		
		int[] picked  = new int[8];
		int[] finalPicked  = new int[8];
		
		//initialize
		for(int i =0; i<8; i++)
			finalPicked[i] = -1;
		
        boolean first = true;

		for(int p = 0; p<8; p++) {
			
	    int cVal = 0;
        int index = 0;
        int enIndex = 0;
         
		//find the largest trade value and then see if it has intersections
		for(int i = 1; i< 8; i++) {
            if(best[i][1] > cVal) {
            	enIndex = i;
            	index = best[i][0];
            	cVal = best[i][1];
            }
		}
		
		if(index != 0) {
		
		//System.out.println("best Trade is index " + index + " with value " + cVal);
		
		//mark this index as being used 
		picked[enIndex] = 1;
		
		//System.out.println("enemy " + enIndex + " combination " + index + " has best value of " + cVal);
		
		//add first to the final array of combinations picked
		if(first == true)
			finalPicked[enIndex] = index;
		
		//now see if it has any intersections with already picked enemies
		for(int j = 0; j < 8; j++) {
			//for each combination card see if conflict and if so try to find non-conflicting one
			if(finalPicked[j] != -1) {
				for(int z = 0; z<8; z++) {
					//System.out.println(finalPicked[z]);
				}
				//System.out.println("Searching for " +enIndex + " " + index);
				
			int conflict = searchForNonConflicting(c, finalPicked,enIndex, index);
				 if(conflict != -1) {
					finalPicked[enIndex] = conflict;
					//System.out.println("conflict return val is " + conflict);
							}
		}
		}
		
		best[enIndex][0] = 0;
		best[enIndex][1] = 0;
        first = false;
		}
	}
		return finalPicked;
}

public int searchForNonConflicting(Card[][][] c, int[] f, int xIndex, int yIndex){
	int index = yIndex;
	boolean conflict = false;
	
	for(int u = 0; u< 8; u++) {
		if(f[u]!= -1) {
			//if the current picked combination overlaps with any other combination that isn't our own...
			if(overlappingCombinations(c, u, f[u], xIndex, yIndex) != true 
				&& c[u][f[u]][0] != null && c[xIndex][yIndex][0] != null
				&& u != xIndex && f[u] != yIndex) {
				//System.out.println("conflict at enemy " + xIndex + " combin " + yIndex);
					conflict = true;
			}
		}
	}
	
	if(conflict == true) {
		
	//check to see if any possible combinations conflict with already picked ones
	for(int u = 0; u< 8; u++) {
		for(int i = 0; i < 50; i++) {
			if(f[u]!= -1) {
				//if the current picked combination doesn't overlap with any other combination return that non conflicting value
				if(overlappingCombinations(c, u, f[u], xIndex, i) != true && c[u][f[u]][0] != null 
					&& c[xIndex][i][0] != null && u != xIndex && f[u] != yIndex) {
				//System.out.println(u + " " + f[u] + " and " + xIndex + " " + i + "don't conflict!");
					index = i;
				}
			}
		}
		}
	}
	return index;
}

public boolean isPicked(int[] p, int index) {
	for(int z = 0; z< 8; z++) {
		if(p[z] == 1 && z == index)
			return true;
	}
	return false;
}
	
	/**
	 * checks to see if combinations intersect
	 * @param c
	 * @param firstXIndex
	 * @param firstYIndex
	 * @param secondXIndex
	 * @param secondYIndex
	 * @return
	 */
	public boolean overlappingCombinations(Card[][][] c, int firstXIndex, int firstYIndex, int secondXIndex, int secondYIndex) {
//		System.out.println("_______["+firstXIndex+ "]"+"["+firstYIndex+ "]_______"); 
//		
//		for(int i = 0; i < 8; i++) {
//			if(c[firstXIndex][firstYIndex][i] != null)
//				System.out.println(c[firstXIndex][firstYIndex][i].name);
//		}
//		
//		System.out.println("_______["+secondXIndex+ "]"+"["+secondYIndex+ "]_______"); 
//		for(int i = 0; i < 8; i++) {
//			if(c[secondXIndex][secondYIndex][i] != null)
//				System.out.println(c[secondXIndex][secondYIndex][i].name);
//		}

		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++){
				if( c[firstXIndex][firstYIndex][i] != null && c[secondXIndex][secondYIndex][j] != null)
					//System.out.println(c[firstXIndex][firstYIndex][i].name + " compared to " + c[secondXIndex][secondYIndex][j].name); 
				if(c[firstXIndex][firstYIndex][i] != null && c[secondXIndex][secondYIndex][j] != null) {
					//System.out.println(c[firstXIndex][firstYIndex][i].name + " compared to " + c[secondXIndex][secondYIndex][j].name); 
					if (c[firstXIndex][firstYIndex][i].EntityID == (c[secondXIndex][secondYIndex][j].EntityID)) {
							//System.out.println("combination " + firstXIndex + " " + firstYIndex + " is the same as " + 
					//"combination " + secondXIndex + " " + secondYIndex); 
							return true;
					}
				}
			}
		}
		return false;
	}
	
	/** For now just weight by 2 x Atk 1 x HP **/
	public int cardValue(Card c) {
		int val = 0;
		val += c.atk * 2;
		val += c.hp;
		return val;
	}
	
	public void handSize() throws IOException, InterruptedException {
		int hSize = 0;
		Parser p = new Parser();
		p.parseHand();
		for(int i=0; i< p.hand.length; i++) {
			if(p.hand[i] !=null) {
				hSize++;
			}
		}
		handSize = hSize;
	}
	
	/**
	 * if new cards were played...take them out of our combat array UNLESS they have charge=
	 */
	
	public Card[] chargeCheck(int[] justPlayed, Parser p) {
		//so make new array and only take out the appropriate cards
		Card myPlay[] = new Card[8];
		
		for(int z = 0; z< 8; z++) {
			for(int g = 0; g< 8; g++) {
			//if this is a card that was just played
				if(justPlayed[g] != -1) {
					//if the played card index doesn't = z the old value goes into the new play array 
					//put the card back in unless it was just played
					if(z != justPlayed[g])
						myPlay[z] = p.myPlayCards[z];
			//put in charge minions by searching the card database
			if(cDB.cards.get(p.myPlayCards[z].name) != null) {
				if(cDB.cards.get(p.myPlayCards[z].name).charge == 1) {
					System.out.println("CHARGER");
					myPlay[z] = p.myPlayCards[z];
				}
			}
			
			}
				}
		}
		return myPlay;
	}
	
	/**
	 * returns arrayList of spells in hand and gives them appropriate stats
	 */
		public ArrayList<Card> checkSpells(Card[] c) {
			ArrayList<Card> s = new ArrayList();
			
			for(int i =0; i< c.length; i++) {
				if(c[i] != null) {
					//get card from database to check if its a spell
					DBCard db = cDB.cards.get(c[i].name);
					if(db != null) {
						if(db.spell == 1) {
							c[i].spell = 1;
							c[i].atk = db.atk;
							s.add(c[i]);
						}
					}
				}
		}
			return s;
	}
		
	/**
	 * Combines spells with cards in play
	 */
		
		public Card[] combineSpells(Card[] c, Parser p) {
			ArrayList<Card> spells = checkSpells(p.myHand);
			//p.printHand();
//			for(Card spell : spells) {
//				System.out.println("MY SPELLS " + spell.name);
//			}
			Card[] combined = new Card[c.length + spells.size()];
			int endOfPlay = 1;
						
			for(int i =1; i< c.length; i++) {
				if(c[i] != null) {
					combined[i] = c[i];
					//System.out.println("Added " + c[i].name);
					endOfPlay++;
				}
			}
			
			for(int i = endOfPlay, j =0; i< spells.size() + endOfPlay; i++, j++) {
				combined[i] = spells.get(j);
				//System.out.println("Added " + spells.get(j).name);
			}
			
			return combined;
			
		}
	
	/**
	 * Returns a 3D array of combinations available for your cards in play for each enemy card on the field
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Card[][][] combatCombinations(int[] justPlayed, Parser p) throws IOException, InterruptedException {
		
		int counter = 0;
		Card[][][] combinations = new Card[9][50][19];
				
		//loop for every enemy card
		for(int i = 0; i < p.enPlay.length; i++) {
			
			Card[] myPlay = p.myPlayCards.clone();	
			
			//if new cards were played...take them out of our combat array UNLESS they have charge
			if(justPlayed != null) {
				myPlay = chargeCheck(justPlayed, p);
				System.out.println("charge check");
			}
			
			//get the hp of each enemy card
			int enHP = 0;
			if(p.enPlayCards[i] != null)
				enHP = p.enPlayCards[i].hp;	
			else
				continue;
			
			//get length of playable cards (includes spells and cards in play)
			myPlay = combineSpells(myPlay, p);
			
			//for(int o = 0; o < myPlay.length; o++) {
					//if(myPlay[o] != null)
					//System.out.println("PLAY " + myPlay[o].name);
			//}
					
			int pLength = myPlayLength(myPlay);
			
			//fixes bug for only 1 card in play
			if(pLength == 1)
				pLength = 2;

			//create and add every combination that can kill the enemy
			for( int z = 0; z < pLength - 1; z++) {
				
				//System.out.println("shifting in play # " + z);
				
				/**after first iteration shift the arrays minus the first element to the right in circular fashion
				this will allow for every permutation calculation*/
				if(z >= 1) {
					
					Card lastIndex = myPlay[1];
					
					for(int x = 1; x < pLength; x++) {
						
						Card currentXplusOne = null;
						int lastCardInPlay = -1;
						
						//save the index you are about to delete (shift)
						if(x + 1 < pLength)  
							currentXplusOne = myPlay[x + 1];
						else
							lastCardInPlay = x;
						
						
						//if shifting last element, shift it to the 2nd element
						if(x == lastCardInPlay) {
							myPlay[1] = myPlay[x + 1];
							myPlay[x+1] = lastIndex;
							continue; 
						}
						
						//if first swap swap index 1 with 2
						if(x == 1)
							myPlay[2] = myPlay[1];
						
						//else swap the next index with the last
						else
							myPlay[x + 1] = lastIndex;
						
						//save the index you are about to delete (shift)
						lastIndex = currentXplusOne;
						
					}
				}
				
				//print array to see if its same
//				System.out.println("CARD ARRAY!!!\n");
//				for(int v = 1 ; v < 11 ; v++) {
//					if(myPlay[v] != null) {
//						try {
//							System.out.println(myPlay[v].name + " is in position " + v + " " +  myPlay[v].atk + 
//									"/" + myPlay[v].hp + " id " + myPlay[v].EntityID);
//						} catch (Exception e) {
//						System.out.println("\n\nPROBLEM NAME IS " + myPlay[v].name +"\n\n");
//					}
//					}}
//				System.out.println("\n\nCARD ARRAY!!!\n");

				
				/**this n^2 block will try adding myPlay[1] + myPlay[2] and so on until it gets an added attack
				 value that is >= enemy health. The combination will be stored in a 3D array
				 combinations[enemy card][combination #][first card in combination]*/
				 				
				for(int j = 1; j < myPlay.length; j++) {
					
				//if(myPlay[j] != null)
						//System.out.println("mana " + myMana + " checking combinations of " + myPlay[j].name + " in position " + j);
					
					//reset the added attk every combination
					int addedAttk = 0;
					int comboNumber = 0;
					int mana = myMana;

					//for each playable or usable card in play...
					for(int k =  j; k < myPlay.length; k++) {
						
						int manaCost = 0;
						
						//if we run into null because all are tested
						if(myPlay[k] == null) {
							break;
						 }
						
						//if we haven't killed the target yet, the spell does damage and we have enough mana for the spell...
						if(addedAttk < enHP && myPlay[k].atk != -1) {
							
							try {
								manaCost = cDB.cards.get(myPlay[k].name).cost;
								//if available mana is < our mana skip this spell
								if(mana < manaCost) {
									k++;
									continue;
								}
								mana -= manaCost;
								
							} catch (Exception e) {
								//not a spell so its ok
							}	
							
				
							//System.out.println("this combination kills " +  p.enPlayCards[i].name + "  " + myPlay[j].name);
							addedAttk += myPlay[k].atk;
							
							//System.out.println("manacost " + manaCost + " mana is " + mana + " combo " + myPlay[k].name);
							
							//put a card object into the combination # for the enemy card
							combinations[i][counter][comboNumber] = myPlay[k];
						
						//if the target has been killed...
						} else {
							//increment the combination counter if one has been found
							counter++;
							break;
						}
						comboNumber++;
				}
					//move the counter even if the target hasn't been reached
					//remove combos if it wasn't killed though
					combinations = checkForKill(combinations, p, counter, i, j, myPlay);
					if(combinations[i][counter][0] != null)
						counter++;
			}
				if(myPlay[3] == null) { //won't operate for 2 or less cards
					break;
				}
		}
			counter = 0;
	}
		
		return combinations;
	}
	
	/**
	 * deletes combinations that don't kill their target, returning new Card[][][] 
	 * @param combinations
	 * @param p
	 * @param counter
	 * @param i
	 * @param j
	 * @param myPlay
	 * @return
	 */
	public Card[][][] checkForKill(Card[][][] combinations, Parser p, int counter, int i, int j, Card [] myPlay) {
		int kill = p.enPlayCards[i].hp;
		int comboNumber = 0;
		for(int l = j; l < myPlay.length; l++) {
			if(combinations[i][counter][comboNumber] != null)  {
				kill -= combinations[i][counter][comboNumber].atk;
//				System.out.println(combinations[i][counter][comboNumber].name + " hits " + p.enPlayCards[i].name +
//						" kill val is " + kill);
				comboNumber++;
			}
		}
		comboNumber = 0;
		//if enemy not killed... clear all combinations here
		
		if(kill > 0) {
			for(int l = j; l < myPlay.length; l++) {
					combinations[i][counter][comboNumber] = null;
					comboNumber++;
			}
		}
		return combinations;
	}
	
	/**Returns a boolean by checking to see if you have drawn a card*/
	public boolean cardsInHandChange() throws IOException, InterruptedException {
		boolean start = false;
		int hSize = 0;
		Parser p = new Parser();
		p.parseHand();
		for(int i=0; i< p.hand.length; i++) {
			if(p.hand[i] !=null) {
				hSize++;
			}
		}
		if(hSize != handSize){
			handSize = hSize;
			return true;
		}
		return false;
	}
	
	public boolean minionAvailable(Parser p) {
		for(int i  = 0; i < 11; i++) {
			if(p.myHand[i] != null) {
				if(p.myHand[i].spell != 1)
					return true;
			}
		}
		return false;
	}
	
	/**Plays a card that equals your mana pool or the next highest one*/
	public void playCurve(Parser p, Bot r) throws IOException, AWTException, InterruptedException {
		System.out.println("playing curve!");

		//get mana available
		int turn = myMana;
		boolean endPlay = false;
		//get hand costs
		int[] costs = handCosts(p);
		
		while(endPlay == false) {
		
			turn = myMana;
			int cost = -1;
			int cardIndex  = 0;
				
			//looks for a cost that  = the mana available
			for(int i = 1; i<9; i++) {
				if(costs[i] == turn) {
					cost = costs[i];
					cardIndex = i;
				}
			}
	
			//finds highest cost if no cost = turn 
			if(cost == -1) {
					for(int i = 1; i<8; i++) {
						if(costs[i] > cost && costs[i] < turn) {
								cost = costs[i];
								cardIndex = i;
						}
						//System.out.println("highest cost card is" + p.hand[cardIndex] + " with index " + cardIndex + " with cost " + card);
					}
			}
		
			System.out.println("turn " + turn + " with " + myMana + " available mana the highest playable card is " + p.hand[cardIndex] + " with cost " + cost + " at index "
				+ cardIndex);
		
		DBCard c = null;
		
		//get a card containing the spell cost if it's a spell
		if(p.myHand[cardIndex].spell == 1) {
			c = cDB.cards.get(p.hand[cardIndex]);
			//System.out.println("spell " + c.name);
		} 
		
		//if this card value isn't null for some god awful reason
		if(p.hand[cardIndex] != null || c!= null) {
		
		//if a minion is available give it play priority
		boolean minion = false;
		if(minion = minionAvailable(p) == true) {
			r.playCard(r.c, cardIndex, r.handHeight);
		}
		
		//only play this inside code if there are no minions available
		if(minion != true) {
			
		//if not a spell or the spell has no attack
		if(c.spell == 1 && c.atk == -1) {
			
				r.playCard(r.c, cardIndex, r.handHeight);
				//get spell cost
				cost = c.cost;

			//if the cost was correctly found and not the default -1 subtract it from mana
			if(cost != -1)
				myMana-= cost;
		}
		
		
		//else if a spell w/ attk
		if(c != null && c.spell == 1 && c.atk != -1) {
			
			//stupid routine just attacks first enemy for now or face if none
			for(int i = 0; i< 8; i++) {
				if(p.enPlayCards[i] != null)
					r.spellToEnemy(cardIndex, i, r.enPlayHeight);
				else
					r.spellToFace(cardIndex);
				
//					//remove mana cost from turn mana
					if(cost != -1)
						myMana-= cost;	
					
					break;
					}
			}
		}

		//delete this card from the cost array
		costs[cardIndex] = -1;
}
		
		//get new handsize		
		p.parseHand();
		r.computeHand(r.numElems(p.hand));

		
		//now check if any costs are < myMana
		for(int i = 0; i < costs.length; i++) {
			if(costs[i] <= myMana && costs[i] > 0)
				continue;
		}
			endPlay = true;
	}
}
	
	
	public int isMyTurn() throws IOException, InterruptedException{
		int turn = 0;
		Parser p = new Parser();
		
		for(int i = 0; i< 2; i++) {
			turn = p.findTurn();
			Thread.sleep(50);
		}
		
		System.out.println(turn);

		//sets the current turn to see if its different than the last to allow for the the next action		
		if(turn % 2 > 0 && turn != -1 && first == true)
			return 1;
		if(turn % 2 == 0 && turn != -1 && first == false)
			return 1;
		return 0;
	}
	
	/** returns the costs of cards in hand*/
	public int[] handCosts(Parser p){
		int[] costs = new int[9];
		for(int i = 1; i< 9; i++) {
			if(p.hand[i] != null) {
			Card c = new Card();
			c = p.cards.get(p.hand[i]);
			//System.out.println(p.hand[i] + " " + c.cost);
			costs[i] = c.cost;
			}
		}
		return costs;
		}
	
	public Parser parse() throws IOException, InterruptedException {
		Parser p = new Parser();
		return p;
	}
}
