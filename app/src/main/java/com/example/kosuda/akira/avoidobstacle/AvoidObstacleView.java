package com.example.kosuda.akira.avoidobstacle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by aki on 2018/04/12.
 */


// TODO: 2018/04/18  実機操作 おそらく2回タッチするとゲームがクラッシュするので、ゲーム開始のフラグを立てたい
    
public class AvoidObstacleView extends SurfaceView implements SurfaceHolder.Callback ,Runnable { //1


    private static final int GOAL_HEIGHT = 150; //A xhdpiの機種の値。 xxhdpi →200, 　xxxhdpi→300
    private static final int START_HEIGHT = 150; //A xhdpiの機種の値。 xxhdpi →200, 　xxxhdpi→300
    private static final int JUMP_HEIGHT = START_HEIGHT - 30;

    private static final int OUT_WIDTH = 50;
    private static final int DROID_POS = OUT_WIDTH + 50;

    private int mWidth;
    private int mHeight;

    private boolean mIsGoal = false;
    private boolean mIsGone = false;

    private boolean mIsAttached;
    private Thread mThread;

    private SurfaceHolder mHolder;
    private Canvas mCanvas = null;
    private Paint mPaint = null;
    private Path mGoalZone;
    private Path mStartZone;
    private Path mOutZoneR;
    private Path mOutZoneL;
    private Region mRegionGoalZone;
    private Region mRegionStartZone;
    private Region mRegionOutZoneR;
    private Region mRegionOutZoneL;

    private Region mRegionWholeScreen;
    private long startTime;
    private long endTime;

    //ドロイド君用のビットマップ
    private Bitmap mBitmapDroid;
    //ドロイド君クラス
    private Droid mDroid;

    //障害物用のビットマップ
    private Bitmap mBitmapObstalce;
    //障害物のクラス
    private Obstacle mObstacle;

    //障害物のリスト
    private List<Obstacle> mObstacleList = new ArrayList<Obstacle>(20);

    //乱数
    private Random mRand;



