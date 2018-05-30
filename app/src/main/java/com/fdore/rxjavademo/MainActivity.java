package com.fdore.rxjavademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * 参考 http://gank.io/post/560e15be2dca930e00da1083#toc_20
 *     https://www.jianshu.com/p/a93c79e9f689
 */

public class MainActivity extends AppCompatActivity {
    private Button btnSubscribe, btnDispose;
    private int defer = 1;
    private Disposable mDisposable;
    private long l = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSubscribe = findViewById(R.id.btn_subscribe);
        btnDispose = findViewById(R.id.btn_dispose);
        btnSubscribe.setOnClickListener(v -> {
            reply();
        });
        btnDispose.setOnClickListener(v -> {
            if (mDisposable != null && !mDisposable.isDisposed()){
                mDisposable.dispose();
            }
        });
    }

    /**
     * 对位合并多个被观察者（Observable）发送的事件，生成一个新的事件序列（即组合过后的事件序列），并最终发送。
     *
     */
    private void zip() {
        Observable<Long> ob1 = Observable.intervalRange(0, 3, 1 ,1, TimeUnit.SECONDS);

        Observable<Long> ob2 = Observable.intervalRange(0, 3 , 2, 2, TimeUnit.SECONDS);

        mDisposable = Observable.zip(ob1, ob2, (x, y) -> {
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

        mDisposable = Observable.combineLatest(ob1, ob2, (x, y) -> {
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
        Observable<Long> ob1 = Observable.intervalRange(1, 3L, 1L, 0L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(x -> Log.e("ob1", x.toString()));
        Observable<Long> ob2 = Observable.just(4L, 5L, 6L)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(x -> Log.e("ob2", x.toString()));;
        mDisposable = Observable.concat(ob1, ob2)
                .subscribe(x -> Log.e("concat:", x.toString()));
    }

    /**
     * 将两个Observable连接起来组成一个新的（按事件顺序）
     */
    private void concatDelayError(){
        Observable<Integer> ob1 = Observable.<Integer>create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onError(new NullPointerException());
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<Integer> ob2 = Observable.<Integer>create(emitter -> {
            emitter.onNext(5);
            emitter.onNext(6);
            emitter.onNext(7);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Observable<Integer> ob = Observable.concatDelayError(Arrays.asList(ob1, ob2))
                .firstOrError()
                .toObservable();
        mDisposable = ob.subscribe(res -> Log.e("concatDelayError", res + ""),
                throwable -> Log.e("concatDelayError", "err")
        );
    }



    /**
     * 将两个Observable合并起来组成一个新的（不需要按事件顺序）
     */
    private void merge(){
        Observable<Long> ob1 = Observable.intervalRange(1, 3, 0, 1, TimeUnit.SECONDS);
        Observable<Long> ob2 = Observable.just(4L, 5L, 6L);
        mDisposable = Observable.merge(ob1, ob2)
                .subscribe(x -> Log.e("merge:", x.toString()));
    }


    /**
     * 将一个Observable发射的每个数据源都转换成Observable, 然后合并并发射它们(最后合并Observables采用的merge)
     * 但无法保证事件的顺序
     */
    private void flatMap(){
        mDisposable = Observable.just(1, 2, 3, 4, 5)
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
        mDisposable = Observable.just(1, 2, 3, 4, 5)
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
        mDisposable = Observable.just(1, 1, 2, 2, 3)
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
        mDisposable = Observable.just(1, 2, 3, 4, 5)
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
        mDisposable = Observable.just(1, 2, 3)
                .skip(1)
                .subscribe(x -> Log.e("skip:",  ""+x));
    }

    /**
     * 最多接收2个数据
     */
    private void take(){
        mDisposable = Observable.just(1, 2, 3)
                .take(2)
                .subscribe(x -> Log.e("take:",  ""+x));
    }

    /**
     * 去除发送频率过快的数据，如果在timeout时间内没有别的结果产生，
     * 则将这个结果提交给订阅者，否则忽略该结果。
     */
    private void debounce(){
        mDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .debounce(x -> {
                    l+=1;
                    return Observable.create(emitter -> {
                        if (l>=3){
                            emitter.onNext(x);
                            emitter.onComplete();
                            l = 0;
                        }
                    });
                })
                .subscribe(x -> Log.e("debounce",  ""+x));
    }

    /**
     * 去除发送频率过快的数据，如果在timeout时间内没有别的结果产生，
     * 则将这个结果提交给订阅者，否则忽略该结果。
     */
    private void throttleWithTimeout(){
        mDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .throttleWithTimeout(3, TimeUnit.SECONDS)
                .subscribe(x -> Log.e("throttleWithTimeout",  ""+x));
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
        mDisposable = Observable.just(1, 2, 3)
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
        mDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(15)
                .subscribeOn(AndroidSchedulers.mainThread())
                .window(3, TimeUnit.SECONDS)
                .subscribe(ob -> {
                    Log.e("window", "------");
                    ob.subscribe(x -> Log.e("value", x.toString()));
                });
    }

    /**
     * 取样，每隔2秒从上游取一个数据给下游
     */
    private void sample(){
        mDisposable = Observable.interval(0, 150, TimeUnit.MILLISECONDS)
                .take(15)
                .subscribeOn(AndroidSchedulers.mainThread())
                .sample(200, TimeUnit.MILLISECONDS)
                .subscribe(
                    x -> Log.e("value", x.toString())
                );
    }

    /**
     * 分组, 根据func将源Observable<T>转化为Observable<GroupedObservable<K, T>>
     */
    private void groupBy(){
        Observable<Pair<String, Integer>> x = Observable.create(emitter -> {
            Observable.interval(0, 150, TimeUnit.MILLISECONDS)
                    .take(15)
                    .subscribe(time -> emitter.onNext(new Pair<>("x", time.intValue())));
        });
        Observable<Pair<String, Integer>> y = Observable.create(emitter -> {
            Observable.interval(0, 150, TimeUnit.MILLISECONDS)
                    .take(15)
                    .subscribe(time -> emitter.onNext(new Pair<>("y", time.intValue())));
        });

        Observable<GroupedObservable<String, Pair<String, Integer>>> obGroup = /*Observable.<Pair<String, Integer>>create(emitter -> {
            emitter.onNext(new Pair<>("x", 1));
            Thread.sleep(150);
            emitter.onNext(new Pair<>("y", 1));
            Thread.sleep(150);
            emitter.onNext(new Pair<>("x", 2));
            Thread.sleep(150);
            emitter.onNext(new Pair<>("y", 2));
            Thread.sleep(150);
            emitter.onNext(new Pair<>("x", 3));
            Thread.sleep(150);
            emitter.onNext(new Pair<>("y", 3));
            //emitter.onComplete();
        })*/
        Observable.merge(x, y)
                .groupBy(data -> data.first);
        mDisposable = obGroup.flatMap(group -> group.sample(200, TimeUnit.MILLISECONDS, false))
                .subscribe(pair -> Log.e("groupBy", pair.first+": "+pair.second), err -> {}, () -> Log.e("group", "compelete"));
        /*Observable.concat(obGroup)
                //.sample(200, TimeUnit.MILLISECONDS)
                .subscribe(pair -> Log.e("groupBy", pair.first+": "+pair.second), err -> {}, () -> Log.e("group", "compelete"));*/

    }

    /**
     * 线程调度
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



    /**
     * -------------------- cold hot ----------------
     * 参考 https://juejin.im/post/590b398eac502e00582bc616
     */

    /**
     * Hot Observable 意思是如果他开始传输数据，你不主动喊停(dispose()/cancel())，那么他就不会停，一直发射数据，即使他已经没有Observer了；
     * 且所有observer共享一条数据链。
     *
     * 而Cold Observable则是subscribe时才会发射数据， 且所有observer数据链独立， 接收到的数据相同。
     *
     * ConnectableObservable 其实在内部，有一个PublishObserver，他有两个作用。
     * 一个是当我们调用 connect()方法时， PublishObserver开始接受上游的数据，所以才能在我们没有调用 subscribe方法时，他也能开始发送数据。
     * 第二个作用是 PublishObserver存储所有的下游Observer, 在 PublishObserver 每次接到一个上游数据，就会将接收到的结果，依次分发给他存储的所有 Observers ,
     * 如果下游 Observer 调用了 dispose方法，那么他就会在自己的缓存中删除这个 Observer，下次接受到上游数据便不会传给这个Observer。
     *
     * connect()方法会返回一个 Disposable 给我们来控制是否继续接受上游的数据。
     */

    /**
     * observable1 -> 1, 2 3 ....   observable2 -> 5, 6, 7....  验证PublishObserver存在
     * 当dispose connect()返回的Disposable， 两个observer都将收不到数据
     */
    private void publish(){
        ConnectableObservable ob = Observable.interval(1, TimeUnit.SECONDS)
                .publish();
        mDisposable = ob.connect();
        Disposable disposable1 = ob.subscribe(x -> Log.e("publish", "first:"+x.toString()));
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Disposable disposable2 = ob.subscribe(x -> Log.e("publish", "second:"+x.toString()));
    }



    /**
     * autoConnect()看名字就知道，他会自动链接，如果你单纯调用 autoConnect() ，
     * 那么，他会在你链接第一个 Subscriber 的时候调用 connect()，或者你调用 autoConnect(int Num)，
     * 那么他将会再收到Num个 subscriber的时候链接。
     * 但是，这个操作符的关键在于，由于我们为了链式调用，autoConnect会返回Observable给你，
     * 你不会在返回方法里获得一个 Disposable来控制上游的开关, 要传入一个回调函数获得
     */
    private void autoConnect(){
        Observable ob = Observable.interval(1, TimeUnit.SECONDS)
                .publish()
                .autoConnect(1, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mDisposable = disposable;
                    }
                });
        Disposable disposable = ob.subscribe(x -> Log.e("autoConnect", "first:"+x.toString()));
       /* try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Disposable disposable2 = ob.subscribe(x -> Log.e("publish", "second:"+x.toString()));*/
    }



    /**
     * refCount()可以说是最常用的操作符了。
     * 他会把 ConnectableObservable变为一个通常的Observable但又保持了HotObservable的特性。
     * 也就是说，如果出现第一个Observer,他就会自动调用 connect()方法，如果他开始接受之后，下游的 Observers全部dispose，那么他也会停止接受上游的数据；
     * 但是所有Observers依然共享数据链。
     * publish().refCount() 等于 share()
     */
    private void refCount(){
        Observable ob = Observable.interval(1, TimeUnit.SECONDS)
                .publish()
                .refCount();
        mDisposable = ob.subscribe(x -> Log.e("refCount", "first:"+x.toString()));
       /* try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Disposable disposable2 = ob.subscribe(x -> Log.e("publish", "second:"+x.toString()));*/
    }



    /**
     *replay()方法和 publish()一样，会返回一个 ConnectableObservable，
     * 区别是, replay()会为新的observer重放他之前所收到的上游数据
     * observer2马上收到了缓存的3个数据
     */
    private void reply(){
        ConnectableObservable ob = Observable.interval(1, TimeUnit.SECONDS)
                .replay(3);
        mDisposable = ob.connect();
        Disposable disposable1 = ob.subscribe(x -> Log.e("reply", "first:"+x.toString()));
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Disposable disposable2 = ob.subscribe(x -> Log.e("reply", "second:"+x.toString()));
    }







    /**
     * --------------------背压----------------
     * 参考 https://www.jianshu.com/p/ff8167c1d191
     *     https://blog.csdn.net/u013491677/article/details/72783409
     *     https://mp.weixin.qq.com/s?__biz=MzIwMzYwMTk1NA%3D%3D&mid=2247484711&idx=1&sn=c3837b7cad21f0a69d7dccd1aaaf7721&chksm=96cda46aa1ba2d7ce145472449e5a832cd3ac0bc1f766fe09f1ce68ca46c16bab645e507f0b5
     */


    /**
     * 数据流发射，处理，响应可能在各自的线程中独立进行，上游在发射数据的时候，不知道下游是否处理完，也不会等下游处理完之后再发射。
     *
     * 如果上游发射的很快而下游处理的很慢，将会产生很多下游没来得及处理的数据，
     * 这些数据既不会丢失，也不会被垃圾回收机制回收，而是存放在一个异步缓存池中，
     * 如果缓存池中的数据一直得不到处理，越积越多，最后就会造成内存溢出，这便是Rxjava中的背压问题。
     * 下例一直运行最终会导致OOM
     */
    private void backpressure(){
        mDisposable = Observable.interval(10, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(x -> {
                    Thread.sleep(500);
                    Log.e("backpressure", x.toString());
                });
    }


    /**
     * Flowable的异步缓存池不同于Observable，Observable的异步缓存池没有大小限制，
     * 可以无限制向里添加数据，直至OOM,而Flowable的异步缓存池有个固定容量，其大小为128。
     * error:  在此策略下，如果放入Flowable的异步缓存池中的数据超限了，则会抛出MissingBackpressureException异常。
     *
     * subscription.request(n) -> 主要作用是告诉上游下游有处理n个事件的能力：
     *
     * 1. 如果上游和下游处于同一线程，下游无法完全处理上游发送的事件，就会抛出MissingBackpressureException
     * 下例发送到第二个数据就抛出异常
     */
    private void flowableErr1(){
        mDisposable = Flowable.create(emitter -> {
            for (int i = 0; i<3; i++) {
                Log.e("flowable emit", i+"");
                emitter.onNext(i);
            }
            emitter.onComplete();
        }, BackpressureStrategy.ERROR)
                .subscribe(x -> {
                    Thread.sleep(1000);
                    Log.e("flowable value: " , x.toString());
                },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable value: " , "compelete"),
                        subscription -> {
                            subscription.request(1);
                        });
    }


    /**
     * 1.当上游下游处于不同线程， 当缓存池数据小于128时不会抛出异常，但是下游也收不到数据(因为没有request())
     * 2.缓存池数据量大于128时, 当发射到第129个数据时无论如何都会抛出异常(如下例)
     */
    private void flowableErr2(){
        mDisposable = Flowable.create(emitter -> {
            for (int i = 0; i<129; i++) {
                Log.e("flowableErr2 emit", i+"");
                emitter.onNext(i);
            }
            emitter.onComplete();
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(x -> {
                            Thread.sleep(1000);
                            Log.e("flowable value: " , x.toString());
                        },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable value: " , "compelete"),
                        subscription -> {

                        });
    }


    /**
     * drop策略下: 上下游不同线程时，当缓存池数据量大于128，后面的数据会被丢弃
     * 缓存池中数据的清理，并不是Observer接收一条，便清理一条，而是每当下游接收累积到一定条数后清理一次。
     * 下里接收到第96条时开始清除数据，129~288条数据都被丢弃
     * 需要仔细分析日志
     */
    private void flowableDrop(){
        mDisposable = Flowable.create(emitter -> {
            for (int i = 1; i <500 ; i++) {
                Log.e("flowableDrop emit", i+"");
                emitter.onNext(i);
                Thread.sleep(100);
            }
            emitter.onComplete();
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(x -> {
                            Thread.sleep(300);
                            Log.e("flowable value" , x.toString());
                        },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable" , "compelete"),
                        subscription -> {
                            subscription.request(Long.MAX_VALUE);
                        });
    }

    /**
     * 当缓存池满了以后也会丢掉后面的数据，但会强行把最后一条数据塞入缓存池
     */
    private void flowableLatest(){
        mDisposable = Flowable.create(emitter -> {
            for (int i = 1; i <500 ; i++) {
                Log.e("flowableLatest emit", i+"");
                emitter.onNext(i);
                Thread.sleep(100);
            }
            emitter.onComplete();
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(x -> {
                            Thread.sleep(300);
                            Log.e("flowable value" , x.toString());
                        },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable" , "compelete"),
                        subscription -> {
                            subscription.request(Long.MAX_VALUE);
                        });
    }


    /**
     * 此策略下，Flowable的异步缓存池同Observable的一样，没有固定大小，可以无限制向里添加数据，不会抛出MissingBackpressureException异常，但会导致OOM。
     *
     * 会发现和使用Observalbe时一样，都会导致内存剧增，最后导致OOM,不同的是使用Flowable内存增长的速度要慢得多，那是因为基于Flowable发射的数据流，
     * 以及对数据加工处理的各操作符都添加了背压支持，附加了额外的逻辑，其运行效率要比Observable低得多。
     */
    private void flowableBuffer(){
        mDisposable = Flowable.create(emitter -> {
            for (int i = 1;  ; i++) {
                Log.e("flowableBuffer current", emitter.requested()+"");
                emitter.onNext(i);
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(x -> {
                            Thread.sleep(5000);
                            Log.e("flowable value" , x.toString());
                        },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable" , "compelete"),
                        subscription -> {
                            //subscription.request(Long.MAX_VALUE);
                        });
    }

    /**
     * 同步状态下requested获取到的是一个动态的值，会随着下游已经接收的数据的数量而递减
     * 3 -> 2 -> 1 -> 0
     */
    private void flowableRequestd(){
        mDisposable = Flowable.create(emitter -> {
            for (int i = 1; i<10; i++) {
                Log.e("requested", emitter.requested()+"");
                emitter.onNext(i);
            }
        }, BackpressureStrategy.BUFFER)
                .subscribe(x -> {
                            Log.e("flowable value" , x.toString());
                                                                                                                   },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable" , "compelete"),
                        subscription -> {
                            subscription.request(3);
                        });

    }

    /**
     * 异步下current requested初始值始终为128，即缓存池的最大容量
     * 上下游相同流速下，当下游需要的数据超过128时，requested值从128递减，直到下游接收到95条数据以后又开始回升
     * 这是因为缓存池进行了清理，清理后容量回升
     *
     */
    private void flowableRequestdAsync(){
        mDisposable = Flowable.create(emitter -> {
            for (int i = 1; i<=150; i++) {
                Log.e("requested", emitter.requested()+"");
                emitter.onNext(i);
            }
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(x -> {
                            Log.e("flowable value" , x.toString());
                        },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable" , "compelete"),
                        subscription -> {
                            subscription.request(150);
                        });

    }

    /**
     * 由于下游流速比上游要慢，则缓存池向下游传递数据的速度也相应变慢，进而缓存池没有传递完的数据可清除，也就没有足够的空间存放新的数据，上游通过e.requested()获取的值也就变成了0
     * 当e.requested()为0时，上游暂停发射数据，等到下游消费了足够数量(95)的事件后再发射，就能解决背压问题
     * e.requested(): 128 -> 0 -> 96 -> 0 -> 96
     */
    private void flowableDemo(){
        mDisposable = Flowable.create(emitter -> {
            int i = 0;
            while (true){
                Thread.sleep(100);
                Log.e("flowable requested:", emitter.requested()+"");
                if (emitter.requested() == 0)
                    continue;
                //Log.e("flowable emit:", i+"");
                emitter.onNext(i);
                i++;
            }
        }, BackpressureStrategy.MISSING)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(x -> {
                            Thread.sleep(300);
                            Log.e("flowable value" , x.toString());
                            //mSubscription.request(1);
                        },
                        err -> Log.e("flowable", "err", err),
                        () -> Log.e("flowable" , "compelete"),
                        subscription -> {
                            //subscription.request(1);
                            //mSubscription = subscription;
                            subscription.request(Long.MAX_VALUE);
                        });
    }


}
