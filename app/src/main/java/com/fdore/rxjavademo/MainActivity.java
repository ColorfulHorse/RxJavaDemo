package com.fdore.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(v -> flatMap());

    }

    /**
     * 对位合并多个被观察者（Observable）发送的事件，生成一个新的事件序列（即组合过后的事件序列），并最终发送。
     *
     */
    private void zip() {
        Observable<Long> ob1 = Observable.intervalRange(0, 3, 1 ,1, TimeUnit.SECONDS);

        Observable<Long> ob2 = Observable.intervalRange(0, 3 , 2, 2, TimeUnit.SECONDS);

        Observable.zip(ob1, ob2, (x, y) -> {
            Log.e("x:", x.toString());
            Log.e("y:", y.toString());
            return x+y;
        })
                .subscribe(z -> Log.e("zip:", z.toString()), err -> {

                });
    }

    /**
     * 当两个Observables中的任何一个发送了数据后(前提是两个Observable都发送过数据)，
     * 将先发送了数据的Observables 的最新（最后）一个数据 与 另外一个Observable发送的每个数据结合，最终基于该函数的结果发送数据
     * 与Zip（）的区别：Zip（） = 按个数合并，即1对1合并；CombineLatest（） = 按时间合并，即在同一个时间点上合并
     */
    private void combineLastest() {
        Observable<Long> ob1 = Observable.intervalRange(0, 3, 1 ,1, TimeUnit.SECONDS);

        Observable<Long> ob2 = Observable.intervalRange(0, 3 , 2, 2, TimeUnit.SECONDS);

        Observable.combineLatest(ob1, ob2, (x, y) -> {
            Log.e("x:", x.toString());
            Log.e("y:", y.toString());
            return x+y;
        })
                .subscribe(z -> Log.e("combineLatest:", z.toString()), err -> {

                });
    }

    /**
     * 将两个Observable连接起来组成一个新的
     */
    private void concat(){
        Observable<Long> ob1 = Observable.intervalRange(1, 3, 0, 1, TimeUnit.SECONDS);
        Observable<Long> ob2 = Observable.just(4L, 5L, 6L);
        Observable.concat(ob1, ob2)
                .subscribe(x -> Log.e("concat:", x.toString()));
    }


    /**
     * 将一个Observable发射的每个数据源都转换成Observable, 然后合并并发射它们
     * 但无法保证事件的顺序
     */
    private void flatMap(){
        Observable.just(1, 2, 3, 4, 5)
                .flatMap(i -> {
                    Log.e("value:", i.toString());
                    int delayTime = (int) (1 + Math.random() * 10);
                    // 延时操作检验是否顺序发射
                    return Observable.just(i).delay(delayTime, TimeUnit.MILLISECONDS);
                })
                .subscribe(i -> Log.e("flatMap:", i.toString()));
    }


    private void concatMap(){

    }


}
