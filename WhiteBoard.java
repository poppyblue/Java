import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import java.io.*;
import java.net.*;
import java.util.*;
abstract class Shape implements Serializable {
    public abstract void draw(Graphics g);
}
class Line extends  Shape{//线段
    int startX,startY;
    int endX,endY;

    public Line(int x1,int y1 ,int x2 ,int y2){
        startX = x1; startY = y1;
        endX = x2; endY = y2;
    }  

    public void draw(Graphics g){       
       g.drawLine(startX,startY, endX,endY);
}
}
class Oval extends Shape{  //椭圆
    int startX,startY;
    int  width,height;

    public Oval(int x1,int y1 ,int x2 ,int y2){
        startX = x1; startY = y1;
        width = x2;  height= y2;
    }  

    public void draw(Graphics g){       
       g.drawOval(startX,startY,width,height);
}
}
class Rect extends Shape{//矩形
	int  startX,startY;
	int width,height;
	public Rect(int x1,int y1,int x2,int y2){
		startX=x1;
		startY=y1;
		width=x2;
		height=y2;
	}
	public void draw(Graphics g){
		g.drawRect(startX,startY,width,height);
	}
}

public class WhiteBoard extends Canvas implements Runnable{
	MulticastSocket mSocket;   // 用于收发数据的MulticastSocket对象
	InetAddress inetAddress;   //多播地址
	ArrayList<Shape> graphs = new ArrayList<>(); //存放所有绘制线
	int lastx, lasty;              // 上次鼠标点击位置
	int prex, prey;   //橡皮筋绘制上次鼠标位置
	int type = 1;  //形状类型,1—直线,2—椭圆,3-矩形

	public WhiteBoard() {
		connect(); //加入多播
		new Thread(this).start(); //创建消息监听线程
		this.addMouseListener(new MouseAdapter() {
	public void mousePressed(MouseEvent e) { 
    //鼠标按下，记住坐标位置
			lastx = e.getX(); lasty = e.getY();
			prex = lastx; prey = lasty;
        }
        public void mouseReleased(MouseEvent e){
			int x = e.getX(), y = e.getY();
			if (type==1)
				sendData(new Line(lastx,lasty,x,y));//数据报
			else if(type==2)
				sendData(new Oval(lastx,lasty,x-lastx,y-lasty));
			else if(type==3)
				sendData(new Rect(lastx,lasty,x-lastx,y-lasty));
        }
     });
     this.addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) {      
			Graphics g = getGraphics();
			g.setXORMode(getBackground());
			if ( type==1)
				g.drawLine(lastx,lasty,prex,prey); //檫除原直线
			else if(type==2)
				g.drawOval(lastx,lasty,prex-lastx,prey-lasty);
			else if(type==3)
				g.drawRect(lastx,lasty,prex-lastx,prey-lasty);
			int x = e.getX(), y = e.getY();
			if (type==1)
				g.drawLine(lastx,lasty,x,y); //绘制新直线
			else if(type==2)
				g.drawOval(lastx,lasty,x-lastx,y-lasty);
			else if(type==3)
				g.drawRect(lastx,lasty,x-lastx,y-lasty);
			prex = x; prey = y;
        }
      });
}
 
   public void connect() {  //加入多播组
    try {
 		 mSocket = new MulticastSocket(7777);
  		 inetAddress = InetAddress.getByName("230.0.0.1");
  		 mSocket.joinGroup(inetAddress);
} catch (Exception e) { 	}
   }

   public void sendData(Shape data) { //发送数据
      try {
         ByteArrayOutputStream byteStream = new
          ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(new
         BufferedOutputStream(byteStream));
        os.writeObject(data);
        os.flush();
      byte[] sendBuf = byteStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(
                      sendBuf, sendBuf.length, inetAddress, 7777);
	     mSocket.send(packet); //发送数据报
     } catch(Exception e) {System.out.println(e); }
   }

   public void run() { //接收消息并显示
       try {
          byte[ ] data = new byte[100]; // 字节缓冲区存放接收数据
		    DatagramPacket packet = new DatagramPacket(data,
 					data.length);
       	while (true) { 
		       mSocket.receive(packet); // 接收数据报
ByteArrayInputStream byteStream = new
            ByteArrayInputStream(data);
             ObjectInputStream is = new
             ObjectInputStream(new BufferedInputStream(byteStream));
Shape p = (Shape)is.readObject();
               drawGraph(p); //绘制收到的图形
  			}
	} catch (Exception e) { System.out.println(e);}
   }

   public void drawGraph(Shape p) { //绘制形状
		Graphics g = getGraphics();  
		p.draw(g);
		graphs.add(p);       
   }

   public void paint(Graphics g) {
         // 遍历列表显示图形
        for (int k=0;k<graphs.size();k++)
           graphs.get(k).draw(g);
   }

   public static void main(String[ ] args) {
        Frame x = new  Frame();
        Button line = new Button("line");
        Button oval = new Button("Oval");
		Button rect=new Button("Rect");
        Panel p = new Panel();
        x.add("South",p);
        p.add(line);
		p.add(oval);
		p.add(rect);
        WhiteBoard b = new  WhiteBoard();
        x.add(b);
        line.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               b.type = 1;              
           }
        });
        oval.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               b.type = 2;              
           }
        });
		rect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				b.type=3;
			}
		});
        x.setSize(200,150);
        x.setVisible(true);
}
}