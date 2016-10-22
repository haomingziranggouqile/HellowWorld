import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class LiveGame extends JFrame implements ActionListener,MouseListener,MouseMotionListener
{
	private final int XSIZE = 75,YSIZE = 60;               //图像长宽
	private final int SIZE = 10;                           //方块大小
	private Container container;                           
	private JPanel livePane,controlPane;            
	private JButton button[];
    private Square square;	
    private Stack stack;                                   //堆栈
    private Living living;                                 //演化线程
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
		                                                  //初始化生命状态矩阵
        initButton();                                     //初始化按钮
		stack = new Stack();                              
		square = new Square();
	    square.addMouseListener(this);
	    square.addMouseMotionListener(this);
		livePane.add(square);
		living = new Living();
		living.start();                                   //生命演化进程运行
        living.suspend();                                 //演化暂停
        
        container.add(livePane,BorderLayout.CENTER );	
        container.add(controlPane,BorderLayout.EAST );	
		setSize(845,638);
		setVisible(true);
    }// end LiveGame
    
//******************************初始化按钮**************************************
    public void initButton()
    {
    	String option[] = {"随机生成","人工布局"," 开  始 "," 载  入 "," 保  存 ","增加细胞","减少细胞"," 关  于 "};
    	controlPane.add( new JLabel("     控制面板") );
    	for( int i=0; i<button.length; i++ )
    	{
    		button[ i ] = new JButton( option[ i ] );
    		button[ i ].addActionListener( this );
    		controlPane.add( button[i] );
    	}
    }

//******************************初始化方块界面********************************** 
    public void initSquare()
    {
    	living.suspend();
    	initLives();
    	button[2].setText(" 开  始 ");
    	while(!stack.isEmpty())stack.pop();
    	initLives();
    	square.initGraphic();
    	square.setNew();
    }
    
//******************************初始化生命状态矩阵**********************************             
    private void initLives()
    {	
        for( int i=0; i<XSIZE+8; i++ )
          for( int j=0; j<YSIZE+8; j++ )
            lives[i][j] = 0;
    }

//******************************生成随机状态************************************     
    public void randomSquare()
    {   
    	int x,y; 
    	initSquare();   
    	for( int i=0; i<XSIZE*YSIZE/number; i++ )
    	{
    	   x = (int)(Math.random()*(XSIZE));
    	   y = (int)(Math.random()*(YSIZE));
    	   square.setAlive(x,y);                           //设置x,y位置为活状态
    	   stack.push( new Point(x+4,y+4) );                   //储存或状态位置
           lives[x+4][y+4] = 1;
        }
        square.setNew();                                   //刷新界面
    }

//******************************生命演示图的类**********************************    
    public class Square extends JPanel
	{
		private Graphics g;
		private Image frontImage,backImage;
        private Graphics ft,bg;
		
		public Square()
		{
			backImage = new BufferedImage( XSIZE*SIZE, YSIZE*SIZE, 1);
   	        bg = backImage.getGraphics();                  //背景图像缓存        
   	        frontImage = new BufferedImage( XSIZE*SIZE, YSIZE*SIZE,  1);
   	        ft = frontImage.getGraphics();                 //前景图像缓存
   	        initBack();
   	        initGraphic();
		}
		
		public void paintComponent( Graphics g )
		{
			super.paintComponent(g);
			g.drawImage(frontImage, 0, 0, this);           //显示前景图像
		}
		
		public void initGraphic()
		{
			ft.drawImage(backImage, 0, 0, this);           //刷新背景图像
		}
		
		public void initBack()                             //初始化背景图像
		{
			bg.setColor(Color.LIGHT_GRAY);
   	        bg.fillRect( 0, 0, XSIZE*SIZE, YSIZE*SIZE );
   	        bg.setColor( Color.WHITE );
            for ( int i = 0; i < XSIZE; i++ )
              for ( int j = 0; j < YSIZE; j++ )
                bg.drawRect(i * SIZE, j * SIZE, SIZE,SIZE);    
        }
		
		public void setAlive( int x,int y )               //设置x、y位置为活状态
		{
			ft.setColor( Color.BLUE );
			ft.fillRect( x * SIZE+1, y * SIZE+1, SIZE-1,SIZE-1 );  
		}
		
		public void setNew()                             //重画
		{
			repaint();
		}
	}//end class Square

