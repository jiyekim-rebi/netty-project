package org.example.eventloop;

import java.util.concurrent.Future;
import java.util.logging.Logger;

// future pattern
public class SpecialCake {

    public static void main(String[] args) throws InterruptedException {
        Bakery bakery = new Bakery();

        Future future = bakery.orderCake();

        doSomthing(); // 다른일을 함 :)

        if (future.isDone()) {
            Cake cake = future.getCake();
        } else {
            while(future.isDone() != true) {
                doSomthing();
            }

            Cake cake = future.getCake();
        }

    }

    private static void doSomthing() throws InterruptedException {
        Thread.sleep(100);
    }
}

class Bakery {
    public Future orderCake() {
        return null;
    }
}

class Cake {

}
