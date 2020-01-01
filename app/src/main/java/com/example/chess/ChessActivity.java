package com.example.chess;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ChessActivity extends AppCompatActivity implements View.OnClickListener{

    private ChessView chessView;
    private Button chessButton;

    //游戏结束信息提醒
    private AlertDialog.Builder alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);
        initDialog();
        //判断是否为第一次进入游戏 是的话需点击开始游戏，否则直接进行游戏
        Intent intent = getIntent();
        chessButton = findViewById(R.id.chess_button);
        chessView = findViewById(R.id.chess_view);

        if(intent.getIntExtra("re",0) == 1){
            chessButton.setVisibility(View.INVISIBLE);
            //使棋盘接收点击事件
            chessView.setOnTouchListener();
        }else {
            chessButton.setText("开始游戏");
            chessButton.setOnClickListener(this);
        }


        chessView.setOnSuccessListener(new ChessView.OnSuccessListener() {
            @Override
            public void onSuccess(ChessView.Player player) {
                //根据回调结果显示结束信息
                switch (player){
                    case NONE:
                        alertDialog.setMessage("平局！");  //设置提示信息
                        break;
                    case USER_ONE:
                        alertDialog.setMessage("×方获胜！");  //设置提示信息
                        break;
                    case USER_TWO:
                        alertDialog.setMessage("○方获胜！");  //设置提示信息
                        break;
                }
                alertDialog.show(); //显示
            }
        });
    }

    //初始化Dialog
    private void initDialog(){
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("游戏信息");  //设置标题
        alertDialog.setCancelable(false);   //是否点击屏幕可取消
        //确定按钮点击事件
        alertDialog.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ChessActivity.this,ChessActivity.class);
                intent.putExtra("re",1);
                startActivity(intent);
                finish();
            }
        });
        //取消按钮点击事件
        alertDialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chessButton.setVisibility(View.VISIBLE);
                chessButton.setText("再来一局");
                chessButton.setOnClickListener(ChessActivity.this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.chess_button) {
            if (chessButton.getText().toString().equals("开始游戏")) {
                Log.e("ChessActivity:","开始游戏");
                chessView.setOnTouchListener();
            } else {
                Intent intent = new Intent(ChessActivity.this, ChessActivity.class);
                intent.putExtra("re", 1);
                startActivity(intent);
                finish();
            }
        }
    }
}