//******************************生命演化线程************************************	
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
        	{                                               //计算每个生命周围的生命数
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
        	    {                                 //设置生命状态为活状态
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
        		next();                                 //设置下一刻状态
        		square.setNew();                        //刷新所有生命状态
        	}
   	    }  
    }//end class Living

//******************************鼠标响应事件************************************    
    public void mouseClicked( MouseEvent event )
    { 
   	   int x = event.getX()/10;
   	   int y = event.getY()/10;
   	   if( x<XSIZE && x>=0 && y<YSIZE && y>=0 )
   	   {                                                 //将鼠标点击处的生命设为活状态
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

//*****************************按钮响应事件*************************************    
    public void actionPerformed( ActionEvent event )
    {
    	if( event.getSource() == button[0] )
    	   randomSquare();                                   //生成随机状态
    	else if( event.getSource() == button[1] )
    	   initSquare();                                     //手工初始化
    	else if( event.getSource() == button[2] )
    	{
    		if(!button[2].getText().equals("暂停")){         //暂停/继续
    			living.resume();
    			button[2].setText("暂停");
    		}
    		else{
    			living.suspend();
    			button[2].setText("继续");
    		}
    	}
    	else if( event.getSource() == button[3] )load();
    	else if( event.getSource() == button[4] )
    	{
    		living.suspend();
         	button[2].setText("继续");
         	String name = JOptionPane.showInputDialog( "请输入要保存的名字" );
         	if( name == null)return;
    		if( name.equals("") )name = "default";
    		save(name);            
        }
    	else if( event.getSource() == button[5] ){           //增减生命数
    	   number = Math.abs((number-1)%(XSIZE*YSIZE));
    	   randomSquare();}
    	else if( event.getSource() == button[6] ){
    	   number = (number+1)%(XSIZE*YSIZE);
    	   randomSquare();}
    	else if( event.getSource() == button[7] )            //关于
    	   JOptionPane.showMessageDialog( null,
    	   "  ====Powered by ************* ==== \n","关于LiveGame",JOptionPane.PLAIN_MESSAGE );
    }//end method actionPerformed

//***********************************加载*************************************** 
    private void load()
    {
    	living.suspend();
    	button[2].setText("继续");
    	JFileChooser fileChooser = new JFileChooser( );
    	fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
    	if( fileChooser.showOpenDialog( this ) == JFileChooser.CANCEL_OPTION )
    	  return;
    	
    	File fileName = fileChooser.getSelectedFile();
    	if( fileName==null || !fileName.getName().endsWith(".lib") )//选择的文件为空或非正确格式	
    	  JOptionPane.showMessageDialog( this,"Invalid File Nmae\n","Error",JOptionPane.ERROR_MESSAGE );
    	else{//选择正确格式的文件
     		ObjectInputStream input;
    	    try{
    	    	initSquare();
    	    	input = new ObjectInputStream( new FileInputStream(fileName.getAbsolutePath()));
    	    	for( int i=4; i<XSIZE+4; i++ )//加载文件内容
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

//***********************************保存*************************************** 
    private void save( String name )
    {
    	ObjectOutputStream output;
    	try{
    		output = new ObjectOutputStream( new FileOutputStream(name+".lib"));
    		for( int i=4; i<XSIZE+4; i++ ) //保存
    		  for( int j=4; j<YSIZE+4; j++ )
                output.writeInt( lives[i][j] );
                output.flush(); 
    		}
        catch( IOException ioException ){}
    }     

//******************************main方法****************************************		
	public static void main(String args[])
	{
		LiveGame application = new LiveGame();
		application.setResizable( false );//windows can not resizable
        application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	}	
}

