import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import java.io.*;
import java.net.*;
import java.util.*;
abstract class Shape implements Serializable {
    public abstract void draw(Graphics g);
}
class Line extends  Shape{//�߶�
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
class Oval extends Shape{  //��Բ
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
class Rect extends Shape{//����
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
	MulticastSocket mSocket;   // �����շ����ݵ�MulticastSocket����
	InetAddress inetAddress;   //�ಥ��ַ
	ArrayList<Shape> graphs = new ArrayList<>(); //������л�����
	int lastx, lasty;              // �ϴ������λ��
	int prex, prey;   //��Ƥ������ϴ����λ��
	int type = 1;  //��״����,1��ֱ��,2����Բ,3-����

	public WhiteBoard() {
		connect(); //����ಥ
		new Thread(this).start(); //������Ϣ�����߳�
		this.addMouseListener(new MouseAdapter() {
	public void mousePressed(MouseEvent e) { 
    //��갴�£���ס����λ��
			lastx = e.getX(); lasty = e.getY();
			prex = lastx; prey = lasty;
        }
        public void mouseReleased(MouseEvent e){
			int x = e.getX(), y = e.getY();
			if (type==1)
				sendData(new Line(lastx,lasty,x,y));//���ݱ�
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
				g.drawLine(lastx,lasty,prex,prey); //�߳�ԭֱ��
			else if(type==2)
				g.drawOval(lastx,lasty,prex-lastx,prey-lasty);
			else if(type==3)
				g.drawRect(lastx,lasty,prex-lastx,prey-lasty);
			int x = e.getX(), y = e.getY();
			if (type==1)
				g.drawLine(lastx,lasty,x,y); //������ֱ��
			else if(type==2)
				g.drawOval(lastx,lasty,x-lastx,y-lasty);
			else if(type==3)
				g.drawRect(lastx,lasty,x-lastx,y-lasty);
			prex = x; prey = y;
        }
      });
}
 
   public void connect() {  //����ಥ��
    try {
 		 mSocket = new MulticastSocket(7777);
  		 inetAddress = InetAddress.getByName("230.0.0.1");
  		 mSocket.joinGroup(inetAddress);
} catch (Exception e) { 	}
   }

   public void sendData(Shape data) { //��������
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
	     mSocket.send(packet); //�������ݱ�
     } catch(Exception e) {System.out.println(e); }
   }

   public void run() { //������Ϣ����ʾ
       try {
          byte[ ] data = new byte[100]; // �ֽڻ�������Ž�������
		    DatagramPacket packet = new DatagramPacket(data,
 					data.length);
       	while (true) { 
		       mSocket.receive(packet); // �������ݱ�
ByteArrayInputStream byteStream = new
            ByteArrayInputStream(data);
             ObjectInputStream is = new
             ObjectInputStream(new BufferedInputStream(byteStream));
Shape p = (Shape)is.readObject();
               drawGraph(p); //�����յ���ͼ��
  			}
	} catch (Exception e) { System.out.println(e);}
   }

   public void drawGraph(Shape p) { //������״
		Graphics g = getGraphics();  
		p.draw(g);
		graphs.add(p);       
   }

   public void paint(Graphics g) {
         // �����б���ʾͼ��
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