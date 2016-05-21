import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class Bot {
	int width;
	int height;
	int c[] = new int[10];
	int handHeight, playHeight, enPlayHeight;
	int heroP[] = new int[2];
	int p[] = new int[8];
	int enP[] = new int[8];
	int hero, enHero;
	int lastX, lastY = 0;
	Robot r;

	
	public static void main(String[] args) throws AWTException, InterruptedException, FileNotFoundException, IOException {
		Parser p = new Parser();
		p.parse();
		Bot m = new Bot(p);
		//m.moveNaturally(0, 0, 500, 500);
		m.randomness(0, 0);
//		m.endTurn();		
//		for(int i=1;i<8; i++) {
//		System.out.println(ca.enPlay[i]);	
//		}

//	m.move(m.c, 1, m.handHeight);
	//m.move(m.enP, 1, m.enPlayHeight);
	//m.move2(m.p, 1, m.playHeight);
	//m.move(m.heroP, 0, m.heroP[1]);


//		for(int i=1; i< m.p.length; i++) {
//			m.move2(m.p, i, m.playHeight);
//	}
		
//	for(int i=1;i< m.numElems(ca.hand) + 1; i++) {
//		m.move(m.c, i, m.handHeight);
//	}
	
//	for(int i=1;i<3; i++) {
//	m.move(m.enP, i, m.enPlayHeight);
//}
	
//	for(int i=1;i<8; i++) {
//		System.out.println(m.enP[i]);	
//		}
	
	//System.out.println(m.numElems(ca.hand));
				
	//m.move2(m.enP, 1,  m.enPlayHeight);
	
	//m.move(m.width/2,  m.height * 5/7);
	
	//m.spellToEnemy(m.c[7], m.enP[1]);
	
	//m.heroPower();
	//m.playCard(m.c, 3, m.handHeight);
	//m.playCard(m.c, 5, m.handHeight);

	//m.attack(1,1,m.enPlayHeight);
	//m.endTurn();
	}

	
	public Bot(Parser ca) throws IOException, InterruptedException, AWTException {		
		//load game data
		
		r = new Robot();

		//get screen dimensions
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = (int) screenSize.getWidth();
		height = (int)screenSize.getHeight();
				
		//set hero portrait heights
		enHero = height * 1/7;
		hero = height * 5/7;
		
		//initialize positions of cards in hand
		handHeight = height * 74/80;
		computeHand(numElems(ca.hand));
	
		//init positions of play card positions
		playHeight = handHeight - height * 3/8;
		
		//init enemy play
		enPlayHeight = handHeight - height * 450/800;

		
		//init hero power
		heroP[0] = (width * 17/48) + width * 320/1280;
		heroP[1] = height - (height * 300/1280);
		
		System.out.println(numElems(ca.myPlay));
		
		//sets your board positions
		computePlay(numElems(ca.myPlay)); //sets positioning based on # of cards in play
		computeEnPlay(numElems(ca.enPlay)); //sets positioning based on # of cards in play
	}

	
	public void heroPower() throws AWTException, InterruptedException {
		Thread.sleep(2500);
		r.mouseMove(heroP[0], heroP[1]);
		Thread.sleep(200);
		r.mousePress(InputEvent.BUTTON1_MASK);
		Thread.sleep(200);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	/**parameters are the target array, the target array index, and the target height*/
	public void heroPowerTarget(int i[], int j, int k) throws AWTException, InterruptedException{
		Thread.sleep(2500);
		r.mouseMove(heroP[0], heroP[1]);
		Thread.sleep(200);
		r.mouseMove(i[j], k);
		Thread.sleep(200);
		r.mousePress(InputEvent.BUTTON1_MASK);
		Thread.sleep(200);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	public void endTurn() throws InterruptedException, AWTException{
		Thread.sleep(2500);
		r.mouseMove(width * 27/32, height/2 - height * 40/800);
		Thread.sleep(200);
		r.mousePress(InputEvent.BUTTON1_MASK);
		Thread.sleep(200);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	public void attack(int playPos, int enPos, int height) throws AWTException, InterruptedException{
		Thread.sleep(2500);
		r.mousePress(InputEvent.BUTTON1_MASK);
		Thread.sleep(200);
		moveNaturally(p[playPos], playHeight, enPos, height);
		Thread.sleep(200);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
		
		lastX = enPos;
		lastY = height;
	}
	
	public void attackFace(int myCardIndex) throws InterruptedException {
		Thread.sleep(2500);
		r.mouseMove(p[myCardIndex], playHeight);
		Thread.sleep(200);
		r.mousePress(InputEvent.BUTTON1_MASK);
		Thread.sleep(200);
		r.mouseMove(width/2, enHero);
		Thread.sleep(200);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	/**parameters are the # of the spell hand position, the target width position, and the target height*/
	public void spellToEnemy(int spellHandPos, int enPos, int height) throws AWTException, InterruptedException{
		Thread.sleep(2500);
		
		Thread.sleep(200);
		moveNaturally(lastX, lastY, spellHandPos, handHeight);
		Thread.sleep(200);
		
		r.mousePress(InputEvent.BUTTON1_MASK);
		
		Thread.sleep(200);
		moveNaturally(spellHandPos, handHeight, enPos, height);
		Thread.sleep(200);
		
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	public int numElems(String[] s) {
		int n = 0;
		for(int i = 0; i<s.length; i++) {
			if(s[i] != null)
				n++;
		}
		return n;
	}
	

	/**Gives correct position based on cards in hand. They shift by a small amount depending on the #*/
	public void computeHand(int j) {
		//System.out.println(j);
		int startInc = 8;
		if(j  > 3)
			startInc = 24;
		if(j >= 7) {
			startInc = 18;
		}
		if(j > 8) {
			startInc = 17;
		}
		int firstC = (width * 540/1280) - ((width * startInc/1280) * (j-1));
		int inc = ( ((width * 100/1280) ) - ((j-2) * (width * 8/1280)) ); 
		//System.out.println(inc);
		for(int i = 1; i < 10; i++) {
				c[i] += firstC;
				firstC += inc;
		}
	}
	
	/**Gives correct position based on cards in play*/
	public void computeEnPlay(int j) {
		//System.out.println(j);
		int firstP = (width/2);
		int firstPos = firstP - ((j -1) * (width * 50/1280));
		
		for(int i = 1; i < 8; i++) {
			enP[i] = firstPos;
			firstPos += (width * 100/1280);
		}
	}
	
	/**Gives correct position based on cards in play*/
	public void computePlay(int j) {
		int firstP = (width / 2);
		int firstPos = firstP - ((j -1) * (width * 50/1280));
		
		for(int i = 1; i < 8; i++) {
			p[i] = firstPos;
			firstPos += width * 100/1280;
		}
	}
	
		public void playCard(int i[], int j, int k) throws AWTException, InterruptedException{
			Thread.sleep(2500);
			System.out.println(i[j] + " " + j);
			r.mouseMove(i[j], k);
			Thread.sleep(200);
			r.mousePress(InputEvent.BUTTON1_MASK);
			Thread.sleep(200);
			r.mouseMove(i[j] , handHeight - 300);
			Thread.sleep(200);
			r.mouseRelease(InputEvent.BUTTON1_MASK);
		}
		
		public void move2(int[] a, int i, int j) throws AWTException, InterruptedException{
			Thread.sleep(2500);
			r.mouseMove(a[i], j);
		}
		
		public void move(int i, int j) throws AWTException, InterruptedException{
			Thread.sleep(2500);
			r.mouseMove(i, j);
		}
		
		public void moveNaturally(int beginX, int beginY, int endX, int endY) throws InterruptedException{
			int slope =  (endY - beginY) /  (endX - beginX);
						
			while(beginX != endX && beginY != endY) {
				Thread.sleep(5);
				r.mouseMove(beginX, beginY);
				beginX = beginX + 1;
				beginY = beginY + slope;
			}
			
			lastX = beginX;
			lastY= beginY;
		}
		
		public void sineWave(int x, int y){
			
			for(int i = 0; i < 360; i++) {
				//double Math.s
			}
		}
		
		public void randomness(int beginX, int beginY) throws InterruptedException {
			long timer = System.currentTimeMillis();
			long timeNow = System.currentTimeMillis();

			while((timeNow - timer) < 10000) {
				Random r = new Random();
				int rX = r.nextInt(800);
				int rY = r.nextInt(1200);

		
				
				moveNaturally(beginX, beginY, rX, rY);
				beginX = rX;
				beginY = rY;
				timeNow = System.currentTimeMillis();
			}
		}
}
