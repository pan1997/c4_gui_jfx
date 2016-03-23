/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4adv;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Pankaj
 */
public class C4UIBoard extends Pane {
	board b;
	Group peices;
	Runnable dropper;
	static final int rad = 25;
	// Rectangle background;
	Executor e;
	TextField adj;
	File lg;
	PrintWriter log;
	XYChart.Series<Number, Number> nsr,nsb;
	player pThis;

	C4UIBoard(int aa, int ba,String gn,TextField x) throws Exception {
		b = new board(aa, ba);
		adj=x;
		lg=new File(gn);
		log=new PrintWriter(lg);
		getChildren().add(peices = new Group());
		nsr=new XYChart.Series<>();
		nsr.setName("Red");
		nsb=new XYChart.Series<>();
		nsb.setName("Blue");
		// setBackground(new Background(new BackgroundFill(Color.,null,null)));
		// background=bb;
		// background.setWidth(aa*2*rad);
		// background.setHeight(ba*2*rad);
		// FillTransition f=new
		// FillTransition(Duration.seconds(2),background,Color.AZURE,Color.CADETBLUE);
		// f.setCycleCount(Transition.INDEFINITE);
		// f.setAutoReverse(true);
		// Platform.runLater(f::play);
		pThis = new pl();
		// drop(7,board.min);
		// drop(3,board.max);
		dropper = () -> {
			// Circle c=new Circle(rad,col==board.max?Color.RED:Color.BLUE);
			Rectangle c = new Rectangle(rad * 2, rad * 2,
					col == board.max ? Color.RED : Color.BLUE);
			PathTransition p = new PathTransition(
					Duration.millis((b.H - b.h[i]+1) * 50), new Path(new MoveTo(i
							* 2 * rad + rad, 0), new LineTo(i * 2 * rad + rad,
									((b.H - b.h[i]) * 2 - 1) * rad)), c);
			peices.getChildren().add(c);
			p.setInterpolator(Interpolator.EASE_IN);
			p.play();
			b.drop(i, col);
		};
		// drop(7,board.min);
		e=Executors.newCachedThreadPool();
		// t.setFont(Font.font(20));
		// setOnMousePressed((EventHandler<? super MouseEvent>) pThis);
	}

	int i, col;

	void drop(int i, int col) {
		this.i = i;
		this.col = col;
		Platform.runLater(dropper);
	}

