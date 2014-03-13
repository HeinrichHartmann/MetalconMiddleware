package de.RequestDispatcher.Test;

/**
 * Created by hartmann on 3/13/14.
 */
public class EchoServerMain {


    public static void main(String[] args) {
        EchoServer A = new EchoServer("tcp://*:60123","A");
        A.start();

        try {
            Thread.sleep(1*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Need to call ctx.term first
        // https://github.com/zeromq/jeromq/issues/116
        EchoServer.term();
        A.join();
    }
}
