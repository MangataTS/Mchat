package com.example.sss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String[] name = {"我睡觉的时候不困","月亮邮递员","开心市民小张","幼儿园抢饭第一名","迪士尼在逃公主","吱屋猪","脸上有肉","BLUE","油炸小可爱","我咋又饿了","我的鱼干呢",
    "草莓感冒片","九磅十五便士","你是胖虎吗","Qw1ko","星星泡饭","小熊软糖","放星星的月亮","秃头小可爱","趁月色温柔","被窝探险家","你算哪块小饼干","买一斤温柔","偷亲一口神明",
    "桃花换小鱼干","今天不喝奶茶","今天不点外卖","星星暗了","恶龙咆哮","诺贝尔可爱奖","吐个泡泡","幼儿园一姐","北岛千代子","矢夜桃奈","幼儿园的高材生","舔奶盖的小仙女","萝莉教主",
    "捧花少女","社会主义接班人","Serendipity","Flipped","Fairy","Dreamboat","crush","Cheryl","打铁王","幼儿园抢饭第一名","不甜主义"};
    private OutputStream outputStream=null;
    private Socket socket=null;
    private String ip="192.168.1.66";
    private Button btn_cnt;
    private EditText et_ip;
    private EditText et_name;
    private EditText et_port;
    private TextView myName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.denglu);

        setContentView(R.layout.activity_main);
        btn_cnt = (Button)findViewById(R.id.btn_cnt);
        et_ip=findViewById(R.id.et_ip);
        et_port=findViewById(R.id.et_port);
        et_name=findViewById(R.id.et_name);
        myName=findViewById(R.id.my_name);
        Random random = new Random(System.currentTimeMillis());
        et_name.setText(name[random.nextInt(name.length)]);
        btn_cnt.setOnClickListener(MainActivity.this);
    }
    public void onClick(View view){
        String name = et_name.getText().toString();
        if("".equals(name)){
            Toast.makeText(this, "请输入用户名：", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(MainActivity.this,ChatRoom.class);
            intent.putExtra("name",et_name.getText().toString());
            intent.putExtra("ip",et_ip.toString());
            intent.putExtra("port",et_port.toString());
            startActivity(intent);
        }
    }
}
