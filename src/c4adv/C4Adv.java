/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4adv;

import static c4adv.C4UIBoard.rad;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 *
 * @author Pankaj
 */
public class C4Adv extends Application {
	C4UIBoard b;
	Rectangle r;
	static final int W=9;
	static final int H=7;
	static TextFlow Console=new TextFlow();
	static ScrollPane s;
	LineChart<Number,Number> lc;
	NumberAxis xAxis,yAxis;
	TextField adj;
	@Override
	public void init()
	{
		try{
		b=new C4UIBoard(W,H,"t2.txt",null);
		}catch(Exception e){
			
		}
	}
	@Override
	public void start(Stage primaryStage) {
		BorderPane rt=new BorderPane();
		StackPane root = new StackPane();
		adj=new TextField();
		b.adj=adj;
		root.setMaxSize(2*W*rad,2*W*rad);
		root.setMinSize(2*W*rad,2*W*rad);
		root.setPrefSize(2*W*rad,2*W*rad);
		xAxis=new NumberAxis();
		yAxis=new NumberAxis();
		lc=new LineChart<>(xAxis,yAxis);
		rt.setCenter(root);
		Console.getChildren().add(adj);
		Console.getChildren().add(new Text("\n"));
		Console.getChildren().add(lc);
		lc.setTitle("Move Scores");
		lc.getData().add(b.nsb);
		lc.getData().add(b.nsr);
		rt.setRight(s=new ScrollPane(Console));
		s.setMinWidth(200);
		root.setOnMousePressed((EventHandler<? super MouseEvent>) b.pThis);
		Pane lines=new Pane();

		for(int j=0;j<=H;j++)
			lines.getChildren().add(new Line(0,2*j*rad,W*2*rad,2*j*rad));
		for(int j=0;j<=W;j++)
			lines.getChildren().add(new Line(j*rad*2,0,j*rad*2,2*H*rad));
		//root.getChildren().add(r);
		root.getChildren().add(b);
		root.getChildren().add(lines);

		Scene scene = new Scene(rt, W*C4UIBoard.rad*2+500, H*C4UIBoard.rad*2+100);

		/*primaryStage.titleProperty().bind(b.t.textProperty());*/
		primaryStage.setTitle("C4");
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e->System.exit(0));
		//player p3=new NegaScout(3);
		//System.out.println(System.getProperty("user.dir"));
		//player p1=new NegaScoutEx(1);
		player p0=new NegaScout(1);
		player p2=null,p3=null,p4=null;
		try{
			p2=new externalPlayer("./lyanna");
			p3=new externalPlayer("./c4_mcts");
			//p4=new externalPlayer("./lyanna3");
		}catch(Exception e){
			System.out.println("Error");
			System.out.println(e);
		}
		//b.startGame(p3,p2,primaryStage,5000);
		//b.startGame(p4,p1,primaryStage,500);
	    //b.startGame(p0,null,primaryStage,500);
		Text txt=new Text("");
		Console.getChildren().add(new Text("\n"));
		Console.getChildren().add(txt);
		//b.log.println("TRNMENT 1");
		//b.log.println("0-NSex\n1 Ns\n2 CC44 2.0\n");
	    //b.startTournament(primaryStage,txt,5000,p2,p0);
		//String st[]={"444535","444535","455454","455454","6424","6424","4435","4435","545434","545434"};
		//String st[]={"3433","3433","3444","3444","4435","4435","4343","4343","6424","6424","0123","0123","4243","4243","4423","4423","5335","5335","3443","3443","1717","1717"};
		//String st[]={"4423","4423","5335","5335","3443","3443","1717","1717"};
		//String cont[]={"3444","3444"};
		String[] st={"6424","6424","0123","0123","4243","4243"};
	    //String st[]={"44","44","34","34","22","22","14","14","58","58"};
		b.startFaceoff(primaryStage, txt,5000,5000,p3, p2, st);
	}/*
	static void print(String s)
	{
		Platform.runLater(()->Console.getChildren().add(new Text(s+"\n")));
	}*/
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}