    public AvoidObstacleView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this); //surfaceViewイベントの通知先の指定（このクラス）
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPaint = new Paint(); //③
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);

        mWidth = getWidth();
        mHeight = getHeight();

        Resources rsc = getResources();

        mBitmapDroid = BitmapFactory.decodeResource(rsc,R.mipmap.ufo); //④
        mBitmapObstalce = BitmapFactory.decodeResource(rsc,R.mipmap.rock);

        zoneDecide(); //⑤

        mRand = new Random(); //⑥

        newDroid();  //⑦

        newObstacle(); //8

        mIsAttached = true; //9
        mThread = new Thread(this); //10
        mThread.start(); //11
    }


    @Override
    public void run() {
        while (mIsAttached){
            drawGameBoard(); // 12
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){//13
        switch (event.getAction()){
            case    MotionEvent.ACTION_DOWN:
                if(mRegionStartZone.contains((int)event.getX(),(int)event.getY())){
                    newDroid();
                    newObstacle();

            }
                break;
            default:
                break;
        }
        return  true;
    }


    private void zoneDecide(){     //14
        mRegionWholeScreen = new Region(0,0,mWidth,mHeight);
        mGoalZone = new Path();
        mGoalZone.addRect(OUT_WIDTH,0,mWidth-OUT_WIDTH,GOAL_HEIGHT,Path.Direction.CW);
        mRegionGoalZone = new Region();
        mRegionGoalZone.setPath(mGoalZone,mRegionWholeScreen);

        mStartZone = new Path();
        mStartZone.addRect(OUT_WIDTH,mHeight-START_HEIGHT,mWidth-OUT_WIDTH,mHeight,Path.Direction.CW);
        mRegionStartZone = new Region();
        mRegionStartZone.setPath(mStartZone,mRegionWholeScreen);

        mOutZoneL = new Path();
        mOutZoneL.addRect(0,0,OUT_WIDTH,mHeight, Path.Direction.CW);
        mRegionOutZoneL =  new Region();
        mRegionOutZoneL.setPath(mOutZoneL,mRegionWholeScreen);

        mOutZoneR = new Path();
        mOutZoneR.addRect(mWidth-OUT_WIDTH,0,mWidth,mHeight, Path.Direction.CW);
        mRegionOutZoneR =  new Region();
        mRegionOutZoneR.setPath(mOutZoneR,mRegionWholeScreen);


        /*まとめると　
        * まずmRegionWholeScreenの特定
        * Pathを特定
        * addRect(x1,y1,x2,y2)で区間になる四角形Pathを設定（設定しただけ）
        * setPathでRegionに四角形をセットすることで、やっとタッチ区間が設定できる。*/

    }


    public void drawGameBoard(){
        if((mIsGone)||(mIsGoal)){  //15
            return;
        }

        mDroid.move(MainActivity.role,MainActivity.pitch); //16
        if(mDroid.getBottom()>mHeight){
            mDroid.setLocate(mDroid.getLeft(),(int)(mHeight- JUMP_HEIGHT));
        }

        try{
            for(Obstacle obstacle :mObstacleList){   //17
                if(obstacle != null){
                    obstacle.move();
                }
            }

            mCanvas = getHolder().lockCanvas();  //18
            mCanvas.drawColor(Color.LTGRAY);

            mPaint.setColor(Color.MAGENTA);
            mCanvas.drawPath(mGoalZone,mPaint);
            mPaint.setColor(Color.GRAY);
            mCanvas.drawPath(mStartZone,mPaint);
            mPaint.setColor(Color.BLACK);
            mCanvas.drawPath(mOutZoneL,mPaint);
            mCanvas.drawPath(mOutZoneR,mPaint);

            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(50);


            //Goal文字列
            mCanvas.drawText(getResources().getString(R.string.goal),(int)mWidth/2-50,100,mPaint);
            //Start文字列
            mCanvas.drawText(getResources().getString(R.string.start),(int)mWidth/2-50,mHeight-50,mPaint);


            if(mRegionOutZoneL.contains(mDroid.getCenterX(),mDroid.getCenterY())) { //19
                mIsGoal = true;
            }
            if(mRegionOutZoneR.contains(mDroid.getCenterX(),mDroid.getCenterY())){
                mIsGoal =true;
            }
            if(mRegionGoalZone.contains(mDroid.getCenterX(),mDroid.getCenterY())){
                mIsGoal = true;
                //ゴールした
                String msg = goaled();
                mPaint.setColor(Color.BLACK);
                mCanvas.drawText(msg,OUT_WIDTH+10,GOAL_HEIGHT-100,mPaint);
            }

            //隕石はスタートゾーンにかかると消える
            for(Obstacle obstacle:mObstacleList){ //20
                if(mRegionStartZone.contains(obstacle.getLeft(),obstacle.getBottom())){
                    obstacle.setLocate(obstacle.getLeft(),0);
                }
            }
            if(!mIsGoal){
                for(Obstacle obstacle :mObstacleList){             //21
                    if(mDroid.collisionCheck(obstacle)){
                        String msg = getResources().getString(R.string.collision);
                        mPaint.setColor(Color.WHITE);
                        mCanvas.drawText(msg,OUT_WIDTH+10,GOAL_HEIGHT-100,mPaint);
                        mIsGoal = true;
                    }

                }
            }
                if(!((mIsGone)||(mIsGoal))){  //22
                    mPaint.setColor(Color.DKGRAY);
                    for(Obstacle obstacle :mObstacleList){
                        mCanvas.drawBitmap(mBitmapObstalce,obstacle.getLeft(),obstacle.getTop(),null);
                    }
                    mCanvas.drawBitmap(mBitmapDroid,mDroid.getLeft(),mDroid.getTop(),null);

                }
                getHolder().unlockCanvasAndPost(mCanvas);  //23


        }catch (Exception e){
                e.printStackTrace();
        }
    }

    private String goaled(){  //24
        endTime = System.currentTimeMillis();
        //経過時間
        long erapsedTime = endTime - startTime;
        int secTime = (int)(erapsedTime/1000);
        return("Goal"+secTime+"秒");


    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { //25
        //Bitmapリソースをメモリから解放する
        if(mBitmapDroid != null){
            mBitmapDroid.recycle();
            mBitmapDroid =null;
        }
        if(mBitmapObstalce != null){
            mBitmapObstalce.recycle();
            mBitmapObstalce = null;
        }
        mIsAttached = false;
        while(mThread.isAlive());

    }



    public void newDroid(){     //26
        mDroid = new Droid(DROID_POS,mHeight - JUMP_HEIGHT, mBitmapDroid.getWidth(),
                mBitmapDroid.getHeight());
        mIsGoal = false;
        mIsGone = false;
        startTime = System.currentTimeMillis();
    }

    private void newObstacle(){    //27
        Obstacle obstacle;

        mObstacleList.clear();


        for(int i = 0; i<20; i++){
            //左座標を乱数で決める
            int left = mRand.nextInt(mWidth-(OUT_WIDTH*2 + mBitmapObstalce.getWidth())) +OUT_WIDTH;
            //top座標を乱数で決める
            int top = mRand.nextInt(mHeight-mBitmapObstalce.getHeight()*2);

            //1~3の乱数生成
            int speed = mRand.nextInt(3)+1;
            obstacle = new Obstacle(left,top,mBitmapObstalce.getWidth(),mBitmapObstalce.getHeight(),speed);
            mObstacleList.add(obstacle);
        }
    }


}
