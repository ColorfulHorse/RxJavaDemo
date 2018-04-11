package com.fdore.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 参考 http://gank.io/post/560e15be2dca930e00da1083#toc_20
 *     https://www.jianshu.com/p/a93c79e9f689
 */

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private int defer = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(v -> schedulers());

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
     * 将两个Observable连接起来组成一个新的（按事件顺序）
     */
    private void concat(){
        Observable<Long> ob1 = Observable.intervalRange(1, 3, 0, 1, TimeUnit.SECONDS);
        Observable<Long> ob2 = Observable.just(4L, 5L, 6L);
        Observable.concat(ob1, ob2)
                .subscribe(x -> Log.e("concat:", x.toString()));
    }

    /**
     * 将两个Observable合并起来组成一个新的（不需要按事件顺序）
     */
    private void merge(){
        Observable<Long> ob1 = Observable.intervalRange(1, 3, 0, 1, TimeUnit.SECONDS);
        Observable<Long> ob2 = Observable.just(4L, 5L, 6L);
        Observable.merge(ob1, ob2)
                .subscribe(x -> Log.e("merge:", x.toString()));
    }


    /**
     * 将一个Observable发射的每个数据源都转换成Observable, 然后合并并发射它们(最后合并Observables采用的merge)
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

    /**
     * 将一个Observable发射的每个数据源都转换成Observable, 然后按顺序连接并发射它们(最后合并Observables采用的concat)
     * 顺序不变
     */
    private void concatMap(){
        Observable.just(1, 2, 3, 4, 5)
                .concatMap(i -> {
                    Log.e("value:", i.toString());
                    int delayTime = (int) (1 + Math.random() * 10);
                    // 延时操作检验是否顺序发射
                    return Observable.just(i).delay(delayTime, TimeUnit.MILLISECONDS);
                })
                .subscribe(i -> Log.e("concatMap:", i.toString()));
    }

    /**
     * 去除重复
     */
    private void distinct(){
        Observable.just(1, 1, 2, 2, 3)
                .distinct()
                .subscribe(i -> Log.e("distinct:", i.toString()));
    }

    /**
     * buffer 操作符接受两个参数，buffer(count,skip)，
     * 作用是将 Observable 中的数据分成若干个buffer ,然后生成一个observable,
     * 每个buffer的数据量最大为count, 每次切分跳过skip个数据
     * (1, 2,) -> (3, 4) -> 5
     */
    private void buffer(){
        Observable.just(1, 2, 3, 4, 5)
                .buffer(2, 2)
                .subscribe(list -> {
                    Log.e("buffer:",  "----");
                    for (Integer x : list) {
                        Log.e("value:",  ""+x);
                    }
                });
    }

    /**
     * 跳过skip个再开始接收
     */
    private void skip(){
        Observable.just(1, 2, 3)
                .skip(1)
                .subscribe(x -> Log.e("skip:",  ""+x));
    }

    /**
     * 最多接收2个数据
     */
    private void take(){
        Observable.just(1, 2, 3)
                .take(2)
                .subscribe(x -> Log.e("take:",  ""+x));
    }

    /**
     * 去除发送频率过快的数据，如果在timeout时间内没有别的结果产生，
     * 则将这个结果提交给订阅者，否则忽略该结果。
     */
    private void debounce(){
        Observable.create(emitter -> {
            emitter.onNext(1);
            Thread.sleep(550);
            emitter.onNext(2);
            Thread.sleep(550);
            emitter.onNext(3);
            Thread.sleep(100);
            emitter.onNext(4);
            Thread.sleep(700);
            emitter.onComplete();
        })
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(x -> Log.e("debounce",  ""+x));
    }

    /**
     * defer每次被订阅都会创建一个新的observable，如果没有任何订阅则不会创建(所以此sample改变值有效)
     */
    private void defer(){
        defer = 1;
        Observable<Integer> ob1 = Observable.defer(() -> Observable.just(defer, 2, 3));
        ob1.subscribe(res -> Log.e("defer1",  ""+res));
        defer = 2;
        ob1.subscribe(res -> Log.e("defer2",  ""+res));
        /*defer = 1;
        Observable<Integer> ob2 = Observable.just(defer, 2, 3);
        ob2.subscribe(res -> Log.e("defer1",  ""+res));
        defer = 2;
        ob2.subscribe(res -> Log.e("defer2",  ""+res));*/
    }

    /**
     * 作用等同于reduce，但是会把每次计算的结果发射出来 1 -> 3 -> 6
     */
    private void scan(){
        Observable.just(1, 2, 3)
                .scan((x, y) -> {
                    Log.e("scan", "x:"+x+"y:"+y);
                    return x+y;
                })
                .subscribe(x -> Log.e("scan", x.toString()));
    }


    /**
     * 在时间间隔内将原始数据分割并封成observable发射，
     * 和buffer类似，不同于buffer，它发送的是observable
     */
    private void window(){
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(15)
                .subscribeOn(AndroidSchedulers.mainThread())
                .window(3, TimeUnit.SECONDS)
                .subscribe(ob -> {
                    Log.e("window", "------");
                    ob.subscribe(x -> Log.e("value", x.toString()));
                });
    }

    /**
     * 遵循线程不变原则，默认在哪个线程调用 subscribe()，就在哪个线程生产事件（onSubscribe）；
     * 在哪个线程生产事件，就在哪个线程消费事件（observer）。
     */
    private void schedulers(){
        new Thread(() -> {
            Observable.create(emitter -> {
                Log.e("schedulers", "thread onSubscribe: "+Thread.currentThread().getName());
                emitter.onNext(1);
            })
                    /*.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())*/
                    .subscribe(x -> Log.e("schedulers", "thread observer: "+Thread.currentThread().getName()));
            Log.e("schedulers", "thread subscribe: "+Thread.currentThread().getName());
        }).start();
    }

}
