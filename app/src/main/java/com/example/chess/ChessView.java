package com.example.chess;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChessView extends View implements View.OnTouchListener {

    //棋盘画笔
    private Paint mPaint;
    //玩家选择图案画笔
    private Paint userPaint;
    //是否第一次加载
    private boolean isFirst = true;
    //棋盘格子长度
    private int length;
    //棋盘线条颜色
    private int lineColor;
    //玩家图案对应颜色
    private int userColorOne;
    private int userColorTwo;
    //当前回合玩家
    private Player currentPlayer;
    //被选格子（玩家选择前先通过此数组判断是否可选，选择后将对应格子值设为非0）
    private int[] locations = new int[9];
    //每个格子对应字母编号，将通过字母组合判断是否获胜
    private String[] items = new String[9];
    //玩家所选格子对应字母集合
    private List<String> user1Selected = new ArrayList<>();
    private List<String> user2Selected = new ArrayList<>();
    //已选格子信息集合（格子所选玩家，格子中心点坐标）
    private List<ItemInformation> mList = new ArrayList<>();
    //获胜的所有格子字母组合（按字典序排序）
    private List<String> successResult = new ArrayList<>();
    //存储玩家获胜的字母组合
    private List<String> result = new ArrayList<>();
    //上一回合被选中格子数目
    private int lastCount;
    //代表不同玩家（NONE用来表示平局情况）
    public enum Player{
        USER_ONE,USER_TWO,NONE
    }
    //游戏结束监听器
    private OnSuccessListener mListener;

    public ChessView(Context context) {
        this(context,null);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attributeSet) {
        initData();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ChessView);
        lineColor = typedArray.getColor(R.styleable.ChessView_lineColor,Color.BLACK);
        userColorOne = typedArray.getColor(R.styleable.ChessView_user_color_one,Color.BLACK);
        userColorTwo = typedArray.getColor(R.styleable.ChessView_user_color_two,Color.RED);
        typedArray.recycle();

        userPaint = new Paint();
        userPaint.setStyle(Paint.Style.STROKE);
        userPaint.setAntiAlias(true);

        mPaint = new Paint();
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);

        currentPlayer = Player.USER_ONE;
    }

    private void initData(){
        items[0] = "A";
        items[1] = "B";
        items[2] = "C";
        items[3] = "D";
        items[4] = "E";
        items[5] = "F";
        items[6] = "G";
        items[7] = "H";
        items[8] = "I";
        successResult.add("ABC");
        successResult.add("DEF");
        successResult.add("GHI");
        successResult.add("ADG");
        successResult.add("BEH");
        successResult.add("CFI");
        successResult.add("AEI");
        successResult.add("CEG");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size,size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        length = Math.min(getWidth(),getHeight())/3;
        //棋盘绘制
        for(int i = 0;i < 4;i++){
            canvas.drawLine(length*i,0,length*i,3*length,mPaint);
            canvas.drawLine(0,length*i,length*3,length*i,mPaint);
        }
        //选中格子绘制
        if(!isFirst){
            for(int i = 0;i < mList.size();i++){
                switch (mList.get(i).getPlayer()){
                    case USER_ONE:
                        userPaint.setColor(userColorOne);
                        drawUserSelected(canvas,mList.get(i));
                        break;
                    case USER_TWO:
                        userPaint.setColor(userColorTwo);
                        drawUserSelected(canvas,mList.get(i));
                        break;
                }
            }
            //查看游戏状态是否结束
            checkStatus();

        }
        if(isFirst){
            isFirst = false;
        }
    }

    //绘制选中格子
    private void drawUserSelected(Canvas canvas, ItemInformation itemInformation) {
        int centerX = itemInformation.getJ() * length + length/2;
        int centerY = itemInformation.getI() * length + length/2;
        userPaint.setStrokeWidth(5f);
        //玩家一 绘制 × 图案
        if(itemInformation.getPlayer() == Player.USER_ONE){
            float delta = (float) Math.sqrt(0.08*length*length);
            canvas.drawLine(centerX-delta,centerY-delta,centerX+delta,centerY+delta,userPaint);
            canvas.drawLine(centerX+delta,centerY-delta,centerX-delta,centerY+delta,userPaint);
        }else {
            //玩家二 绘制 ○ 图案
            float radius =  0.4f * length;
            canvas.drawCircle(centerX,centerY,radius,userPaint);
        }
    }

    //查看游戏状态
    private void checkStatus(){
        // 避免非用户点击情况下 onDraw()方法调用 造成 当前玩家的切换
        if(mList.size() == 0 || mList.size()==lastCount) return;
        lastCount = mList.size();
        //查看是否有人获胜
        boolean isSuccess = checkIsSuccessful();
        if(isSuccess){
            if(mListener != null){
                mListener.onSuccess(currentPlayer);
            }
            setOnTouchListener(null);
        }else {
            //判断是否平局
            if(mList.size() == 9){
                if(mListener != null){
                    mListener.onSuccess(Player.NONE);
                }
                return;
            }
            //切换当前用户
            switch (currentPlayer){
                case USER_TWO:
                    currentPlayer = Player.USER_ONE;
                    break;
                case USER_ONE:
                    currentPlayer = Player.USER_TWO;
                    break;
            }
        }
        Log.e("Chess:",currentPlayer+"");
    }


    private boolean checkIsSuccessful() {
        boolean isSuccess = false;
        String tmp = "";
        if(currentPlayer == Player.USER_ONE){
            if(user1Selected.size() >= 3){
                //将玩家所选格子字母编号排序
                Collections.sort(user1Selected);
                //回溯法判断是否获胜
                searchResult(user1Selected,tmp,0);
                isSuccess = result.size() > 0;
            }
        }else {
            if(user2Selected.size() >= 3){
                Collections.sort(user2Selected);
                searchResult(user2Selected,tmp,0);
                isSuccess = result.size() > 0;
            }
        }
        return isSuccess;
    }

    //回溯法 将所有情况进行判断
    private void searchResult(List<String> userSelected,String tmp,int index) {
        if(tmp.length() == 3){
            System.out.println(tmp);
            if(successResult.contains(tmp)){
                result.add(tmp);
            }
            return;
        }
        for(int i = index;i < userSelected.size();i++){
            tmp += userSelected.get(i);
            searchResult(userSelected,tmp,i+1);
            tmp = tmp.substring(0,tmp.length()-1);
        }

    }

    // down 事件 坐标
    private float lastX;
    private float lastY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                // UP 事件坐标 与 Down事件坐标 距离不能太远
                if(Math.abs(lastX-x)<length/2 && Math.abs(lastY-y)<length/2){
                    //通过UP事件坐标计算选中格子
                    calculateTouchItem(x,y);
                }
                break;
        }
        return true;
    }



    //存储选中格子信息
    private class ItemInformation{

        private int i;
        private int j;
        private String location;
        private Player player;

        public ItemInformation(String location, Player player,int i,int j) {
            this.location = location;
            this.player = player;
            this.i = i;
            this.j = j;
        }

        public String getLocation() {
            return location;
        }

        public Player getPlayer() {
            return player;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }
    }




    private void calculateTouchItem(float x, float y) {
        //判断所在行列
        int j = (int)x/length;
        int i = (int)y/length;
        //判断是否在棋盘内
        if(i < 3 && j < 3){
            //判断是否可选
            if(locations[i*3+j] == 0){
                //创建选中格子信息并添加到选中格子list中
                ItemInformation itemInformation = new ItemInformation(items[i*3+j],currentPlayer,i,j);
                mList.add(itemInformation);
                //将格子设为已选状态
                locations[i*3+j] = 1;
                //将选中格子字母编号添加到当前玩家选中格子list中
                if(currentPlayer == Player.USER_ONE){
                    user1Selected.add(items[i*3+j]);
                }else {
                    user2Selected.add(items[i*3+j]);
                }
                //重绘
                invalidate();
            }
        }

    }

    //设置控件 可触摸
    public void setOnTouchListener(){
        setOnTouchListener(this);
    }

    //游戏结束回调接口
    public interface OnSuccessListener{
        public void onSuccess(Player player);
    }

    //设置游戏结束回调接口
    public void setOnSuccessListener(OnSuccessListener listener){
        mListener = listener;
    }
}
