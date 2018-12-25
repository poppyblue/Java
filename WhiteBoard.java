package hello1;

import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import java.io.*;
import java.net.*;
import java.util.*;
abstract class Shape implements Serializable {
    public abstract void draw(Graphics g);
}
class Line extends  Shape{
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
class Oval extends Shape{  
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
class Rect extends Shape{
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

public class WhiteBoard extends Canvas implements Runnable,ActionListener{
	MulticastSocket mSocket;  
	InetAddress inetAddress;   
	ArrayList<Shape> graphs = new ArrayList<>(); 
	int lastx, lasty;              
	int prex, prey;  
	int type = 1;  
	static boolean SW = false;
	Color color = Color.black; // 画笔颜色
	PopupMenu popup; //弹出菜单
	
	public WhiteBoard() {
		popup=new PopupMenu("Color");
		String labels[]={"Clear", "Red", "Green", "Blue", "Black"}; 
		for(int i = 0; i < labels.length; i++) {
			MenuItem mi = new MenuItem(labels[i]); //创建菜单项
			mi.addActionListener(this); //给菜单项注册动作监听者
			popup.add(mi); //将菜单项加入弹出菜单中
		}
		this.add(popup); //将弹出菜单附在画布上。
		
		connect(); 
		new Thread(this).start(); 
		this.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) { 
			Graphics g = getGraphics();
			lastx = e.getX(); lasty = e.getY();
			prex = lastx; prey = lasty;
        }
        public void mouseReleased(MouseEvent e){
			int x = e.getX(), y = e.getY();
			if (type==1)
				sendData(new Line(lastx,lasty,x,y));
			else if(type==2)
				sendData(new Oval(lastx,lasty,x-lastx,y-lasty));
			else if(type==3)
				sendData(new Rect(lastx,lasty,x-lastx,y-lasty));
        }
     });
     this.addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) {      
			Graphics g = getGraphics();
			g.setColor(color);
			g.setXORMode(getBackground());
			if ( type==1)
				g.drawLine(lastx,lasty,prex,prey); 
			else if(type==2) {
				if (WhiteBoard.SW) {
    	 				g.fillOval(lastx,lasty,prex-lastx,prey-lasty);
     				}else {
     					g.drawOval(lastx,lasty,prex-lastx,prey-lasty);
     			}				
			}
			else if(type==3) {
				if (WhiteBoard.SW) {
					g.fillRect(lastx,lasty,prex-lastx,prey-lasty);
 				}else {
 					g.drawRect(lastx,lasty,prex-lastx,prey-lasty);
 				}	
			}
			
			int x = e.getX(), y = e.getY();
			if (type==1)
				g.drawLine(lastx,lasty,x,y); 
			else if(type==2) {
				if(WhiteBoard.SW) {
					g.fillOval(lastx,lasty,x-lastx,y-lasty);
				} else {
					g.drawOval(lastx,lasty,x-lastx,y-lasty);
				}				
			}
			
			else if(type==3) {
				if(WhiteBoard.SW) {
					g.fillRect(lastx,lasty,x-lastx,y-lasty);
				} else {
					g.drawRect(lastx,lasty,x-lastx,y-lasty);
				}	
			}
				
			else if(type==4) {
				g.setPaintMode();
				g.setColor(getBackground());
				g.fillRect(x, y, 8,8);
			}
			else if(type==5) {
				g.fillOval(lastx, lasty, 4, 4);
			}
			prex = x; prey = y;
        }
      });
}
  public void processMouseEvent(MouseEvent e){
	  if(e.isPopupTrigger())
		  popup.show(this, e.getX(), e.getY());
	  else super.processMouseEvent(e);
  }
  
   public void connect() {  
    try {
 		 mSocket = new MulticastSocket(7777);
  		 inetAddress = InetAddress.getByName("230.0.0.1");
  		 mSocket.joinGroup(inetAddress);
} catch (Exception e) { 	}
   }

   public void sendData(Shape data) { 
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
	     mSocket.send(packet); 
     } catch(Exception e) {System.out.println(e); }
   }

   
   public void run() { 
       try {
          byte[ ] data = new byte[100]; 
		    DatagramPacket packet = new DatagramPacket(data,
 					data.length);
       	while (true) { 
		       mSocket.receive(packet); 
ByteArrayInputStream byteStream = new
            ByteArrayInputStream(data);
             ObjectInputStream is = new
             ObjectInputStream(new BufferedInputStream(byteStream));
Shape p = (Shape)is.readObject();
               drawGraph(p); 
  			}
	} catch (Exception e) { System.out.println(e);}
   }

   public void actionPerformed(ActionEvent e) { //实现弹出式接口
	   String name =((MenuItem)e.getSource()).getLabel();
	   if(name.equals("Clear")) { //清除画面
		   Graphics g = this.getGraphics();
		   g.setColor(this.getBackground());
		   g.fillRect(0, 0, this.getSize().width,this.getSize().height);
	   }
	   else if(name.equals("Red")) color=Color.red;
	   else if(name.equals("Green")) color=Color.green;
	   else if(name.equals("Blue")) color=Color.blue;
	   else if(name.equals("Black")) color=Color.black;
   }
   
   public void drawGraph(Shape p) { 
		Graphics g = getGraphics();  
		p.draw(g);
		graphs.add(p);       
   }

   public void paint(Graphics g) {
        for (int k=0;k<graphs.size();k++)
           graphs.get(k).draw(g);
   }

   public static void main(String[ ] args) {
        Frame x = new  Frame();
        Button eraser=new Button("Eraser");
        Button dot=new Button("Dot");
        Button line = new Button("line");
        Button oval = new Button("Oval");
		Button rect=new Button("Rect");
		Button fill = new Button("fill");
		
        Panel p = new Panel();
        x.add("South",p);
    	p.add(eraser);
    	p.add(dot);
        p.add(line);
		p.add(oval);
		p.add(rect);
		p.add(fill);
        WhiteBoard b = new  WhiteBoard();
        x.add(b);
        x.addWindowListener(new WindowAdapter(){  
            public void windowClosing(WindowEvent e){  
                System.exit(0);  
            }  
            }); 
        
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
		eraser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b.type=4;
			}
		});
		
		fill.addActionListener(new ActionListener(){
	           public void actionPerformed(ActionEvent e){
	        	   WhiteBoard.SW = !WhiteBoard.SW;
	           }
	        });
		dot.addActionListener(new ActionListener(){
	           public void actionPerformed(ActionEvent e){
	        	   b.type=5;
	           }
	        });
        x.setSize(500,500);
        x.setVisible(true);
}
}
