import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class LiveGame extends JFrame implements ActionListener,MouseListener,MouseMotionListener
{
	private final int XSIZE = 75,YSIZE = 60;               //ͼ�񳤿�
	private final int SIZE = 10;                           //�����С
	private Container container;                           
	private JPanel livePane,controlPane;            
	private JButton button[];
    private Square square;	
    private Stack stack;                                   //��ջ
    private Living living;                                 //�ݻ��߳�
    private int number = 5,lives[][];

	public LiveGame()
	{
		super("LiveGame");
		container = getContentPane();
		container.setLayout( new BorderLayout() );
		livePane = new JPanel( new BorderLayout() );
		livePane.setSize(new Dimension(802,638)); 
		controlPane = new JPanel( new GridLayout( 15,1,0,10 ) );
		controlPane.setSize(new Dimension(38,300));
		button = new JButton[8];
		lives = new int[XSIZE+8][YSIZE+8];     
		                                                  //��ʼ������״̬����
        initButton();                                     //��ʼ����ť
		stack = new Stack();                              
		square = new Square();
	    square.addMouseListener(this);
	    square.addMouseMotionListener(this);
		livePane.add(square);
		living = new Living();
		living.start();                                   //�����ݻ���������
        living.suspend();                                 //�ݻ���ͣ
        
        container.add(livePane,BorderLayout.CENTER );	
        container.add(controlPane,BorderLayout.EAST );	
		setSize(845,638);
		setVisible(true);
    }// end LiveGame
    
//******************************��ʼ����ť**************************************
    public void initButton()
    {
    	String option[] = {"�������","�˹�����"," ��  ʼ "," ��  �� "," ��  �� ","����ϸ��","����ϸ��"," ��  �� "};
    	controlPane.add( new JLabel("     �������") );
    	for( int i=0; i<button.length; i++ )
    	{
    		button[ i ] = new JButton( option[ i ] );
    		button[ i ].addActionListener( this );
    		controlPane.add( button[i] );
    	}
    }

//******************************��ʼ���������********************************** 
    public void initSquare()
    {
    	living.suspend();
    	initLives();
    	button[2].setText(" ��  ʼ ");
    	while(!stack.isEmpty())stack.pop();
    	initLives();
    	square.initGraphic();
    	square.setNew();
    }
    
//******************************��ʼ������״̬����**********************************             
    private void initLives()
    {	
        for( int i=0; i<XSIZE+8; i++ )
          for( int j=0; j<YSIZE+8; j++ )
            lives[i][j] = 0;
    }

//******************************�������״̬************************************     
    public void randomSquare()
    {   
    	int x,y; 
    	initSquare();   
    	for( int i=0; i<XSIZE*YSIZE/number; i++ )
    	{
    	   x = (int)(Math.random()*(XSIZE));
    	   y = (int)(Math.random()*(YSIZE));
    	   square.setAlive(x,y);                           //����x,yλ��Ϊ��״̬
    	   stack.push( new Point(x+4,y+4) );                   //�����״̬λ��
           lives[x+4][y+4] = 1;
        }
        square.setNew();                                   //ˢ�½���
    }

//******************************������ʾͼ����**********************************    
    public class Square extends JPanel
	{
		private Graphics g;
		private Image frontImage,backImage;
        private Graphics ft,bg;
		
		public Square()
		{
			backImage = new BufferedImage( XSIZE*SIZE, YSIZE*SIZE, 1);
   	        bg = backImage.getGraphics();                  //����ͼ�񻺴�        
   	        frontImage = new BufferedImage( XSIZE*SIZE, YSIZE*SIZE,  1);
   	        ft = frontImage.getGraphics();                 //ǰ��ͼ�񻺴�
   	        initBack();
   	        initGraphic();
		}
		
		public void paintComponent( Graphics g )
		{
			super.paintComponent(g);
			g.drawImage(frontImage, 0, 0, this);           //��ʾǰ��ͼ��
		}
		
		public void initGraphic()
		{
			ft.drawImage(backImage, 0, 0, this);           //ˢ�±���ͼ��
		}
		
		public void initBack()                             //��ʼ������ͼ��
		{
			bg.setColor(Color.LIGHT_GRAY);
   	        bg.fillRect( 0, 0, XSIZE*SIZE, YSIZE*SIZE );
   	        bg.setColor( Color.WHITE );
            for ( int i = 0; i < XSIZE; i++ )
              for ( int j = 0; j < YSIZE; j++ )
                bg.drawRect(i * SIZE, j * SIZE, SIZE,SIZE);    
        }
		
		public void setAlive( int x,int y )               //����x��yλ��Ϊ��״̬
		{
			ft.setColor( Color.BLUE );
			ft.fillRect( x * SIZE+1, y * SIZE+1, SIZE-1,SIZE-1 );  
		}
		
		public void setNew()                             //�ػ�
		{
			repaint();
		}
	}//end class Square

//******************************�����ݻ��߳�************************************	
    private class Living extends Thread
    {
    	private Point point;
    	
    	public Living()
    	{}
        
        private void next()
        {
        	initLives();                 
        	square.initGraphic();
        	while( !stack.empty() )                      
        	{                                               //����ÿ��������Χ��������
        		point = (Point)stack.pop();  		   
        		lives[ point.x ][ point.y ] += -8;
        		lives[ point.x + 1 ][ point.y ] += 1;
        		lives[ point.x - 1 ][ point.y ] += 1;
        		lives[ point.x + 1 ][ point.y + 1 ] += 1;
        		lives[ point.x + 1 ][ point.y - 1 ] += 1;
        		lives[ point.x - 1 ][ point.y + 1 ] += 1;
        		lives[ point.x - 1 ][ point.y - 1 ] += 1;
        		lives[ point.x ][ point.y + 1 ] += 1;
        		lives[ point.x ][ point.y - 1 ] += 1;
        	}
        	for( int i=1; i<XSIZE+7; i++ )
        	  for( int j=1; j<YSIZE+7; j++ )
        	  {
        	    if( lives[i][j]==-5 ||  lives[i][j]==-6 || lives[i][j]==3  )
        	    {                                 //��������״̬Ϊ��״̬
        	        lives[i][j] = 1;
        	        stack.push( new Point( i, j ) );                                   
        	    	if( (i-4<XSIZE && i-4>=0) && (j-4<YSIZE && j-4>=0) )
        	    	  square.setAlive( i-4, j-4 );     	    		
        	    }
        	    else lives[i][j] = 0;
        	 }
        }//end method next;
         	
    	public void run()
        {  	
        	while(true)
        	{	
        		try{Thread.sleep(100);}
        		catch( InterruptedException exception ){}
        		next();                                 //������һ��״̬
        		square.setNew();                        //ˢ����������״̬
        	}
   	    }  
    }//end class Living

//******************************�����Ӧ�¼�************************************    
    public void mouseClicked( MouseEvent event )
    { 
   	   int x = event.getX()/10;
   	   int y = event.getY()/10;
   	   if( x<XSIZE && x>=0 && y<YSIZE && y>=0 )
   	   {                                                 //�����������������Ϊ��״̬
   	   	  square.setAlive(x,y);
   	   	  square.setNew();
   	   	  stack.push( new Point( x+4, y+4 ) );
   	   	  lives[x+4][y+4]  = 1;	
   	   }
    }//end method mouseClicked
   
    public void mousePressed( MouseEvent event )
    {}
    public void mouseReleased( MouseEvent event )
    {}
    public void mouseEntered( MouseEvent event )
    {}
    public void mouseExited( MouseEvent event )
    {}
    public void mouseDragged( MouseEvent event )
    {}
    public void mouseMoved( MouseEvent event )
    {}

//*****************************��ť��Ӧ�¼�*************************************    
    public void actionPerformed( ActionEvent event )
    {
    	if( event.getSource() == button[0] )
    	   randomSquare();                                   //�������״̬
    	else if( event.getSource() == button[1] )
    	   initSquare();                                     //�ֹ���ʼ��
    	else if( event.getSource() == button[2] )
    	{
    		if(!button[2].getText().equals("��ͣ")){         //��ͣ/����
    			living.resume();
    			button[2].setText("��ͣ");
    		}
    		else{
    			living.suspend();
    			button[2].setText("����");
    		}
    	}
    	else if( event.getSource() == button[3] )load();
    	else if( event.getSource() == button[4] )
    	{
    		living.suspend();
         	button[2].setText("����");
         	String name = JOptionPane.showInputDialog( "������Ҫ���������" );
         	if( name == null)return;
    		if( name.equals("") )name = "default";
    		save(name);            
        }
    	else if( event.getSource() == button[5] ){           //����������
    	   number = Math.abs((number-1)%(XSIZE*YSIZE));
    	   randomSquare();}
    	else if( event.getSource() == button[6] ){
    	   number = (number+1)%(XSIZE*YSIZE);
    	   randomSquare();}
    	else if( event.getSource() == button[7] )            //����
    	   JOptionPane.showMessageDialog( null,
    	   "  ====Powered by ************* ==== \n","����LiveGame",JOptionPane.PLAIN_MESSAGE );
    }//end method actionPerformed

//***********************************����*************************************** 
    private void load()
    {
    	living.suspend();
    	button[2].setText("����");
    	JFileChooser fileChooser = new JFileChooser( );
    	fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
    	if( fileChooser.showOpenDialog( this ) == JFileChooser.CANCEL_OPTION )
    	  return;
    	
    	File fileName = fileChooser.getSelectedFile();
    	if( fileName==null || !fileName.getName().endsWith(".lib") )//ѡ����ļ�Ϊ�ջ����ȷ��ʽ	
    	  JOptionPane.showMessageDialog( this,"Invalid File Nmae\n","Error",JOptionPane.ERROR_MESSAGE );
    	else{//ѡ����ȷ��ʽ���ļ�
     		ObjectInputStream input;
    	    try{
    	    	initSquare();
    	    	input = new ObjectInputStream( new FileInputStream(fileName.getAbsolutePath()));
    	    	for( int i=4; i<XSIZE+4; i++ )//�����ļ�����
    	    	  for( int j=4; j<YSIZE+4; j++ ){
                    if( input.readInt()==1){ 
                       square.setAlive( i-4,j-4 );
                       lives[i][j] = 1;                        
    	               stack.push( new Point(i,j) );}
                  }
    	        input.close();
                } 
            catch( IOException ioException ){}
            square.setNew();
            }
    }   

//***********************************����*************************************** 
    private void save( String name )
    {
    	ObjectOutputStream output;
    	try{
    		output = new ObjectOutputStream( new FileOutputStream(name+".lib"));
    		for( int i=4; i<XSIZE+4; i++ ) //����
    		  for( int j=4; j<YSIZE+4; j++ )
                output.writeInt( lives[i][j] );
                output.flush(); 
    		}
        catch( IOException ioException ){}
    }     

//******************************main����****************************************		
	public static void main(String args[])
	{
		LiveGame application = new LiveGame();
		application.setResizable( false );//windows can not resizable
        application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	}	
}