	void startGame(player blue, player red, Stage t, long time) {
		Game g = new Game();
		g.p1 = blue == null ? pThis : blue;
		g.p2 = red == null ? pThis : red;
		g.tmR=g.tmB = time;
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {
			Logger.getLogger(C4UIBoard.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		// t.titleProperty().unbind();
		t.titleProperty().bind(g.messageProperty());
		e.execute(g);
	}
	void startTournament(Stage s,Text tx,int tl,player...p){
		Tournament t=new Tournament();
		t.score=new String[p.length][p.length];
		t.pl=p;
		t.tm=tl;
		t.t=s;
		tx.textProperty().bind(t.messageProperty());
		e.execute(t);
	}
	class Tournament extends Task<Void>{
		player[] pl;
		String [][]score;
		int tm;
		Stage t;
		@Override
		protected Void call() throws Exception {
			log.println("Tournament Started");
			for(int i=0;i<score.length;i++)
				for(int j=0;j<score[i].length;j++)
					score[i][j]="";
			for(int i=0;i<pl.length;i++){
				for(int j=0;j<pl.length;j++)if(i!=j){
					if(pl[i]!=null)
						pl[i].clear();
					if(pl[j]!=null)
						pl[j].clear();
					Game g=new Game();
					Platform.runLater(()->t.titleProperty().bind(g.messageProperty()));
					g.p1=pl[i]==null?pThis:pl[i];
					g.p2=pl[j]==null?pThis:pl[j];
					g.tmR=g.tmB=tm;
					e.execute(g);
					int r=g.get();
					score[i][j]+=r>0?"+":r==0?"=":"-";
					score[j][i]+=r>0?"-":r==0?"=":"+";
					log.println("Match ended between "+i+" "+j+" result "+(r>0?i:j)+" wins");
					log.println("Red("+i+") time "+g.redTime+" Blue("+j+") time "+g.blueTime);
					log.println(getScore());
					updateMessage(getScore());
					try{
						Thread.sleep(3000);
					}catch(Exception e){}
				}
				log.flush();
			}
			log.println("Final result");
			log.println(getScore());
			log.flush();
			return null;
		}
		String getScore(){
			String ans="";
			for(int i=0;i<pl.length;i++){
				ans=ans+i+"::";
				for(int j=0;j<pl.length;j++)
					ans=ans+"\t"+score[i][j];
				ans=ans+"\n";
			}
			return ans;
		}
	}

	void startFaceoff(Stage s,Text tx,int tlR,int tlB,player p1,player p2,String[] st){
		Faceoff f=new Faceoff();
		f.p1=p1;f.p2=p2;
		f.tmR=tlR;
		f.tmB=tlB;
		f.t=s;
		f.start=st;
		tx.textProperty().bind(f.messageProperty());
		e.execute(f);
	}
	class Faceoff extends Task<Void>{
		player p1,p2;
		String score;
		int tmR,tmB;
		String[] start;
		Stage t;
		@Override
		protected Void call() throws Exception {
			log.println("Faceoff Started");
			score="\n";
			for(int i=0;i<start.length;i++){
				if(p1!=null)
					p1.clear();
				if(p2!=null)
					p2.clear();
				Game g=new Game();
				//Platform.runLater(()->t.titleProperty().bind(g.messageProperty()));
				if(i%2==0){
					g.p1=p1==null?pThis:p1;
					g.p2=p2==null?pThis:p2;
				}
				else{
					g.p1=p2==null?pThis:p2;
					g.p2=p1==null?pThis:p1;
				}
				g.start=start[i];
				g.tmR=tmR;
				g.tmB=tmB;
				e.execute(g);
				int r=g.get();
				if(i%2==0)
					score+=r>0?"+":r==0?"=":"-";
				else
					score+=r>0?"-":r==0?"=":"+";
				log.println("Player 1 "+(r>0?(i%2==0?"Wins":"Loses"):(r==0?"Draws":(i%2==0?"Loses":"Wins"))));
				log.println(getScore());
				updateMessage(getScore());
				try{
					Thread.sleep(3000);
				}catch(Exception e){}
				log.flush();
			}
			log.println("Final result");
			log.println(getScore());
			log.flush();
			return null;
		}
		String getScore(){
			return score;
		}
	}class graphher implements Runnable{
		int x,y;
		boolean r;
		graphher(int a,int b,boolean xx){
			x=a;y=b;
			r=xx;
		}
		public void run(){
			(r?nsr:nsb).getData().add(new XYChart.Data<Number, Number>(x, y));
		}
	}

	class Game extends Task<Integer> {
		player p1, p2;
		long redTime = 0, blueTime = 0;
		long tmR;
		long tmB;
		String start;
		@Override
		protected Integer call() throws Exception {
			clear();
			nsb.getData().clear();
			nsr.getData().clear();
			p1.clear();
			p2.clear();
			// updateMessage("Game Begins");
			//C4Adv.print("Game Begins");
			int move;
			long s;
			if(start!=null){
				for(int i=0;i<start.length();i++){
					int m=start.charAt(i)-'0';
					drop(m,i%2==0?board.max:board.min);
					p1.drop(m);
					if(p1!=p2)
						p2.drop(m);
					try{
						Thread.sleep(500);
					}catch(Exception E){}
				}
			}
			/*
			 * for(int i=0;i<3;i++) {
			 * drop(pThis.analyse(b,board.max,tm),board.max);
			 * 
			 * drop(pThis.analyse(b,board.min,tm),board.min); }
			 */
			for (int i = start==null?0:start.length(); i < b.W * b.H; i += 2) {
				if(tmB>=2000)
					System.gc();
				if(b.calcQuick()==0){
					updateMessage("DRAW"+timeInfo());
					return 0;
				}
				if (b.win[board.max] != -1) {
					updateMessage("Red Wins " + timeInfo());
					drop(b.win[board.max], board.max);
					return 1;
				}
				if(adj.getText().contains("+")){
					adj.setText("");
					return 1;
				}
				else if(adj.getText().contains("-")){
					adj.setText("");
					return -1;
				}
				System.out.println("RED");
				updateMessage("Red thinking " + timeInfo());
				s = System.currentTimeMillis();
				try{
				move = p1.analyse(b, board.max, tmR);
				}catch(Exception e){
					return -1;
				}
				if(move<0){
					return -1;
				}
				redTime += System.currentTimeMillis() - s;
				//C4Adv.print("RED PV-->>" + p1.getpv(b, board.max));
				drop(move, board.max);
				p1.drop(move);
				if(p1!=p2)
					p2.drop(move);
				Platform.runLater(new graphher(i,p1.bms,true));
				// b.print();
				if(tmR>=2000)
					System.gc();
				Thread.sleep(100);
				if(b.calcQuick()==0){
					updateMessage("DRAW"+timeInfo());
					return 0;
				}
				if (b.win[board.min] != -1) {
					updateMessage("Blue Wins " + timeInfo());
					drop(b.win[board.min], board.min);
					return -1;
				}
				if(adj.getText().contains("+")){
					adj.setText("");
					return 1;
				}
				else if(adj.getText().contains("-")){
					adj.setText("");
					return -1;
				}
				System.out.println("BLUE");
				updateMessage("Blue thinking " + timeInfo());
				s = System.currentTimeMillis();
				try{
				move = p2.analyse(b, board.min, tmB);
				}catch(Exception e){
					return 1;
				}
				if(move<0){
					return 1;
				}
				blueTime += System.currentTimeMillis() - s;
				//C4Adv.print("BLUE PV-->>" + p2.getpv(b, board.min));
				drop(move, board.min);
				p1.drop(move);
				if(p1!=p2)
					p2.drop(move);
				Platform.runLater(new graphher(i+1,p2 instanceof externalPlayer?p2.bms:-p2.bms,false));
				Thread.sleep(100);
				// b.print();
				updateProgress(i, b.W * b.H);
			}
			return 0;
		}

		String timeInfo() {
			return String.format("Red[%s:%s] Blue[%s:%s]", redTime / 60000,
					(redTime / 1000) % 60, blueTime / 60000,
					(blueTime / 1000) % 60);
		}

	}/*
	 * void clear() { b=new board(b.W,b.H); this.getChildren().clear(); for(int
	 * j=0;j<=b.W;j++) getChildren().add(new Line(0,2*j*rad,b.H*2*rad,2*j*rad));
	 * for(int j=0;j<=b.H;j++) getChildren().add(new
	 * Line(j*rad*2,0,j*rad*2,2*b.W*rad)); }
	 */

	class pl extends player implements EventHandler<MouseEvent> {
		int ans;
		boolean asked;
		CountDownLatch l;

		@Override
		int analyse(board b, int col, long t) {
			asked = true;
			l = new CountDownLatch(1);
			try {
				l.await();
				return ans;
			} catch (InterruptedException ex) {
				Logger.getLogger(C4UIBoard.class.getName()).log(Level.SEVERE,
						null, ex);
			}
			return 0;
		}

		@Override
		public void handle(MouseEvent event) {
			ans = (int) (event.getX() / (2 * rad));
			if (asked)
				l.countDown();
			asked = false;
		}

		@Override
		String getpv(board b, int col) {
			return "" + ans;
		}
	}

	void clear() {
		b.clear();
		System.out.println("Clearing");
		Platform.runLater(peices.getChildren()::clear);
		// getChildren().clear();
		System.out.println("Cleared");
	}
}
