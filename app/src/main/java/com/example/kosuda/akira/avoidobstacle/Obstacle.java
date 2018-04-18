package com.example.kosuda.akira.avoidobstacle;

/**
 * Created by aki on 2018/04/12.
 */

public class Obstacle extends GameObject {

    private int speed;


    public Obstacle(int left, int top, int width, int height,int speed) {
        super(left, top, width, height);
        setSpeed(speed);
    }

    public  void move(){
        super.move(0,speed);
    }

    public void setSpeed(int speed){
        this.speed= speed;
    }
}
